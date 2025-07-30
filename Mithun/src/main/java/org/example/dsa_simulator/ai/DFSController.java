package org.example.dsa_simulator.ai;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class DFSController implements Initializable {

    // FXML UI Elements
    @FXML private TextArea edgeInputArea;
    @FXML private Pane graphPane;
    @FXML private Label visitedNodesLabel;
    @FXML private ListView<String> pseudocodeList;
    @FXML private TextField getStartNode;
    @FXML private Label stackText;
    @FXML private RadioButton addNodeRadio;
    @FXML private RadioButton addEdgeRadio;
    @FXML private RadioButton deleteRadio;
    @FXML private CheckBox directedGraphCheckBox;
    @FXML private ToggleGroup modeToggleGroup;
    @FXML private Button resetButton;
    @FXML private Button runDFSButton;
    @FXML private Button drawFromTextButton;

    // Graph data structure
    private Graph graph;
    private int nodeCount = 0;

    // State for creating edges
    private StackPane firstNodeForEdge = null;

    // State for deleting elements
    private Object elementToDelete = null;
    private boolean wasDragged = false;

    // Animation control
    private Timeline currentAnimation;

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
    private static final Color NODE_VISITED_COLOR = Color.ORANGE; // The only visited color
    private static final Color NODE_DELETE_HIGHLIGHT_COLOR = Color.RED;
    private static final Color EDGE_COLOR = Color.BLACK;
    private static final Color EDGE_TRAVERSAL_COLOR = Color.RED;
    private static final Color EDGE_DELETE_HIGHLIGHT_COLOR = Color.RED;

    // Updated DFS Pseudocode for recursive approach
    private final String[] dfsPseudoCode = {
            "1. procedure DFS(G, v, visited)",
            "2.   mark v as visited",
            "3.   // process v",
            "4.   for each neighbor w of v",
            "5.      if w is not visited",
            "6.         DFS(G, w, visited)",
            "7.      end if",
            "8.   end for",
            "9. end procedure"
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pseudocodeList.getItems().addAll(dfsPseudoCode);
        visitedNodesLabel.setText("Visited nodes will appear here");
        this.graph = new Graph();
        addNodeRadio.setSelected(true);
        createDefaultGraph();
    }

    @FXML
    private void handleGraphPaneClick(MouseEvent event) {
        if (wasDragged) {
            wasDragged = false;
            return;
        }
        if (addNodeRadio.isSelected()) addNode(event.getX(), event.getY());
        else if (addEdgeRadio.isSelected()) handleEdgeCreation(event.getX(), event.getY());
        else if (deleteRadio.isSelected()) handleDeletion(event.getX(), event.getY());
    }

    private void addNode(double x, double y) {
        double boundedX = Math.max(NODE_RADIUS, Math.min(x, graphPane.getWidth() - NODE_RADIUS));
        double boundedY = Math.max(NODE_RADIUS, Math.min(y, graphPane.getHeight() - NODE_RADIUS));
        nodeCount++;
        String nodeId = String.valueOf(nodeCount);
        graph.addNode(nodeId);
        StackPane nodePane = createVisualNode(nodeId);
        nodePane.setLayoutX(boundedX - NODE_RADIUS);
        nodePane.setLayoutY(boundedY - NODE_RADIUS);
        visualNodes.put(nodeId, nodePane);
        graphPane.getChildren().add(nodePane);
        setupNodeDragging(nodePane);
    }

    private void handleEdgeCreation(double x, double y) {
        Optional<StackPane> clickedNodeOpt = findNodeAt(x, y);
        if (clickedNodeOpt.isEmpty()) {
            if (firstNodeForEdge != null) {
                resetNodeHighlight(firstNodeForEdge);
                firstNodeForEdge = null;
            }
            return;
        }
        StackPane clickedNode = clickedNodeOpt.get();
        if (firstNodeForEdge == clickedNode) {
            resetNodeHighlight(firstNodeForEdge);
            firstNodeForEdge = null;
            return;
        }
        if (firstNodeForEdge == null) {
            firstNodeForEdge = clickedNode;
            highlightNode(firstNodeForEdge, NODE_HIGHLIGHT_COLOR);
        } else {
            String firstNodeId = getNodeId(firstNodeForEdge);
            String secondNodeId = getNodeId(clickedNode);
            if (graph.getAdjacencyList().get(firstNodeId).contains(secondNodeId)) {
                resetNodeHighlight(firstNodeForEdge);
                firstNodeForEdge = null;
                return;
            }
            graph.addEdge(firstNodeId, secondNodeId, directedGraphCheckBox.isSelected());
            drawEdges();
            resetNodeHighlight(firstNodeForEdge);
            firstNodeForEdge = null;
        }
    }

    private void handleDeletion(double x, double y) {
        Optional<StackPane> clickedNodeOpt = findNodeAt(x, y);
        Optional<String> clickedEdgeOpt = clickedNodeOpt.isEmpty() ? findEdgeAt(x, y) : Optional.empty();
        if (elementToDelete instanceof StackPane) resetNodeHighlight((StackPane) elementToDelete);
        else if (elementToDelete instanceof String) resetEdgeHighlight((String) elementToDelete);
        elementToDelete = null;
        if (clickedNodeOpt.isPresent()) {
            elementToDelete = clickedNodeOpt.get();
            highlightNode((StackPane) elementToDelete, NODE_DELETE_HIGHLIGHT_COLOR);
        } else if (clickedEdgeOpt.isPresent()) {
            elementToDelete = clickedEdgeOpt.get();
            highlightEdge((String) elementToDelete, EDGE_DELETE_HIGHLIGHT_COLOR, 4.0);
        }
    }

    @FXML
    private void handleDeleteSelectedAction() {
        if (elementToDelete == null) return;
        if (elementToDelete instanceof StackPane) {
            String nodeId = getNodeId((StackPane) elementToDelete);
            graph.removeNode(nodeId);
            visualNodes.remove(nodeId);
            graphPane.getChildren().remove((StackPane) elementToDelete);
        } else if (elementToDelete instanceof String) {
            String[] nodes = ((String) elementToDelete).split("-");
            if (nodes.length == 2) {
                graph.removeEdge(nodes[0], nodes[1], directedGraphCheckBox.isSelected());
            }
        }
        drawEdges();
        elementToDelete = null;
    }

    private Optional<StackPane> findNodeAt(double x, double y) {
        return visualNodes.values().stream().filter(node -> node.getBoundsInParent().contains(x, y)).findFirst();
    }

    private Optional<String> findEdgeAt(double x, double y) {
        return visualEdges.entrySet().stream().filter(entry -> {
            Line line = (Line) entry.getValue().getChildren().getFirst();
            return distancePointToLineSegment(x, y, line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY()) <= 10.0;
        }).map(Map.Entry::getKey).findFirst();
    }

    private double distancePointToLineSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        if (dx == 0 && dy == 0) return Math.hypot(px - x1, py - y1);
        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        return Math.hypot(px - (x1 + t * dx), py - (y1 + t * dy));
    }

    private String getNodeId(StackPane nodePane) {
        return ((Text) nodePane.getChildren().get(1)).getText();
    }

    private void highlightNode(StackPane nodePane, Color color) {
        ((Circle) nodePane.getChildren().get(0)).setFill(color);
    }

    private void resetNodeHighlight(StackPane nodePane) {
        ((Circle) nodePane.getChildren().get(0)).setFill(NODE_COLOR);
    }

    private void highlightEdge(String edgeKey, Color color, double width) {
        Group edgeGroup = visualEdges.get(edgeKey);
        if (edgeGroup != null) {
            Line line = (Line) edgeGroup.getChildren().get(0);
            line.setStroke(color);
            line.setStrokeWidth(width);
            if (edgeGroup.getChildren().size() > 1) ((Polygon) edgeGroup.getChildren().get(1)).setFill(color);
        }
    }

    private void resetEdgeHighlight(String edgeKey) {
        Group edgeGroup = visualEdges.get(edgeKey);
        if (edgeGroup != null) {
            Line line = (Line) edgeGroup.getChildren().get(0);
            line.setStroke(EDGE_COLOR);
            line.setStrokeWidth(3.0);
            if (edgeGroup.getChildren().size() > 1) ((Polygon) edgeGroup.getChildren().get(1)).setFill(EDGE_COLOR);
        }
    }

    private void highlightPseudoCode(int lineNumber) {
        pseudocodeList.getSelectionModel().select(lineNumber);
        pseudocodeList.scrollTo(lineNumber);
    }

    private void createDefaultGraph() {
        handleResetButtonAction();
        this.graph = new Graph();
        String[][] defaultEdges = {{"1", "2"}, {"1", "3"}, {"2", "4"}, {"2", "5"}, {"3", "6"}, {"3", "7"}, {"5", "8"}};
        nodeCount = 8;
        for (String[] edge : defaultEdges) {
            graph.addEdge(edge[0], edge[1], false);
        }
        StringBuilder defaultText = new StringBuilder();
        for (String[] edge : defaultEdges) {
            defaultText.append(edge[0]).append(" ").append(edge[1]).append("\n");
        }
        edgeInputArea.setText(defaultText.toString().trim());

        Set<String> nodeIds = graph.getNodes();
        for (String nodeId : nodeIds) {
            StackPane nodePane = createVisualNode(nodeId);
            visualNodes.put(nodeId, nodePane);
            graphPane.getChildren().add(nodePane);
            setupNodeDragging(nodePane);
        }

        renderCircularLayout();
    }

    @FXML
    void handleDrawGraph(ActionEvent event) {
        handleResetButtonAction();
        this.graph = new Graph();
        Set<String> nodeIds = new HashSet<>();
        for (String line : edgeInputArea.getText().trim().split("\n")) {
            String[] nodes = line.trim().split("\\s+");
            if (nodes.length == 2) {
                nodeIds.add(nodes[0]);
                nodeIds.add(nodes[1]);
                graph.addEdge(nodes[0], nodes[1], directedGraphCheckBox.isSelected());
            }
        }
        for (String nodeId : new TreeSet<>(nodeIds)) {
            StackPane nodePane = createVisualNode(nodeId);
            visualNodes.put(nodeId, nodePane);
            graphPane.getChildren().add(nodePane);
            setupNodeDragging(nodePane);
        }
        nodeCount = nodeIds.size();
        renderCircularLayout();
    }

    @FXML
    void handleDFS(ActionEvent event) {
        if (graph == null || graph.getNodes().isEmpty()) return;
        resetVisualState();
        String startNode = getStartNode.getText().trim();
        if (startNode.isEmpty() || !visualNodes.containsKey(startNode)) {
            System.out.println("Please enter a valid start node.");
            return;
        }
        performDFSWithVisualization(startNode);
        getStartNode.clear();
    }

    @FXML
    private void handleResetButtonAction() {
        if(currentAnimation != null) currentAnimation.stop();
        graphPane.getChildren().clear();
        visualNodes.clear();
        allEdges.clear();
        visualEdges.clear();
        graph = new Graph();
        nodeCount = 0;
        firstNodeForEdge = null;
        elementToDelete = null;
        visitedOrder.clear();
        visitedNodesLabel.setText("Visited nodes will appear here");
        stackText.setText("Stack: []");
    }

    private void resetVisualState() {
        visitedOrder.clear();
        visualNodes.values().forEach(this::resetNodeHighlight);
        allEdges.forEach(edgeGroup -> {
            Line line = (Line) edgeGroup.getChildren().get(0);
            line.setStroke(EDGE_COLOR);
            line.setStrokeWidth(2.5);
            if (edgeGroup.getChildren().size() > 1) ((Polygon) edgeGroup.getChildren().get(1)).setFill(EDGE_COLOR);
        });
        visitedNodesLabel.setText("Traversal will start...");
    }

    private void highlightVisitedNode(String nodeId) {
        StackPane nodePane = visualNodes.get(nodeId);
        if (nodePane != null) {
            Circle circle = (Circle) nodePane.getChildren().get(0);
            circle.setFill(NODE_VISITED_COLOR);
            circle.setStroke(Color.web("#E74C3C"));
            circle.setStrokeWidth(4);
        }
    }

    /**
     * Main entry point for the DFS animation. Sets up the initial state and starts the recursive animation.
     */
    private void performDFSWithVisualization(String startNode) {
        resetVisualState();
        currentAnimation = new Timeline();
        Set<String> visited = new HashSet<>();
        Stack<String> stack = new Stack<>(); // For visual display only

        // Start the recursive animation chain
        animateDFSStep(null, startNode, visited, stack, currentAnimation);

        // Add a final frame to signal completion
        currentAnimation.getKeyFrames().add(new KeyFrame(currentAnimation.getTotalDuration().add(Duration.seconds(1)), e -> {
            visitedNodesLabel.setText(visitedNodesLabel.getText() + " - DFS Complete!");
            updateStackDisplay(new Stack<>()); // Clear the stack display
        }));

        currentAnimation.play();
    }

    /**
     * This recursive method builds the animation for one step of DFS.
     * It animates visiting the current node, then recursively calls itself for each unvisited neighbor.
     */
    private void animateDFSStep(String parentNode, String currentNode, Set<String> visited, Stack<String> stack, Timeline timeline) {
        // Base case: if node is already visited, do nothing.
        if (visited.contains(currentNode)) {
            return;
        }

        // Mark as visited immediately to prevent cycles in the animation logic
        visited.add(currentNode);
        stack.push(currentNode);

        // Create a copy of the stack at this point in time for the animation frame
        final Stack<String> stackStateForAnimation = (Stack<String>) stack.clone();

        // Add a KeyFrame to animate visiting THIS node
        timeline.getKeyFrames().add(new KeyFrame(timeline.getTotalDuration().add(Duration.seconds(1.2)), e -> {
            highlightPseudoCode(2); // mark v as visited
            highlightVisitedNode(currentNode);
            if (parentNode != null) {
                highlightTraversalEdge(parentNode, currentNode);
            }
            visitedOrder.add(currentNode);
            visitedNodesLabel.setText("Visited: " + String.join(" → ", visitedOrder));
            updateStackDisplay(stackStateForAnimation);
        }));

        // Get neighbors and sort them to ensure a consistent, intuitive traversal order
        List<String> neighbors = new ArrayList<>(graph.getAdjacencyList().get(currentNode));
        neighbors.sort(Comparator.comparingInt(Integer::parseInt));

        // Recursively build the animation for each unvisited neighbor
        for (String neighbor : neighbors) {
            // Add a keyframe to show the algorithm considering the neighbor
            final String currentNeighbor = neighbor;
            timeline.getKeyFrames().add(new KeyFrame(timeline.getTotalDuration().add(Duration.seconds(0.5)), e -> {
                highlightPseudoCode(5); // "if w is not visited"
                // Temporarily highlight the edge being considered
                highlightTraversalEdge(currentNode, currentNeighbor);
            }));

            if (!visited.contains(neighbor)) {
                // The timeline's duration grows as we add keyframes for the entire subtree
                animateDFSStep(currentNode, neighbor, visited, stack, timeline);
            }
            // No 'else' block needed here because the edge is already highlighted above.
            // You could add a pause here if you want to explicitly show the check failing.
        }

        // After exploring all children, this node's part in the recursion is done. Pop it from the visual stack.
        stack.pop();
        final Stack<String> stackStateAfterBacktrack = (Stack<String>) stack.clone();
        timeline.getKeyFrames().add(new KeyFrame(timeline.getTotalDuration().add(Duration.seconds(0.8)), e -> {
            updateStackDisplay(stackStateAfterBacktrack);
        }));
    }


    private void updateStackDisplay(Stack<String> s) {
        if (stackText != null) {
            String stackContent = s.isEmpty() ? "[]" : new ArrayList<>(s).toString();
            stackText.setText("Stack: " + stackContent);
        }
    }

    private void highlightTraversalEdge(String u, String v) {
        String edgeKey = directedGraphCheckBox.isSelected() ? u + "-" + v : (u.compareTo(v) < 0 ? u + "-" + v : v + "-" + u);
        highlightEdge(edgeKey, EDGE_TRAVERSAL_COLOR, 4.0);
    }

    private void renderCircularLayout() {
        Set<String> nodeIds = graph.getNodes();
        if (nodeIds.isEmpty()) return;
        double width = Math.max(graphPane.getWidth(), 600), height = Math.max(graphPane.getHeight(), 400);
        double radius = Math.min(width, height) / 3;
        double angleStep = 2 * Math.PI / nodeIds.size();
        int i = 0;
        List<String> sortedNodes = new ArrayList<>(nodeIds);
        Collections.sort(sortedNodes);
        for (String nodeId : sortedNodes) {
            StackPane nodePane = visualNodes.get(nodeId);
            if (nodePane != null) {
                double angle = i * angleStep;
                nodePane.setLayoutX(width / 2 + radius * Math.cos(angle) - 25);
                nodePane.setLayoutY(height / 2 + radius * Math.sin(angle) - 25);
                i++;
            }
        }
        drawEdges();
    }

    private void drawEdges() {
        graphPane.getChildren().removeAll(allEdges);
        allEdges.clear();
        visualEdges.clear();
        Map<String, List<String>> adjList = graph.getAdjacencyList();
        Set<String> processedEdges = new HashSet<>();
        boolean isDirected = directedGraphCheckBox.isSelected();
        for (String sourceId : adjList.keySet()) {
            for (String targetId : adjList.get(sourceId)) {
                String edgeKey = isDirected ? sourceId + "-" + targetId : (sourceId.compareTo(targetId) < 0 ? sourceId + "-" + targetId : targetId + "-" + sourceId);
                if (!isDirected && processedEdges.contains(edgeKey)) continue;
                StackPane sourcePane = visualNodes.get(sourceId);
                StackPane targetPane = visualNodes.get(targetId);
                if (sourcePane != null && targetPane != null) {
                    Group edgeGroup = createVisualEdge(sourcePane, targetPane, isDirected);
                    graphPane.getChildren().add(0, edgeGroup);
                    allEdges.add(edgeGroup);
                    visualEdges.put(edgeKey, edgeGroup);
                    processedEdges.add(edgeKey);
                }
            }
        }
    }

    private void setupNodeDragging(StackPane nodePane) {
        nodePane.setOnMousePressed(e -> {
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
                wasDragged = true;
                drawEdges();
            }
            e.consume();
        });
        nodePane.setOnMouseReleased(e -> {
            if (draggedNode != null) {
                draggedNode = null;
                drawEdges();
            }
            e.consume();
        });
    }

    private StackPane createVisualNode(String id) {
        Circle circle = new Circle(NODE_RADIUS, NODE_COLOR);
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
            if (!circle.getFill().equals(NODE_DELETE_HIGHLIGHT_COLOR) && !circle.getFill().equals(NODE_HIGHLIGHT_COLOR) && !circle.getFill().equals(NODE_VISITED_COLOR)) {
                circle.setStrokeWidth(3);
                circle.setStroke(NODE_STROKE_COLOR);
                nodePane.setStyle("-fx-cursor: default;");
            }
        });
        return nodePane;
    }

    private Group createVisualEdge(StackPane source, StackPane target, boolean isDirected) {
        Group edgeGroup = new Group();
        double startX = source.getLayoutX() + NODE_RADIUS, startY = source.getLayoutY() + NODE_RADIUS;
        double endX = target.getLayoutX() + NODE_RADIUS, endY = target.getLayoutY() + NODE_RADIUS;
        double dx = endX - startX, dy = endY - startY;
        double length = Math.hypot(dx, dy);
        if (length == 0) return edgeGroup;
        double unitDx = dx / length, unitDy = dy / length;
        double lineStartX = startX + unitDx * NODE_RADIUS, lineStartY = startY + unitDy * NODE_RADIUS;
        double lineEndX = endX - unitDx * NODE_RADIUS, lineEndY = endY - unitDy * NODE_RADIUS;
        Line line = new Line(lineStartX, lineStartY, lineEndX, lineEndY);
        line.setStrokeWidth(3);
        line.setStroke(EDGE_COLOR);
        line.setOpacity(0.8);
        edgeGroup.getChildren().add(line);
        if (isDirected) {
            double arrowLength = 12, arrowWidth = 7;
            double arrowBaseX = lineEndX - unitDx * arrowLength, arrowBaseY = lineEndY - unitDy * arrowLength;
            double perpDx = -unitDy * arrowWidth, perpDy = unitDx * arrowWidth;
            Polygon arrow = new Polygon(lineEndX, lineEndY, arrowBaseX + perpDx, arrowBaseY + perpDy, arrowBaseX - perpDx, arrowBaseY - perpDy);
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
