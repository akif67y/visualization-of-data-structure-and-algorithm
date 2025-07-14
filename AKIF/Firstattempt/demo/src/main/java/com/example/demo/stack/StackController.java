package com.example.demo.stack;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Stack;

public class StackController {

    @FXML
    private VBox buttonContainer;
    @FXML
    private TextField inputField;
    @FXML
    private AnchorPane linkedListContainer;
    @FXML
    private Label nullLabel;
    @FXML
    private Stack<Button> dynamicButtons = new Stack<>();

    // For linked list representation
    private java.util.List<Button> linkedListNodes = new ArrayList<>();
    private java.util.List<Line> connectionLines = new ArrayList<>();
    private java.util.List<Polygon> arrowHeads = new ArrayList<>();

    @FXML
    private void handleTextFieldKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onAddClicked();
        }
    }

    @FXML
    private void onAddClicked() {
        String label = inputField.getText().trim();
        if (label.isEmpty()) return;

        // Create a beautifully styled button
        Button btn = new Button(label);

        // Apply beautiful styling
        btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ff7b7b, #ff5252);" +
                        "-fx-background-radius: 12;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 15 25;" +
                        "-fx-min-width: 200;" +
                        "-fx-max-width: 200;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        );

        // Add hover effects
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #ff9999, #ff7777);" +
                            "-fx-background-radius: 12;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 15 25;" +
                            "-fx-min-width: 200;" +
                            "-fx-max-width: 200;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 6);" +
                            "-fx-cursor: hand;"
            );
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #ff7b7b, #ff5252);" +
                            "-fx-background-radius: 12;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 15 25;" +
                            "-fx-min-width: 200;" +
                            "-fx-max-width: 200;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);" +
                            "-fx-cursor: hand;"
            );
        });

        // Add push animation
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), btn);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), btn);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Add to stack and container
        dynamicButtons.push(btn);
        buttonContainer.getChildren().add(0, btn);

        // Add to linked list representation
        addToLinkedList(label);

        // Play animations
        scaleIn.play();
        fadeIn.play();

        // Clear input
        inputField.clear();
    }

    @FXML
    private void poppedCalled() {
        if (dynamicButtons.isEmpty()) return;

        Button topButton = dynamicButtons.peek();

        // Add pop animation
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), topButton);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), topButton);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Remove after animation completes
        fadeOut.setOnFinished(e -> {
            buttonContainer.getChildren().remove(topButton);
            dynamicButtons.pop();
            removeFromLinkedList();
        });

        // Play animations
        scaleOut.play();
        fadeOut.play();

        System.out.println("Button removed: " + topButton.getText());
    }

    private void addToLinkedList(String value) {
        // Create new node button
        Button nodeButton = new Button(value);
        nodeButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049);" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 12;" +
                        "-fx-min-width: 50;" +
                        "-fx-max-width: 50;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );

        // Position calculation
        double nodeX = 70 + (linkedListNodes.size() * 80);
        double nodeY = 50;

        // Position the node
        nodeButton.setLayoutX(nodeX);
        nodeButton.setLayoutY(nodeY);

        // Add to linked list container
        linkedListContainer.getChildren().add(nodeButton);
        linkedListNodes.add(nodeButton);

        // Hide NULL label if this is the first node
        if (linkedListNodes.size() == 1) {
            nullLabel.setVisible(false);
        }

        // Create connection line and arrow if not the first node
        if (linkedListNodes.size() > 1) {
            // Get previous node position
            Button prevNode = linkedListNodes.get(linkedListNodes.size() - 2);

            // Create connection line
            Line connectionLine = new Line();
            connectionLine.setStartX(prevNode.getLayoutX() + 50);
            connectionLine.setStartY(prevNode.getLayoutY() + 15);
            connectionLine.setEndX(nodeX);
            connectionLine.setEndY(nodeY + 15);
            connectionLine.setStroke(javafx.scene.paint.Color.WHITE);
            connectionLine.setStrokeWidth(2);

            // Create arrow head
            Polygon arrowHead = new Polygon();
            arrowHead.getPoints().addAll(new Double[]{
                    nodeX - 8.0, nodeY + 10.0,
                    nodeX - 8.0, nodeY + 20.0,
                    nodeX, nodeY + 15.0
            });
            arrowHead.setFill(javafx.scene.paint.Color.WHITE);

            // Add to container
            linkedListContainer.getChildren().addAll(connectionLine, arrowHead);
            connectionLines.add(connectionLine);
            arrowHeads.add(arrowHead);
        }

        // Add NULL pointer from the new node
        createNullPointer(nodeButton);
    }

    private void removeFromLinkedList() {
        if (linkedListNodes.isEmpty()) return;

        // Remove the last node (top of stack)
        Button lastNode = linkedListNodes.get(linkedListNodes.size() - 1);
        linkedListContainer.getChildren().remove(lastNode);
        linkedListNodes.remove(lastNode);

        // Remove connection line and arrow if exists
        if (!connectionLines.isEmpty()) {
            Line lastLine = connectionLines.get(connectionLines.size() - 1);
            Polygon lastArrow = arrowHeads.get(arrowHeads.size() - 1);
            linkedListContainer.getChildren().removeAll(lastLine, lastArrow);
            connectionLines.remove(lastLine);
            arrowHeads.remove(lastArrow);
        }

        // Show NULL label if no nodes left
        if (linkedListNodes.isEmpty()) {
            nullLabel.setVisible(true);
        } else {
            // Update NULL pointer for the new last node
            Button newLastNode = linkedListNodes.get(linkedListNodes.size() - 1);
            createNullPointer(newLastNode);
        }
    }

    private void createNullPointer(Button node) {
        // Remove existing NULL pointers
        linkedListContainer.getChildren().removeIf(child ->
                child instanceof Line && ((Line)child).getStroke() == javafx.scene.paint.Color.GRAY ||
                        child instanceof Polygon && ((Polygon)child).getFill() == javafx.scene.paint.Color.GRAY
        );

        // Create NULL pointer line
        Line nullLine = new Line();
        nullLine.setStartX(node.getLayoutX() + 50);
        nullLine.setStartY(node.getLayoutY() + 15);
        nullLine.setEndX(node.getLayoutX() + 80);
        nullLine.setEndY(node.getLayoutY() + 15);
        nullLine.setStroke(javafx.scene.paint.Color.GRAY);
        nullLine.setStrokeWidth(2);
        nullLine.getStrokeDashArray().addAll(5.0, 5.0);

        // Create NULL arrow
        Polygon nullArrow = new Polygon();
        nullArrow.getPoints().addAll(new Double[]{
                node.getLayoutX() + 72.0, node.getLayoutY() + 10.0,
                node.getLayoutX() + 72.0, node.getLayoutY() + 20.0,
                node.getLayoutX() + 80.0, node.getLayoutY() + 15.0
        });
        nullArrow.setFill(javafx.scene.paint.Color.GRAY);

        linkedListContainer.getChildren().addAll(nullLine, nullArrow);
    }
}