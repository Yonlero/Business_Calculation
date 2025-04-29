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

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApportionmentProcessor {
    private final ApportionmentRepository apportionmentRepository;
    private final KafkaProducerPort kafkaProducerPort;
    private final RedisService redisService;

    /**TODO
     *  Remove Global variables
     *  Change strategy to use cache and db
     *  Add a number in each apportionment, and only apportionment most used (high number) will be in memory/cache to fast consulting/update
     */
    private Map<UUID, Apportionment> apportionmentsById = new ConcurrentHashMap<>();
    private Map<UUID, List<UUID>> adjacencyList = new ConcurrentHashMap<>();
    private Map<UUID, Integer> inDegree = new ConcurrentHashMap<>();

    @Transactional
    public void addApportionment(Apportionment apportionment) {
        apportionmentsById.put(apportionment.getId(), apportionment);
        adjacencyList.putIfAbsent(apportionment.getId(), new ArrayList<>());

        for (List<ValueDistribution> distributions : apportionment.getValuesByMonth().values()) {
            for (ValueDistribution distribution : distributions) {
                adjacencyList.get(apportionment.getId()).add(distribution.getDestinationId());
            }
        }

        redisService.put(apportionment.getId().toString(), apportionment);
    }

    public List<UUID> getExecutionOrder() {
        Queue<UUID> queue = new LinkedList<>();
        List<UUID> executionOrder = new ArrayList<>();

        for (UUID id : apportionmentsById.keySet()) {
            if (inDegree.get(id) == 0) {
                queue.add(id);
            }
        }

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            executionOrder.add(current);

            for (UUID neighbor : adjacencyList.getOrDefault(current, Collections.emptyList())) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (executionOrder.size() != apportionmentsById.size()) {
            throw new IllegalStateException("Circular reference detected in the allocation graph.");
        }

        return executionOrder;
    }

    @Transactional
    public void processAllToCalculation(CalculationKafka calculationKafka) {
        List<UUID> executionOrder = getExecutionOrder();
        initializeFromDatabase(calculationKafka);

        List<BudgetOpeningCacheDTO> budgetOpeningInCache = redisService.getBudgetOpenings("#calculation.budget_opening.toProcess");

        executionOrder.parallelStream().forEach(id -> processApportionment(apportionmentsById.get(id), budgetOpeningInCache));
        kafkaProducerPort.sendApportionmentStatusProcess(KafkaTopics.APPORTIONMENT_CALCULATION_FINISHED, null);
    }

    private void processApportionment(Apportionment apportionment, List<BudgetOpeningCacheDTO> budgetOpeningCached) {
        log.info("Actual Apportionment on Process: {}", apportionment.getId());
        log.info("First Budget Opening: {}", budgetOpeningCached.getFirst());
        // TODO Implement method to update BudgetOpening values.
    }

    public void clearDataStructures() {
        apportionmentsById.clear();
        adjacencyList.clear();
        inDegree.clear();
    }

    public Apportionment getApportionmentFromCache(String id) {
        return redisService.get(id);
    }

    @Transactional
    public Apportionment loadApportionmentFromDatabase(UUID id) {
        Apportionment apportionment = Objects.requireNonNull(apportionmentRepository.findById(id).orElse(null)).toDomain();
        if (apportionment != null) {
            apportionmentsById.put(apportionment.getId(), apportionment);
            adjacencyList.putIfAbsent(apportionment.getId(), new ArrayList<>());

            for (List<ValueDistribution> distributions : apportionment.getValuesByMonth().values()) {
                for (ValueDistribution distribution : distributions) {
                    adjacencyList.get(apportionment.getId()).add(distribution.getDestinationId());
                }
            }

            redisService.put(apportionment.getId().toString(), apportionment);
        }
        return apportionment;
    }

    @Transactional
    public void recalculateForNewApportionment(Apportionment apportionment) {
        UUID newId = apportionment.getId();
        if (apportionmentsById.containsKey(newId)) {
            log.warn("Apportionment with ID {} already exists. Skipping recalculation.", newId);
            return;
        }

        addApportionment(apportionment);

        List<UUID> executionOrder = getExecutionOrder(apportionment, newId);
        List<BudgetOpeningCacheDTO> budgetOpeningInCache = redisService.get("#calculation.budget_opening.toProcess");

        executionOrder.parallelStream().forEach(id -> processApportionment(apportionmentsById.get(id), budgetOpeningInCache));
    }

    private List<UUID> getExecutionOrder(Apportionment apportionment, UUID newId) {
        Set<UUID> affectedVertices = new HashSet<>();
        affectedVertices.add(newId);

        for (List<ValueDistribution> distributions : apportionment.getValuesByMonth().values()) {
            for (ValueDistribution distribution : distributions) {
                UUID destinationId = distribution.getDestinationId();
                if (apportionmentsById.containsKey(destinationId)) {
                    affectedVertices.add(destinationId);
                }
            }
        }

        List<UUID> executionOrder = getExecutionOrder();
        executionOrder.retainAll(affectedVertices);
        return executionOrder;
    }

    @Transactional
    public void initializeFromDatabase(CalculationKafka calculationKafka) {
        List<Apportionment> allApportionments = apportionmentRepository.findAllByYearMonthBetween(
                YearMonth.from(calculationKafka.startDate()), YearMonth.from(calculationKafka.endDate()))
                .stream().map(ApportionmentJPA::toDomain).toList();

        for (Apportionment apportionment : allApportionments) {
            apportionmentsById.put(apportionment.getId(), apportionment);
            adjacencyList.putIfAbsent(apportionment.getId(), new ArrayList<>());

            for (List<ValueDistribution> distributions : apportionment.getValuesByMonth().values()) {
                for (ValueDistribution distribution : distributions) {
                    adjacencyList.get(apportionment.getId()).add(distribution.getDestinationId());
                }
            }

            redisService.put(apportionment.getId().toString(), apportionment);
        }
    }
}