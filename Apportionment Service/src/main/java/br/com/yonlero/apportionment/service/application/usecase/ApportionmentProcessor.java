package br.com.yonlero.apportionment.service.application.usecase;

import br.com.yonlero.apportionment.service.domain.model.Apportionment;
import br.com.yonlero.apportionment.service.domain.model.ValueDistribution;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

@Slf4j
public class ApportionmentProcessor {
    private final Map<UUID, Apportionment> apportionmentsById = new HashMap<>();
    private final Map<UUID, List<UUID>> adjacencyList = new HashMap<>();
    private final Map<UUID, Integer> inDegree = new HashMap<>();

    public void addApportionment(Apportionment apportionment) {
        apportionmentsById.put(apportionment.getId(), apportionment);
        adjacencyList.putIfAbsent(apportionment.getId(), new ArrayList<>());

        for (List<ValueDistribution> distributions : apportionment.getValuesByMonth().values()) {
            for (ValueDistribution distribution : distributions) {
                adjacencyList.get(apportionment.getId()).add(distribution.getDestinationId());
            }
        }
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

    public void processAll() {
        List<UUID> executionOrder = getExecutionOrder();
        executionOrder.parallelStream().forEach(id -> processApportionment(apportionmentsById.get(id)));
    }

    private void processApportionment(Apportionment apportionment) {
        log.info("Actual Apportionment on Process: {}", apportionment.getId());
        // TODO Implement value change methods
    }
}