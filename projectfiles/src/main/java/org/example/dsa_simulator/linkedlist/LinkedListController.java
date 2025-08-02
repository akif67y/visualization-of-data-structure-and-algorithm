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

    private double currentX = 50; // Increased from 20 to 50
    private double currentY = 20; // Keep this as is
    private final double BUTTON_WIDTH = 70;
    private final double BUTTON_HEIGHT = 35;
    private final double SPACING = 100;
    private final double ROW_SPACING = 60;
    private final double MARGIN = 50; // Increased from 20 to 50 to match currentX
    private int currentRow = 0;
    private int nodesInCurrentRow = 0;
    private final int MAX_NODES_PER_ROW = 9;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        nodeButtons = new ArrayList<>();
        connectionLines = new ArrayList<>();
        arrowHeads = new ArrayList<>();


        addButton.setOnAction(e -> addNode());
        clearButton.setOnAction(e -> clearAll());
        deleteButton.setOnAction(e -> deleteNode());
        insertButton.setOnAction(e -> insertNode());
        swapButton.setOnAction(e -> swapNodes());
        inputField.setOnAction(e -> addNode());
        deleteField.setOnAction(e -> deleteNode());
        insertValueField.setOnAction(e -> insertNode());
        insertPositionField.setOnAction(e -> insertNode());


        inputField.setPromptText("Enter node value");
        deleteField.setPromptText("Value to delete");
        insertValueField.setPromptText("New value");
        insertPositionField.setPromptText("Insert after position");
        swapField1.setPromptText("Pos 1");
        swapField2.setPromptText("Pos 2");


        styleInputField(inputField);
        styleInputField(deleteField);
        styleInputField(insertValueField);
        styleInputField(insertPositionField);
        styleInputField(swapField1);
        styleInputField(swapField2);


        styleControlButton(addButton, "#38a169"); // Updated colors
        styleControlButton(clearButton, "#e53e3e");
        styleControlButton(deleteButton, "#dd6b20");
        styleControlButton(insertButton, "#3182ce");
        styleControlButton(swapButton, "#805ad5");

        updateSizeLabel();
    }

    private void styleInputField(TextField field) {

        field.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #cbd5e0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8; -fx-font-size: 13px; -fx-text-fill: #4a5568;");
    }

    private void styleControlButton(Button button, String color) {

        button.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 13px; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 8 16 8 16; " + // Reduced padding
                        "-fx-cursor: hand;", color));

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


        if (nodesInCurrentRow >= MAX_NODES_PER_ROW) {
            currentRow++;
            nodesInCurrentRow = 0;
            currentX = MARGIN;

            currentY = MARGIN + (currentRow * (BUTTON_HEIGHT + ROW_SPACING));
        }


        Button nodeButton = new Button(value.trim());
        nodeButton.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        nodeButton.setLayoutX(currentX);
        nodeButton.setLayoutY(currentY);


        styleNodeButton(nodeButton);


        addEntranceAnimation(nodeButton);


        drawingPane.getChildren().add(nodeButton);
        nodeButtons.add(nodeButton);


        if (nodeButtons.size() > 1) {
            createConnection(nodeButtons.size() - 2, nodeButtons.size() - 1);
        }


        currentX += SPACING;
        nodesInCurrentRow++;


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


        final int finalIndexToDelete = indexToDelete;


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

            if (position < 0 || position > nodeButtons.size() - 1) {
                showAlert("Error", "Position must be between 0 and " + (nodeButtons.size() - 1));
                return;
            }


            Button newNodeButton = new Button(value.trim());
            newNodeButton.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);


            nodeButtons.add(position + 1, newNodeButton);


            styleNodeButton(newNodeButton);


            drawingPane.getChildren().add(newNodeButton);


            addEntranceAnimation(newNodeButton);


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


            Button button1 = nodeButtons.get(pos1);
            Button button2 = nodeButtons.get(pos2);
            String temp = button1.getText();
            button1.setText(button2.getText());
            button2.setText(temp);


            addSwapAnimation(button1, button2);

            swapField1.clear();
            swapField2.clear();
        } catch (NumberFormatException e) {
            showAlert("Error", "Positions must be valid numbers.");
        }
    }

    private void addSwapAnimation(Button button1, Button button2) {

        ScaleTransition scale1 = new ScaleTransition(Duration.millis(200), button1);
        scale1.setFromX(1.0);
        scale1.setFromY(1.0);
        scale1.setToX(1.15);
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

        drawingPane.getChildren().removeAll(connectionLines);
        drawingPane.getChildren().removeAll(arrowHeads);
        connectionLines.clear();
        arrowHeads.clear();


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

            if (nodesInCurrentRow >= MAX_NODES_PER_ROW) {
                currentRow++;
                nodesInCurrentRow = 0;
                currentX = MARGIN;

                currentY = MARGIN + (currentRow * (BUTTON_HEIGHT + ROW_SPACING));
            }


            nodeButton.setLayoutX(currentX);
            nodeButton.setLayoutY(currentY);


            currentX += SPACING;
            nodesInCurrentRow++;
        }


        redrawConnections();
    }


    private void styleNodeButton(Button nodeButton) {

        String gradientStyle =
                "-fx-background-color: linear-gradient(to bottom, #6366f1 0%, #4f46e5 100%);" + // Purple gradient
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;";

        nodeButton.setStyle(gradientStyle);


        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.gray(0, 0.4));
        dropShadow.setOffsetX(1.5);
        dropShadow.setOffsetY(1.5);
        dropShadow.setRadius(3);
        nodeButton.setEffect(dropShadow);


        nodeButton.setOnMouseEntered(e -> {

            nodeButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #4f46e5 0%, #4338ca 100%);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 12px;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-cursor: hand;");

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), nodeButton);
            scaleUp.setToX(1.08);
            scaleUp.setToY(1.08);
            scaleUp.play();
        });

        nodeButton.setOnMouseExited(e -> {
            nodeButton.setStyle(gradientStyle);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), nodeButton);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }

    private void addEntranceAnimation(Button nodeButton) {

        nodeButton.setOpacity(0);
        nodeButton.setScaleX(0.3);
        nodeButton.setScaleY(0.3);


        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), nodeButton);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);


        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), nodeButton);
        scaleUp.setFromX(0.3);
        scaleUp.setFromY(0.3);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);


        fadeIn.play();
        scaleUp.play();
    }

    private void createConnection(int fromIndex, int toIndex) {
        Button fromButton = nodeButtons.get(fromIndex);
        Button toButton = nodeButtons.get(toIndex);


        double fromX = fromButton.getLayoutX() + BUTTON_WIDTH;
        double fromY = fromButton.getLayoutY() + BUTTON_HEIGHT / 2;
        double toX = toButton.getLayoutX();
        double toY = toButton.getLayoutY() + BUTTON_HEIGHT / 2;


        if (fromButton.getLayoutY() != toButton.getLayoutY()) {

            createCurvedConnection(fromX, fromY, toX, toY);
        } else {

            createStraightConnection(fromX, fromY, toX, toY);
        }
    }

    private void createStraightConnection(double fromX, double fromY, double toX, double toY) {

        Line connectionLine = new Line(fromX, fromY, toX - 15, toY); // Reduced arrow offset
        connectionLine.setStroke(Color.web("#4a5568")); // Darker color
        connectionLine.setStrokeWidth(2); // Thinner line

        // Create arrowhead
        Polygon arrowHead = createArrowHead(toX - 15, toY, toX, toY); // Match line end
        arrowHead.setFill(Color.web("#4a5568"));


        drawingPane.getChildren().addAll(connectionLine, arrowHead);
        connectionLines.add(connectionLine);
        arrowHeads.add(arrowHead);
    }

    private void createCurvedConnection(double fromX, double fromY, double toX, double toY) {

        double verticalMidY = fromY + (ROW_SPACING / 2.0);


        Line line1 = new Line(fromX, fromY, fromX + 20, fromY); // Extend right
        line1.setStroke(Color.web("#4a5568"));
        line1.setStrokeWidth(2);


        Line line2 = new Line(fromX + 20, fromY, fromX + 20, verticalMidY);
        line2.setStroke(Color.web("#4a5568"));
        line2.setStrokeWidth(2);


        Line line3 = new Line(fromX + 20, verticalMidY, toX - 20, verticalMidY);
        line3.setStroke(Color.web("#4a5568"));
        line3.setStrokeWidth(2);


        Line line4 = new Line(toX - 20, verticalMidY, toX - 20, toY);
        line4.setStroke(Color.web("#4a5568"));
        line4.setStrokeWidth(2);


        Line line5 = new Line(toX - 20, toY, toX - 15, toY); // Short segment before arrow
        line5.setStroke(Color.web("#4a5568"));
        line5.setStrokeWidth(2);


        Polygon arrowHead = createArrowHead(toX - 15, toY, toX, toY);
        arrowHead.setFill(Color.web("#4a5568"));


        drawingPane.getChildren().addAll(line1, line2, line3, line4, line5, arrowHead);
        connectionLines.addAll(List.of(line1, line2, line3, line4, line5));
        arrowHeads.add(arrowHead);
    }


    private Polygon createArrowHead(double fromX, double fromY, double toX, double toY) {

        double dx = toX - fromX;
        double dy = toY - fromY;
        double length = Math.sqrt(dx * dx + dy * dy);


        if (length == 0) {
            return new Polygon();
        }
        dx /= length;
        dy /= length;


        double arrowLength = 12;
        double arrowWidth = 6;


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

        List<FadeTransition> fadeTransitions = new ArrayList<>();


        for (Button nodeButton : nodeButtons) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), nodeButton);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeTransitions.add(fadeOut);
        }


        for (Line line : connectionLines) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), line);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeTransitions.add(fadeOut);
        }


        for (Polygon arrowHead : arrowHeads) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), arrowHead);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeTransitions.add(fadeOut);
        }


        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition();
        pt.getChildren().addAll(fadeTransitions);


        pt.setOnFinished(e -> {

            drawingPane.getChildren().removeAll(nodeButtons);
            drawingPane.getChildren().removeAll(connectionLines);
            drawingPane.getChildren().removeAll(arrowHeads);


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