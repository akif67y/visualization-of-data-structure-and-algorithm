package org.example.dsa_simulator.dijkstra;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class dijkstraController {
    @FXML
    private Pane canvasPane;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField weightField;
    @FXML
    private VBox currentProcessingBox;
    @FXML
    private Label currentNodeLabel;
    @FXML
    private Label visitedCountLabel;
    @FXML
    private ScrollPane distanceScrollPane;
    @FXML
    private VBox distanceTableBox;
    @FXML
    private Button pauseButton;
    @FXML
    private Slider speedSlider;
    @FXML
    private ScrollPane resultScrollPane;
    @FXML
    private VBox resultBox;

    private Timeline currentDijkstraTimeline;
    private boolean isPaused = false;
    private double animationSpeedFactor = 1.0;

    private final Graph graph = new Graph();
    private final Map<Integer, Circle> nodeCircles = new ConcurrentHashMap<>();
    private final Map<Integer, Text> nodeLabels = new ConcurrentHashMap<>();
    private final List<Line> edgeLines = new ArrayList<>();
    private final List<Polygon> arrowHeads = new ArrayList<>();
    private final List<Text> edgeWeights = new ArrayList<>();

    private final Map<Integer, HBox> distanceRows = new ConcurrentHashMap<>();
    private final Map<Integer, Label> distanceLabels = new ConcurrentHashMap<>();
    private final Map<Integer, Pane> distanceIndicators = new ConcurrentHashMap<>();

    private String selectedMode = "NODE";
    private Circle firstEdgeNode = null;
    private Integer sourceForDijkstra = null;
    private int nodeCounter = 1;
    private Timeline continuousPulse;
    private boolean isAnimating = false;

    private static final Color SOURCE_COLOR = Color.web("#27ae60");
    private static final Color CURRENT_COLOR = Color.web("#e74c3c");
    private static final Color NEIGHBOR_COLOR = Color.web("#f39c12");
    private static final Color VISITED_COLOR = Color.web("#3498db");
    private static final Color UNREACHABLE_COLOR = Color.web("#95a5a6");
    private static final Color DEFAULT_COLOR = Color.LIGHTBLUE;

    @FXML
    public void initialize() {
        setupContinuousAnimation();
        initializeDistancePanel();

        speedSlider.setMin(0.5);
        speedSlider.setMax(3.0);
        speedSlider.setValue(1.0);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);

        pauseButton.setDisable(true);
        pauseButton.setText("⏸ Pause");
    }

    private void initializeDistancePanel() {
        currentNodeLabel.setText("Current Node: None");
        visitedCountLabel.setText("Visited: 0 nodes");
        distanceTableBox.getChildren().clear();
        resultBox.getChildren().clear();
    }

    private void setupContinuousAnimation() {
        continuousPulse = new Timeline(new KeyFrame(Duration.millis(800), e -> {
            for (Circle circle : nodeCircles.values()) {
                if (circle.getFill().equals(NEIGHBOR_COLOR)) {
                    ScaleTransition pulse = new ScaleTransition(Duration.millis(400), circle);
                    pulse.setFromX(1.0);
                    pulse.setFromY(1.0);
                    pulse.setToX(1.2);
                    pulse.setToY(1.2);
                    pulse.setAutoReverse(true);
                    pulse.setCycleCount(2);
                    pulse.play();
                }
            }
        }));
        continuousPulse.setCycleCount(Timeline.INDEFINITE);
        continuousPulse.play();
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
    private void runDijkstra(ActionEvent event) {
        if (isAnimating) return;
        if (nodeCircles.isEmpty()) {
            statusLabel.setText("Please create some nodes first!");
            return;
        }
        selectedMode = "DIJKSTRA_SELECT";
        statusLabel.setText("Select a source node for Dijkstra");
        resetHighlights();
        clearDistanceDisplay();
    }

    @FXML
    private void clearGraph(ActionEvent event) {
        if (isAnimating) return;
        canvasPane.getChildren().clear();
        nodeCircles.clear();
        nodeLabels.clear();
        edgeLines.clear();
        arrowHeads.clear();
        edgeWeights.clear();
        graph.clear();
        nodeCounter = 1;
        selectedMode = "NODE";
        firstEdgeNode = null;
        sourceForDijkstra = null;
        clearDistanceDisplay();
        statusLabel.setText("Graph cleared. Mode: Create Node");
    }

    private void clearDistanceDisplay() {
        distanceTableBox.getChildren().clear();
        distanceRows.clear();
        distanceLabels.clear();
        distanceIndicators.clear();
        currentNodeLabel.setText("Current Node: None");
        visitedCountLabel.setText("Visited: 0 nodes");
        resultBox.getChildren().clear();
    }

    private void initializeDistanceDisplay(Map<Integer, Double> distances) {
        Platform.runLater(() -> {
            distanceTableBox.getChildren().clear();
            distanceRows.clear();
            distanceLabels.clear();
            distanceIndicators.clear();
            List<Integer> sortedNodes = new ArrayList<>(distances.keySet());
            sortedNodes.sort(Integer::compareTo);
            for (Integer nodeId : sortedNodes) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5, 10, 5, 10));
                row.setStyle("-fx-background-color: #a4b2a3; -fx-border-color: #ce8924; -fx-border-width: 1; -fx-border-radius: 3; -fx-text-fill: black;");
                Pane indicator = new Pane();
                indicator.setPrefSize(12, 12);
                indicator.setStyle("-fx-background-color: #b7531a; -fx-background-radius: 6;");
                Label nodeLabel = new Label("Node " + nodeId + ":");
                nodeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                nodeLabel.setPrefWidth(70);
                Label distLabel = new Label("∞");
                distLabel.setStyle("-fx-font-size: 12px; -fx-font-family: monospace;");
                distLabel.setPrefWidth(60);
                if (nodeId.equals(sourceForDijkstra)) {
                    distLabel.setText("0.0");
                    indicator.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 6;");
                }
                row.getChildren().addAll(indicator, nodeLabel, distLabel);
                distanceRows.put(nodeId, row);
                distanceLabels.put(nodeId, distLabel);
                distanceIndicators.put(nodeId, indicator);
                distanceTableBox.getChildren().add(row);
            }
        });
    }

    // Replace the original updateDistanceDisplay function with this one.
    private void updateDistanceDisplay(Integer nodeId, Double distance, String status) {
        Platform.runLater(() -> {
            Label distLabel = distanceLabels.get(nodeId);
            Pane indicator = distanceIndicators.get(nodeId);
            HBox row = distanceRows.get(nodeId);
            if (distLabel != null && indicator != null && row != null) {
                if (distance == Double.MAX_VALUE) {
                    distLabel.setText("∞");
                } else {
                    distLabel.setText(String.format("%.1f", distance));
                }


                String colorHex;
                String textColor = "white";
                String borderWidth = "1";
                String borderColor = "#444";

                switch (status) {
                    case "source":
                        colorHex = "#27ae60";
                        borderColor = colorHex;
                        borderWidth = "2";
                        break;
                    case "current":
                        colorHex = "#e74c3c";
                        borderColor = colorHex;
                        borderWidth = "2";
                        break;
                    case "neighbor":
                        colorHex = "#f39c12";
                        textColor = "black";
                        borderColor = colorHex;
                        break;
                    case "visited":
                        colorHex = "#3498db";
                        break;
                    case "unreachable":
                        colorHex = "#95a5a6";
                        textColor = "black";
                        break;
                    default:
                        colorHex = "#bdc3c7";
                        textColor = "black";
                        break;
                }


                indicator.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 6;");


                String rowStyle = String.format(
                        "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: %s; -fx-border-radius: 3;",
                        colorHex, borderColor, borderWidth
                );
                row.setStyle(rowStyle);


                Label nodeLabel = (Label) row.getChildren().get(1);
                nodeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + textColor + ";");
                distLabel.setStyle("-fx-font-size: 12px; -fx-font-family: monospace; -fx-text-fill: " + textColor + ";");


                if (status.equals("neighbor")) {
                    ScaleTransition scaleUp = new ScaleTransition(scaledDuration(200), distLabel);
                    scaleUp.setToX(1.2);
                    scaleUp.setToY(1.2);
                    ScaleTransition scaleDown = new ScaleTransition(scaledDuration(200), distLabel);
                    scaleDown.setToX(1.0);
                    scaleDown.setToY(1.0);
                    SequentialTransition pulse = new SequentialTransition(scaleUp, scaleDown);
                    pulse.play();
                }
            }
        });
    }

    @FXML
    private void handleCanvasClick(MouseEvent event) {
        if (selectedMode.equals("NODE")) {
            addNodeAt(event.getX(), event.getY());
        } else if (selectedMode.equals("EDGE")) {
            selectNodeForEdge(event);
        } else if (selectedMode.equals("DIJKSTRA_SELECT")) {
            selectSourceForDijkstra(event);
        }
    }

    private void addNodeAt(double x, double y) {
        int nodeId = nodeCounter++;
        Circle circle = new Circle(x, y, 15, DEFAULT_COLOR);
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

    private void selectNodeForEdge(MouseEvent event) {
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
                highlightNode(firstEdgeNode, DEFAULT_COLOR);
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
            drawDirectedEdge(firstEdgeNode, second, weight);
            highlightNode(firstEdgeNode, DEFAULT_COLOR);
            firstEdgeNode = null;
            statusLabel.setText("Directed edge: " + sourceId + " → " + targetId + " (weight: " + weight + ")");
        }
    }

    private void drawDirectedEdge(Circle from, Circle to, double weight) {
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
        line.setStroke(Color.DARKGREEN);
        line.setStrokeWidth(2);
        Polygon arrowHead = createArrowHead(endX, endY, dx, dy);
        arrowHead.setFill(Color.DARKGREEN);
        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;
        Text weightText = new Text(midX + 15, midY +10, String.format("%.1f", weight));
        weightText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        weightText.setFill(Color.DARKRED);
        edgeLines.add(line);
        arrowHeads.add(arrowHead);
        edgeWeights.add(weightText);
        canvasPane.getChildren().addAll(line, arrowHead, weightText);
        line.setOpacity(0);
        arrowHead.setOpacity(0);
        weightText.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(scaledDuration(400), line);
        fadeIn.setToValue(1.0);
        FadeTransition fadeInArrow = new FadeTransition(scaledDuration(400), arrowHead);
        fadeInArrow.setToValue(1.0);
        FadeTransition fadeInWeight = new FadeTransition(scaledDuration(400), weightText);
        fadeInWeight.setToValue(1.0);
        ParallelTransition edgeEntrance = new ParallelTransition(fadeIn, fadeInArrow, fadeInWeight);
        edgeEntrance.play();
    }

    private Polygon createArrowHead(double tipX, double tipY, double dx, double dy) {
        double arrowLength = 10;
        double arrowWidth = 6;
        double backX = tipX - dx * arrowLength;
        double backY = tipY - dy * arrowLength;
        double perpX = -dy * arrowWidth;
        double perpY = dx * arrowWidth;
        Polygon arrow = new Polygon();
        arrow.getPoints().addAll(new Double[]{
                tipX, tipY,
                backX + perpX, backY + perpY,
                backX - perpX, backY - perpY
        });
        return arrow;
    }

    private void selectSourceForDijkstra(MouseEvent event) {
        Circle start = findNearestCircle(event.getX(), event.getY());
        if (start == null) return;
        sourceForDijkstra = getNodeById(start);
        resetHighlights();
        highlightNode(start, SOURCE_COLOR);
        statusLabel.setText("Running Dijkstra from node " + sourceForDijkstra + "...");
        Platform.runLater(this::startDijkstraAnimation);
    }

    private Duration scaledDuration(double milliseconds) {
        return Duration.millis(milliseconds / animationSpeedFactor);
    }

    @FXML
    private void pauseDijkstra() {
        if (currentDijkstraTimeline == null) return;
        if (isPaused) {
            currentDijkstraTimeline.play();
            pauseButton.setText("⏸️ Pause");
            statusLabel.setText("Dijkstra resumed...");
            isPaused = false;
        } else {
            currentDijkstraTimeline.pause();
            pauseButton.setText("▶️ Resume");
            statusLabel.setText("Dijkstra paused. Click 'Resume' to continue.");
            isPaused = true;
        }
    }

    private void startDijkstraAnimation() {
        isAnimating = true;
        statusLabel.setText("Dijkstra algorithm running...");
        Map<Integer, Double> distances = new HashMap<>();
        Map<Integer, Integer> predecessors = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        PriorityQueue<NodeEntry> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.distance));

        for (Integer node : graph.getNodes()) {
            distances.put(node, Double.MAX_VALUE);
        }
        distances.put(sourceForDijkstra, 0.0);
        pq.offer(new NodeEntry(sourceForDijkstra, 0.0));

        initializeDistanceDisplay(distances);
        updateDistanceDisplay(sourceForDijkstra, 0.0, "source");

        currentDijkstraTimeline = new Timeline();
        currentDijkstraTimeline.setCycleCount(Timeline.INDEFINITE);

        KeyFrame step = new KeyFrame(scaledDuration(2500), e -> {
            NodeEntry currentEntry;
            do {
                if (pq.isEmpty()) {
                    currentDijkstraTimeline.stop();
                    finishDijkstra(distances, predecessors, visited);
                    cleanupAfterAnimation();
                    return;
                }
                currentEntry = pq.poll();
            } while (visited.contains(currentEntry.nodeId));

            Integer u = currentEntry.nodeId;
            visited.add(u);
            Circle uCircle = nodeCircles.get(u);

            Platform.runLater(() -> {
                currentNodeLabel.setText("Current Node: " + u);
                visitedCountLabel.setText("Visited: " + visited.size() + " nodes");
            });

            if (u.equals(sourceForDijkstra)) {
                updateDistanceDisplay(u, 0.0, "source");
            } else {
                animateNodeTransition(uCircle, getCurrentColor(uCircle), CURRENT_COLOR, scaledDuration(500));
                updateDistanceDisplay(u, distances.get(u), "current");
            }

            PauseTransition delay = new PauseTransition(scaledDuration(1000));
            delay.setOnFinished(ev -> {
                List<Edge> neighbors = graph.getNeighbors(u);
                for (Edge edge : neighbors) {
                    Integer v = edge.target;
                    if (visited.contains(v)) continue;
                    double alt = distances.get(u) + edge.weight;
                    if (alt < distances.get(v)) {
                        distances.put(v, alt);
                        predecessors.put(v, u);
                        pq.offer(new NodeEntry(v, alt));
                        Platform.runLater(() -> {
                            updateDistanceDisplay(v, alt, "neighbor");
                            Circle vCircle = nodeCircles.get(v);
                            animateNodeTransition(vCircle, getCurrentColor(vCircle), NEIGHBOR_COLOR, scaledDuration(300));
                            ScaleTransition pulse = new ScaleTransition(scaledDuration(400), vCircle);
                            pulse.setToX(1.3);
                            pulse.setToY(1.3);
                            pulse.setAutoReverse(true);
                            pulse.setCycleCount(2);
                            pulse.play();
                        });
                    }
                }

                double rawFinalizeDelay = 600 + 200 * neighbors.size();
                PauseTransition finalize = new PauseTransition(scaledDuration(rawFinalizeDelay));
                finalize.setOnFinished(fev -> {
                    if (!u.equals(sourceForDijkstra)) {
                        animateNodeTransition(uCircle, CURRENT_COLOR, VISITED_COLOR, scaledDuration(500));
                    }
                    updateDistanceDisplay(u, distances.get(u), "visited");
                });
                finalize.play();
            });
            delay.play();
        });

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            animationSpeedFactor = newVal.doubleValue();
            if (currentDijkstraTimeline != null) {
                currentDijkstraTimeline.setRate(1.0);
            }
            statusLabel.setText("Speed: " + String.format("%.1fx", animationSpeedFactor));
        });

        currentDijkstraTimeline.getKeyFrames().add(step);
        pauseButton.setDisable(false);
        pauseButton.setText("⏸️ Pause");
        isPaused = false;

        currentDijkstraTimeline.play();
    }

    private void finishDijkstra(Map<Integer, Double> distances, Map<Integer, Integer> predecessors, Set<Integer> visited) {
        isAnimating = false;
        Platform.runLater(() -> {
            currentNodeLabel.setText("Algorithm Complete!");
            visitedCountLabel.setText("Total visited: " + visited.size() + " nodes");
        });

        Platform.runLater(() -> {
            resultBox.getChildren().clear();

            Label headerLabel = new Label("Dijkstra Results from Node " + sourceForDijkstra);
            headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-padding: 5 0 10 0;");
            resultBox.getChildren().add(headerLabel);

            List<Integer> sortedNodes = new ArrayList<>(distances.keySet());
            sortedNodes.sort(Integer::compareTo);

            for (Integer node : sortedNodes) {
                HBox resultRow = new HBox(10);
                resultRow.setAlignment(Pos.CENTER_LEFT);
                resultRow.setPadding(new Insets(3, 10, 3, 10));

                Label nodeLabel = new Label("Node " + node + ":");
                nodeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                nodeLabel.setPrefWidth(70);

                Label distanceLabel;
                if (distances.get(node) == Double.MAX_VALUE) {
                    distanceLabel = new Label("∞ (unreachable)");
                    distanceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffffff;");
                    resultRow.setStyle("-fx-background-color: #ADD8E6; -fx-border-color: #0c0c0c; -fx-border-width: 1; -fx-border-radius: 3;");
                } else {
                    distanceLabel = new Label(String.format("%.1f", distances.get(node)));
                    if (node.equals(sourceForDijkstra)) {
                        distanceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
                        resultRow.setStyle("-fx-background-color: #00008B; -fx-border-color: #0c0c0c; -fx-border-width: 1; -fx-border-radius: 3;");
                    } else {
                        distanceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffffff;");
                        resultRow.setStyle("-fx-background-color: #00008B; -fx-border-color: #0c0c0c; -fx-border-width: 1; -fx-border-radius: 3;");
                    }
                }

                resultRow.getChildren().addAll(nodeLabel, distanceLabel);
                resultBox.getChildren().add(resultRow);

                updateDistanceDisplay(node, distances.get(node),
                        distances.get(node) == Double.MAX_VALUE ? "unreachable" :
                                (node.equals(sourceForDijkstra) ? "source" : "visited"));

                if (distances.get(node) == Double.MAX_VALUE) {
                    Circle nodeCircle = nodeCircles.get(node);
                    if (nodeCircle != null && !node.equals(sourceForDijkstra)) {
                        animateNodeTransition(nodeCircle, getCurrentColor(nodeCircle), UNREACHABLE_COLOR, scaledDuration(500));
                    }
                }
            }
        });

        statusLabel.setText("Dijkstra algorithm completed! Check results panel for details.");

        Timeline celebration = new Timeline();
        List<Integer> sortedNodes = new ArrayList<>(distances.keySet());
        sortedNodes.sort(Integer::compareTo);
        for (int i = 0; i < sortedNodes.size(); i++) {
            Integer nodeId = sortedNodes.get(i);
            Circle nodeCircle = nodeCircles.get(nodeId);
            if (nodeCircle != null && distances.get(nodeId) != Double.MAX_VALUE) {
                KeyFrame celebrationFrame = new KeyFrame(scaledDuration(200 * i), e -> {
                    ScaleTransition finalPulse = new ScaleTransition(scaledDuration(400), nodeCircle);
                    finalPulse.setFromX(1.0);
                    finalPulse.setFromY(1.0);
                    finalPulse.setToX(1.2);
                    finalPulse.setToY(1.2);
                    finalPulse.setAutoReverse(true);
                    finalPulse.setCycleCount(2);
                    finalPulse.play();
                });
                celebration.getKeyFrames().add(celebrationFrame);
            }
        }
        celebration.play();
    }

    private void cleanupAfterAnimation() {
        isAnimating = false;
        pauseButton.setDisable(true);
        pauseButton.setText("⏸️ Pause");
    }

    private SmoothFillTransition createSmoothTransition(Circle circle, Color from, Color to, Duration duration) {
        return new SmoothFillTransition(duration, circle, from, to);
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
        return DEFAULT_COLOR;
    }

    private void resetHighlights() {
        for (Circle circle : nodeCircles.values()) {
            circle.setFill(DEFAULT_COLOR);
        }
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

    private static class NodeEntry {
        final Integer nodeId;
        final double distance;
        NodeEntry(Integer nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
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