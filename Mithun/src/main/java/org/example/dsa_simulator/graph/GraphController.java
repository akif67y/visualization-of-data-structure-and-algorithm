package org.example.dsa_simulator.graph;

// All necessary imports from the original GraphController
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*; // Includes Set, List, Map, HashMap, ArrayList, LinkedList, HashSet
import java.util.Objects; // For Objects.requireNonNull

/**
 * A simple graph data structure using an adjacency list.
 * Merged into GraphController.java
 */
class Graph { // Changed to package-private class

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
        adjacencyList.putIfAbsent(nodeId, new ArrayList<>());
    }

    /**
     * Adds an undirected edge between two nodes.
     * If the nodes do not exist, they are created first.
     * This ensures neighbors appear in each other's adjacency lists.
     * @param nodeId1 The ID of the first node.
     * @param nodeId2 The ID of the second node.
     */
    public void addEdge(String nodeId1, String nodeId2) {
        // Ensure both nodes exist in the graph
        addNode(nodeId1);
        addNode(nodeId2);

        // Add the edge in both directions (undirected graph)
        // Check for duplicates before adding
        if (!adjacencyList.get(nodeId1).contains(nodeId2)) {
            adjacencyList.get(nodeId1).add(nodeId2);
        }
        if (!adjacencyList.get(nodeId2).contains(nodeId1)) {
            adjacencyList.get(nodeId2).add(nodeId1);
        }
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



public class GraphController {

    @FXML
    private TextArea edgeInputArea;
    @FXML
    private Pane graphPane;
    @FXML
    private Label visitedNodesLabel;
    // Add FXML reference for the new DFS button (ensure it exists in your FXML)
    @FXML
    private Button dfsButton; // Add this line for the DFS button

    // A variable to hold our graph data structure
    private Graph graph;

    // Dragging variables
    private StackPane draggedNode = null;
    private double dragStartX, dragStartY;

    // Visual components for traversals
    private Map<String, StackPane> visualNodes = new HashMap<>();
    private List<String> visitedOrder = new ArrayList<>();
    private List<Line> allEdges = new ArrayList<>();

    @FXML
    void initialize() {
        // Initialize visited nodes label
        if (visitedNodesLabel != null) {
            visitedNodesLabel.setText("Visited nodes will appear here during traversal");
        }
        // Create a beautiful default graph when the controller loads
        createDefaultGraph();
    }

    private void createDefaultGraph() {
        this.graph = new Graph();
        String[][] defaultEdges = {
                {"A", "B"}, {"A", "C"}, {"A", "D"},
                {"B", "E"}, {"C", "F"}, {"D", "G"},
                {"E", "H"}, {"F", "H"}, {"G", "H"},
                {"H", "I"}, {"I", "J"}, {"J", "A"}
        };
        for (String[] edge : defaultEdges) {
            graph.addEdge(edge[0], edge[1]);
        }
        StringBuilder defaultText = new StringBuilder();
        for (String[] edge : defaultEdges) {
            defaultText.append(edge[0]).append(" ").append(edge[1]).append("\n");
        }
        edgeInputArea.setText(defaultText.toString().trim());
        renderBeautifulLayout();
    }

    @FXML
    void handleDrawGraph(ActionEvent event) {
        this.graph = new Graph();
        resetVisualState();
        String[] lines = edgeInputArea.getText().trim().split("\n");
        for (String line : lines) {
            String[] nodes = line.trim().split("\\s+");
            if (nodes.length == 2) {
                graph.addEdge(nodes[0], nodes[1]);
            }
        }
        renderCircularLayout();
    }

    @FXML
    void handleBFS(ActionEvent event) {
        if (graph == null || graph.getNodes().isEmpty()) {
            return;
        }
        resetVisualState();
        // Sorting ensures consistent starting node
        List<String> sortedNodes = new ArrayList<>(graph.getNodes());
        Collections.sort(sortedNodes);
        String startNode = sortedNodes.get(0);
        performBFSWithVisualization(startNode);
    }

    // --- New DFS Event Handler ---
    @FXML
    void handleDFS(ActionEvent event) {
        if (graph == null || graph.getNodes().isEmpty()) {
            return;
        }
        resetVisualState();
        // Sorting ensures consistent starting node
        List<String> sortedNodes = new ArrayList<>(graph.getNodes());
        Collections.sort(sortedNodes);
        String startNode = sortedNodes.get(0);
        performDFSWithVisualization(startNode); // Call the new DFS animation method
    }
    // --- End of New DFS Event Handler ---

    private void resetVisualState() {
        visitedOrder.clear();
        for (StackPane nodePane : visualNodes.values()) {
            Circle circle = (Circle) nodePane.getChildren().get(0);
            circle.setFill(Color.web("#45B7D1")); // Default blue
            circle.setStroke(Color.web("#2C3E50"));
            circle.setStrokeWidth(3);
        }
        if (visitedNodesLabel != null) {
            visitedNodesLabel.setText("Traversal will start...");
        }
    }

    // --- Existing Corrected BFS Logic ---
    private void performBFSWithVisualization(String startNode) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Timeline timeline = new Timeline();
        double currentTime = 0.0;
        List<List<String>> levels = new ArrayList<>();
        List<String> currentLevel = new ArrayList<>();

        queue.offer(startNode);
        visited.add(startNode);
        currentLevel.add(startNode);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            levels.add(new ArrayList<>(currentLevel));
            currentLevel.clear();
            for (int i = 0; i < levelSize; i++) {
                String currentNode = queue.poll();
                List<String> neighbors = graph.getAdjacencyList().get(currentNode);
                if (neighbors != null) {
                    // Sort neighbors for consistent order within a level
                    List<String> sortedNeighbors = new ArrayList<>(neighbors);
                    Collections.sort(sortedNeighbors);
                    for (String neighbor : sortedNeighbors) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.offer(neighbor);
                            currentLevel.add(neighbor);
                        }
                    }
                }
            }
        }
        if (!currentLevel.isEmpty()) {
            levels.add(currentLevel);
        }

        currentTime = 0.0;
        visitedOrder.clear();

        for (int levelIndex = 0; levelIndex < levels.size(); levelIndex++) {
            List<String> levelNodes = levels.get(levelIndex);
            double levelStartTime = currentTime;

            for (int i = 0; i < levelNodes.size(); i++) {
                String nodeId = levelNodes.get(i);
                visitedOrder.add(nodeId);
                double nodeTimeOffset = i * 1.0;
                KeyFrame highlightFrame = new KeyFrame(
                        Duration.seconds(levelStartTime + nodeTimeOffset),
                        e -> {
                            highlightVisitedNode(nodeId);
                            updateVisitedNodesLabel();
                        }
                );
                timeline.getKeyFrames().add(highlightFrame);
            }
            currentTime = levelStartTime + Math.max(1.2, 1.1 * levelNodes.size());
        }

        KeyFrame completionFrame = new KeyFrame(Duration.seconds(currentTime + 1.0), e -> {
            if (visitedNodesLabel != null) {
                visitedNodesLabel.setText("BFS Complete! Final order: " + String.join(" → ", visitedOrder));
            }
        });
        timeline.getKeyFrames().add(completionFrame);
        timeline.play();
    }
    // --- End of Existing Corrected BFS Logic ---

    // --- New DFS Animation Logic ---
    private void performDFSWithVisualization(String startNode) {
        Set<String> visited = new HashSet<>();
        Timeline timeline = new Timeline();
        double currentTime = 0.0;
        visitedOrder.clear(); // Clear for DFS order

        // Recursive DFS helper that schedules animations
        // It returns the time it expects to finish, allowing sequential scheduling
        dfsRecursive(startNode, visited, timeline, currentTime, 0);

        // Schedule completion message after the last node is processed
        // We estimate the end time. A more robust way is to track the max time scheduled.
        // For simplicity here, we add a delay based on the number of nodes.
        // A better approach is shown below by tracking the max scheduled time.
        double estimatedEndTime = currentTime + visitedOrder.size() * 1.0 + 1.0; // Estimate

        KeyFrame completionFrame = new KeyFrame(Duration.seconds(estimatedEndTime), e -> {
            if (visitedNodesLabel != null) {
                visitedNodesLabel.setText("DFS Complete! Final order: " + String.join(" → ", visitedOrder));
            }
        });
        timeline.getKeyFrames().add(completionFrame);

        timeline.play();
    }

    // Recursive DFS helper that also schedules animations
    // Returns the time at which this specific call (and its children) are expected to finish
    private double dfsRecursive(String nodeId, Set<String> visited, Timeline timeline, double currentTime, int depth) {
        if (visited.contains(nodeId)) {
            return currentTime; // Already visited, no time taken by this specific call
        }

        visited.add(nodeId);
        visitedOrder.add(nodeId); // Record the visit order for DFS

        // Schedule animation for visiting this node
        KeyFrame visitFrame = new KeyFrame(Duration.seconds(currentTime), e -> {
            highlightVisitedNode(nodeId);
            updateVisitedNodesLabel();
        });
        timeline.getKeyFrames().add(visitFrame);

        // Increment time for the next action (visiting children or returning)
        double nextTime = currentTime + 1.0; // 1 second delay between visiting a node and its children

        List<String> neighbors = graph.getAdjacencyList().get(nodeId);
        if (neighbors != null) {
            // Sort neighbors for consistent traversal order
            List<String> sortedNeighbors = new ArrayList<>(neighbors);
            Collections.sort(sortedNeighbors);
            for (String neighbor : sortedNeighbors) {
                if (!visited.contains(neighbor)) {
                    // Recursive call: process the neighbor and get the time it finishes
                    nextTime = dfsRecursive(neighbor, visited, timeline, nextTime, depth + 1);
                    // Add a small delay after processing each child subtree
                    nextTime += 0.2;
                }
            }
        }
        // Return the time when this node's processing (including children) is expected to finish
        return nextTime;
    }
    // --- End of New DFS Animation Logic ---


    private void highlightVisitedNode(String nodeId) {
        StackPane nodePane = visualNodes.get(nodeId);
        if (nodePane != null) {
            Circle circle = (Circle) nodePane.getChildren().get(0);
            circle.setFill(Color.web("#FF6B6B")); // Red for visited
            circle.setStroke(Color.web("#E74C3C"));
            circle.setStrokeWidth(4);
        }
    }

    private void updateVisitedNodesLabel() {
        if (visitedNodesLabel != null) {
            // Check the current text to determine which traversal is running or just finished
            if (visitedNodesLabel.getText().startsWith("BFS") || visitedNodesLabel.getText().startsWith("DFS")) {
                // If it's already showing progress for a specific traversal, update that
                // This simple check works if the completion message is different
                // A more robust way is to have a flag indicating the current traversal type
                // For now, we'll just update the prefix based on the length of visitedOrder
                // This is a bit fragile. A better way is to store the traversal type.
                // Let's assume BFS/DFS logic handles its own final message.
                // Just update the progress part.
                visitedNodesLabel.setText( (visitedNodesLabel.getText().startsWith("BFS") ? "BFS" : "DFS") + " Progress: " + String.join(" → ", visitedOrder));
            } else {
                // Default case, assume it's the currently running one (DFS if DFS button was pressed last)
                // Or just show progress generically
                visitedNodesLabel.setText("Traversal Progress: " + String.join(" → ", visitedOrder));
            }
        }
    }


    private void renderBeautifulLayout() {
        graphPane.getChildren().clear();
        visualNodes.clear();
        allEdges.clear();
        Set<String> nodeIds = graph.getNodes();
        if (nodeIds.isEmpty()) return;
        double width = Math.max(graphPane.getWidth(), 600);
        double height = Math.max(graphPane.getHeight(), 400);

        Map<String, double[]> positions = new HashMap<>();
        positions.put("A", new double[]{width * 0.5, height * 0.2});
        positions.put("B", new double[]{width * 0.2, height * 0.4});
        positions.put("C", new double[]{width * 0.5, height * 0.4});
        positions.put("D", new double[]{width * 0.8, height * 0.4});
        positions.put("E", new double[]{width * 0.1, height * 0.7});
        positions.put("F", new double[]{width * 0.4, height * 0.7});
        positions.put("G", new double[]{width * 0.9, height * 0.7});
        positions.put("H", new double[]{width * 0.5, height * 0.8});
        positions.put("I", new double[]{width * 0.7, height * 0.9});
        positions.put("J", new double[]{width * 0.3, height * 0.9});

        for (String nodeId : nodeIds) {
            StackPane nodePane = createVisualNode(nodeId);
            double[] pos = positions.get(nodeId);
            if (pos != null) {
                nodePane.setLayoutX(pos[0] - 25);
                nodePane.setLayoutY(pos[1] - 25);
            } else {
                double angle = nodeIds.size() > 1 ? 2 * Math.PI * new ArrayList<>(nodeIds).indexOf(nodeId) / nodeIds.size() : 0;
                double radius = Math.min(width, height) / 3;
                double x = width / 2 + radius * Math.cos(angle) - 25;
                double y = height / 2 + radius * Math.sin(angle) - 25;
                nodePane.setLayoutX(x);
                nodePane.setLayoutY(y);
            }
            visualNodes.put(nodeId, nodePane);
            graphPane.getChildren().add(nodePane);
            setupNodeDragging(nodePane);
        }
        drawEdges();
    }

    private void renderCircularLayout() {
        graphPane.getChildren().clear();
        visualNodes.clear();
        allEdges.clear();
        Set<String> nodeIds = graph.getNodes();
        if (nodeIds.isEmpty()) return;
        double width = Math.max(graphPane.getWidth(), 600);
        double height = Math.max(graphPane.getHeight(), 400);
        double centerX = width / 2;
        double centerY = height / 2;

        double baseRadius = Math.min(width, height) / 3;
        double radius = Math.max(80, Math.min(baseRadius, baseRadius + nodeIds.size() * 8));
        double angleStep = 2 * Math.PI / nodeIds.size();
        int i = 0;
        List<String> sortedNodes = new ArrayList<>(nodeIds);
        Collections.sort(sortedNodes);
        for (String nodeId : sortedNodes) {
            StackPane nodePane = createVisualNode(nodeId);
            double angle = i * angleStep;
            double x = centerX + radius * Math.cos(angle) - 25;
            double y = centerY + radius * Math.sin(angle) - 25;
            nodePane.setLayoutX(x);
            nodePane.setLayoutY(y);
            visualNodes.put(nodeId, nodePane);
            graphPane.getChildren().add(nodePane);
            setupNodeDragging(nodePane);
            i++;
        }
        drawEdges();
    }

    private void drawEdges() {
        Map<String, List<String>> adjList = graph.getAdjacencyList();
        Set<String> processedEdges = new HashSet<>();
        for (String sourceId : adjList.keySet()) {
            StackPane sourcePane = visualNodes.get(sourceId);
            List<String> neighbors = adjList.get(sourceId);
            for (String targetId : neighbors) {
                // Create unique edge identifier (sorting node IDs for undirected graph)
                String edgeId1 = sourceId.compareTo(targetId) < 0 ? sourceId + "-" + targetId : targetId + "-" + sourceId;
                // Only process each edge once
                if (!processedEdges.contains(edgeId1)) {
                    StackPane targetPane = visualNodes.get(targetId);
                    if (sourcePane != null && targetPane != null) {
                        Line line = createVisualEdge(sourcePane, targetPane);
                        graphPane.getChildren().add(0, line); // Add edges behind nodes
                        allEdges.add(line);
                        processedEdges.add(edgeId1);
                    }
                }
            }
        }
    }

    private void setupNodeDragging(StackPane nodePane) {
        nodePane.setOnMousePressed(e -> {
            draggedNode = nodePane;
            dragStartX = e.getSceneX() - nodePane.getLayoutX();
            dragStartY = e.getSceneY() - nodePane.getLayoutY();
            e.consume();
        });
        nodePane.setOnMouseDragged(e -> {
            if (draggedNode == nodePane) {
                nodePane.setLayoutX(e.getSceneX() - dragStartX);
                nodePane.setLayoutY(e.getSceneY() - dragStartY);
                // Update edges connected to this node (bindings should handle this)
                // updateEdgesForNode(nodePane); // Optional helper if manual updates needed
            }
            e.consume();
        });
        nodePane.setOnMouseReleased(e -> {
            draggedNode = null;
            e.consume();
        });
    }

    // Optional helper for manual edge updates if bindings are insufficient
    // private void updateEdgesForNode(StackPane nodePane) { /* ... */ }

    private StackPane createVisualNode(String id) {
        Circle circle = new Circle(25);
        Color nodeColor = Color.web("#45B7D1"); // Sky Blue
        circle.setFill(nodeColor);
        circle.setStroke(Color.web("#2C3E50"));
        circle.setStrokeWidth(3);
        circle.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.5, 2, 2);");
        Text label = new Text(id);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-font-family: 'Arial';");
        StackPane nodePane = new StackPane(circle, label);
        nodePane.setOnMouseEntered(e -> {
            circle.setStrokeWidth(4);
            circle.setStroke(Color.web("#F39C12"));
            nodePane.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(243,156,18,0.6), 12, 0.8, 3, 3);");
        });
        nodePane.setOnMouseExited(e -> {
            circle.setStrokeWidth(3);
            circle.setStroke(Color.web("#2C3E50"));
            nodePane.setStyle("-fx-cursor: default; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.5, 2, 2);");
        });
        return nodePane;
    }

    private Line createVisualEdge(StackPane source, StackPane target) {
        Line line = new Line();
        line.setStrokeWidth(2.5);
        line.setStroke(Color.web("#34495E"));
        line.setOpacity(0.8);
        line.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0.3, 1, 1);");
        // Bind line endpoints to the centers of the nodes
        line.startXProperty().bind(source.layoutXProperty().add(25));
        line.startYProperty().bind(source.layoutYProperty().add(25));
        line.endXProperty().bind(target.layoutXProperty().add(25));
        line.endYProperty().bind(target.layoutYProperty().add(25));
        return line;
    }

    @FXML
    void returnToHome(ActionEvent event) {
        try {
            Parent homeScreenRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/Home-screen.fxml")));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homeScreenRoot));
            stage.setTitle("DSA Simulator");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}