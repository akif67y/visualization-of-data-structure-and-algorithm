package com.example.demo.prim;


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

    // Get all unique edges in the graph (for undirected graph)
    public List<WeightedEdge> getAllEdges() {
        List<WeightedEdge> allEdges = new ArrayList<>();
        Set<String> addedEdges = new HashSet<>();

        for (Map.Entry<Integer, List<Edge>> entry : adjacencyList.entrySet()) {
            Integer source = entry.getKey();
            for (Edge edge : entry.getValue()) {
                // Create a consistent edge key (smaller node first)
                String edgeKey = Math.min(source, edge.target) + "-" + Math.max(source, edge.target);

                if (!addedEdges.contains(edgeKey)) {
                    allEdges.add(new WeightedEdge(source, edge.target, edge.weight));
                    addedEdges.add(edgeKey);
                }
            }
        }

        return allEdges;
    }

    // Helper class for representing weighted edges with source and target
    public static class WeightedEdge {
        public final Integer source;
        public final Integer target;
        public final double weight;

        public WeightedEdge(Integer source, Integer target, double weight) {
            this.source = source;
            this.target = target;
            this.weight = weight;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            WeightedEdge that = (WeightedEdge) obj;
            return Double.compare(that.weight, weight) == 0 &&
                    Objects.equals(source, that.source) &&
                    Objects.equals(target, that.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target, weight);
        }

        @Override
        public String toString() {
            return "WeightedEdge{" +
                    "source=" + source +
                    ", target=" + target +
                    ", weight=" + weight +
                    '}';
        }
    }
}