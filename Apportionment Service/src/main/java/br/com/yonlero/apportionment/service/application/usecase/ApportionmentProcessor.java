package br.com.yonlero.apportionment.service.application.usecase;

import br.com.yonlero.apportionment.service.domain.model.Apportionment;
import br.com.yonlero.apportionment.service.domain.model.ValueDistribution;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ApportionmentProcessor {
    private Map<UUID, Apportionment> apportionmentsById = new HashMap<>();
    private Map<UUID, List<UUID>> adjacencyList = new HashMap<>();

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
        Map<UUID, Integer> inDegree = new HashMap<>();
        for (UUID id : apportionmentsById.keySet()) {
            inDegree.put(id, 0);
        }
        for (List<UUID> neighbors : adjacencyList.values()) {
            for (UUID neighbor : neighbors) {
                inDegree.put(neighbor, inDegree.getOrDefault(neighbor, 0) + 1);
            }
        }

        Queue<UUID> queue = new LinkedList<>();
        for (UUID id : inDegree.keySet()) {
            if (inDegree.get(id) == 0) {
                queue.add(id);
            }
        }

        List<UUID> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            order.add(current);

            for (UUID neighbor : adjacencyList.getOrDefault(current, Collections.emptyList())) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (order.size() != apportionmentsById.size()) {
            throw new IllegalStateException("Circular reference detected in the apportionment graph.");
        }

        return order;
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