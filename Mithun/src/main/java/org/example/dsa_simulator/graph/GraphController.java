package org.example.dsa_simulator.graph;

// All necessary imports from the original GraphController
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    // A variable to hold our graph data structure
    private Graph graph; // Now uses the merged Graph class
    // Add a field to store the current layout type for redraw on resize
    private String currentLayout = "default"; // or "circular"

    // Dragging variables
    private StackPane draggedNode = null;
    private double dragStartX, dragStartY;

    // Visual components for BFS
    private Map<String, StackPane> visualNodes = new HashMap<>();
    private List<String> visitedOrder = new ArrayList<>();

    // Fix: Add missing allEdges field
    private List<Line> allEdges = new ArrayList<>();

    @FXML
    void initialize() {
        // Initialize visited nodes label
        if (visitedNodesLabel != null) {
            visitedNodesLabel.setText("Visited nodes will appear here during BFS traversal");
        }

        // Create a beautiful default graph when the controller loads
        createDefaultGraph();
        currentLayout = "default"; // Set initial layout type

        // Add a listener to redraw the graph if the pane is resized
        // This helps ensure the graph stays within bounds if the window is manually resized
        graphPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (graph != null && !graph.getNodes().isEmpty()) {
                if ("circular".equals(currentLayout)) {
                    renderCircularLayout(); // Redraw with new dimensions
                } else {
                    renderBeautifulLayout(); // Redraw with new dimensions
                }
            }
        });
        graphPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (graph != null && !graph.getNodes().isEmpty()) {
                if ("circular".equals(currentLayout)) {
                    renderCircularLayout();
                } else {
                    renderBeautifulLayout();
                }
            }
        });
    }

    private void createDefaultGraph() {
        this.graph = new Graph(); // Instantiate the merged Graph class
        // Create a beautiful sample graph - a small network
        String[][] defaultEdges = {
                {"A", "B"}, {"A", "C"}, {"A", "D"},
                {"B", "E"}, {"C", "F"}, {"D", "G"},
                {"E", "H"}, {"F", "H"}, {"G", "H"},
                {"H", "I"}, {"I", "J"}, {"J", "A"}
        };
        for (String[] edge : defaultEdges) {
            graph.addEdge(edge[0], edge[1]); // Uses the updated addEdge for undirected
        }
        // Set default text in the input area
        StringBuilder defaultText = new StringBuilder();
        for (String[] edge : defaultEdges) {
            defaultText.append(edge[0]).append(" ").append(edge[1]).append("\n");
        }
        edgeInputArea.setText(defaultText.toString().trim());
        // Render the default graph with a beautiful layout
        renderBeautifulLayout();
        currentLayout = "default"; // Record the layout type
    }

    @FXML
    void handleDrawGraph(ActionEvent event) {
        // Clear and rebuild graph from input
        this.graph = new Graph(); // Instantiate the merged Graph class
        resetVisualState();
        String[] lines = edgeInputArea.getText().trim().split("\n");
        for (String line : lines) {
            String[] nodes = line.trim().split("\\s+");
            if (nodes.length == 2) {
                // Use the corrected addEdge for undirected graph
                graph.addEdge(nodes[0], nodes[1]);
            }
        }
        // Use circular layout for custom graphs
        renderCircularLayout();
        currentLayout = "circular"; // Record the layout type
    }

    @FXML
    void handleBFS(ActionEvent event) {
        if (graph == null || graph.getNodes().isEmpty()) {
            return;
        }
        // Reset visual state before starting BFS
        resetVisualState();
        // Start BFS from the first node (you can modify this to select starting node)
        // Sorting ensures consistent starting node for testing the cycle graph case
        List<String> sortedNodes = new ArrayList<>(graph.getNodes());
        Collections.sort(sortedNodes);
        String startNode = sortedNodes.get(0); // Start from the lexicographically first node
        performBFSWithVisualization(startNode);
    }

    private void resetVisualState() {
        visitedOrder.clear();
        // Reset all nodes to default blue color
        for (StackPane nodePane : visualNodes.values()) {
            Circle circle = (Circle) nodePane.getChildren().get(0);
            circle.setFill(Color.web("#45B7D1")); // Default blue
            circle.setStroke(Color.web("#2C3E50"));
            circle.setStrokeWidth(3);
        }
        // Reset all edges stay black - no edge coloring
        // Edges will remain their default color
        if (visitedNodesLabel != null) {
            visitedNodesLabel.setText("BFS traversal will start...");
        }
    }

    // --- Corrected BFS Animation Logic ---
    private void performBFSWithVisualization(String startNode) {
        // Data structures for BFS
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Timeline timeline = new Timeline();
        double currentTime = 0.0; // Time in seconds for the animation

        // Lists to hold nodes for each level for correct animation sequencing
        List<List<String>> levels = new ArrayList<>();
        List<String> currentLevel = new ArrayList<>();

        // --- Phase 1: BFS Traversal to Determine Levels ---
        queue.offer(startNode);
        visited.add(startNode);
        currentLevel.add(startNode);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            // Add the nodes discovered in the previous iteration to levels
            levels.add(new ArrayList<>(currentLevel));
            currentLevel.clear(); // Prepare for the next level

            // Process all nodes in the current level
            for (int i = 0; i < levelSize; i++) {
                String currentNode = queue.poll();

                // Discover neighbors from the graph's adjacency list
                List<String> neighbors = graph.getAdjacencyList().get(currentNode);
                if (neighbors != null) {
                    // Process neighbors in the order they appear in the adjacency list
                    // This is crucial for correct BFS order, especially in undirected graphs
                    for (String neighbor : neighbors) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.offer(neighbor);
                            // Add to currentLevel list for the *next* iteration/level
                            currentLevel.add(neighbor);
                        }
                    }
                }
            }
        }
        // Add the last discovered level (if any nodes were found in the final iteration)
        if (!currentLevel.isEmpty()) {
            levels.add(currentLevel);
        }

        // --- Phase 2: Create Animation Sequence Based on Discovered Levels ---
        currentTime = 0.0; // Reset time for scheduling animations
        visitedOrder.clear(); // Clear the order tracking list

        for (int levelIndex = 0; levelIndex < levels.size(); levelIndex++) {
            List<String> levelNodes = levels.get(levelIndex);
            double levelStartTime = currentTime; // Time when this level's animation starts

            // Schedule highlighting for all nodes in this level
            for (int i = 0; i < levelNodes.size(); i++) {
                String nodeId = levelNodes.get(i);
                visitedOrder.add(nodeId); // Record the visit order

                // Optional: Small stagger within the same level for visual clarity
                double nodeTimeOffset = i * 0.1;

                KeyFrame highlightFrame = new KeyFrame(
                        Duration.seconds(levelStartTime + nodeTimeOffset),
                        e -> {
                            highlightVisitedNode(nodeId);
                            updateVisitedNodesLabel(); // Update label immediately
                        }
                );
                timeline.getKeyFrames().add(highlightFrame);
            }

            // Advance the timeline cursor for the next level
            // Ensure a minimum duration per level, scaling slightly with the number of nodes
            currentTime = levelStartTime + Math.max(1.0, 0.2 * levelNodes.size());
        }

        // --- Schedule Completion Message ---
        KeyFrame completionFrame = new KeyFrame(Duration.seconds(currentTime + 1.0), e -> {
            if (visitedNodesLabel != null) {
                visitedNodesLabel.setText("BFS Complete! Final order: " + String.join(" → ", visitedOrder));
            }
        });
        timeline.getKeyFrames().add(completionFrame);

        // --- Play the Animation ---
        timeline.play();
    }
    // --- End of Corrected BFS Animation Logic ---


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
            visitedNodesLabel.setText("BFS Progress: " + String.join(" → ", visitedOrder));
        }
    }

    private void renderBeautifulLayout() {
        graphPane.getChildren().clear();
        visualNodes.clear();
        allEdges.clear();
        Set<String> nodeIds = graph.getNodes();
        if (nodeIds.isEmpty()) return;
        // Use graphPane width/height if available, otherwise fallback
        double width = graphPane.getWidth() > 0 ? graphPane.getWidth() : 800;
        double height = graphPane.getHeight() > 0 ? graphPane.getHeight() : 600;

        // Beautiful predefined positions for the default graph
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

        // Create nodes at predefined positions
        for (String nodeId : nodeIds) {
            StackPane nodePane = createVisualNode(nodeId);
            double[] pos = positions.get(nodeId);
            if (pos != null) {
                nodePane.setLayoutX(pos[0] - 25);
                nodePane.setLayoutY(pos[1] - 25);
            } else {
                // Fallback to circular layout for any extra nodes
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
        // Draw edges and store references
        drawEdges();
    }

    private void renderCircularLayout() {
        graphPane.getChildren().clear();
        visualNodes.clear();
        allEdges.clear();
        Set<String> nodeIds = graph.getNodes();
        if (nodeIds.isEmpty()) return;
        // Use graphPane width/height if available, otherwise fallback
        double width = graphPane.getWidth() > 0 ? graphPane.getWidth() : 800;
        double height = graphPane.getHeight() > 0 ? graphPane.getHeight() : 600;
        double centerX = width / 2;
        double centerY = height / 2;

        // Make radius proportional to number of nodes, but not too small or large
        double baseRadius = Math.min(width, height) / 3;
        double radius = Math.max(80, Math.min(baseRadius, baseRadius + nodeIds.size() * 8));
        double angleStep = 2 * Math.PI / nodeIds.size();
        int i = 0;
        // Sort nodes for consistent positioning
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
        // Draw edges and store references
        drawEdges();
    }

    private void drawEdges() {
        Map<String, List<String>> adjList = graph.getAdjacencyList();
        Set<String> processedEdges = new HashSet<>();
        for (String sourceId : adjList.keySet()) {
            StackPane sourcePane = visualNodes.get(sourceId);
            List<String> neighbors = adjList.get(sourceId);
            for (String targetId : neighbors) {
                // Create unique edge identifier to avoid duplicates in undirected graphs
                // Sorting node IDs ensures consistent edge ID regardless of direction
                String edgeId1 = sourceId.compareTo(targetId) < 0 ? sourceId + "-" + targetId : targetId + "-" + sourceId;
                // Only process each edge once
                if (!processedEdges.contains(edgeId1)) {
                    StackPane targetPane = visualNodes.get(targetId);
                    if (sourcePane != null && targetPane != null) {
                        Line line = createVisualEdge(sourcePane, targetPane);
                        graphPane.getChildren().add(0, line); // Add to back
                        // Store edge reference (optional, if needed later)
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
                // Update edges connected to this node
                updateEdgesForNode(nodePane);
            }
            e.consume();
        });
        nodePane.setOnMouseReleased(e -> {
            draggedNode = null;
            e.consume();
        });
    }

    // Helper to update edge positions when a node is dragged
    private void updateEdgesForNode(StackPane nodePane) {
        // This relies on the Line's startX/Y and endX/Y being bound to the node positions
        // The bindings in createVisualEdge should handle this automatically.
        // If manual updates were needed, you'd iterate through edges and update
        // their startX/Y or endX/Y based on nodePane's layoutX/Y.
        // With current binding setup, no explicit update code is strictly necessary here,
        // but this method exists if manual updates are preferred or bindings fail.
    }


    private StackPane createVisualNode(String id) {
        Circle circle = new Circle(25);
        // Default blue color for all nodes
        Color nodeColor = Color.web("#45B7D1"); // Sky Blue
        circle.setFill(nodeColor);
        circle.setStroke(Color.web("#2C3E50"));
        circle.setStrokeWidth(3);
        // Add a subtle shadow effect
        circle.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.5, 2, 2);");
        Text label = new Text(id);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-font-family: 'Arial';");
        StackPane nodePane = new StackPane(circle, label);
        // Hover effects
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
        // Add subtle shadow to edges too
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