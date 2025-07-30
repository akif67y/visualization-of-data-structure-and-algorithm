package org.example.dsa_simulator.linkedlist;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    private double currentX = 20; // Updated initial margin
    private double currentY = 20; // Updated initial margin
    private final double BUTTON_WIDTH = 70; // Reduced width
    private final double BUTTON_HEIGHT = 35; // Reduced height
    private final double SPACING = 100; // Reduced horizontal spacing
    private final double ROW_SPACING = 60; // Reduced vertical spacing between rows
    private final double MARGIN = 20; // Reduced margin
    private int currentRow = 0;
    private int nodesInCurrentRow = 0;
    private final int MAX_NODES_PER_ROW = 8; // Increased max nodes per row

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
        swapField1.setPromptText("Pos 1");
        swapField2.setPromptText("Pos 2");

        // Style the input fields
        styleInputField(inputField);
        styleInputField(deleteField);
        styleInputField(insertValueField);
        styleInputField(insertPositionField);
        styleInputField(swapField1);
        styleInputField(swapField2);

        // Style the buttons
        styleControlButton(addButton, "#38a169"); // Updated colors
        styleControlButton(clearButton, "#e53e3e");
        styleControlButton(deleteButton, "#dd6b20");
        styleControlButton(insertButton, "#3182ce");
        styleControlButton(swapButton, "#805ad5");

        // Update size label
        updateSizeLabel();
    }

    private void styleInputField(TextField field) {
        // Slightly refined styling
        field.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #cbd5e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8; -fx-font-size: 13px; -fx-text-fill: #4a5568;");
    }

    private void styleControlButton(Button button, String color) {
        // Updated styling with refined colors and smaller radius
        button.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 13px; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 8 16 8 16; " + // Reduced padding
                        "-fx-cursor: hand;", color));
        // Add hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(String.format(
                    "-fx-background-color: derive(%s, -10%%); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 13px; " +
                            "-fx-border-radius: 6; " +
                            "-fx-background-radius: 6; " +
                            "-fx-padding: 8 16 8 16; " +
                            "-fx-cursor: hand;", color));
        });
        button.setOnMouseExited(e -> {
            button.setStyle(String.format(
                    "-fx-background-color: %s; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 13px; " +
                            "-fx-border-radius: 6; " +
                            "-fx-background-radius: 6; " +
                            "-fx-padding: 8 16 8 16; " +
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
            // Adjust Y position calculation for new row
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
            // Allow inserting at the end (position == size)
            if (position < 0 || position > nodeButtons.size() - 1) {
                showAlert("Error", "Position must be between 0 and " + (nodeButtons.size() - 1));
                return;
            }

            // Create new button
            Button newNodeButton = new Button(value.trim());
            newNodeButton.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);

            // Insert at position + 1 (after the given position)
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
        scale1.setToX(1.15); // Slightly reduced scale for smaller buttons
        scale1.setToY(1.15);
        scale1.setAutoReverse(true);
        scale1.setCycleCount(2);

        ScaleTransition scale2 = new ScaleTransition(Duration.millis(200), button2);
        scale2.setFromX(1.0);
        scale2.setFromY(1.0);
        scale2.setToX(1.15);
        scale2.setToY(1.15);
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
                // Adjust Y position calculation for new row
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
        // Updated gradient for smaller buttons
        String gradientStyle =
                "-fx-background-color: linear-gradient(to bottom, #6366f1 0%, #4f46e5 100%);" + // Purple gradient
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" + // Smaller font
                        "-fx-border-radius: 10;" + // Adjusted radius
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;";

        nodeButton.setStyle(gradientStyle);

        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.gray(0, 0.4)); // Slightly reduced shadow opacity
        dropShadow.setOffsetX(1.5); // Reduced shadow offset
        dropShadow.setOffsetY(1.5);
        dropShadow.setRadius(3); // Reduced shadow radius
        nodeButton.setEffect(dropShadow);

        // Add hover effects
        nodeButton.setOnMouseEntered(e -> {
            // Darker gradient on hover
            nodeButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #4f46e5 0%, #4338ca 100%);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 12px;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-cursor: hand;");
            // Scale animation on hover (slightly less for smaller buttons)
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), nodeButton);
            scaleUp.setToX(1.08);
            scaleUp.setToY(1.08);
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
        nodeButton.setScaleX(0.3); // Adjusted scale for smaller button
        nodeButton.setScaleY(0.3);

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), nodeButton);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Scale up
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), nodeButton);
        scaleUp.setFromX(0.3);
        scaleUp.setFromY(0.3);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        // Play animations
        fadeIn.play();
        scaleUp.play();
    }

    private void createConnection(int fromIndex, int toIndex) {
        Button fromButton = nodeButtons.get(fromIndex);
        Button toButton = nodeButtons.get(toIndex);

        // Calculate connection points (right edge of 'from' to left edge of 'to')
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
        // Create connection line (shorten end point slightly before arrow)
        Line connectionLine = new Line(fromX, fromY, toX - 15, toY); // Reduced arrow offset
        connectionLine.setStroke(Color.web("#4a5568")); // Darker color
        connectionLine.setStrokeWidth(2); // Thinner line

        // Create arrowhead
        Polygon arrowHead = createArrowHead(toX - 15, toY, toX, toY); // Match line end
        arrowHead.setFill(Color.web("#4a5568"));

        // Add to pane and collections
        drawingPane.getChildren().addAll(connectionLine, arrowHead);
        connectionLines.add(connectionLine);
        arrowHeads.add(arrowHead);
    }

    private void createCurvedConnection(double fromX, double fromY, double toX, double toY) {
        // Calculate the vertical midpoint based on the actual ROW_SPACING
        double verticalMidY = fromY + (ROW_SPACING / 2.0);

        // First horizontal segment: from node to the right
        Line line1 = new Line(fromX, fromY, fromX + 20, fromY); // Extend right
        line1.setStroke(Color.web("#4a5568"));
        line1.setStrokeWidth(2);

        // Vertical segment: down to the midpoint between rows
        Line line2 = new Line(fromX + 20, fromY, fromX + 20, verticalMidY);
        line2.setStroke(Color.web("#4a5568"));
        line2.setStrokeWidth(2);

        // Horizontal segment: across to the left of the target node
        Line line3 = new Line(fromX + 20, verticalMidY, toX - 20, verticalMidY);
        line3.setStroke(Color.web("#4a5568"));
        line3.setStrokeWidth(2);

        // Vertical segment: down to the target node's level
        Line line4 = new Line(toX - 20, verticalMidY, toX - 20, toY);
        line4.setStroke(Color.web("#4a5568"));
        line4.setStrokeWidth(2);

        // Final horizontal segment: to the target node (with arrow space)
        Line line5 = new Line(toX - 20, toY, toX - 15, toY); // Short segment before arrow
        line5.setStroke(Color.web("#4a5568"));
        line5.setStrokeWidth(2);

        // Create arrowhead pointing to the target node
        Polygon arrowHead = createArrowHead(toX - 15, toY, toX, toY);
        arrowHead.setFill(Color.web("#4a5568"));

        // Add to pane and collections
        drawingPane.getChildren().addAll(line1, line2, line3, line4, line5, arrowHead);
        connectionLines.addAll(List.of(line1, line2, line3, line4, line5));
        arrowHeads.add(arrowHead);
    }


    private Polygon createArrowHead(double fromX, double fromY, double toX, double toY) {
        // Calculate arrow direction
        double dx = toX - fromX;
        double dy = toY - fromY;
        double length = Math.sqrt(dx * dx + dy * dy);

        // Normalize direction (avoid division by zero)
        if (length == 0) {
            return new Polygon(); // Return empty polygon if points are the same
        }
        dx /= length;
        dy /= length;

        // Arrow dimensions (slightly smaller)
        double arrowLength = 12;
        double arrowWidth = 6;

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
        // Create lists to hold the transitions for all elements
        List<FadeTransition> fadeTransitions = new ArrayList<>();

        // Create fade out transitions for all node buttons
        for (Button nodeButton : nodeButtons) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), nodeButton);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeTransitions.add(fadeOut);
        }

        // Create fade out transitions for all connection lines
        for (Line line : connectionLines) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), line);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeTransitions.add(fadeOut);
        }

        // Create fade out transitions for all arrowheads
        for (Polygon arrowHead : arrowHeads) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), arrowHead);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeTransitions.add(fadeOut);
        }

        // Play all fade out animations in parallel
        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition();
        pt.getChildren().addAll(fadeTransitions);

        // Set the action to perform after all animations finish
        pt.setOnFinished(e -> {
            // Remove all graphical elements from the pane
            drawingPane.getChildren().removeAll(nodeButtons);
            drawingPane.getChildren().removeAll(connectionLines);
            drawingPane.getChildren().removeAll(arrowHeads);

            // Clear the collections
            nodeButtons.clear();
            connectionLines.clear();
            arrowHeads.clear();

            // Reset positioning variables
            currentX = MARGIN;
            currentY = MARGIN;
            currentRow = 0;
            nodesInCurrentRow = 0;

            // Update the size label
            updateSizeLabel();
        });

        // Start the parallel animation
        pt.play();
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