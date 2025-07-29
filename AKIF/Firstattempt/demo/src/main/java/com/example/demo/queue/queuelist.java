package com.example.demo.queue;


import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;

public class queuelist {

    @FXML private Text status;
    @FXML private HBox buttonContainer;
    @FXML private TextField inputField;
    @FXML private AnchorPane linkedListContainer;
    @FXML private Label nullLabel;

    // Queue to hold Buttons for visual push/pop
    private Queue<Button> dynamicButtons = new LinkedList<>();

    // Linked-list representation
    private List<Button> linkedListNodes = new ArrayList<>();
    private List<Line> connectionLines = new ArrayList<>();
    private List<Polygon> arrowHeads = new ArrayList<>();

    @FXML
    private void handleTextFieldKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEnqueueClicked();
        }
    }

//    @FXML
//    private void displaypos(MouseEvent event) {
//        status.setText("X: " + event.getX() + " Y: " + event.getY());
//    }

    @FXML
    private void onEnqueueClicked() {
        String label = inputField.getText().trim();
        if (label.isEmpty()) return;

        // Create styled button
        Button btn = new Button(label);
        btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2196F3, #1976D2);" +
                        "-fx-background-radius: 12;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 15 25;" +
                        "-fx-min-width: 70;" +
                        "-fx-max-width: 70;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        );
        // Hover effects
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #64B5F6, #42A5F5);" +
                        "-fx-background-radius: 12;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 15 25;" +
                        "-fx-min-width: 70;" +
                        "-fx-max-width: 70;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 6);" +
                        "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2196F3, #1976D2);" +
                        "-fx-background-radius: 12;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 15 25;" +
                        "-fx-min-width: 70;" +
                        "-fx-max-width: 70;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        ));

        // Entrance animations
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), btn);
        scaleIn.setFromX(0.8); scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0); scaleIn.setToY(1.0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), btn);
        fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);

        dynamicButtons.add(btn);
        buttonContainer.getChildren().add(btn);
        scaleIn.play(); fadeIn.play();

        // Linked list visualization
        addToLinkedListEnd(label);
        inputField.clear();
    }

    @FXML
    private void onDequeueClicked() {
        if (dynamicButtons.isEmpty()) return;
        Button frontBtn = dynamicButtons.remove();

        // Exit animations
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), frontBtn);
        scaleOut.setFromX(1.0); scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8); scaleOut.setToY(0.8);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), frontBtn);
        fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            buttonContainer.getChildren().remove(frontBtn);
            removeFromLinkedListFront();
        });
        scaleOut.play(); fadeOut.play();
    }

    private void addToLinkedListEnd(String value) {
        Button node = new Button(value);
        node.setStyle(
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
        double nodeX = 70 + linkedListNodes.size() * 80;
        double nodeY = 50;
        node.setLayoutX(nodeX);
        node.setLayoutY(nodeY);
        linkedListContainer.getChildren().add(node);
        linkedListNodes.add(node);
        nullLabel.setVisible(false);

        if (linkedListNodes.size() > 1) {
            Button prev = linkedListNodes.get(linkedListNodes.size()-2);
            Line line = new Line(prev.getLayoutX()+50, prev.getLayoutY()+15, nodeX, nodeY+15);
            line.setStroke(Color.BLACK); line.setStrokeWidth(2);
            Polygon arrow = new Polygon(nodeX-8, nodeY+10, nodeX-8, nodeY+20, nodeX+2, nodeY+15);
            arrow.setFill(Color.BLACK);
            linkedListContainer.getChildren().addAll(line, arrow);
            connectionLines.add(line);
            arrowHeads.add(arrow);
            new FadeTransition(Duration.millis(200), line).play();
            new FadeTransition(Duration.millis(200), arrow).play();
        }
    }

    private void removeFromLinkedListFront() {
        if (linkedListNodes.isEmpty()) { nullLabel.setVisible(true); return; }
        Button first = linkedListNodes.remove(0);
        linkedListContainer.getChildren().remove(first);
        if (!connectionLines.isEmpty()) {
            Line ln = connectionLines.remove(0);
            Polygon ar = arrowHeads.remove(0);
            linkedListContainer.getChildren().removeAll(ln, ar);
        }
        // shift remaining nodes left
        for (Button b : linkedListNodes) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), b);
            tt.setByX(-80); tt.play();
        }
        for (Line l : connectionLines) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), l);
            tt.setByX(-80); tt.play();
        }
        for (Polygon p : arrowHeads) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), p);
            tt.setByX(-80); tt.play();
        }
    }
}