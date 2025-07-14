package com.example.demo;

// LinkedListController.java
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LinkedListController implements Initializable {

    @FXML
    private TextField inputField;

    @FXML
    private Button addButton;

    @FXML
    private Button clearButton;

    @FXML
    private Pane drawingPane;

    private List<Button> nodeButtons;
    private List<Line> connectionLines;
    private List<Polygon> arrowHeads;
    private double currentX = 50;
    private final double BUTTON_WIDTH = 80;
    private final double BUTTON_HEIGHT = 40;
    private final double SPACING = 120;
    private final double Y_POSITION = 50;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize collections
        nodeButtons = new ArrayList<>();
        connectionLines = new ArrayList<>();
        arrowHeads = new ArrayList<>();

        // Set up event handlers
        addButton.setOnAction(e -> addNode());
        clearButton.setOnAction(e -> clearAll());
        inputField.setOnAction(e -> addNode());

        // Set prompt text
        inputField.setPromptText("Enter value");
    }

    @FXML
    private void addNode() {
        String value = inputField.getText();
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        // Create new button for the node
        Button nodeButton = new Button(value.trim());
        nodeButton.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        nodeButton.setLayoutX(currentX);
        nodeButton.setLayoutY(Y_POSITION);

        // Style the button
        nodeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Add hover effect
        nodeButton.setOnMouseEntered(e ->
                nodeButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;"));
        nodeButton.setOnMouseExited(e ->
                nodeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;"));

        // Add button to the pane
        drawingPane.getChildren().add(nodeButton);
        nodeButtons.add(nodeButton);

        // Create connection line and arrow if this is not the first node
        if (nodeButtons.size() > 1) {
            createConnection(nodeButtons.size() - 2, nodeButtons.size() - 1);
        }

        // Update position for next node
        currentX += SPACING;

        // Clear input field
        inputField.clear();
     //   inputField.requestFocus();
    }

    private void createConnection(int fromIndex, int toIndex) {
        Button fromButton = nodeButtons.get(fromIndex);
        Button toButton = nodeButtons.get(toIndex);

        // Calculate connection points
        double fromX = fromButton.getLayoutX() + BUTTON_WIDTH;
        double fromY = fromButton.getLayoutY() + BUTTON_HEIGHT / 2;
        double toX = toButton.getLayoutX();
        double toY = toButton.getLayoutY() + BUTTON_HEIGHT / 2;

        // Create connection line
        Line connectionLine = new Line(fromX, fromY, toX - 15, toY);
        connectionLine.setStroke(Color.BLACK);
        connectionLine.setStrokeWidth(2);

        // Create arrowhead
        Polygon arrowHead = createArrowHead(toX - 15, toY, toX, toY);
        arrowHead.setFill(Color.BLACK);

        // Add to pane and collections
        drawingPane.getChildren().addAll(connectionLine, arrowHead);
        connectionLines.add(connectionLine);
        arrowHeads.add(arrowHead);
    }

    private Polygon createArrowHead(double fromX, double fromY, double toX, double toY) {
        // Calculate arrow direction
        double dx = toX - fromX;
        double dy = toY - fromY;
        double length = Math.sqrt(dx * dx + dy * dy);

        // Normalize direction
        dx /= length;
        dy /= length;

        // Arrow dimensions
        double arrowLength = 15;
        double arrowWidth = 8;

        // Calculate arrow points
        double x1 = toX - arrowLength * dx + arrowWidth * dy;
        double y1 = toY - arrowLength * dy - arrowWidth * dx;
        double x2 = toX - arrowLength * dx - arrowWidth * dy;
        double y2 = toY - arrowLength * dy + arrowWidth * dx;

        return new Polygon(toX, toY, x1, y1, x2, y2);
    }

    @FXML
    private void clearAll() {
        // Remove all visual elements
        drawingPane.getChildren().removeAll(nodeButtons);
        drawingPane.getChildren().removeAll(connectionLines);
        drawingPane.getChildren().removeAll(arrowHeads);

        // Clear collections
        nodeButtons.clear();
        connectionLines.clear();
        arrowHeads.clear();

        // Reset position
        currentX = 50;

        // Focus back to input field
     //   inputField.requestFocus();
    }
}

