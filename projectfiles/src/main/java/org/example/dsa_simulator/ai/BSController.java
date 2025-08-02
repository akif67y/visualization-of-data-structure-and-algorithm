package org.example.dsa_simulator.ai;

// All necessary imports from the original BFSController
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*; // Includes Set, List, Map, HashMap, ArrayList, LinkedList, HashSet
import javafx.scene.Group;
import javafx.scene.shape.Polygon;
import java.util.Objects; // For Objects.requireNonNull

class Graph { // Changed to package-private class

    // The key is the node ID, the value is a list of its neighbors.
    private final Map<String, List<String>> adjacencyList;

    public Graph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addNode(String nodeId) {
        adjacencyList.putIfAbsent(nodeId, new ArrayList<>());
    }

    public void addEdge(String nodeId1, String nodeId2, boolean isDirected) {
        // Ensure both nodes exist in the graph
        addNode(nodeId1);
        addNode(nodeId2);

        // Add the edge - directed or undirected based on parameter
        if (!adjacencyList.get(nodeId1).contains(nodeId2)) {
            adjacencyList.get(nodeId1).add(nodeId2);
        }

        // For undirected graphs, add edge in both directions
        if (!isDirected && !adjacencyList.get(nodeId2).contains(nodeId1)) {
            adjacencyList.get(nodeId2).add(nodeId1);
        }
    }

    public void removeNode(String nodeId) {
        // Remove the node from all adjacency lists
        for (List<String> neighbors : adjacencyList.values()) {
            neighbors.remove(nodeId);
        }
        // Remove the node itself
        adjacencyList.remove(nodeId);
    }

    public void removeEdge(String nodeId1, String nodeId2, boolean isDirected) {
        if (adjacencyList.containsKey(nodeId1)) {
            adjacencyList.get(nodeId1).remove(nodeId2);
        }
        if (!isDirected && adjacencyList.containsKey(nodeId2)) {
            adjacencyList.get(nodeId2).remove(nodeId1);
        }
    }

    public Set<String> getNodes() {
        return adjacencyList.keySet();
    }

    public Map<String, List<String>> getAdjacencyList() {
        return adjacencyList;
    }
}

public class BSController implements Initializable {

    // FXML UI Elements
    @FXML private TextArea edgeInputArea;
    @FXML private Pane graphPane;
    @FXML private Label visitedNodesLabel;
    @FXML private ListView<String> pseudocodeList;
    @FXML private TextField getStartNode;
    @FXML private Label queueText;
    @FXML private RadioButton addNodeRadio;
    @FXML private RadioButton addEdgeRadio;
    @FXML private RadioButton deleteRadio;
    @FXML private CheckBox directedGraphCheckBox;
    @FXML private ToggleGroup modeToggleGroup;
    @FXML private Button resetButton;
    @FXML private Button runBFSButton;
    @FXML private Button drawFromTextButton;

    // Graph data structure
    private Graph graph;
    private int nodeCount = 0;

    // State for creating edges
    private StackPane firstNodeForEdge = null;

    // State for deleting elements
    private Object elementToDelete = null;
    private boolean wasDragged = false;// Can hold StackPane (node) or String (edge key)

    // Dragging variables
    private StackPane draggedNode = null;
    private double dragStartX, dragStartY;

    // Visual components for traversals
    private Map<String, StackPane> visualNodes = new HashMap<>();
    private List<String> visitedOrder = new ArrayList<>();
    private List<Group> allEdges = new ArrayList<>();
    private Map<String, Group> visualEdges = new HashMap<>();

    // Constants for styling
    private static final double NODE_RADIUS = 25.0;
    private static final Color NODE_COLOR = Color.web("#45B7D1");
    private static final Color NODE_STROKE_COLOR = Color.web("#2C3E50");
    private static final Color NODE_HIGHLIGHT_COLOR = Color.ORANGE;
    private static final Color NODE_DELETE_HIGHLIGHT_COLOR = Color.RED;
    private static final Color EDGE_COLOR = Color.BLACK;
    private static final Color EDGE_DELETE_HIGHLIGHT_COLOR = Color.RED;

    private final String[] bfsPseudoCode = {
            "1. procedure BFS(G, startNode)",
            "2.   let Q be a queue",
            "3.   Q.enqueue(startNode)",
            "4.   mark startNode as visited",
            "5.   while Q is not empty",
            "6.      v = Q.dequeue()",
            "7.      // process v",
            "8.      for each neighbor w of v",
            "9.         if w is not visited",
            "10.           mark w as visited",
            "11.           Q.enqueue(w)",
            "12.        end if",
            "13.     end for",
            "14.  end while",
            "15. end procedure"
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize pseudocode list
        pseudocodeList.getItems().addAll(bfsPseudoCode);
        if (visitedNodesLabel != null) {
            visitedNodesLabel.setText("Visited nodes will appear here during traversal");
        }

        // Initialize graph
        this.graph = new Graph();

        // Set default mode
        if (addNodeRadio != null) {
            addNodeRadio.setSelected(true);
        }

        // Create a beautiful default graph when the controller loads
        createDefaultGraph();
    }

    /**
     * Handles clicks on the main graph pane for interactive node/edge creation.
     */
    @FXML
    private void handleGraphPaneClick(MouseEvent event) {
        if(wasDragged)
        {
            wasDragged=false;
            return;
        }
        if (addNodeRadio.isSelected()) {
            addNode(event.getX(), event.getY());
        } else if (addEdgeRadio.isSelected()) {
            handleEdgeCreation(event.getX(), event.getY());
        } else if (deleteRadio.isSelected()) {
            handleDeletion(event.getX(), event.getY());
        }
    }

    /**
     * Creates and draws a new node on the pane.
     */
    private void addNode(double x, double y) {
        // Ensure node doesn't go outside pane bounds
        double boundedX = Math.max(NODE_RADIUS, Math.min(x, graphPane.getWidth() - NODE_RADIUS));
        double boundedY = Math.max(NODE_RADIUS, Math.min(y, graphPane.getHeight() - NODE_RADIUS));

        nodeCount++;
        // In BSController.java -> addNode method

        String nodeId = String.valueOf(nodeCount); // 1, 2, 3, etc.

        // Add to graph data structure
        graph.addNode(nodeId);

        // Create visual representation
        StackPane nodePane = createVisualNode(nodeId);
        nodePane.setLayoutX(boundedX - NODE_RADIUS);
        nodePane.setLayoutY(boundedY - NODE_RADIUS);

        visualNodes.put(nodeId, nodePane);
        graphPane.getChildren().add(nodePane);
        setupNodeDragging(nodePane);

        System.out.println("Node " + nodeId + " added at (" + (int) boundedX + ", " + (int) boundedY + ").");
    }

    /**
     * Manages the two-click process of creating an edge between two nodes.
     */
    private void handleEdgeCreation(double x, double y) {
        Optional<StackPane> clickedNodeOpt = findNodeAt(x, y);
        if (clickedNodeOpt.isEmpty()) {
            // Click was not on a node, so reset selection
            if (firstNodeForEdge != null) {
                resetNodeHighlight(firstNodeForEdge);
                firstNodeForEdge = null;
                System.out.println("Edge creation cancelled. Select the first node.");
            }
            return;
        }

        StackPane clickedNode = clickedNodeOpt.get();

        // Reset any previous edge creation highlight if clicking the same node again
        if (firstNodeForEdge == clickedNode) {
            resetNodeHighlight(firstNodeForEdge);
            firstNodeForEdge = null;
            System.out.println("Edge creation cancelled for Node " + getNodeId(clickedNode) + ".");
            return;
        }

        if (firstNodeForEdge == null) {
            // This is the first node selected for the edge
            firstNodeForEdge = clickedNode;
            highlightNode(firstNodeForEdge, NODE_HIGHLIGHT_COLOR);
            System.out.println("Selected Node " + getNodeId(firstNodeForEdge) + ". Select the second node.");
        } else {
            // This is the second node
            String firstNodeId = getNodeId(firstNodeForEdge);
            String secondNodeId = getNodeId(clickedNode);

            // Check if edge already exists
            boolean edgeExists = graph.getAdjacencyList().get(firstNodeId).contains(secondNodeId);
            if (edgeExists) {
                System.out.println("Error: Edge already exists between these nodes.");
                resetNodeHighlight(firstNodeForEdge);
                firstNodeForEdge = null;
                return;
            }

            // Create the edge
            boolean isDirected = directedGraphCheckBox.isSelected();
            graph.addEdge(firstNodeId, secondNodeId, isDirected);

            // Redraw all edges to include the new one
            drawEdges();

            System.out.println((isDirected ? "Directed" : "Undirected") + " edge added between Node " + firstNodeId + " and Node " + secondNodeId + ".");

            // Reset for next edge
            resetNodeHighlight(firstNodeForEdge);
            firstNodeForEdge = null;
        }
    }

    /**
     * Handles the deletion of nodes or edges.
     */
    private void handleDeletion(double x, double y) {
        // Check for node first (prioritize nodes over edges)
        Optional<StackPane> clickedNodeOpt = findNodeAt(x, y);
        Optional<String> clickedEdgeOpt = Optional.empty();

        // Only check for edge if no node was found
        if (clickedNodeOpt.isEmpty()) {
            clickedEdgeOpt = findEdgeAt(x, y);
        }

        // Reset previous selection highlight
        if (elementToDelete instanceof StackPane) {
            resetNodeHighlight((StackPane) elementToDelete);
        } else if (elementToDelete instanceof String) {
            resetEdgeHighlight((String) elementToDelete);
        }
        elementToDelete = null;

        // Select new element for deletion
        if (clickedNodeOpt.isPresent()) {
            StackPane nodeToSelect = clickedNodeOpt.get();
            elementToDelete = nodeToSelect;
            highlightNode(nodeToSelect, NODE_DELETE_HIGHLIGHT_COLOR);
            System.out.println("Node " + getNodeId(nodeToSelect) + " selected for deletion. Click 'Delete Selected' button or click again to deselect.");
        } else if (clickedEdgeOpt.isPresent()) {
            String edgeKey = clickedEdgeOpt.get();
            elementToDelete = edgeKey;
            highlightEdge(edgeKey, EDGE_DELETE_HIGHLIGHT_COLOR, 4.0);
            System.out.println("Edge " + edgeKey + " selected for deletion. Click 'Delete Selected' button or click again to deselect.");
        } else {
            System.out.println("Click on a node or edge to select it for deletion.");
        }
    }

    /**
     * Deletes the currently selected node or edge.
     */
    @FXML
    private void handleDeleteSelectedAction() {
        if (elementToDelete == null) {
            System.out.println("No element selected for deletion.");
            return;
        }

        if (elementToDelete instanceof StackPane) {
            StackPane nodeToDelete = (StackPane) elementToDelete;
            String nodeId = getNodeId(nodeToDelete);

            // Remove from graph data structure
            graph.removeNode(nodeId);

            // Remove from visual representation
            visualNodes.remove(nodeId);
            graphPane.getChildren().remove(nodeToDelete);

            // Redraw edges (this will automatically remove edges connected to deleted node)
            drawEdges();

            System.out.println("Node " + nodeId + " deleted.");
        } else if (elementToDelete instanceof String) {
            String edgeKey = (String) elementToDelete;
            String[] nodes = edgeKey.split("-");
            if (nodes.length == 2) {
                boolean isDirected = directedGraphCheckBox.isSelected();
                graph.removeEdge(nodes[0], nodes[1], isDirected);
                drawEdges();
                System.out.println("Edge " + edgeKey + " deleted.");
            }
        }
        elementToDelete = null;
    }

    // Helper methods for node/edge management
    private Optional<StackPane> findNodeAt(double x, double y) {
        return visualNodes.values().stream()
                .filter(node -> node.getBoundsInParent().contains(x, y))
                .findFirst();
    }

    private Optional<String> findEdgeAt(double x, double y) {
        final double tolerance = 10.0;
        return visualEdges.entrySet().stream()
                .filter(entry -> {
                    Group edgeGroup = entry.getValue();
                    Line line = (Line) edgeGroup.getChildren().getFirst();
                    double distance = distancePointToLineSegment(x, y,
                            line.getStartX(), line.getStartY(),
                            line.getEndX(), line.getEndY());
                    return distance <= tolerance;
                })
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private double distancePointToLineSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            return Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1));
        }

        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);

        if (t < 0) {
            return Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1));
        } else if (t > 1) {
            return Math.sqrt((px - x2) * (px - x2) + (py - y2) * (py - y2));
        } else {
            double closestX = x1 + t * dx;
            double closestY = y1 + t * dy;
            return Math.sqrt((px - closestX) * (px - closestX) + (py - closestY) * (py - closestY));
        }
    }

    private String getNodeId(StackPane nodePane) {
        Text label = (Text) nodePane.getChildren().get(1);
        return label.getText();
    }

    private void highlightNode(StackPane nodePane, Color color) {
        Circle circle = (Circle) nodePane.getChildren().get(0);
        circle.setFill(color);
    }

    private void resetNodeHighlight(StackPane nodePane) {
        Circle circle = (Circle) nodePane.getChildren().get(0);
        circle.setFill(NODE_COLOR);
    }

    private void highlightEdge(String edgeKey, Color color, double width) {
        Group edgeGroup = visualEdges.get(edgeKey);
        if (edgeGroup != null) {
            Line line = (Line) edgeGroup.getChildren().get(0);
            line.setStroke(color);
            line.setStrokeWidth(width);
            // Update arrow color if it exists
            if (edgeGroup.getChildren().size() > 1) {
                Polygon arrow = (Polygon) edgeGroup.getChildren().get(1);
                arrow.setFill(color);
            }
        }
    }

    private void resetEdgeHighlight(String edgeKey) {
        Group edgeGroup = visualEdges.get(edgeKey);
        if (edgeGroup != null) {
            Line line = (Line) edgeGroup.getChildren().get(0);
            line.setStroke(EDGE_COLOR);
            line.setStrokeWidth(3.0);
            // Reset arrow color if it exists
            if (edgeGroup.getChildren().size() > 1) {
                Polygon arrow = (Polygon) edgeGroup.getChildren().get(1);
                arrow.setFill(EDGE_COLOR);
            }
        }
    }

    // Original BFS methods (preserved with minor modifications)
    private void highlightPseudoCode(int lineNumber) {
        pseudocodeList.getSelectionModel().select(lineNumber);
        pseudocodeList.scrollTo(lineNumber);
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
            graph.addEdge(edge[0], edge[1], false); // Undirected by default
        }
        StringBuilder defaultText = new StringBuilder();
        for (String[] edge : defaultEdges) {
            defaultText.append(edge[0]).append(" ").append(edge[1]).append("\n");
        }
        if (edgeInputArea != null) {
            edgeInputArea.setText(defaultText.toString().trim());
        }
        renderBeautifulLayout();
    }

    @FXML
    void handleDrawGraph(ActionEvent event) {
        this.graph = new Graph();
        resetVisualState();
        graphPane.getChildren().clear();
        visualNodes.clear();
        nodeCount = 0;

        String[] lines = edgeInputArea.getText().trim().split("\n");
        Set<String> nodeIds = new HashSet<>();

        for (String line : lines) {
            String[] nodes = line.trim().split("\\s+");
            if (nodes.length == 2) {
                nodeIds.add(nodes[0]);
                nodeIds.add(nodes[1]);
                graph.addEdge(nodes[0], nodes[1], directedGraphCheckBox.isSelected());
            }
        }

        // Create visual nodes for all unique node IDs
        for (String nodeId : nodeIds) {
            StackPane nodePane = createVisualNode(nodeId);
            visualNodes.put(nodeId, nodePane);
            graphPane.getChildren().add(nodePane);
            setupNodeDragging(nodePane);
        }

        nodeCount = nodeIds.size();
        renderCircularLayout();
    }

    @FXML
    void handleBFS(ActionEvent event) {
        if (graph == null || graph.getNodes().isEmpty()) {
            return;
        }
        resetVisualState();
        String startNode = getStartNode.getText().trim();
        if (startNode.isEmpty() || !graph.getNodes().contains(startNode)) {
            System.out.println("Please enter a valid start node.");
            return;
        }
        performBFSWithVisualization(startNode);
        getStartNode.clear();
    }

    @FXML
    private void handleResetButtonAction() {
        graphPane.getChildren().clear();
        visualNodes.clear();
        allEdges.clear();
        visualEdges.clear();
        graph = new Graph();
        nodeCount = 0;
        firstNodeForEdge = null;
        elementToDelete = null;
        visitedOrder.clear();

        if (visitedNodesLabel != null) {
            visitedNodesLabel.setText("Visited nodes will appear here during traversal");
        }
        if (queueText != null) {
            queueText.setText("Queue: []");
        }

        System.out.println("Canvas cleared. You can start a new graph.");
    }

    private void resetVisualState() {
        visitedOrder.clear();
        for (StackPane nodePane : visualNodes.values()) {
            Circle circle = (Circle) nodePane.getChildren().get(0);
            circle.setFill(NODE_COLOR);
            circle.setStroke(NODE_STROKE_COLOR);
            circle.setStrokeWidth(3);
        }
        for (Group edgeGroup : allEdges) {
            Line line = (Line) edgeGroup.getChildren().get(0);
            line.setStroke(EDGE_COLOR);
            line.setStrokeWidth(2.5);
            // Reset arrow color if it exists
            if (edgeGroup.getChildren().size() > 1) {
                Polygon arrow = (Polygon) edgeGroup.getChildren().get(1);
                arrow.setFill(EDGE_COLOR);
            }
        }
        if (visitedNodesLabel != null) {
            visitedNodesLabel.setText("Traversal will start...");
        }
    }

    private void performBFSWithVisualization(String startNode) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Timeline timeline = new Timeline();
        double time = 0.0;
        visitedOrder.clear();
        resetVisualState();

        // Initial Setup Animation
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
            highlightPseudoCode(1);
            updateQueueDisplay(new LinkedList<>());
            if (visitedNodesLabel != null) {
                visitedNodesLabel.setText("Starting BFS traversal...");
            }
        }));
        time += 0.5;

        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> highlightPseudoCode(2)));
        time += 0.5;

        // Start of Algorithm
        queue.offer(startNode);
        visited.add(startNode);
        visitedOrder.add(startNode);

        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
            highlightPseudoCode(3);
            updateQueueDisplay(new LinkedList<>(queue));
            if (visitedNodesLabel != null) {
                visitedNodesLabel.setText("Enqueued start node: " + startNode);
            }
        }));
        time += 0.5;

        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
            highlightPseudoCode(4);
            highlightVisitedNode(startNode);
            if (visitedNodesLabel != null) {
                visitedNodesLabel.setText("Visited: " + startNode);
            }
        }));
        time += 1.0;

        // Main Loop Visualization
        while (!queue.isEmpty()) {
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
                highlightPseudoCode(5);
            }));
            time += 0.5;

            String currentNode = queue.poll();
            final Queue<String> queueAfterDequeue = new LinkedList<>(queue);

            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
                highlightPseudoCode(6);
                updateQueueDisplay(queueAfterDequeue);
            }));
            time += 1.0;

            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
                highlightPseudoCode(7);
            }));
            time += 0.8;

            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
                highlightPseudoCode(8);
            }));
            time += 0.8;

            List<String> neighbors = new ArrayList<>(graph.getAdjacencyList().get(currentNode));
            Collections.sort(neighbors);

            for (String neighbor : neighbors) {
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
                    highlightPseudoCode(9);
                }));
                time += 0.8;

                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                    visitedOrder.add(neighbor);

                    final String currentNodeRef = currentNode;
                    final String neighborRef = neighbor;
                    final Queue<String> queueAfterEnqueue = new LinkedList<>(queue);
                    final List<String> currentVisitedOrder = new ArrayList<>(visitedOrder);

                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
                        highlightPseudoCode(10);
                        highlightVisitedNode(neighborRef);
                        highlightTraversalEdge(currentNodeRef, neighborRef);
                        if (visitedNodesLabel != null) {
                            String currentText = visitedNodesLabel.getText();
                            if (currentText.equals("Visited: " + visitedOrder.get(0))) {
                                visitedNodesLabel.setText(currentText + " → " + neighborRef);
                            } else if (currentText.startsWith("Visited: ")) {
                                visitedNodesLabel.setText(currentText + " → " + neighborRef);
                            } else {
                                visitedNodesLabel.setText("Visited: " + String.join(" → ", currentVisitedOrder));
                            }
                        }
                    }));
                    time += 0.5;

                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
                        highlightPseudoCode(11);
                        updateQueueDisplay(queueAfterEnqueue);
                    }));
                    time += 0.5;
                } else {
                    final String currentNodeRef = currentNode;
                    final String neighborRef = neighbor;
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
                        highlightTraversalEdge(currentNodeRef, neighborRef);
                    }));
                    time += 0.3;
                }
            }

            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
                highlightPseudoCode(13);
            }));
            time += 0.5;
        }

        // Completion
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
            highlightPseudoCode(14);
        }));
        time += 1.0;

        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(time), e -> {
            highlightPseudoCode(15);
            if (visitedNodesLabel != null) {
                String currentText = visitedNodesLabel.getText();
                visitedNodesLabel.setText(currentText + " - BFS Complete!");
            }
            updateQueueDisplay(new LinkedList<>());
        }));

        timeline.play();
    }

    private void updateQueueDisplay(Queue<String> q) {
        if (queueText != null) {
            List<String> queueList = new ArrayList<>(q);
            String queueContent = queueList.isEmpty() ? "[]" : queueList.toString();
            queueText.setText("Queue: " + queueContent);
        }
    }

    private void highlightTraversalEdge(String u, String v) {
        boolean isDirected = directedGraphCheckBox.isSelected();
        String edgeKey;

        if (isDirected) {
            // For directed graphs, use the exact direction
            edgeKey = u + "-" + v;
        } else {
            // For undirected graphs, use sorted key
            edgeKey = u.compareTo(v) < 0 ? u + "-" + v : v + "-" + u;
        }

        Group edgeGroup = visualEdges.get(edgeKey);
        if (edgeGroup != null) {
            Line line = (Line) edgeGroup.getChildren().get(0);
            line.setStroke(Color.RED);
            line.setStrokeWidth(4);
            // Update arrow color if it exists
            if (edgeGroup.getChildren().size() > 1) {
                Polygon arrow = (Polygon) edgeGroup.getChildren().get(1);
                arrow.setFill(Color.RED);
            }
        }
    }

    private void highlightVisitedNode(String nodeId) {
        StackPane nodePane = visualNodes.get(nodeId);
        if (nodePane != null) {
            Circle circle = (Circle) nodePane.getChildren().get(0);
            circle.setFill(Color.web("#FF6B6B"));
            circle.setStroke(Color.web("#E74C3C"));
            circle.setStrokeWidth(4);
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
        nodeCount = nodeIds.size();
        drawEdges();
    }

    private void renderCircularLayout() {
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
            StackPane nodePane = visualNodes.get(nodeId);
            if (nodePane != null) {
                double angle = i * angleStep;
                double x = centerX + radius * Math.cos(angle) - 25;
                double y = centerY + radius * Math.sin(angle) - 25;
                nodePane.setLayoutX(x);
                nodePane.setLayoutY(y);
                i++;
            }
        }
        drawEdges();
    }

    private void drawEdges() {
        // Clear previous edges from the pane and maps
        graphPane.getChildren().removeAll(allEdges);
        allEdges.clear();
        visualEdges.clear();

        Map<String, List<String>> adjList = graph.getAdjacencyList();
        Set<String> processedEdges = new HashSet<>();
        boolean isDirected = directedGraphCheckBox.isSelected();

        for (String sourceId : adjList.keySet()) {
            StackPane sourcePane = visualNodes.get(sourceId);
            for (String targetId : adjList.get(sourceId)) {
                String edgeKey;

                if (isDirected) {
                    // For directed graphs, each direction is separate
                    edgeKey = sourceId + "-" + targetId;
                } else {
                    // For undirected graphs, use sorted key to avoid duplicates
                    edgeKey = sourceId.compareTo(targetId) < 0 ? sourceId + "-" + targetId : targetId + "-" + sourceId;
                    if (processedEdges.contains(edgeKey)) {
                        continue; // Skip if already processed for undirected
                    }
                }

                StackPane targetPane = visualNodes.get(targetId);
                if (sourcePane != null && targetPane != null) {
                    Group edgeGroup = createVisualEdge(sourcePane, targetPane, isDirected);
                    graphPane.getChildren().add(0, edgeGroup); // Add edges behind nodes
                    allEdges.add(edgeGroup);
                    visualEdges.put(edgeKey, edgeGroup);
                    processedEdges.add(edgeKey);
                }
            }
        }
    }

    private void setupNodeDragging(StackPane nodePane) {
        nodePane.setOnMousePressed(e -> {
            // Only allow dragging if not in delete or edge creation mode
            if (!deleteRadio.isSelected() && !addEdgeRadio.isSelected()) {
                draggedNode = nodePane;
                dragStartX = e.getSceneX() - nodePane.getLayoutX();
                dragStartY = e.getSceneY() - nodePane.getLayoutY();
            }
            e.consume();
        });

        nodePane.setOnMouseDragged(e -> {
            if (draggedNode == nodePane && !deleteRadio.isSelected() && !addEdgeRadio.isSelected()) {
                nodePane.setLayoutX(e.getSceneX() - dragStartX);
                nodePane.setLayoutY(e.getSceneY() - dragStartY);
                wasDragged=true;
                drawEdges();
            }
            e.consume();
        });

        nodePane.setOnMouseReleased(e -> {
            draggedNode = null;
            e.consume();
        });
    }

    private StackPane createVisualNode(String id) {
        Circle circle = new Circle(NODE_RADIUS);
        circle.setFill(NODE_COLOR);
        circle.setStroke(NODE_STROKE_COLOR);
        circle.setStrokeWidth(3);

        Text label = new Text(id);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-font-family: 'Arial';");

        StackPane nodePane = new StackPane(circle, label);

        nodePane.setOnMouseEntered(e -> {
            if (!deleteRadio.isSelected() && !addEdgeRadio.isSelected()) {
                circle.setStrokeWidth(4);
                circle.setStroke(Color.web("#F39C12"));
                nodePane.setStyle("-fx-cursor: hand;");
            }
        });

        nodePane.setOnMouseExited(e -> {
            if (!circle.getFill().equals(NODE_DELETE_HIGHLIGHT_COLOR) &&
                    !circle.getFill().equals(NODE_HIGHLIGHT_COLOR) &&
                    !circle.getFill().equals(Color.web("#FF6B6B"))) {
                circle.setStrokeWidth(3);
                circle.setStroke(NODE_STROKE_COLOR);
                nodePane.setStyle("-fx-cursor: default; ");
            }
        });

        return nodePane;
    }


private Group createVisualEdge(StackPane source, StackPane target, boolean isDirected) {
    Group edgeGroup = new Group();

    // Get center coordinates of the source and target nodes
    double startX = source.getLayoutX() + NODE_RADIUS;
    double startY = source.getLayoutY() + NODE_RADIUS;
    double endX = target.getLayoutX() + NODE_RADIUS;
    double endY = target.getLayoutY() + NODE_RADIUS;

    // Calculate direction vector and length
    double dx = endX - startX;
    double dy = endY - startY;
    double length = Math.sqrt(dx * dx + dy * dy);

    // Avoid division by zero
    if (length == 0) {
        return edgeGroup;
    }

    // Normalize the direction vector (unit vector)
    double unitDx = dx / length;
    double unitDy = dy / length;

    // Calculate the actual start and end points on the circumference of the circles
    double lineStartX = startX + unitDx * NODE_RADIUS;
    double lineStartY = startY + unitDy * NODE_RADIUS;
    double lineEndX = endX - unitDx * NODE_RADIUS;
    double lineEndY = endY - unitDy * NODE_RADIUS;

    // Create the line
    Line line = new Line(lineStartX, lineStartY, lineEndX, lineEndY);
    line.setStrokeWidth(3);
    line.setStroke(EDGE_COLOR);
    line.setOpacity(0.8);
//    line.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0.3, 1, 1);");
    edgeGroup.getChildren().add(line);

    // Add arrowhead for directed graphs
    if (isDirected) {
        double arrowLength = 12;
        double arrowWidth = 7;

        // Calculate the base of the arrowhead by moving back from the tip
        double arrowBaseX = lineEndX - unitDx * arrowLength;
        double arrowBaseY = lineEndY - unitDy * arrowLength;

        // Calculate the two corners of the arrowhead base using a perpendicular vector
        double perpDx = -unitDy * arrowWidth;
        double perpDy = unitDx * arrowWidth;

        Polygon arrow = new Polygon();
        arrow.getPoints().addAll(
                lineEndX, lineEndY, // Tip of the arrow
                arrowBaseX + perpDx, arrowBaseY + perpDy, // One corner of the base
                arrowBaseX - perpDx, arrowBaseY - perpDy  // The other corner of the base
        );
        arrow.setFill(EDGE_COLOR);
        edgeGroup.getChildren().add(arrow);
    }

    return edgeGroup;
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