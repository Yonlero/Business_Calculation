package br.com.yonlero.apportionment.service.application.usecase;

import br.com.yonlero.apportionment.service.domain.model.Apportionment;
import br.com.yonlero.apportionment.service.domain.model.KafkaTopics;
import br.com.yonlero.apportionment.service.domain.model.ValueDistribution;
import br.com.yonlero.apportionment.service.infrastructure.entity.ApportionmentJPA;
import br.com.yonlero.apportionment.service.infrastructure.redis.RedisService;
import br.com.yonlero.apportionment.service.infrastructure.repository.ApportionmentRepository;
import br.com.yonlero.apportionment.service.interfaceadapter.dto.cache.BudgetOpeningCacheDTO;
import br.com.yonlero.apportionment.service.interfaceadapter.dto.kafka.incoming.CalculationKafka;
import br.com.yonlero.apportionment.service.port.output.KafkaProducerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApportionmentProcessor {
    private final ApportionmentRepository apportionmentRepository;
    private final KafkaProducerPort kafkaProducerPort;
    private final RedisService redisService;

    /**
     * TODO
     *  Remove Global variables
     *  Change strategy to use cache and db
     *  Add a number in each apportionment, and only apportionment most used (high number) will be in memory/cache to fast consulting/update
     */
    private final Map<UUID, Apportionment> apportionmentsById = new ConcurrentHashMap<>();

    // Graph: father -> son
    private final Map<UUID, Set<UUID>> adjacencyList = new ConcurrentHashMap<>();

    // Reverse graph: son -> father
    private final Map<UUID, Set<UUID>> reverseAdjacencyList = new ConcurrentHashMap<>();

    private final Map<UUID, Integer> inDegree = new ConcurrentHashMap<>();

    @Transactional
    public void addApportionment(Apportionment apportionment) {
        UUID id = apportionment.getId();

        apportionmentsById.put(id, apportionment);
        adjacencyList.putIfAbsent(id, new HashSet<>());
        reverseAdjacencyList.putIfAbsent(id, new HashSet<>());
        inDegree.putIfAbsent(id, 0);

        for (ValueDistribution distribution : apportionment.getDistributions()) {
            UUID destinationId = distribution.getDestinationId();

            adjacencyList.putIfAbsent(destinationId, new HashSet<>());
            reverseAdjacencyList.putIfAbsent(destinationId, new HashSet<>());

            adjacencyList.get(id).add(destinationId);
            reverseAdjacencyList.get(destinationId).add(id);

            inDegree.putIfAbsent(destinationId, 0);
            inDegree.put(destinationId, inDegree.get(destinationId) + 1);
        }
    }

    public List<List<UUID>> getExecutionOrderGroupedByComponent() {
        List<List<UUID>> allOrders = new ArrayList<>();
        Set<UUID> visited = Collections.newSetFromMap(new ConcurrentHashMap<>());
        KahnAlgorithm kahnAlgorithm = new KahnAlgorithm(adjacencyList);

        for (UUID node : apportionmentsById.keySet()) {
            if (!visited.contains(node)) {
                Set<UUID> component = findComponent(node, visited);
                List<UUID> order = kahnAlgorithm.topologicalSort(component);
                allOrders.add(order);
            }
        }

        return allOrders;
    }

    // Searching for components using interactive BFS.
    private Set<UUID> findComponent(UUID start, Set<UUID> visited) {
        Set<UUID> component = ConcurrentHashMap.newKeySet();
        Deque<UUID> queue = new LinkedBlockingDeque<>();
        queue.add(start);
        visited.add(start);
        component.add(start);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();

            // Add sons to the queue
            for (UUID child : adjacencyList.getOrDefault(current, Collections.emptySet())) {
                if (!visited.contains(child)) {
                    visited.add(child);
                    component.add(child);
                    queue.add(child);
                }
            }

            // Add Father to the queue
            for (UUID parent : reverseAdjacencyList.getOrDefault(current, Collections.emptySet())) {
                if (!visited.contains(parent)) {
                    visited.add(parent);
                    component.add(parent);
                    queue.add(parent);
                }
            }
        }

        return component;
    }

    @Transactional
    public void processAllToCalculation(CalculationKafka calculationKafka) {
        initializeFromDatabase(calculationKafka);
        List<List<UUID>> executionOrder = this.getExecutionOrderGroupedByComponent();
        List<BudgetOpeningCacheDTO> budgetOpeningInCache = redisService.getBudgetOpenings("#calculation.budget_opening.toProcess");
        Map<String, BudgetOpeningCacheDTO> budgetOpeningMapped = new ConcurrentHashMap<>();

        //TODO Put updated budgetOpeningMap in Cache -> Get this in another service and Persist
        executionOrder.parallelStream().forEach(apportionmentGraph -> {
            for (BudgetOpeningCacheDTO budgetOpeningCacheDTO : budgetOpeningInCache) {
                budgetOpeningMapped.put(buildBudgetKey(budgetOpeningCacheDTO), budgetOpeningCacheDTO);
            }

            for (UUID id : apportionmentGraph) {
                processApportionment(id, apportionmentsById, budgetOpeningMapped);
            }
        });

        kafkaProducerPort.sendApportionmentStatusProcess(KafkaTopics.APPORTIONMENT_CALCULATION_FINISHED, null);
    }

    private String buildBudgetKey(BudgetOpeningCacheDTO budgetOpeningCacheDTO) {
        return String.format("%s:%s:%s:%s", budgetOpeningCacheDTO.getAccount(),
                budgetOpeningCacheDTO.getCostCenter(), budgetOpeningCacheDTO.getBusinessUnit(),
                budgetOpeningCacheDTO.getYearMonth());
    }

    private String buildApportionmentKey(Apportionment apportionment) {
        return String.format("%s:%s:%s:%s", apportionment.getAccount(),
                apportionment.getCostCenter(), apportionment.getBusinessUnit(),
                apportionment.getYearMonth());
    }

    private void processApportionment(UUID apportionmentIdOnProcess, Map<UUID, Apportionment> apportionmentMap, Map<String, BudgetOpeningCacheDTO> budgetOpeningMap) {
        Apportionment actualApportionmentOnProcess = apportionmentMap.get(apportionmentIdOnProcess);
        List<ValueDistribution> valuesByMonthToDestination;
        Month monthInProcess = actualApportionmentOnProcess.getYearMonth().getMonth();
        Apportionment originApportionment = actualApportionmentOnProcess;

        if (actualApportionmentOnProcess.getOriginId() != null) {
            originApportionment = apportionmentMap.get(actualApportionmentOnProcess.getOriginId());
            valuesByMonthToDestination = originApportionment.getValuesByMonth().get(monthInProcess).stream()
                    .filter(x -> x.getDestinationId().equals(actualApportionmentOnProcess.getId()))
                    .toList();
        } else {
            valuesByMonthToDestination = originApportionment.getValuesByMonth().get(monthInProcess);
        }

        BudgetOpeningCacheDTO originBudgetOpening = budgetOpeningMap.get(buildApportionmentKey(originApportionment));


        for (ValueDistribution valueToDistribution : valuesByMonthToDestination) {
            Apportionment apportionmentDestination = apportionmentMap.get(valueToDistribution.getDestinationId());
            String destinationApportionment = buildApportionmentKey(apportionmentDestination);

            BudgetOpeningCacheDTO budgetDestination = budgetOpeningMap.get(destinationApportionment);
            BigDecimal valueToDestination = originBudgetOpening.getProjectedValue().multiply(valueToDistribution.getPercentage());

            originBudgetOpening.setProjectedValue(originBudgetOpening.getProjectedValue().subtract(valueToDestination));
            budgetDestination.setApportionmentValue(budgetDestination.getApportionmentValue().add(valueToDestination));
        }

    }

    public void clearDataStructures() {
        apportionmentsById.clear();
        adjacencyList.clear();
        inDegree.clear();
    }

    public Apportionment getApportionmentFromCache(String id) {
        return redisService.get(id);
    }

//    @Transactional
//    public Apportionment loadApportionmentFromDatabase(UUID id) {
//        Apportionment apportionment = Objects.requireNonNull(apportionmentRepository.findById(id).orElse(null)).toDomain();
//        if (apportionment != null) {
//            apportionmentsById.put(apportionment.getId(), apportionment);
//            adjacencyList.putIfAbsent(apportionment.getId(), new ArrayList<>());
//
//            for (List<ValueDistribution> distributions : apportionment.getValuesByMonth().values()) {
//                for (ValueDistribution distribution : distributions) {
//                    adjacencyList.get(apportionment.getId()).add(distribution.getDestinationId());
//                }
//            }
//
//            redisService.put(apportionment.getId().toString(), apportionment);
//        }
//        return apportionment;
//    }

//    @Transactional
//    public void recalculateForNewApportionment(Apportionment apportionment) {
//        UUID newId = apportionment.getId();
//        if (apportionmentsById.containsKey(newId)) {
//            log.warn("Apportionment with ID {} already exists. Skipping recalculation.", newId);
//            return;
//        }
//
//        addApportionment(apportionment);
//
//        List<UUID> executionOrder = getExecutionOrder(apportionment, newId);
//        List<BudgetOpeningCacheDTO> budgetOpeningInCache = redisService.get("#calculation.budget_opening.toProcess");
//
//        Map<String, BudgetOpeningCacheDTO> budgetOpeningMapped = new HashMap<>();
//
//        for (BudgetOpeningCacheDTO budgetOpeningCacheDTO : budgetOpeningInCache) {
//            budgetOpeningMapped.put(buildBudgetKey(budgetOpeningCacheDTO), budgetOpeningCacheDTO);
//        }
//
//        executionOrder.parallelStream().forEach(id -> processApportionment(apportionmentsById.get(id), budgetOpeningMapped));
//    }

//    private List<UUID> getExecutionOrder(Apportionment apportionment, UUID newId) {
//        Set<UUID> affectedVertices = new HashSet<>();
//        affectedVertices.add(newId);
//
//        for (List<ValueDistribution> distributions : apportionment.getValuesByMonth().values()) {
//            for (ValueDistribution distribution : distributions) {
//                UUID destinationId = distribution.getDestinationId();
//                if (apportionmentsById.containsKey(destinationId)) {
//                    affectedVertices.add(destinationId);
//                }
//            }
//        }
//
//        List<UUID> executionOrder = getExecutionOrder();
//        executionOrder.retainAll(affectedVertices);
//        return executionOrder;
//    }

    @Transactional
    public void initializeFromDatabase(CalculationKafka calculationKafka) {
        apportionmentsById.clear();
        adjacencyList.clear();
        inDegree.clear();

        List<Apportionment> allApportionment = apportionmentRepository.findAllByYearMonthBetween(
                        YearMonth.from(calculationKafka.startDate()), YearMonth.from(calculationKafka.endDate()))
                .stream()
                .map(ApportionmentJPA::toDomain)
                .toList();

        for (Apportionment apportionment : allApportionment) {
            addApportionment(apportionment);
        }
    }
}