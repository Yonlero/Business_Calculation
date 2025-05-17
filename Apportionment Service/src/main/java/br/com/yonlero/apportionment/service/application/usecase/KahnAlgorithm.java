package br.com.yonlero.apportionment.service.application.usecase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class KahnAlgorithm {

    private final Map<UUID, Set<UUID>> fullAdjacencyList;

    public KahnAlgorithm(Map<UUID, Set<UUID>> fullAdjacencyList) {
        this.fullAdjacencyList = fullAdjacencyList;
    }

    public List<UUID> topologicalSort(Set<UUID> component) {
        if (component == null || component.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, Set<UUID>> filteredAdjacencyList = filterAdjacencyList(component);
        Map<UUID, Integer> localInDegree = calculateLocalInDegree(filteredAdjacencyList, component);
        Queue<UUID> queue = new LinkedList<>();

        for (UUID node : component) {
            if (localInDegree.getOrDefault(node, 0) == 0) {
                queue.add(node);
            }
        }

        List<UUID> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            order.add(current);

            for (UUID neighbor : filteredAdjacencyList.getOrDefault(current, Collections.emptySet())) {
                int updated = localInDegree.get(neighbor) - 1;
                localInDegree.put(neighbor, updated);
                if (updated == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (order.size() != component.size()) {
            throw new IllegalStateException("Graph incompleted or circle reference.");
        }

        return order;
    }

    private Map<UUID, Set<UUID>> filterAdjacencyList(Set<UUID> component) {
        Map<UUID, Set<UUID>> filtered = new HashMap<>();

        for (UUID node : component) {
            Set<UUID> neighbors = fullAdjacencyList.getOrDefault(node, Collections.emptySet());
            Set<UUID> localNeighbors = new HashSet<>();

            for (UUID neighbor : neighbors) {
                if (component.contains(neighbor)) {
                    localNeighbors.add(neighbor);
                }
            }

            filtered.put(node, localNeighbors);
        }

        return filtered;
    }

    private Map<UUID, Integer> calculateLocalInDegree(Map<UUID, Set<UUID>> filteredAdjacencyList, Set<UUID> component) {
        Map<UUID, Integer> localInDegree = new HashMap<>();

        for (UUID node : component) {
            localInDegree.put(node, 0);
        }

        for (UUID source : filteredAdjacencyList.keySet()) {
            for (UUID target : filteredAdjacencyList.get(source)) {
                localInDegree.put(target, localInDegree.get(target) + 1);
            }
        }

        return localInDegree;
    }
}