package org.example.dsa_simulator.prim;


import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class primController {
    @FXML
    private Pane canvasPane;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField weightField;
    @FXML
    private VBox currentProcessingBox;
    @FXML
    private Label currentEdgeLabel;
    @FXML
    private Label mstWeightLabel;
    @FXML
    private ScrollPane edgeScrollPane;
    @FXML
    private VBox edgeTableBox;
    @FXML
    private Button pauseButton;
    @FXML
    private Slider speedSlider;

    private Timeline currentPrimTimeline;
    private boolean isPaused = false;
    private double animationSpeedFactor = 1.0;

    private final Graph graph = new Graph();
    private final Map<Integer, Circle> nodeCircles = new ConcurrentHashMap<>();
    private final Map<Integer, Text> nodeLabels = new ConcurrentHashMap<>();
    private final List<Line> edgeLines = new ArrayList<>();
    private final List<Text> edgeWeights = new ArrayList<>();
    private final Map<String, Line> edgeMap = new HashMap<>();
    private final Map<String, Text> edgeWeightMap = new HashMap<>();

    private final Map<String, HBox> edgeRows = new ConcurrentHashMap<>();
    private final Map<String, Label> edgeStatusLabels = new ConcurrentHashMap<>();
    private final Map<String, Pane> edgeIndicators = new ConcurrentHashMap<>();

    private String selectedMode = "NODE";
    private Circle firstEdgeNode = null;
    private Integer sourceForPrim = null;
    private int nodeCounter = 1;
    private boolean isAnimating = false;

    private final Set<Integer> mstNodes = new HashSet<>();
    private final Set<String> mstEdges = new HashSet<>();
    private final Set<String> cutEdges = new HashSet<>();
    private double totalMstWeight = 0.0;

    private static final Color MST_NODE_COLOR = Color.web("#27ae60");
    private static final Color CURRENT_EDGE_COLOR = Color.web("#e74c3c");
    private static final Color CUT_EDGE_COLOR = Color.web("#f39c12");
    private static final Color MST_EDGE_COLOR = Color.web("#2ecc71");
    private static final Color NORMAL_EDGE_COLOR = Color.web("#34495e");
    private static final Color CANDIDATE_NODE_COLOR = Color.web("#3498db");
    private static final Color DEFAULT_NODE_COLOR = Color.LIGHTBLUE;

    @FXML
    public void initialize() {
        initializeMstPanel();


        speedSlider.setMin(0.5);
        speedSlider.setMax(3.0);
        speedSlider.setValue(1.0);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);

        pauseButton.setDisable(true);
        pauseButton.setText("⏸️ Pause");
    }

    private void initializeMstPanel() {
        currentEdgeLabel.setText("Current Edge: None");
        mstWeightLabel.setText("MST Weight: 0.0");
        edgeTableBox.getChildren().clear();
    }

    @FXML
    private void setCreateNodeMode(ActionEvent event) {
        if (isAnimating) return;
        selectedMode = "NODE";
        firstEdgeNode = null;
        statusLabel.setText("Mode: Create Node (click on canvas)");
        resetHighlights();
    }

    @FXML
    private void setCreateEdgeMode(ActionEvent event) {
        if (isAnimating) return;
        selectedMode = "EDGE";
        firstEdgeNode = null;
        statusLabel.setText("Mode: Create Edge (click on two nodes)");
        resetHighlights();
    }

    @FXML
    private void runPrim(ActionEvent event) {
        if (isAnimating) return;
        if (nodeCircles.isEmpty()) {
            statusLabel.setText("Please create some nodes first!");
            return;
        }
        selectedMode = "PRIM_SELECT";
        statusLabel.setText("Select a starting node for Prim's algorithm");
        resetHighlights();
        clearMstDisplay();
    }

    @FXML
    private void clearGraph(ActionEvent event) {
        if (isAnimating) return;
        canvasPane.getChildren().clear();
        nodeCircles.clear();
        nodeLabels.clear();
        edgeLines.clear();
        edgeWeights.clear();
        edgeMap.clear();
        edgeWeightMap.clear();
        graph.clear();
        nodeCounter = 1;
        selectedMode = "NODE";
        firstEdgeNode = null;
        sourceForPrim = null;
        mstNodes.clear();
        mstEdges.clear();
        cutEdges.clear();
        totalMstWeight = 0.0;
        clearMstDisplay();
        statusLabel.setText("Graph cleared. Mode: Create Node");
    }

    private void clearMstDisplay() {
        edgeTableBox.getChildren().clear();
        edgeRows.clear();
        edgeStatusLabels.clear();
        edgeIndicators.clear();
        currentEdgeLabel.setText("Current Edge: None");
        mstWeightLabel.setText("MST Weight: 0.0");
        totalMstWeight = 0.0;
    }

    private void initializeMstDisplay() {
        Platform.runLater(() -> {
            edgeTableBox.getChildren().clear();
            edgeRows.clear();
            edgeStatusLabels.clear();
            edgeIndicators.clear();

            List<Edge> allEdges = new ArrayList<>();
            for (Integer nodeId : graph.getNodes()) {
                for (org.example.dsa_simulator.prim.Edge edge : graph.getNeighbors(nodeId)) {
                    if (nodeId < edge.target) {
                        allEdges.add(new Edge(nodeId, edge.target, edge.weight));
                    }
                }
            }

            allEdges.sort(Comparator.comparingDouble(e -> e.weight));

            for (Edge edge : allEdges) {
                String edgeKey = getEdgeKey(edge.source, edge.target);

                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5, 10, 5, 10));
                row.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 3;");

                Pane indicator = new Pane();
                indicator.setPrefSize(12, 12);
                indicator.setStyle("-fx-background-color: #6c757d; -fx-background-radius: 6;");

                Label edgeLabel = new Label(edge.source + " ↔ " + edge.target + ":");
                edgeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                edgeLabel.setPrefWidth(80);

                Label weightLabel = new Label(String.format("%.1f", edge.weight));
                weightLabel.setStyle("-fx-font-size: 12px; -fx-font-family: monospace;");
                weightLabel.setPrefWidth(50);

                Label statusLabel = new Label("Waiting");
                statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
                statusLabel.setPrefWidth(80);

                row.getChildren().addAll(indicator, edgeLabel, weightLabel, statusLabel);

                edgeRows.put(edgeKey, row);
                edgeStatusLabels.put(edgeKey, statusLabel);
                edgeIndicators.put(edgeKey, indicator);
                edgeTableBox.getChildren().add(row);
            }
        });
    }

    private void updateEdgeDisplay(String edgeKey, String status) {
        Platform.runLater(() -> {
            Pane indicator = edgeIndicators.get(edgeKey);
            Label statusLabel = edgeStatusLabels.get(edgeKey);
            HBox row = edgeRows.get(edgeKey);

            if (indicator != null && statusLabel != null && row != null) {
                String indicatorColor = "#6c757d";
                String rowStyle = "-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 3;";

                Label edgeLabel = null;
                Label weightLabel = null;

                for (var child : row.getChildren()) {
                    if (child instanceof Label) {
                        Label label = (Label) child;
                        if (label != statusLabel) {
                            if (edgeLabel == null) {
                                edgeLabel = label;
                            } else {
                                weightLabel = label;
                            }
                        }
                    }
                }

                switch (status) {
                    case "cut":
                        indicatorColor = "#f39c12";
                        statusLabel.setText("Cut Edge");
                        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        rowStyle = "-fx-background-color: #fff8e1; -fx-border-color: #f39c12; -fx-border-width: 1; -fx-border-radius: 3;";

                        if (edgeLabel != null) {
                            edgeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #e65100;");
                        }
                        if (weightLabel != null) {
                            weightLabel.setStyle("-fx-font-size: 12px; -fx-font-family: monospace; -fx-text-fill: #e65100;");
                        }
                        break;

                    case "minimum":
                        indicatorColor = "#e74c3c";
                        statusLabel.setText("Minimum");
                        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
                        rowStyle = "-fx-background-color: #e74c3c; -fx-border-color: #c0392b; -fx-border-width: 2; -fx-border-radius: 3;";

                        if (edgeLabel != null) {
                            edgeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #ffffff;");
                        }
                        if (weightLabel != null) {
                            weightLabel.setStyle("-fx-font-size: 12px; -fx-font-family: monospace; -fx-text-fill: #ffffff;");
                        }
                        break;

                    case "mst":
                        indicatorColor = "#27ae60";
                        statusLabel.setText("In MST");
                        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
                        rowStyle = "-fx-background-color: #27ae60; -fx-border-color: #229954; -fx-border-width: 1; -fx-border-radius: 3;";

                        if (edgeLabel != null) {
                            edgeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #ffffff;");
                        }
                        if (weightLabel != null) {
                            weightLabel.setStyle("-fx-font-size: 12px; -fx-font-family: monospace; -fx-text-fill: #ffffff;");
                        }
                        break;

                    case "rejected":
                        indicatorColor = "#95a5a6";
                        statusLabel.setText("Rejected");
                        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
                        rowStyle = "-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 3;";

                        if (edgeLabel != null) {
                            edgeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                        }
                        if (weightLabel != null) {
                            weightLabel.setStyle("-fx-font-size: 12px; -fx-font-family: monospace; -fx-text-fill: #7f8c8d;");
                        }
                        break;

                    default:
                        if (edgeLabel != null) {
                            edgeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #000000;");
                        }
                        if (weightLabel != null) {
                            weightLabel.setStyle("-fx-font-size: 12px; -fx-font-family: monospace; -fx-text-fill: #000000;");
                        }
                        break;
                }

                indicator.setStyle("-fx-background-color: " + indicatorColor + "; -fx-background-radius: 6;");
                row.setStyle(rowStyle);
            }
        });
    }

    @FXML
    private void handleCanvasClick(javafx.scene.input.MouseEvent event) {
        if (selectedMode.equals("NODE")) {
            addNodeAt(event.getX(), event.getY());
        } else if (selectedMode.equals("EDGE")) {
            selectNodeForEdge(event);
        } else if (selectedMode.equals("PRIM_SELECT")) {
            selectSourceForPrim(event);
        }
    }

    private void addNodeAt(double x, double y) {
        int nodeId = nodeCounter++;
        Circle circle = new Circle(x, y, 15, DEFAULT_NODE_COLOR);
        circle.setStroke(Color.DARKBLUE);
        circle.setStrokeWidth(2);

        Text label = new Text(x, y + 5, String.valueOf(nodeId));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setFill(Color.DARKBLUE);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setX(x - label.getBoundsInLocal().getWidth() / 2);

        nodeCircles.put(nodeId, circle);
        nodeLabels.put(nodeId, label);
        graph.addNode(nodeId);

        canvasPane.getChildren().addAll(circle, label);

        circle.setScaleX(0);
        circle.setScaleY(0);
        label.setScaleX(0);
        label.setScaleY(0);

        ScaleTransition scaleCircle = new ScaleTransition(scaledDuration(300), circle);
        scaleCircle.setToX(1.0);
        scaleCircle.setToY(1.0);
        ScaleTransition scaleLabel = new ScaleTransition(scaledDuration(300), label);
        scaleLabel.setToX(1.0);
        scaleLabel.setToY(1.0);

        ParallelTransition entrance = new ParallelTransition(scaleCircle, scaleLabel);
        entrance.play();

        statusLabel.setText("Node " + nodeId + " created.");
    }
    private void updateWeightLabelPosition(Text weightLabel, double startX, double startY, double endX, double endY) {
        // 1. Find the midpoint of the edge
        double midX = (startX + endX) / 2.0;
        double midY = (startY + endY) / 2.0;

        // 2. Calculate a normalized perpendicular vector to the edge line
        double dx = endX - startX;
        double dy = endY - startY;
        double length = Math.hypot(dx, dy);

        if (length > 0) {
            double perpDx = -dy / length; // Perpendicular X component
            double perpDy = dx / length;  // Perpendicular Y component
            double offset = 15.0;         // How far to push the label away

            // 3. Calculate the target point for the label's center
            double targetX = midX + perpDx * offset;
            double targetY = midY + perpDy * offset;

            // 4. Get the dimensions of the text itself
            Bounds bounds = weightLabel.getLayoutBounds();

            // 5. Set the label's position, adjusting by its own width and height
            //    This crucial step centers the label on the target point.
            weightLabel.setX(targetX - bounds.getWidth() / 2.0);
            weightLabel.setY(targetY + bounds.getHeight() / 4.0); // Y is adjusted by ~1/4 for better visual centering
        }
    }

    private void selectNodeForEdge(javafx.scene.input.MouseEvent event) {
        Circle clicked = findNearestCircle(event.getX(), event.getY());
        if (clicked == null) return;

        if (firstEdgeNode == null) {
            firstEdgeNode = clicked;
            highlightNode(clicked, Color.ORANGE);
            statusLabel.setText("Selected first node. Now select second node.");
        } else {
            Circle second = clicked;
            if (firstEdgeNode == second) {
                statusLabel.setText("Can't create edge to itself. Try another.");
                return;
            }

            Integer sourceId = getNodeById(firstEdgeNode);
            Integer targetId = getNodeById(second);

            if (graph.hasEdge(sourceId, targetId)) {
                statusLabel.setText("Edge already exists between these nodes!");
                highlightNode(firstEdgeNode, DEFAULT_NODE_COLOR);
                firstEdgeNode = null;
                return;
            }

            double weight;
            try {
                weight = Double.parseDouble(weightField.getText().trim());
                if (weight <= 0) {
                    statusLabel.setText("Weight must be positive!");
                    return;
                }
            } catch (NumberFormatException e) {
                weight = 1.0;
            }

            graph.addEdge(sourceId, targetId, weight);
            graph.addEdge(targetId, sourceId, weight);
            drawUndirectedEdge(firstEdgeNode, second, weight);

            highlightNode(firstEdgeNode, DEFAULT_NODE_COLOR);
            firstEdgeNode = null;
            statusLabel.setText("Undirected edge: " + sourceId + " ↔ " + targetId + " (weight: " + weight + ")");
        }
    }

    private void drawUndirectedEdge(Circle from, Circle to, double weight) {
        double fromX = from.getCenterX();
        double fromY = from.getCenterY();
        double toX = to.getCenterX();
        double toY = to.getCenterY();

        double dx = toX - fromX;
        double dy = toY - fromY;
        double length = Math.sqrt(dx * dx + dy * dy);
        dx /= length;
        dy /= length;

        double startX = fromX + dx * 15;
        double startY = fromY + dy * 15;
        double endX = toX - dx * 15;
        double endY = toY - dy * 15;

        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(NORMAL_EDGE_COLOR);
        line.setStrokeWidth(3);

        Text weightText = new Text(String.format("%.1f", weight));
        updateWeightLabelPosition(weightText, startX, startY, endX, endY);
        weightText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        weightText.setFill(Color.DARKRED);

        edgeLines.add(line);
        edgeWeights.add(weightText);

        Integer fromId = getNodeById(from);
        Integer toId = getNodeById(to);
        String edgeKey = getEdgeKey(fromId, toId);
        edgeMap.put(edgeKey, line);
        edgeWeightMap.put(edgeKey, weightText);

        canvasPane.getChildren().addAll(line, weightText);

        line.setOpacity(0);
        weightText.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(scaledDuration(400), line);
        fadeIn.setToValue(1.0);
        FadeTransition fadeInWeight = new FadeTransition(scaledDuration(400), weightText);
        fadeInWeight.setToValue(1.0);

        ParallelTransition edgeEntrance = new ParallelTransition(fadeIn, fadeInWeight);
        edgeEntrance.play();
    }

    private void selectSourceForPrim(javafx.scene.input.MouseEvent event) {
        Circle start = findNearestCircle(event.getX(), event.getY());
        if (start == null) return;

        sourceForPrim = getNodeById(start);
        resetHighlights();
        highlightNode(start, MST_NODE_COLOR);
        statusLabel.setText("Running Prim's algorithm from node " + sourceForPrim + "...");
        Platform.runLater(this::startPrimAnimation);
    }

    private Duration scaledDuration(double milliseconds) {
        return Duration.millis(milliseconds / animationSpeedFactor);
    }

    @FXML
    private void pausePrim() {
        if (currentPrimTimeline == null) return;

        if (isPaused) {
            currentPrimTimeline.play();
            pauseButton.setText("⏸️ Pause");
            statusLabel.setText("Prim's algorithm resumed...");
            isPaused = false;
        } else {
            currentPrimTimeline.pause();
            pauseButton.setText("▶️ Resume");
            statusLabel.setText("Prim's algorithm paused. Click 'Resume' to continue.");
            isPaused = true;
        }
    }

    private void startPrimAnimation() {
        isAnimating = true;
        mstNodes.clear();
        mstEdges.clear();
        cutEdges.clear();
        totalMstWeight = 0.0;

        mstNodes.add(sourceForPrim);

        initializeMstDisplay();

        statusLabel.setText("Prim's algorithm running...");

        currentPrimTimeline = new Timeline();
        currentPrimTimeline.setCycleCount(Timeline.INDEFINITE);

        KeyFrame step = new KeyFrame(Duration.millis(2500), e -> {
            if (mstNodes.size() == graph.getNodes().size()) {
                currentPrimTimeline.stop();
                finishPrim();
                cleanupAfterAnimation();
                return;
            }

            updateCutEdges();

            Edge minEdge = findMinimumCutEdge();
            if (minEdge == null) {
                currentPrimTimeline.stop();
                statusLabel.setText("No more edges available - Graph might be disconnected!");
                cleanupAfterAnimation();
                return;
            }

            String minEdgeKey = getEdgeKey(minEdge.source, minEdge.target);
            highlightEdge(minEdgeKey, CURRENT_EDGE_COLOR, 4);
            updateEdgeDisplay(minEdgeKey, "minimum");

            Platform.runLater(() -> {
                currentEdgeLabel.setText("Current Edge: " + minEdge.source + " ↔ " + minEdge.target + " (weight: " + String.format("%.1f", minEdge.weight) + ")");
            });

            // Create a timeline for the delay instead of PauseTransition
            Timeline delayTimeline = new Timeline(new KeyFrame(Duration.millis(1500), ev -> {
                mstEdges.add(minEdgeKey);
                totalMstWeight += minEdge.weight;

                Integer newNode = mstNodes.contains(minEdge.source) ? minEdge.target : minEdge.source;
                mstNodes.add(newNode);

                highlightEdge(minEdgeKey, MST_EDGE_COLOR, 4);
                updateEdgeDisplay(minEdgeKey, "mst");
                highlightNode(nodeCircles.get(newNode), MST_NODE_COLOR);

                Platform.runLater(() -> {
                    mstWeightLabel.setText("MST Weight: " + String.format("%.1f", totalMstWeight));
                });

                Circle newNodeCircle = nodeCircles.get(newNode);
                ScaleTransition pulse = new ScaleTransition(scaledDuration(600), newNodeCircle);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.4);
                pulse.setToY(1.4);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(2);
                pulse.play();
            }));
            delayTimeline.setRate(animationSpeedFactor);
            delayTimeline.play();
        });

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            animationSpeedFactor = newVal.doubleValue();
            // Update the main timeline rate
            if (currentPrimTimeline != null) {
                currentPrimTimeline.setRate(animationSpeedFactor);
            }
            statusLabel.setText("Speed: " + String.format("%.1fx", animationSpeedFactor));
        });

        currentPrimTimeline.getKeyFrames().add(step);
        currentPrimTimeline.setRate(animationSpeedFactor); // Add this line
        pauseButton.setDisable(false);
        pauseButton.setText("⏸️ Pause");
        isPaused = false;

        currentPrimTimeline.play();
    }

    private void updateCutEdges() {
        cutEdges.clear();

        for (Integer mstNode : mstNodes) {
            for (org.example.dsa_simulator.prim.Edge edge : graph.getNeighbors(mstNode)) {
                if (!mstNodes.contains(edge.target)) {
                    String edgeKey = getEdgeKey(mstNode, edge.target);
                    if (!mstEdges.contains(edgeKey)) {
                        cutEdges.add(edgeKey);
                        highlightEdge(edgeKey, CUT_EDGE_COLOR, 3);
                        updateEdgeDisplay(edgeKey, "cut");
                    }
                }
            }
        }
    }

    private Edge findMinimumCutEdge() {
        Edge minEdge = null;
        double minWeight = Double.MAX_VALUE;

        for (String edgeKey : cutEdges) {
            if (mstEdges.contains(edgeKey)) continue;

            String[] parts = edgeKey.split("-");
            Integer node1 = Integer.parseInt(parts[0]);
            Integer node2 = Integer.parseInt(parts[1]);

            for (org.example.dsa_simulator.prim.Edge edge : graph.getNeighbors(node1)) {
                if (edge.target.equals(node2) && edge.weight < minWeight) {
                    minWeight = edge.weight;
                    minEdge = new Edge(node1, node2, edge.weight);
                }
            }
        }

        return minEdge;
    }

    private void finishPrim() {
        isAnimating = false;
        Platform.runLater(() -> {
            currentEdgeLabel.setText("Algorithm Complete!");
            statusLabel.setText("Prim's algorithm completed! MST found with weight: " + String.format("%.1f", totalMstWeight));
        });

        Timeline celebration = new Timeline();
        int delay = 0;
        for (Integer nodeId : mstNodes) {
            Circle nodeCircle = nodeCircles.get(nodeId);
            KeyFrame celebrationFrame = new KeyFrame(scaledDuration(200 * delay), e -> {
                ScaleTransition finalPulse = new ScaleTransition(scaledDuration(500), nodeCircle);
                finalPulse.setFromX(1.0);
                finalPulse.setFromY(1.0);
                finalPulse.setToX(1.3);
                finalPulse.setToY(1.3);
                finalPulse.setAutoReverse(true);
                finalPulse.setCycleCount(2);
                finalPulse.play();
            });
            celebration.getKeyFrames().add(celebrationFrame);
            delay++;
        }
        celebration.play();
    }

    private void cleanupAfterAnimation() {
        isAnimating = false;
        pauseButton.setDisable(true);
        pauseButton.setText("⏸️ Pause");
    }

    private void highlightEdge(String edgeKey, Color color, double width) {
        Line edge = edgeMap.get(edgeKey);
        if (edge != null) {
            edge.setStroke(color);
            edge.setStrokeWidth(width);
        }
    }

    private void resetHighlights() {
        for (Circle circle : nodeCircles.values()) {
            circle.setFill(DEFAULT_NODE_COLOR);
        }
        for (Line edge : edgeLines) {
            edge.setStroke(NORMAL_EDGE_COLOR);
            edge.setStrokeWidth(3);
        }
        mstNodes.clear();
        mstEdges.clear();
        cutEdges.clear();
    }

    private Circle findNearestCircle(double x, double y) {
        final double threshold = 25;
        Circle closest = null;
        double minDist = threshold;

        for (Circle c : nodeCircles.values()) {
            double dx = c.getCenterX() - x;
            double dy = c.getCenterY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < minDist) {
                minDist = dist;
                closest = c;
            }
        }
        return closest;
    }

    private Integer getNodeById(Circle c) {
        for (var entry : nodeCircles.entrySet()) {
            if (entry.getValue() == c) return entry.getKey();
        }
        return null;
    }

    private void highlightNode(Circle c, Color color) {
        animateNodeTransition(c, getCurrentColor(c), color, scaledDuration(300));
    }

    private void animateNodeTransition(Circle circle, Color from, Color to, Duration duration) {
        SmoothFillTransition transition = new SmoothFillTransition(duration, circle, from, to);
        transition.play();
    }

    private Color getCurrentColor(Circle circle) {
        Paint fill = circle.getFill();
        if (fill instanceof Color) {
            return (Color) fill;
        }
        return DEFAULT_NODE_COLOR;
    }

    private String getEdgeKey(Integer node1, Integer node2) {
        return Math.min(node1, node2) + "-" + Math.max(node1, node2);
    }

    private static class Edge {
        final Integer source;
        final Integer target;
        final double weight;

        Edge(Integer source, Integer target, double weight) {
            this.source = source;
            this.target = target;
            this.weight = weight;
        }
    }

    public static class SmoothFillTransition extends Transition {
        private final Circle circle;
        private final Color from;
        private final Color to;

        public SmoothFillTransition(Duration duration, Circle circle, Color from, Color to) {
            this.circle = circle;
            this.from = from;
            this.to = to;
            setCycleDuration(duration);
            setInterpolator(Interpolator.EASE_BOTH);
        }

        @Override
        protected void interpolate(double frac) {
            Color current = from.interpolate(to, frac);
            circle.setFill(current);
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