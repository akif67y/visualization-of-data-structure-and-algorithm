package org.example.dsa_simulator.graph;

import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

public class MST {


    @FXML private Pane graphPane;
    @FXML private Button runButton;
    @FXML private Button defaultGraphButton;
    @FXML private Button editGraphButton;
    @FXML private Label statusLabel;
    @FXML private Label costLabel;


    private final Map<String, Button> visualNodes = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<Edge, Line> visualEdges = new HashMap<>();


    private static final double NODE_DIAMETER = 50;
    private static final Color NODE_COLOR = Color.SKYBLUE;
    private static final Color NODE_BORDER_COLOR = Color.BLACK;
    private static final Color EDGE_DEFAULT_COLOR = Color.GRAY;
    private static final Color EDGE_HIGHLIGHT_COLOR = Color.BLUE;
    private static final Color EDGE_INCLUDED_COLOR = Color.ORANGE;
    private static final Color EDGE_REJECTED_COLOR = Color.LIGHTGRAY;
    private static final Color NODE_INCLUDED_COLOR = Color.ORANGE;



    @FXML
    public void runKruskal() {
        if (edges.isEmpty()) {
            statusLabel.setText("Graph is empty. Load a graph first.");
            return;
        }
        resetGraphVisuals();
        animateKruskal();
    }

    @FXML
    public void loadDefaultGraph() {
        List<Edge> defaultEdges = new ArrayList<>();
        defaultEdges.add(new Edge("A", "B", 7));
        defaultEdges.add(new Edge("A", "D", 5));
        defaultEdges.add(new Edge("B", "C", 8));
        defaultEdges.add(new Edge("B", "D", 9));
        defaultEdges.add(new Edge("B", "E", 7));
        defaultEdges.add(new Edge("C", "E", 5));
        defaultEdges.add(new Edge("D", "E", 15));
        defaultEdges.add(new Edge("D", "F", 6));
        defaultEdges.add(new Edge("E", "F", 8));
        defaultEdges.add(new Edge("E", "G", 9));
        defaultEdges.add(new Edge("F", "G", 11));

        updateGraphFromData(defaultEdges);
        statusLabel.setText("Default graph loaded.");
    }

    @FXML
    public void openEditGraphWindow() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dsa_simulator/MSTPop.fxml"));
            Parent root = loader.load();


            EditGraph editController = loader.getController();
            editController.setMainController(this);


            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Edit Graph");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error: Could not open edit window.");
        }
    }



    private void animateKruskal() {
        setButtonsDisabled(true);


        List<Edge> sortedEdges = new ArrayList<>(edges);
        sortedEdges.sort(Comparator.comparingInt(e -> e.weight));


        Set<String> vertices = new HashSet<>();
        for (Edge edge : edges) {
            vertices.add(edge.source);
            vertices.add(edge.destination);
        }

        DisjointSet dsu = new DisjointSet(vertices);
        SequentialTransition mainAnimation = new SequentialTransition();
        final int[] totalCost = {0};
        final int[] edgesAdded = {0};
        final int targetEdges = vertices.size() - 1;

        for (Edge edge : sortedEdges) {
            Line line = visualEdges.get(edge);
            Button node1 = visualNodes.get(edge.source);
            Button node2 = visualNodes.get(edge.destination);


            mainAnimation.getChildren().add(createHighlightStep(line, "Considering edge " + edge + "..."));


            String root1 = dsu.find(edge.source);
            String root2 = dsu.find(edge.destination);

            if (!root1.equals(root2) && edgesAdded[0] < targetEdges) {

                dsu.union(edge.source, edge.destination);
                totalCost[0] += edge.weight;
                edgesAdded[0]++;
                mainAnimation.getChildren().add(createIncludeStep(line, node1, node2,
                        "Accepted. MST Cost: " + totalCost[0] + " (Edges: " + edgesAdded[0] + "/" + targetEdges + ")"));
            } else {

                String reason = (edgesAdded[0] >= targetEdges) ?
                        "Rejected (MST complete - " + targetEdges + " edges already selected)." :
                        "Rejected (forms a cycle).";
                mainAnimation.getChildren().add(createRejectStep(line, reason));
            }
        }

        mainAnimation.setOnFinished(e -> {
            if (edgesAdded[0] == targetEdges) {
                statusLabel.setText("Kruskal's Algorithm Complete! MST Cost: " + totalCost[0]);
            } else {
                statusLabel.setText("Warning: Graph may be disconnected. MST Cost: " + totalCost[0]);
            }
            setButtonsDisabled(false);
        });

        mainAnimation.play();
    }

    private Timeline createHighlightStep(Line line, String message) {
        return new Timeline(
                new KeyFrame(Duration.millis(1), e -> {
                    statusLabel.setText(message);
                    line.setStroke(EDGE_HIGHLIGHT_COLOR);
                    line.setStrokeWidth(8);
                }),
                new KeyFrame(Duration.millis(800))
        );
    }

    private Timeline createIncludeStep(Line line, Button node1, Button node2, String message) {
        return new Timeline(
                new KeyFrame(Duration.millis(1), e -> {
                    statusLabel.setText(message);
                    String costPart = message.split("MST Cost: ")[1].split(" ")[0];
                    costLabel.setText("MST Cost: " + costPart);
                    line.setStroke(EDGE_INCLUDED_COLOR);
                    line.setStrokeWidth(6);
                    setButtonColor(node1, NODE_INCLUDED_COLOR);
                    setButtonColor(node2, NODE_INCLUDED_COLOR);
                }),
                new KeyFrame(Duration.millis(500))
        );
    }

    private Timeline createRejectStep(Line line, String message) {
        return new Timeline(
                new KeyFrame(Duration.millis(1), e -> {
                    statusLabel.setText(message);
                    line.setStroke(EDGE_REJECTED_COLOR);
                    line.setStrokeWidth(2);
                    line.getStrokeDashArray().addAll(5d, 5d);
                }),
                new KeyFrame(Duration.millis(500))
        );
    }



    public void updateGraphFromData(List<Edge> newEdges) {
        clearGraph();
        this.edges.addAll(newEdges);


        Set<String> nodeNames = new HashSet<>();
        for (Edge edge : newEdges) {
            nodeNames.add(edge.source);
            nodeNames.add(edge.destination);
        }

        Map<String, Point> positions = new HashMap<>();
        double centerX = graphPane.getWidth() / 2;
        double centerY = graphPane.getHeight() / 2;
        double radius = Math.min(centerX, centerY) * 0.75;


        List<String> nodeList = new ArrayList<>(nodeNames);
        Collections.sort(nodeList);

        for (int i = 0; i < nodeList.size(); i++) {
            String name = nodeList.get(i);
            double angle = 2 * Math.PI * i / nodeList.size();
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            positions.put(name, new Point(x, y));
        }

        drawGraph(positions);
    }

    private void drawGraph(Map<String, Point> positions) {

        positions.forEach((name, pos) -> {
            Button nodeButton = createVisualNode(pos.x, pos.y, name);
            visualNodes.put(name, nodeButton);
        });


        for (Edge edge : edges) {
            Button sourceButton = visualNodes.get(edge.source);
            Button destButton = visualNodes.get(edge.destination);

            Line line = new Line(sourceButton.getLayoutX() + NODE_DIAMETER / 2, sourceButton.getLayoutY() + NODE_DIAMETER / 2,
                    destButton.getLayoutX() + NODE_DIAMETER / 2, destButton.getLayoutY() + NODE_DIAMETER / 2);
            line.setStroke(EDGE_DEFAULT_COLOR);
            line.setStrokeWidth(2);

            Text weightText = new Text(String.valueOf(edge.weight));
            weightText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            weightText.xProperty().bind(line.startXProperty().add(line.endXProperty()).divide(2).add(5));
            weightText.yProperty().bind(line.startYProperty().add(line.endYProperty()).divide(2).subtract(5));


            graphPane.getChildren().addAll(line, weightText);
            visualEdges.put(edge, line);
        }


        graphPane.getChildren().addAll(visualNodes.values());
    }

    private void clearGraph() {
        graphPane.getChildren().clear();
        visualNodes.clear();
        edges.clear();
        visualEdges.clear();
        costLabel.setText("MST Cost: 0");
        statusLabel.setText("Ready.");
    }

    private void resetGraphVisuals() {
        visualEdges.values().forEach(line -> {
            line.setStroke(EDGE_DEFAULT_COLOR);
            line.setStrokeWidth(2);
            line.getStrokeDashArray().clear();
        });
        visualNodes.values().forEach(button -> setButtonColor(button, NODE_COLOR));
        costLabel.setText("MST Cost: 0");
    }

    private Button createVisualNode(double x, double y, String name) {
        Button button = new Button(name);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        button.setMinSize(NODE_DIAMETER, NODE_DIAMETER);
        button.setPrefSize(NODE_DIAMETER, NODE_DIAMETER);
        button.setMaxSize(NODE_DIAMETER, NODE_DIAMETER);
        setButtonColor(button, NODE_COLOR);
        button.setBorder(new Border(new BorderStroke(NODE_BORDER_COLOR, BorderStrokeStyle.SOLID,
                new CornerRadii(NODE_DIAMETER / 2.0), new BorderWidths(2.0), Insets.EMPTY)));
        button.setLayoutX(x - NODE_DIAMETER / 2.0);
        button.setLayoutY(y - NODE_DIAMETER / 2.0);
        return button;
    }

    private void setButtonColor(Button button, Color color) {
        button.setBackground(new Background(new BackgroundFill(color, new CornerRadii(NODE_DIAMETER / 2.0), Insets.EMPTY)));
    }

    private void setButtonsDisabled(boolean disabled) {
        runButton.setDisable(disabled);
        defaultGraphButton.setDisable(disabled);
        editGraphButton.setDisable(disabled);
    }



    public static class Edge {
        String source, destination;
        int weight;

        Edge(String source, String destination, int weight) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return source + " - " + destination + " (" + weight + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Edge edge = (Edge) obj;
            return weight == edge.weight &&
                    ((source.equals(edge.source) && destination.equals(edge.destination)) ||
                            (source.equals(edge.destination) && destination.equals(edge.source)));
        }

        @Override
        public int hashCode() {

            return Objects.hash(Math.min(source.hashCode(), destination.hashCode()),
                    Math.max(source.hashCode(), destination.hashCode()), weight);
        }
    }

    public static class Point {
        double x, y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class DisjointSet {
        private final Map<String, String> parent = new HashMap<>();
        private final Map<String, Integer> rank = new HashMap<>();

        DisjointSet(Set<String> vertices) {
            for (String v : vertices) {
                parent.put(v, v);
                rank.put(v, 0);
            }
        }


        String find(String x) {
            if (!parent.get(x).equals(x)) {
                parent.put(x, find(parent.get(x))); // Path compression
            }
            return parent.get(x);
        }


        void union(String x, String y) {
            String rootX = find(x);
            String rootY = find(y);

            if (rootX.equals(rootY)) return;


            int rankX = rank.get(rootX);
            int rankY = rank.get(rootY);

            if (rankX < rankY) {
                parent.put(rootX, rootY);
            } else if (rankX > rankY) {
                parent.put(rootY, rootX);
            } else {
                parent.put(rootY, rootX);
                rank.put(rootX, rankX + 1);
            }
        }
    }
}