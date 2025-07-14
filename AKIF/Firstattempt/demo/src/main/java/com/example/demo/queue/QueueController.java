package com.example.demo.queue;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

public class QueueController {
    @FXML private TextField inputField;
    @FXML private TextField sizeField;
    @FXML private Label frontLabel;
    @FXML private HBox queueView;
    @FXML private HBox arrayView;
    @FXML private Button enqueueButton;
    @FXML private Button dequeueButton;
    @FXML private Button frontButton;
    @FXML private Button createQueueButton;
    @FXML private HBox arrowLabelBox;

    private int[] queueArray;
    private int size;
    private int front = -1;
    private int rear = -1;

    @FXML
    public void createQueue() {
        try {
            size = Integer.parseInt(sizeField.getText());
            queueArray = new int[size];
            front = rear = -1;
            updateViews();
            enqueueButton.setDisable(false);
            dequeueButton.setDisable(false);
            frontButton.setDisable(false);
        } catch (NumberFormatException e) {
            showAlert("Enter a valid integer size.");
        }
    }

    @FXML
    public void enqueue() {
        try {
            int val = Integer.parseInt(inputField.getText());
            if ((rear + 1) % size == front) {
                showAlert("Queue is full.");
                return;
            }

            if (front == -1) front = 0;
            rear = (rear + 1) % size;
            queueArray[rear] = val;

            updateViews();
        } catch (NumberFormatException e) {
            showAlert("Enter a valid integer to enqueue.");
        }
        inputField.clear();
    }

    @FXML
    public void dequeue() {
        if (front == -1) {
            showAlert("Queue is empty.");
            return;
        }

        if (front == rear) {
            front = rear = -1; // only one element
        } else {
            front = (front + 1) % size;
        }

        updateViews();
        inputField.clear();
    }

    @FXML
    public void showFront() {
        if (front == -1) {
            frontLabel.setText("Front: null");
        } else {
            frontLabel.setText("Front: " + queueArray[front]);
        }
    }

    private void updateViews() {
        queueView.getChildren().clear();
        arrayView.getChildren().clear();
        arrowLabelBox.getChildren().clear(); // new label row above array

        // === Logical Queue View ===
        if (front != -1) {
            int i = front;
            while (true) {
                Label label = new Label(String.valueOf(queueArray[i]));
                label.setStyle("-fx-background-color: #c8e6c9; -fx-border-color: green; -fx-padding: 10; -fx-font-weight: bold;");
                queueView.getChildren().add(label);
                if (i == rear) break;
                i = (i + 1) % size;
            }
        }

        // === Array and Arrows ===
        for (int i = 0; i < size; i++) {
            // -- Build array cell --
            Label cell = new Label("null");
            cell.setStyle("-fx-border-color: gray; -fx-padding: 10; -fx-min-width: 50px; -fx-alignment: center;");

            if (front != -1 && isInQueue(i)) {
                cell.setText(String.valueOf(queueArray[i]));
                cell.setStyle("-fx-background-color: #e0f7fa; -fx-border-color: gray; -fx-padding: 10;");
            }

            arrayView.getChildren().add(cell);

            // -- Build arrow label above it --


               if(i != front && i!= rear){
                   VBox dummy = new VBox(2); // 2px spacing
                   dummy.prefWidthProperty().bind(cell.widthProperty());
                   arrowLabelBox.getChildren().add(dummy);
               }
               else{
                   VBox arrowStack = new VBox(2); // 2px spacing


                   Label roleLabel = new Label();

                   if (front == rear && front == i) {
                       roleLabel.setText("Front/Rear");
                   } else if (i == front) {
                       roleLabel.setText("Front");
                   } else if (i == rear) {
                       roleLabel.setText("Rear");
                   }

// Optional: draw a vertical line down to the array cell
                   Line connector = new Line(0, 0, 0, 30); // vertical line
                   connector.setStrokeWidth(2);
                   connector.setStyle("-fx-stroke: gray;");

// 3) little arrow head (using Polyline)
                   Polyline arrowHead = new Polyline(
                           -5.0, 0.0,   // left wing start
                           0.0,  5.0,  // tip
                           5.0,  0.0   // right wing end
                   );
                   arrowHead.setStrokeWidth(2);
                   arrowHead.setTranslateY(-2);
                   arrowHead.setStroke(Color.GRAY);
                   arrowHead.setFill(Color.GRAY);

// Build the VBox

                   arrowStack.setAlignment(Pos.TOP_CENTER);
                   arrowStack.getChildren().addAll(roleLabel, connector, arrowHead);

// bind its width to the cell (if you’re using binding from before)
                   arrowStack.prefWidthProperty().bind(cell.widthProperty());

// add to your arrowLabelBox
                   arrowLabelBox.getChildren().add(arrowStack);
               }

            }


        showFront();
    }

    private boolean isInQueue(int index) {
        if (front <= rear) {
            return index >= front && index <= rear;
        } else {
            return index >= front || index <= rear;
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}
