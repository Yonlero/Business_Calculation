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
import br.com.yonlero.apportionment.service.util.MonthRangeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
     *  Add a number in each apportionment, and only apportionment most used (high number) will be storage in memory/cache to fast consulting/update
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
        List<BudgetOpeningCacheDTO> budgetOpeningInCache = redisService.getBudgetOpenings("#calculation.budget_opening");
        List<YearMonth> periodToProcess = MonthRangeGenerator.generateYearMonths(calculationKafka.startDate(), calculationKafka.endDate());

        executionOrder.stream().forEach(apportionmentGraph -> {
            // Each component will be processed in a local cache to avoid concurrency problems
            Map<String, BudgetOpeningCacheDTO> localBudgetMap = new HashMap<>();

            for (YearMonth period : periodToProcess) {
                Map<UUID, Set<ValueDistribution>> groupedByOrigin = new HashMap<>();

                for (BudgetOpeningCacheDTO dto : budgetOpeningInCache) {
                    localBudgetMap.put(buildBudgetKey(dto), dto);
                }

                // Group destinations by origin
                groupDestinationsByOrigin(apportionmentGraph, groupedByOrigin, period.getMonth());

                // Process Grouped destinations by origin
                processDestinationByOrigin(groupedByOrigin, localBudgetMap, period);
            }

            // Update Global cache
            updateRedisWithLocalMap(localBudgetMap);
        });

        redisService.put("#calculation.budget_opening", (Serializable) budgetOpeningInCache);
        kafkaProducerPort.sendApportionmentStatusProcess(KafkaTopics.APPORTIONMENT_CALCULATION_FINISHED, null);
        log.info("Apportionment Calculation - Finished");
    }

    private void groupDestinationsByOrigin(List<UUID> apportionmentGraph, Map<UUID, Set<ValueDistribution>> groupedByOrigin, Month actualMonth) {
        for (UUID id : apportionmentGraph) {
            Apportionment ap = apportionmentsById.get(id);
            UUID originId = Optional.ofNullable(ap.getOriginId()).orElse(ap.getId());

            groupedByOrigin.computeIfAbsent(originId, k -> new HashSet<>()).addAll(apportionmentsById.get(originId).getDistributionsForMonth(actualMonth));
        }
    }

    private void processDestinationByOrigin(Map<UUID, Set<ValueDistribution>> groupedByOrigin, Map<String, BudgetOpeningCacheDTO> localBudgetMap, YearMonth actualPeriod) {
        for (Map.Entry<UUID, Set<ValueDistribution>> entry : groupedByOrigin.entrySet()) {
            UUID originId = entry.getKey();
            Set<ValueDistribution> distributions = entry.getValue();
            processGroupedApportionment(originId, distributions, localBudgetMap, actualPeriod);
        }
    }

    private void updateRedisWithLocalMap(Map<String, BudgetOpeningCacheDTO> localBudgetMap) {
        for (Map.Entry<String, BudgetOpeningCacheDTO> entry : localBudgetMap.entrySet()) {
            redisService.put(entry.getKey(), entry.getValue());
        }
    }

    private void processGroupedApportionment(UUID originId, Set<ValueDistribution> distributions,
                                             Map<String, BudgetOpeningCacheDTO> budgetOpeningMap, YearMonth actualPeriod) {
        Apportionment origin = apportionmentsById.get(originId);

        BudgetOpeningCacheDTO originBudget = budgetOpeningMap.get(buildApportionmentKeyWithCustomPeriod(origin, actualPeriod));

        if (originBudget != null) {
            BigDecimal totalPercentage = BigDecimal.ZERO;
            Map<UUID, BigDecimal> distributionValues = new HashMap<>();

            for (ValueDistribution dist : distributions) {
                BigDecimal percentage = dist.getPercentage();
                totalPercentage = totalPercentage.add(percentage);

                BigDecimal value = originBudget.getProjectedValue().multiply(percentage);
                distributionValues.put(dist.getDestinationId(), value);
            }

            if (totalPercentage.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalStateException("Total percentage exceed 100% " + originId);
            }

            BigDecimal totalDistributed = originBudget.getProjectedValue().multiply(totalPercentage);
            BigDecimal negativeTotal = totalDistributed.multiply(new BigDecimal("-1"));

            originBudget.setApportionmentValue(originBudget.getApportionmentValue().add(negativeTotal));
            originBudget.setProjectedValue(originBudget.getProjectedValue().add(negativeTotal));

            for (ValueDistribution dist : distributions) {
                UUID destinationId = dist.getDestinationId();
                BigDecimal value = distributionValues.get(destinationId);

                Apportionment destination = apportionmentsById.get(destinationId);
                String key = buildApportionmentKey(destination);

                BudgetOpeningCacheDTO budgetDestination = budgetOpeningMap.get(key);
                budgetDestination.setApportionmentValue(budgetDestination.getApportionmentValue().add(value));
                budgetDestination.setProjectedValue(budgetDestination.getProjectedValue().add(value));
            }
        }
    }

    private String buildBudgetKey(BudgetOpeningCacheDTO budgetOpeningCacheDTO) {
        return String.format("%s:%s:%s:%s", budgetOpeningCacheDTO.getAccount(), budgetOpeningCacheDTO.getCostCenter(),
                budgetOpeningCacheDTO.getBusinessUnit(), budgetOpeningCacheDTO.getYearMonth());
    }

    private String buildApportionmentKey(Apportionment apportionment) {
        return String.format("%s:%s:%s:%s", apportionment.getAccount(), apportionment.getCostCenter(),
                apportionment.getBusinessUnit(), apportionment.getYearMonth());
    }

    private String buildApportionmentKeyWithCustomPeriod(Apportionment apportionment, YearMonth period) {
        return String.format("%s:%s:%s:%s", apportionment.getAccount(), apportionment.getCostCenter(),
                apportionment.getBusinessUnit(), period);
    }

    public void clearDataStructures() {
        apportionmentsById.clear();
        adjacencyList.clear();
        inDegree.clear();
    }

    @Transactional
    public void initializeFromDatabase(CalculationKafka calculationKafka) {
       clearDataStructures();

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