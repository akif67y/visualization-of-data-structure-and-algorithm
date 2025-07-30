package org.example.dsa_simulator.graph;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the Kruskal's Algorithm visualization tool.
 * This class handles all user interactions, graph creation, and the animated execution of the algorithm.
 */
public class KruskalController implements Initializable {

    // FXML UI Elements
    @FXML private Pane graphPane;
    @FXML private RadioButton addNodeRadio;
    @FXML private RadioButton addEdgeRadio;
    @FXML private RadioButton deleteRadio;
    @FXML private TextField edgeWeightField;
    @FXML private Button runButton;
    @FXML private Button resetButton;
    @FXML private Slider speedSlider;
    @FXML private TextArea logArea;
    @FXML private ToggleGroup modeToggleGroup;
    @FXML private Button returnButton;
    @FXML private Label mstWeightLabel;
    @FXML private Label mstEdgesLabel;
    @FXML private VBox mstResultBox;

    // Graph data structures
    private final List<GraphNode> nodes = new ArrayList<>();
    private final List<GraphEdge> edges = new ArrayList<>();
    private int nodeCount = 0;

    // State for creating edges
    private GraphNode firstNodeForEdge = null;

    // State for deleting elements
    private Object elementToDelete = null;

    // Animation state
    private PauseTransition currentStepAnimation;
    private Duration animationDuration = Duration.millis(500); // Default speed

    // Constants for styling
    private static final double NODE_RADIUS = 20.0;
    private static final Color NODE_COLOR = Color.SKYBLUE;
    private static final Color NODE_STROKE_COLOR = Color.BLACK;
    private static final Color NODE_HIGHLIGHT_COLOR = Color.ORANGE;
    private static final Color NODE_DELETE_HIGHLIGHT_COLOR = Color.RED;
    private static final Color EDGE_COLOR = Color.BLACK;
    private static final Color EDGE_TESTING_COLOR = Color.GOLD;
    private static final Color EDGE_MST_COLOR = Color.FORESTGREEN;
    private static final Color EDGE_DISCARDED_COLOR = Color.LIGHTGRAY;
    private static final Color EDGE_DELETE_HIGHLIGHT_COLOR = Color.RED;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log("Application started. Select 'Add Nodes' to begin.");
        mstWeightLabel.setText("Total Weight: N/A");
        mstEdgesLabel.setText("Edges: N/A");

        // --- FIX: Add a listener to the speed slider to update duration in real-time ---
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // This logic inverts the slider so right is faster
            double newDurationMillis = speedSlider.getMax() - newVal.doubleValue() + speedSlider.getMin();
            animationDuration = Duration.millis(newDurationMillis);
        });
    }

    @FXML
    private void handleGraphPaneClick(MouseEvent event) {
        if (addNodeRadio.isSelected()) {
            addNode(event.getX(), event.getY());
        } else if (addEdgeRadio.isSelected()) {
            handleEdgeCreation(event.getX(), event.getY());
        } else if (deleteRadio.isSelected()) {
            handleDeletion(event.getX(), event.getY());
        }
    }

    private void addNode(double x, double y) {
        double boundedX = Math.max(NODE_RADIUS, Math.min(x, graphPane.getWidth() - NODE_RADIUS));
        double boundedY = Math.max(NODE_RADIUS, Math.min(y, graphPane.getHeight() - NODE_RADIUS));
        nodeCount++;
        GraphNode node = new GraphNode(boundedX, boundedY, nodeCount);
        nodes.add(node);
        graphPane.getChildren().addAll(node.getCircle(), node.getLabel());
        log("Node " + nodeCount + " added at (" + (int) boundedX + ", " + (int) boundedY + ").");
    }

    private void handleEdgeCreation(double x, double y) {
        Optional<GraphNode> clickedNodeOpt = findNodeAt(x, y);
        if (clickedNodeOpt.isEmpty()) {
            if (firstNodeForEdge != null) {
                firstNodeForEdge.resetColor();
                firstNodeForEdge = null;
                log("Edge creation cancelled. Select the first node.");
            }
            return;
        }
        GraphNode clickedNode = clickedNodeOpt.get();

        if (firstNodeForEdge == clickedNode) {
            firstNodeForEdge.resetColor();
            firstNodeForEdge = null;
            log("Edge creation cancelled for Node " + clickedNode.id + ".");
            return;
        }

        if (firstNodeForEdge == null) {
            firstNodeForEdge = clickedNode;
            firstNodeForEdge.getCircle().setFill(NODE_HIGHLIGHT_COLOR);
            log("Selected Node " + firstNodeForEdge.id + ". Select the second node.");
        } else {
            double weight;
            try {
                weight = Double.parseDouble(edgeWeightField.getText());
                if (weight <= 0) {
                    log("Error: Edge weight must be positive.");
                    firstNodeForEdge.resetColor();
                    firstNodeForEdge = null;
                    return;
                }
            } catch (NumberFormatException e) {
                log("Error: Invalid edge weight. Please enter a number.");
                firstNodeForEdge.resetColor();
                firstNodeForEdge = null;
                return;
            }

            if (edges.stream().anyMatch(e -> e.connects(firstNodeForEdge, clickedNode))) {
                log("Error: Edge already exists between these nodes.");
                firstNodeForEdge.resetColor();
                firstNodeForEdge = null;
                return;
            }

            GraphEdge edge = new GraphEdge(firstNodeForEdge, clickedNode, weight);
            edges.add(edge);
            graphPane.getChildren().addAll(edge.getLine(), edge.getWeightLabel());
            edge.getLine().toBack();
            edge.getWeightLabel().toFront();
            log("Edge added between Node " + firstNodeForEdge.id + " and Node " + clickedNode.id + " with weight " + weight + ".");
            firstNodeForEdge.resetColor();
            firstNodeForEdge = null;
        }
    }

    private void handleDeletion(double x, double y) {
        Optional<GraphNode> clickedNodeOpt = findNodeAt(x, y);
        Optional<GraphEdge> clickedEdgeOpt = clickedNodeOpt.isEmpty() ? findEdgeAt(x, y) : Optional.empty();

        Object newlyClickedElement = clickedNodeOpt.isPresent() ? clickedNodeOpt.get() : clickedEdgeOpt.orElse(null);

        if (elementToDelete != null && elementToDelete != newlyClickedElement) {
            resetElementHighlight(elementToDelete);
            elementToDelete = null;
        }

        if (newlyClickedElement != null) {
            if (newlyClickedElement == elementToDelete) {
                resetElementHighlight(elementToDelete);
                log("Deselected " + getElementName(elementToDelete) + ".");
                elementToDelete = null;
            } else {
                elementToDelete = newlyClickedElement;
                highlightElementForDeletion(elementToDelete);
                log(getElementName(elementToDelete) + " selected for deletion. Click the 'Delete Selected' button.");
            }
        }
    }

    @FXML
    private void handleDeleteSelectedAction() {
        if (elementToDelete == null) {
            log("No element selected for deletion.");
            return;
        }

        if (elementToDelete instanceof GraphNode) {
            GraphNode nodeToDelete = (GraphNode) elementToDelete;
            edges.removeIf(edge -> {
                boolean connected = (edge.u == nodeToDelete || edge.v == nodeToDelete);
                if (connected) {
                    graphPane.getChildren().removeAll(edge.getLine(), edge.getWeightLabel());
                }
                return connected;
            });
            nodes.remove(nodeToDelete);
            graphPane.getChildren().removeAll(nodeToDelete.getCircle(), nodeToDelete.getLabel());
        } else if (elementToDelete instanceof GraphEdge) {
            GraphEdge edgeToDelete = (GraphEdge) elementToDelete;
            edges.remove(edgeToDelete);
            graphPane.getChildren().removeAll(edgeToDelete.getLine(), edgeToDelete.getWeightLabel());
        }
        log(getElementName(elementToDelete) + " deleted.");
        elementToDelete = null;
    }

    @FXML
    private void handleRunButtonAction() {
        if (edges.isEmpty()) {
            log("Cannot run algorithm. Please add edges first.");
            return;
        }
        setControlsDisabled(true);
        resetGraphStyles();
        log("--- Starting Kruskal's Algorithm ---");

        List<GraphEdge> sortedEdges = new ArrayList<>(edges);
        sortedEdges.sort(Comparator.comparingDouble(e -> e.weight));

        int maxNodeId = nodes.stream().mapToInt(n -> n.id).max().orElse(0);
        DisjointSet ds = new DisjointSet(maxNodeId + 1);

        runKruskalStep(sortedEdges.iterator(), ds, new ArrayList<>());
    }

    private void runKruskalStep(Iterator<GraphEdge> edgeIterator, DisjointSet ds, List<GraphEdge> mst) {
        if (!edgeIterator.hasNext()) {
            finalizeAlgorithm(mst);
            return;
        }

        GraphEdge currentEdge = edgeIterator.next();

        // --- FIX: Use the animationDuration field which is updated by the slider's listener ---
        currentStepAnimation = new PauseTransition(animationDuration);
        currentStepAnimation.setOnFinished(e -> {
            currentEdge.setStyle(EDGE_TESTING_COLOR, 2.5);
            log("Considering edge: " + currentEdge.u.id + " - " + currentEdge.v.id + " (Weight: " + currentEdge.weight + ")");

            // --- FIX: Use the animationDuration field here as well ---
            currentStepAnimation = new PauseTransition(animationDuration);
            currentStepAnimation.setOnFinished(event -> {
                if (ds.find(currentEdge.u.id) != ds.find(currentEdge.v.id)) {
                    ds.union(currentEdge.u.id, currentEdge.v.id);
                    mst.add(currentEdge);
                    currentEdge.setStyle(EDGE_MST_COLOR, 3.0);
                    currentEdge.u.getCircle().setFill(EDGE_MST_COLOR);
                    currentEdge.v.getCircle().setFill(EDGE_MST_COLOR);
                    log("  -> Edge " + currentEdge.u.id + "-" + currentEdge.v.id + " added to MST.");
                } else {
                    currentEdge.setStyle(EDGE_DISCARDED_COLOR, 1.0);
                    currentEdge.getWeightLabel().setFill(EDGE_DISCARDED_COLOR);
                    currentEdge.getLine().getStrokeDashArray().addAll(5d, 5d);
                    log("  -> Edge " + currentEdge.u.id + "-" + currentEdge.v.id + " forms a cycle. Discarded.");
                }
                runKruskalStep(edgeIterator, ds, mst);
            });
            currentStepAnimation.play();
        });
        currentStepAnimation.play();
    }

    private void finalizeAlgorithm(List<GraphEdge> mst) {
        double finalWeight = mst.stream().mapToDouble(edge -> edge.weight).sum();
        log("--- Kruskal's Algorithm Complete ---");
        log("Minimum Spanning Tree Weight: " + String.format("%.2f", finalWeight));

        mstWeightLabel.setText("Total Weight: " + String.format("%.2f", finalWeight));

        StringBuilder edgesText = new StringBuilder("Edges: ");
        if (mst.isEmpty()) {
            edgesText.append("N/A");
        } else {
            edgesText.append(
                    mst.stream()
                            .map(edge -> edge.u.id + "-" + edge.v.id)
                            .collect(Collectors.joining(", "))
            );
        }
        mstEdgesLabel.setText(edgesText.toString());

        setControlsDisabled(false);
        currentStepAnimation = null;
    }


    @FXML
    private void handleResetButtonAction() {
        if (currentStepAnimation != null) {
            currentStepAnimation.stop();
            currentStepAnimation = null;
        }
        graphPane.getChildren().clear();
        nodes.clear();
        edges.clear();
        nodeCount = 0;
        firstNodeForEdge = null;
        elementToDelete = null;
        logArea.clear();
        mstWeightLabel.setText("Total Weight: N/A");
        mstEdgesLabel.setText("Edges: N/A");
        setControlsDisabled(false);
        log("Canvas cleared. You can start a new graph.");
    }

    private void resetGraphStyles() {
        nodes.forEach(GraphNode::resetColor);
        edges.forEach(edge -> {
            edge.setStyle(edge.originalColor, edge.originalWidth);
            edge.getLine().getStrokeDashArray().clear();
        });
    }

    private Optional<GraphNode> findNodeAt(double x, double y) {
        return nodes.stream()
                .filter(node -> node.getCircle().getBoundsInParent().contains(x, y))
                .findFirst();
    }

    private Optional<GraphEdge> findEdgeAt(double x, double y) {
        final double tolerance = 10.0;
        return edges.stream()
                .filter(edge -> distancePointToLineSegment(x, y, edge.getLine().getStartX(), edge.getLine().getStartY(), edge.getLine().getEndX(), edge.getLine().getEndY()) <= tolerance)
                .findFirst();
    }

    private void resetElementHighlight(Object element) {
        if (element instanceof GraphNode) {
            ((GraphNode) element).resetColor();
        } else if (element instanceof GraphEdge) {
            GraphEdge edge = (GraphEdge) element;
            edge.setStyle(edge.originalColor, edge.originalWidth);
        }
    }

    private void highlightElementForDeletion(Object element) {
        if (element instanceof GraphNode) {
            ((GraphNode) element).getCircle().setFill(NODE_DELETE_HIGHLIGHT_COLOR);
        } else if (element instanceof GraphEdge) {
            ((GraphEdge) element).setStyle(EDGE_DELETE_HIGHLIGHT_COLOR, 3.0);
        }
    }

    private String getElementName(Object element) {
        if (element instanceof GraphNode) {
            return "Node " + ((GraphNode) element).id;
        } else if (element instanceof GraphEdge) {
            GraphEdge edge = (GraphEdge) element;
            return "Edge " + edge.u.id + "-" + edge.v.id;
        }
        return "Element";
    }

    private double distancePointToLineSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (dx == 0 && dy == 0) {
            return Math.hypot(px - x1, py - y1);
        }
        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        if (t < 0) return Math.hypot(px - x1, py - y1);
        if (t > 1) return Math.hypot(px - x2, py - y2);
        return Math.hypot(px - (x1 + t * dx), py - (y1 + t * dy));
    }

    private void log(String message) {
        Platform.runLater(() -> logArea.appendText(message + System.lineSeparator()));
    }

    private void setControlsDisabled(boolean disabled) {
        addNodeRadio.setDisable(disabled);
        addEdgeRadio.setDisable(disabled);
        deleteRadio.setDisable(disabled);
        edgeWeightField.setDisable(disabled);
        runButton.setDisable(disabled);
        resetButton.setDisable(disabled);
        if (graphPane.getScene() != null) {
            Button deleteSelectedBtn = (Button) graphPane.getScene().lookup("#deleteSelectedButton");
            if (deleteSelectedBtn != null) deleteSelectedBtn.setDisable(disabled);
        }
    }

    // --- REMOVED getAnimationSpeed() method ---

    private static class GraphNode {
        final int id;
        final Circle circle;
        final Text label;
        final Color originalColor = NODE_COLOR;

        GraphNode(double x, double y, int id) {
            this.id = id;
            this.circle = new Circle(x, y, NODE_RADIUS, originalColor);
            this.circle.setStroke(NODE_STROKE_COLOR);
            this.circle.setStrokeWidth(2.0);
            this.label = new Text(String.valueOf(id));
            updateLabelPosition();
            this.label.setMouseTransparent(true);

            this.circle.setOnMouseEntered(e -> {
                if (circle.getFill().equals(originalColor)) {
                    circle.setFill(originalColor.brighter());
                }
            });
            this.circle.setOnMouseExited(e -> {
                if (circle.getFill().equals(originalColor.brighter())) {
                    circle.setFill(originalColor);
                }
            });
        }
        public Circle getCircle() { return circle; }
        public Text getLabel() { return label; }
        public double getX() { return circle.getCenterX(); }
        public double getY() { return circle.getCenterY(); }
        public void updateLabelPosition() {
            Bounds bounds = label.getLayoutBounds();
            label.setX(getX() - bounds.getWidth() / 2.0);
            label.setY(getY() + bounds.getHeight() / 4.0);
        }
        public void resetColor() { this.circle.setFill(originalColor); }
    }

    private static class GraphEdge {
        final GraphNode u, v;
        final double weight;
        final Line line;
        final Text weightLabel;
        final Color originalColor = EDGE_COLOR;
        final double originalWidth = 2.5;

        GraphEdge(GraphNode u, GraphNode v, double weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
            this.line = new Line(u.getX(), u.getY(), v.getX(), v.getY());
            this.weightLabel = new Text(String.valueOf(weight));
            setStyle(originalColor, originalWidth);
            updateLabelPosition();
            this.weightLabel.setMouseTransparent(true);
        }
        public Line getLine() { return line; }
        public Text getWeightLabel() { return weightLabel; }
        public void setStyle(Color color, double width) {
            line.setStroke(color);
            line.setStrokeWidth(width);
            if (!color.equals(EDGE_DISCARDED_COLOR)) {
                line.getStrokeDashArray().clear();
            }
        }
        public boolean connects(GraphNode n1, GraphNode n2) {
            return (u == n1 && v == n2) || (u == n2 && v == n1);
        }
        public void updateLabelPosition() {
            double midX = (u.getX() + v.getX()) / 2.0;
            double midY = (u.getY() + v.getY()) / 2.0;
            double dx = v.getX() - u.getX();
            double dy = v.getY() - u.getY();
            double length = Math.hypot(dx, dy);
            if (length > 0) {
                double perpDx = -dy / length;
                double perpDy = dx / length;
                double offset = 15.0;
                weightLabel.setX(midX + perpDx * offset);
                weightLabel.setY(midY + perpDy * offset);
            }
        }
    }

    private static class DisjointSet {
        private final int[] parent;
        DisjointSet(int n) {
            parent = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }
        public int find(int i) {
            if (parent[i] == i) return i;
            return parent[i] = find(parent[i]);
        }
        public void union(int i, int j) {
            int rootI = find(i);
            int rootJ = find(j);
            if (rootI != rootJ) parent[rootI] = rootJ;
        }
    }
    @FXML
    void returnHome(ActionEvent event) {
        try {
            Parent homeScreenRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/Home-screen.fxml")));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homeScreenRoot));
            stage.setTitle("DSA Simulator");
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}
