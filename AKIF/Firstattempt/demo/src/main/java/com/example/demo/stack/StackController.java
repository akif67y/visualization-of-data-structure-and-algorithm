package com.example.demo.stack;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Stack;

import static java.lang.Thread.sleep;

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
    private Text status;
    @FXML
    private Stack<Button> dynamicButtons = new Stack<>();

    // For linked list representation
    private java.util.List<Button> linkedListNodes = new ArrayList<>();
    private java.util.List<Line> connectionLines = new ArrayList<>();
    private java.util.List<Polygon> arrowHeads = new ArrayList<>();

    @FXML
    private void handleTextFieldKeyPressed(KeyEvent event) throws InterruptedException {
        if (event.getCode() == KeyCode.ENTER) {
            onAddClicked();
        }
    }
    @FXML
    private void displaypos(MouseEvent event) {
        status.setText("X: "+ event.getX() + " Y: "+ event.getY());
    }

    @FXML
    private void onAddClicked() throws InterruptedException {
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
        ParallelTransition parallelTransition = new ParallelTransition();
        for(Button nodes: linkedListNodes){
            TranslateTransition transition = new TranslateTransition();
            transition.setNode(nodes);
            transition.setDuration(Duration.millis(200));
            transition.setByX(80);
            parallelTransition.getChildren().add(transition);
        }
        for(Line nodes: connectionLines){
            TranslateTransition transition = new TranslateTransition();
            transition.setNode(nodes);
            transition.setDuration(Duration.millis(200));
            transition.setByX(80);
            parallelTransition.getChildren().add(transition);
        }
        for(Polygon nodes: arrowHeads){
            TranslateTransition transition = new TranslateTransition();
            transition.setNode(nodes);
            transition.setDuration(Duration.millis(200));
            transition.setByX(80);
            parallelTransition.getChildren().add(transition);
        }
        parallelTransition.setOnFinished(e -> {

            // Play animation
            addToLinkedListfront(label);


            // Clear input
            inputField.clear();
        });
        parallelTransition.play();
        scaleIn.play();
        fadeIn.play();

        // Add to linked list representation
        //delete the container of linked list;
//        linkedListContainer.getChildren().removeAll(linkedListNodes);
//        linkedListContainer.getChildren().removeAll(connectionLines);
//        linkedListContainer.getChildren().removeAll(arrowHeads);
      //  int i = 1;
//        for(Button nodes : linkedListNodes) {
//            addToLinkedListbuttons(nodes,  i, 0);
//            i++;
//        }



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


    private void addToLinkedListfront(String value) {
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
        double nodeX =  70 ;
        double nodeY = 50;

        // Position the node
        nodeButton.setLayoutX(nodeX);
        nodeButton.setLayoutY(nodeY);

        // Add to linked list container
        linkedListContainer.getChildren().add(nodeButton);
        linkedListNodes.addFirst(nodeButton);

        // Hide NULL label if this is the first node

        nullLabel.setVisible(false);


        // Create connection line and arrow if not the first node
        if (linkedListNodes.size() > 1) {
            // Get previous node position
            Button prevNode = linkedListNodes.get(0);

            // Create connection line
            Line connectionLine = new Line();
            connectionLine.setStartX(prevNode.getLayoutX() + 50);
            connectionLine.setStartY(prevNode.getLayoutY() + 15);
            connectionLine.setEndX(nodeX + 80);
            connectionLine.setEndY(prevNode.getLayoutY() + 15);
            connectionLine.setStroke(javafx.scene.paint.Color.BLACK);
            connectionLine.setStrokeWidth(2);


            // Create arrow head
            Polygon arrowHead = new Polygon();
            arrowHead.getPoints().addAll(new Double[]{
                    nodeX + 80 - 8.0, nodeY + 10.0,
                    nodeX + 80 - 8.0, nodeY + 20.0,
                    nodeX + 80 + 2, nodeY + 15.0
            });
            arrowHead.setFill(javafx.scene.paint.Color.BLACK);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), connectionLine);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), connectionLine);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            ScaleTransition scaleIn2 = new ScaleTransition(Duration.millis(200), arrowHead);
            scaleIn2.setFromX(0.8);
            scaleIn2.setFromY(0.8);
            scaleIn2.setToX(1.0);
            scaleIn2.setToY(1.0);

            FadeTransition fadeIn2 = new FadeTransition(Duration.millis(200), arrowHead);
            fadeIn2.setFromValue(0.0);
            fadeIn2.setToValue(1.0);

            // Add to container
            linkedListContainer.getChildren().addAll(connectionLine, arrowHead);
            connectionLines.addFirst(connectionLine);
            arrowHeads.addFirst(arrowHead);
            scaleIn.play();
            fadeIn.play();
            scaleIn2.play();
            fadeIn2.play();
        }

        // Add NULL pointer from the new node
     //   createNullPointer(nodeButton);

    }

    private void removeFromLinkedList() {
        if (linkedListNodes.isEmpty()) return;

        // Remove the last node (top of stack)
        Button lastNode = linkedListNodes.getFirst();
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), lastNode);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), lastNode);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        scaleOut.play();
        fadeOut.play();
        linkedListContainer.getChildren().remove(lastNode);
        linkedListNodes.removeFirst();

        // Remove connection line and arrow if exists
        if (!connectionLines.isEmpty()) {
            Line lastLine = connectionLines.getFirst();
            Polygon lastArrow = arrowHeads.getFirst();
            ScaleTransition scale1Out = new ScaleTransition(Duration.millis(150), lastLine);
            scale1Out.setFromX(1.0);
            scale1Out.setFromY(1.0);
            scale1Out.setToX(0.8);
            scale1Out.setToY(0.8);

            FadeTransition fade1Out = new FadeTransition(Duration.millis(150), lastLine);
            fade1Out.setFromValue(1.0);
            fade1Out.setToValue(0.0);
            ScaleTransition scale2Out = new ScaleTransition(Duration.millis(150),lastArrow);
            scale2Out.setFromX(1.0);
            scale2Out.setFromY(1.0);
            scale2Out.setToX(0.8);
            scale2Out.setToY(0.8);

            FadeTransition fade2Out = new FadeTransition(Duration.millis(150), lastArrow);
            fade2Out.setFromValue(1.0);
            fade2Out.setToValue(0.0);
            scale1Out.play();
            fade1Out.play();
            scale2Out.play();
            fade2Out.play();
            linkedListContainer.getChildren().removeAll(lastLine, lastArrow);
            connectionLines.removeFirst();
            arrowHeads.removeFirst();
        }

        ParallelTransition parallelTransition = new ParallelTransition();
        for(Button nodes: linkedListNodes){
            TranslateTransition transition = new TranslateTransition();
            transition.setNode(nodes);
            transition.setDuration(Duration.millis(200));
            transition.setByX(-80);
            parallelTransition.getChildren().add(transition);
        }
        for(Line nodes: connectionLines){
            TranslateTransition transition = new TranslateTransition();
            transition.setNode(nodes);
            transition.setDuration(Duration.millis(200));
            transition.setByX(-80);
            parallelTransition.getChildren().add(transition);
        }
        for(Polygon nodes: arrowHeads){
            TranslateTransition transition = new TranslateTransition();
            transition.setNode(nodes);
            transition.setDuration(Duration.millis(200));
            transition.setByX(-80);
            parallelTransition.getChildren().add(transition);
        }
        parallelTransition.setOnFinished(e -> {
            if (linkedListNodes.isEmpty()) {
           nullLabel.setVisible(true);
      }
        });
        parallelTransition.play();

    }



}