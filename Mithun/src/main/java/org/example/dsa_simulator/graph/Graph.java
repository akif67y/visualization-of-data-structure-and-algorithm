package org.example.dsa_simulator.graph;

import java.util.*;

/**
 * A simple graph data structure using an adjacency list.
 */
public class Graph {

    // The key is the node ID, the value is a list of its neighbors.
    private final Map<String, List<String>> adjacencyList;

    public Graph() {
        this.adjacencyList = new HashMap<>();
    }

    /**
     * Adds a new node to the graph. If it already exists, nothing happens.
     * @param nodeId The ID of the node to add.
     */
    public void addNode(String nodeId) {
        adjacencyList.putIfAbsent(nodeId, new ArrayList<>());    }

    /**
     * Adds a directed edge from a source node to a target node.
     * If the nodes do not exist, they are created first.
     * @param sourceId The ID of the source node.
     * @param targetId The ID of the target node.
     */
    public void addEdge(String sourceId, String targetId) {
        // Ensure both nodes exist in the graph
        addNode(sourceId);
        addNode(targetId);

        // Add the edge (connection)
        adjacencyList.get(sourceId).add(targetId);
    }

    /**
     * Returns a set of all unique node IDs in the graph.
     * @return A Set of node IDs.
     */
    public Set<String> getNodes() {
        return adjacencyList.keySet();
    }

    /**
     * Returns the entire adjacency list.
     * Useful for algorithms that need to traverse the graph.
     * @return The adjacency list map.
     */
    public Map<String, List<String>> getAdjacencyList() {
        return adjacencyList;
    }
}
