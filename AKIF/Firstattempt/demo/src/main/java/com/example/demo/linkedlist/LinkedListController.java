package com.example.demo.linkedlist;

// LinkedListController.java
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.effect.DropShadow;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

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
    private TextField deleteField;

    @FXML
    private Button deleteButton;

    @FXML
    private TextField insertValueField;

    @FXML
    private TextField insertPositionField;

    @FXML
    private Button insertButton;

    @FXML
    private TextField swapField1;

    @FXML
    private TextField swapField2;

    @FXML
    private Button swapButton;

    @FXML
    private Label sizeLabel;

    @FXML
    private Pane drawingPane;

    private List<Button> nodeButtons;
    private List<Line> connectionLines;
    private List<Polygon> arrowHeads;
    private double currentX = 30;
    private double currentY = 30;
    private final double BUTTON_WIDTH = 100;
    private final double BUTTON_HEIGHT = 50;
    private final double SPACING = 140;
    private final double ROW_SPACING = 100;
    private final double MARGIN = 30;
    private int currentRow = 0;
    private int nodesInCurrentRow = 0;
    private final int MAX_NODES_PER_ROW = 4;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize collections
        nodeButtons = new ArrayList<>();
        connectionLines = new ArrayList<>();
        arrowHeads = new ArrayList<>();

        // Set up event handlers
        addButton.setOnAction(e -> addNode());
        clearButton.setOnAction(e -> clearAll());
        deleteButton.setOnAction(e -> deleteNode());
        insertButton.setOnAction(e -> insertNode());
        swapButton.setOnAction(e -> swapNodes());

        inputField.setOnAction(e -> addNode());
        deleteField.setOnAction(e -> deleteNode());
        insertValueField.setOnAction(e -> insertNode());
        insertPositionField.setOnAction(e -> insertNode());

        // Set prompt texts
        inputField.setPromptText("Enter node value");
        deleteField.setPromptText("Value to delete");
        insertValueField.setPromptText("New value");
        insertPositionField.setPromptText("Insert after position");
        swapField1.setPromptText("First position");
        swapField2.setPromptText("Second position");

        // Style the input fields
        styleInputField(inputField);
        styleInputField(deleteField);
        styleInputField(insertValueField);
        styleInputField(insertPositionField);
        styleInputField(swapField1);
        styleInputField(swapField2);

        // Style the buttons
        styleControlButton(addButton, "#4CAF50");
        styleControlButton(clearButton, "#f44336");
        styleControlButton(deleteButton, "#FF9800");
        styleControlButton(insertButton, "#2196F3");
        styleControlButton(swapButton, "#9C27B0");

        // Update size label
        updateSizeLabel();
    }

    private void styleInputField(TextField field) {
        field.setStyle("-fx-background-color: white; -fx-border-color: #2196F3; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8; -fx-font-size: 14px;");
    }

    private void styleControlButton(Button button, String color) {
        button.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 10 20 10 20; " +
                        "-fx-cursor: hand;", color));

        // Add hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(String.format(
                    "-fx-background-color: derive(%s, -10%%); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 14px; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-padding: 10 20 10 20; " +
                            "-fx-cursor: hand;", color));
        });

        button.setOnMouseExited(e -> {
            button.setStyle(String.format(
                    "-fx-background-color: %s; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 14px; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-padding: 10 20 10 20; " +
                            "-fx-cursor: hand;", color));
        });
    }

    private void updateSizeLabel() {
        sizeLabel.setText("Size: " + nodeButtons.size());
    }

    @FXML
    private void addNode() {
        String value = inputField.getText();
        if (value == null || value.trim().isEmpty()) {
            showAlert("Error", "Please enter a node value.");
            return;
        }

        // Check if we need to move to next row
        if (nodesInCurrentRow >= MAX_NODES_PER_ROW) {
            currentRow++;
            nodesInCurrentRow = 0;
            currentX = MARGIN;
            currentY = MARGIN + (currentRow * (BUTTON_HEIGHT + ROW_SPACING));
        }

        // Create new button for the node
        Button nodeButton = new Button(value.trim());
        nodeButton.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        nodeButton.setLayoutX(currentX);
        nodeButton.setLayoutY(currentY);

        // Style the button with gradient and shadow
        styleNodeButton(nodeButton);

        // Add entrance animation
        addEntranceAnimation(nodeButton);

        // Add button to the pane
        drawingPane.getChildren().add(nodeButton);
        nodeButtons.add(nodeButton);

        // Create connection line and arrow if this is not the first node
        if (nodeButtons.size() > 1) {
            createConnection(nodeButtons.size() - 2, nodeButtons.size() - 1);
        }

        // Update position for next node
        currentX += SPACING;
        nodesInCurrentRow++;

        // Clear input field and update size
        inputField.clear();
        updateSizeLabel();
    }

    @FXML
    private void deleteNode() {
        String value = deleteField.getText();
        if (value == null || value.trim().isEmpty()) {
            showAlert("Error", "Please enter a value to delete.");
            return;
        }

        // Find the node to delete
        int indexToDelete = -1;
        for (int i = 0; i < nodeButtons.size(); i++) {
            if (nodeButtons.get(i).getText().equals(value.trim())) {
                indexToDelete = i;
                break;
            }
        }

        if (indexToDelete == -1) {
            showAlert("Error", "Node with value '" + value + "' not found.");
            deleteField.clear();
            return;
        }

        // Make the index final for lambda expression
        final int finalIndexToDelete = indexToDelete;

        // Remove the node with animation
        Button nodeToDelete = nodeButtons.get(finalIndexToDelete);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), nodeToDelete);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            drawingPane.getChildren().remove(nodeToDelete);
            nodeButtons.remove(finalIndexToDelete);
            redrawConnections();
            repositionNodes();
            updateSizeLabel();
        });
        fadeOut.play();

        deleteField.clear();
    }

    @FXML
    private void insertNode() {
        String value = insertValueField.getText();
        String positionStr = insertPositionField.getText();

        if (value == null || value.trim().isEmpty()) {
            showAlert("Error", "Please enter a value to insert.");
            return;
        }

        if (positionStr == null || positionStr.trim().isEmpty()) {
            showAlert("Error", "Please enter a position.");
            return;
        }

        try {
            int position = Integer.parseInt(positionStr.trim());
            if (position < 0 || position >= nodeButtons.size()) {
                showAlert("Error", "Position must be between 0 and " + (nodeButtons.size() - 1));
                return;
            }

            // Create new button
            Button newNodeButton = new Button(value.trim());
            newNodeButton.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);

            // Insert at position + 1
            nodeButtons.add(position + 1, newNodeButton);

            // Style the button
            styleNodeButton(newNodeButton);

            // Add to pane
            drawingPane.getChildren().add(newNodeButton);

            // Add entrance animation
            addEntranceAnimation(newNodeButton);

            // Redraw everything
            redrawConnections();
            repositionNodes();
            updateSizeLabel();

            insertValueField.clear();
            insertPositionField.clear();

        } catch (NumberFormatException e) {
            showAlert("Error", "Position must be a valid number.");
        }
    }

    @FXML
    private void swapNodes() {
        String pos1Str = swapField1.getText();
        String pos2Str = swapField2.getText();

        if (pos1Str == null || pos1Str.trim().isEmpty() ||
                pos2Str == null || pos2Str.trim().isEmpty()) {
            showAlert("Error", "Please enter both positions to swap.");
            return;
        }

        try {
            int pos1 = Integer.parseInt(pos1Str.trim());
            int pos2 = Integer.parseInt(pos2Str.trim());

            if (pos1 < 0 || pos1 >= nodeButtons.size() ||
                    pos2 < 0 || pos2 >= nodeButtons.size()) {
                showAlert("Error", "Positions must be between 0 and " + (nodeButtons.size() - 1));
                return;
            }

            if (pos1 == pos2) {
                showAlert("Error", "Cannot swap a node with itself.");
                return;
            }

            // Swap the button texts
            Button button1 = nodeButtons.get(pos1);
            Button button2 = nodeButtons.get(pos2);

            String temp = button1.getText();
            button1.setText(button2.getText());
            button2.setText(temp);

            // Add swap animation
            addSwapAnimation(button1, button2);

            swapField1.clear();
            swapField2.clear();

        } catch (NumberFormatException e) {
            showAlert("Error", "Positions must be valid numbers.");
        }
    }

    private void addSwapAnimation(Button button1, Button button2) {
        // Scale animation for both buttons
        ScaleTransition scale1 = new ScaleTransition(Duration.millis(200), button1);
        scale1.setFromX(1.0);
        scale1.setFromY(1.0);
        scale1.setToX(1.2);
        scale1.setToY(1.2);
        scale1.setAutoReverse(true);
        scale1.setCycleCount(2);

        ScaleTransition scale2 = new ScaleTransition(Duration.millis(200), button2);
        scale2.setFromX(1.0);
        scale2.setFromY(1.0);
        scale2.setToX(1.2);
        scale2.setToY(1.2);
        scale2.setAutoReverse(true);
        scale2.setCycleCount(2);

        scale1.play();
        scale2.play();
    }

    private void redrawConnections() {
        // Remove all existing connections
        drawingPane.getChildren().removeAll(connectionLines);
        drawingPane.getChildren().removeAll(arrowHeads);
        connectionLines.clear();
        arrowHeads.clear();

        // Redraw all connections
        for (int i = 0; i < nodeButtons.size() - 1; i++) {
            createConnection(i, i + 1);
        }
    }

    private void repositionNodes() {
        currentX = MARGIN;
        currentY = MARGIN;
        currentRow = 0;
        nodesInCurrentRow = 0;

        for (Button nodeButton : nodeButtons) {
            // Check if we need to move to next row
            if (nodesInCurrentRow >= MAX_NODES_PER_ROW) {
                currentRow++;
                nodesInCurrentRow = 0;
                currentX = MARGIN;
                currentY = MARGIN + (currentRow * (BUTTON_HEIGHT + ROW_SPACING));
            }

            // Set new position
            nodeButton.setLayoutX(currentX);
            nodeButton.setLayoutY(currentY);

            // Update position for next node
            currentX += SPACING;
            nodesInCurrentRow++;
        }

        // Redraw connections after repositioning
        redrawConnections();
    }

    private void styleNodeButton(Button nodeButton) {
        // Create gradient background
        String gradientStyle =
                "-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;";

        nodeButton.setStyle(gradientStyle);

        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.gray(0, 0.6));
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        dropShadow.setRadius(5);
        nodeButton.setEffect(dropShadow);

        // Add hover effects
        nodeButton.setOnMouseEntered(e -> {
            nodeButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #5a6fd8 0%, #6a4190 100%);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-border-radius: 15;" +
                            "-fx-background-radius: 15;" +
                            "-fx-cursor: hand;");

            // Scale animation on hover
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), nodeButton);
            scaleUp.setToX(1.1);
            scaleUp.setToY(1.1);
            scaleUp.play();
        });

        nodeButton.setOnMouseExited(e -> {
            nodeButton.setStyle(gradientStyle);

            // Scale back to normal
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), nodeButton);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }

    private void addEntranceAnimation(Button nodeButton) {
        // Start invisible and small
        nodeButton.setOpacity(0);
        nodeButton.setScaleX(0.5);
        nodeButton.setScaleY(0.5);

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), nodeButton);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Scale up
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), nodeButton);
        scaleUp.setFromX(0.5);
        scaleUp.setFromY(0.5);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        // Play animations
        fadeIn.play();
        scaleUp.play();
    }

    private void createConnection(int fromIndex, int toIndex) {
        Button fromButton = nodeButtons.get(fromIndex);
        Button toButton = nodeButtons.get(toIndex);

        // Calculate connection points
        double fromX = fromButton.getLayoutX() + BUTTON_WIDTH;
        double fromY = fromButton.getLayoutY() + BUTTON_HEIGHT / 2;
        double toX = toButton.getLayoutX();
        double toY = toButton.getLayoutY() + BUTTON_HEIGHT / 2;

        // Special handling for connections that go to next row
        if (fromButton.getLayoutY() != toButton.getLayoutY()) {
            // Connection goes to next row - create curved connection
            createCurvedConnection(fromX, fromY, toX, toY);
        } else {
            // Same row - create straight connection
            createStraightConnection(fromX, fromY, toX, toY);
        }
    }

    private void createStraightConnection(double fromX, double fromY, double toX, double toY) {
        // Create connection line
        Line connectionLine = new Line(fromX, fromY, toX - 20, toY);
        connectionLine.setStroke(Color.web("#2196F3"));
        connectionLine.setStrokeWidth(3);

        // Create arrowhead
        Polygon arrowHead = createArrowHead(toX - 20, toY, toX, toY);
        arrowHead.setFill(Color.web("#2196F3"));

        // Add to pane and collections
        drawingPane.getChildren().addAll(connectionLine, arrowHead);
        connectionLines.add(connectionLine);
        arrowHeads.add(arrowHead);
    }

    private void createCurvedConnection(double fromX, double fromY, double toX, double toY) {
        // Create a curved path using multiple line segments
        double midX = fromX + 50; // Extend right from first node
        double midY1 = fromY;
        double midY2 = toY;
        double midX2 = toX - 50; // Extend left to second node

        // First horizontal segment
        Line line1 = new Line(fromX, fromY, midX, midY1);
        line1.setStroke(Color.web("#2196F3"));
        line1.setStrokeWidth(3);

        // Vertical segment
        Line line2 = new Line(midX, midY1, midX, midY2 - (ROW_SPACING/2));
        line2.setStroke(Color.web("#2196F3"));
        line2.setStrokeWidth(3);

        // Second horizontal segment
        Line line3 = new Line(midX, midY2- (ROW_SPACING/2), toX - 20, midY2- (ROW_SPACING/2));
        line3.setStroke(Color.web("#2196F3"));
        line3.setStrokeWidth(3);

        //vertical line
        Line line4 = new Line(toX - 20, midY2- (ROW_SPACING/2),toX - 20, midY2);
        line4.setStroke(Color.web("#2196F3"));
        line4.setStrokeWidth(3);

        // Create arrowhead
        Polygon arrowHead = createArrowHead(toX - 20, toY, toX, toY);
        arrowHead.setFill(Color.web("#2196F3"));

        // Add to pane and collections
        drawingPane.getChildren().addAll(line1, line2, line3, line4 ,arrowHead);
        connectionLines.addAll(List.of(line1,line2, line3, line4));
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
        double arrowLength = 20;
        double arrowWidth = 10;

        // Calculate arrow points
        double x1 = toX - arrowLength * dx + arrowWidth * dy;
        double y1 = toY - arrowLength * dy - arrowWidth * dx;
        double x2 = toX - arrowLength * dx - arrowWidth * dy;
        double y2 = toY - arrowLength * dy + arrowWidth * dx;

        return new Polygon(toX, toY, x1, y1, x2, y2);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void clearAll() {
        // Fade out animation for all nodes
        for (Button nodeButton : nodeButtons) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), nodeButton);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.play();
        }
        for (Line nodeButton : connectionLines) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), nodeButton);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.play();
        }
        for (Polygon nodeButton : arrowHeads) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), nodeButton);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.play();
        }

        drawingPane.getChildren().removeAll(nodeButtons);
        drawingPane.getChildren().removeAll(connectionLines);
        drawingPane.getChildren().removeAll(arrowHeads);

        // Clear collections
        nodeButtons.clear();
        connectionLines.clear();
        arrowHeads.clear();

        // Reset position
        currentX = MARGIN;
        currentY = MARGIN;
        currentRow = 0;
        nodesInCurrentRow = 0;

        // Update size label
        updateSizeLabel();
    }
}