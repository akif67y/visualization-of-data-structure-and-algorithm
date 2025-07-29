package com.example.demo.stack;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class StackByArrayController {
    @FXML
    private HBox arrayContainer;
    @FXML
    private VBox stackContainer;
    @FXML
    private TextField inputField;
    private Stack<Button> dynamicButtons = new Stack<>();
    private List<Button> arrayNodes = new ArrayList<>();
    private List<Line> alllines = new ArrayList<>();
    private List<Label> indexLabels = new ArrayList<>();
    @FXML
    Label status;
    @FXML
    Label pos;

    @FXML
    AnchorPane mainthingy;

    @FXML
    private void onAddClicked() {
        String label = inputField.getText().trim();
        inputField.clear();
        if (label.isEmpty()) return;

        // Create a beautifully styled button for stack
        Button stackBtn = new Button(label);
        stackBtn.setPrefWidth(200);
        stackBtn.setPrefHeight(50);
        stackBtn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9);" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );

        // Add hover effects
        stackBtn.setOnMouseEntered(e -> {
            stackBtn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #3ca0db, #2c8bc9);" +
                            "-fx-background-radius: 8;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 3);"
            );
        });

        stackBtn.setOnMouseExited(e -> {
            stackBtn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9);" +
                            "-fx-background-radius: 8;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
            );
        });

        // Add push animation
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), stackBtn);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), stackBtn);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        scaleIn.play();
        fadeIn.play();

        // Add to stack and container
        dynamicButtons.push(stackBtn);
        stackContainer.getChildren().add(0, stackBtn);
        addToArray(label);

        status.setText("Pushed: " + label + " | Stack size: " + dynamicButtons.size());
    }

    private void addToArray(String label) {
        // Create button for array representation
        Button arrayBtn = new Button(label);
        arrayBtn.setPrefWidth(80);
        arrayBtn.setPrefHeight(40);
        arrayBtn.setPrefWidth(80);
        arrayBtn.setPrefHeight(40);
        arrayBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background-insets: 0;" +
                        "-fx-padding: 0;" +
                        "-fx-border-color: transparent;" +
                        "-fx-text-fill: #2c3e50;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
        );
        arrayNodes.add(arrayBtn);
        arrayContainer.getChildren().add(arrayBtn);

        // Update array lines
        updateArrayLines();
    }

    private void updateArrayLines() {
        // Clear existing lines
        for (Line line : alllines) {
            mainthingy.getChildren().remove(line);
        }
        alllines.clear();
        for (Label label : indexLabels) {
            mainthingy.getChildren().remove(label);
        }
        indexLabels.clear();
        // Draw lines for array representation
        double startX = 60.0; // Starting X position
        double startY = 100.0; // Starting Y position (adjusted for array container)
        double cellWidth = 80.0; // Button width + spacing
        double cellHeight = 40.0; // Button height + spacing

        // Draw outer rectangle and grid lines
        for (int i = 0; i <= arrayNodes.size(); i++) {
            // Vertical lines
            Line verticalLine = new Line(
                    startX + i * cellWidth, startY,
                    startX + i * cellWidth, startY + cellHeight
            );
            verticalLine.setStroke(Color.BLACK);
            verticalLine.setStrokeWidth(1);
            mainthingy.getChildren().add(verticalLine);
            alllines.add(verticalLine);

            if (i < arrayNodes.size()) {
                // Horizontal lines
                Line topLine = new Line(
                        startX + i * cellWidth, startY,
                        startX + (i + 1) * cellWidth, startY
                );
                Line bottomLine = new Line(
                        startX + i * cellWidth, startY + cellHeight,
                        startX + (i + 1) * cellWidth, startY + cellHeight
                );
                topLine.setStroke(Color.BLACK);
                topLine.setStrokeWidth(1);
                bottomLine.setStroke(Color.BLACK);
                bottomLine.setStrokeWidth(1);

                mainthingy.getChildren().add(topLine);
                mainthingy.getChildren().add(bottomLine);
                alllines.add(topLine);
                alllines.add(bottomLine);
            }
        }

        // Add index labels
        for (int i = 0; i < arrayNodes.size(); i++) {
            Label indexLabel = new Label(String.valueOf(i));
            indexLabel.setLayoutX(startX + i * cellWidth + 35);
            indexLabel.setLayoutY(startY + cellHeight + 5);
            indexLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
            mainthingy.getChildren().add(indexLabel);
            indexLabels.add(indexLabel);
        }
    }

    @FXML
    private void onPopClicked() {
        if (dynamicButtons.isEmpty()) {
            status.setText("Stack is empty!");
            return;
        }

        // Remove from stack visually
        Button poppedBtn = dynamicButtons.pop();
        stackContainer.getChildren().remove(poppedBtn);

        // Remove from array visually
        if (!arrayNodes.isEmpty()) {
            Button arrayBtn = arrayNodes.remove(arrayNodes.size() - 1);
            arrayContainer.getChildren().remove(arrayBtn);
            updateArrayLines();
        }

        status.setText("Popped: " + poppedBtn.getText() + " | Stack size: " + dynamicButtons.size());
    }

    @FXML
    private void onTopClicked() {
        if (dynamicButtons.isEmpty()) {
            status.setText("Stack is empty!");
            return;
        }
        status.setText("Top element: " + dynamicButtons.peek().getText());
    }

    @FXML
    private void displaypos(MouseEvent event) {
        pos.setText("X: " + (int) event.getX() + " Y: " + (int) event.getY());
    }
}