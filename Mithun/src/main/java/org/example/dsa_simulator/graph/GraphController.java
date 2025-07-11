package org.example.dsa_simulator.graph;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphController {

    @FXML
    private TextArea edgeInputArea;

    @FXML
    private Pane graphPane;

    // A variable to hold our graph data structure
    private Graph graph;

    @FXML
    void handleDrawGraph(ActionEvent event) {
        // 1. Initialize a new graph data model
        this.graph = new Graph();

        // 2. Read input and populate the graph model
        String[] lines = edgeInputArea.getText().trim().split("\n");
        for (String line : lines) {
            String[] nodes = line.trim().split("\\s+");
            if (nodes.length == 2) {
                // Add the edge to our data structure
                graph.addEdge(nodes[0], nodes[1]);
            }
        }

        // 3. Draw the graph based on the data in the 'graph' object
        renderVisuals();
    }

    private void renderVisuals() {
        graphPane.getChildren().clear();
        Map<String, StackPane> visualNodes = new HashMap<>();
        Set<String> nodeIds = graph.getNodes();

        // --- Draw the nodes in a circle ---
        double width = graphPane.getWidth();
        double height = graphPane.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double radius = Math.min(width, height) / 2 - 50;
        double angleStep = 2 * Math.PI / nodeIds.size();
        int i = 0;

        for (String nodeId : nodeIds) {
            StackPane nodePane = createVisualNode(nodeId);
            double angle = i * angleStep;
            double x = centerX + radius * Math.cos(angle) - 20;
            double y = centerY + radius * Math.sin(angle) - 20;
            nodePane.setLayoutX(x);
            nodePane.setLayoutY(y);

            visualNodes.put(nodeId, nodePane);
            graphPane.getChildren().add(nodePane);
            i++;
        }

        // --- Draw the edges ---
        // Get the adjacency list from our graph model
        Map<String, List<String>> adjList = graph.getAdjacencyList();
        for (String sourceId : adjList.keySet()) {
            StackPane sourcePane = visualNodes.get(sourceId);
            List<String> neighbors = adjList.get(sourceId);

            for (String targetId : neighbors) {
                StackPane targetPane = visualNodes.get(targetId);
                if (sourcePane != null && targetPane != null) {
                    Line line = createVisualEdge(sourcePane, targetPane);
                    graphPane.getChildren().add(line);
                    line.toBack();
                }
            }
        }
    }

    // Helper method to create a visual node
    private StackPane createVisualNode(String id) {
        Circle circle = new Circle(20, Color.DODGERBLUE);
        circle.setStroke(Color.BLACK);
        Text label = new Text(id);
        label.setFill(Color.WHITE);
        return new StackPane(circle, label);
    }

    // Helper method to create a visual edge
    private Line createVisualEdge(StackPane source, StackPane target) {
        Line line = new Line();
        line.setStrokeWidth(2);
        line.setStroke(Color.GRAY);
        line.startXProperty().bind(source.layoutXProperty().add(source.widthProperty().divide(2)));
        line.startYProperty().bind(source.layoutYProperty().add(source.heightProperty().divide(2)));
        line.endXProperty().bind(target.layoutXProperty().add(target.widthProperty().divide(2)));
        line.endYProperty().bind(target.layoutYProperty().add(target.heightProperty().divide(2)));
        return line;
    }

    @FXML
    void returnToHome(ActionEvent event) {
        try {
            Parent homeScreenRoot = FXMLLoader.load(getClass().getResource("/org/example/dsa_simulator/Home-screen.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homeScreenRoot));
            stage.setTitle("DSA Simulator");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
