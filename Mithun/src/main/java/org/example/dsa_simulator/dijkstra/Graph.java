package org.example.dsa_simulator.dijkstra;

import java.util.*;

public class Graph {
    private final Map<Integer, List<Edge>> adjacencyList;

    public Graph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addNode(Integer nodeId) {
        adjacencyList.putIfAbsent(nodeId, new ArrayList<>());
    }

    public void addEdge(Integer sourceId, Integer targetId, double weight) {
        addNode(sourceId);
        addNode(targetId);
        adjacencyList.get(sourceId).add(new Edge(targetId, weight));
    }

    public boolean hasEdge(Integer sourceId, Integer targetId) {
        List<Edge> edges = adjacencyList.get(sourceId);
        if (edges == null) return false;

        return edges.stream().anyMatch(edge -> edge.target.equals(targetId));
    }

    public Set<Integer> getNodes() {
        return adjacencyList.keySet();
    }

    public Map<Integer, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    public List<Edge> getNeighbors(Integer nodeId) {
        return adjacencyList.getOrDefault(nodeId, Collections.emptyList());
    }

    public void clear() {
        adjacencyList.clear();
    }

    public int size() {
        return adjacencyList.size();
    }

    public boolean isEmpty() {
        return adjacencyList.isEmpty();
    }
}