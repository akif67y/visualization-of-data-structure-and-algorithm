package com.example.demo.queue;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class queuearray {
    @FXML private TextField inputField;
    @FXML private TextField sizeField;
    @FXML private Label frontLabel;
    @FXML private HBox queueView;
    @FXML private HBox arrayView;
    @FXML private Button enqueueButton;
    @FXML private Button dequeueButton;
    @FXML private Button frontButton;
    @FXML private HBox arrowLabelBox;
    @FXML private HBox indexLabelBox;

    private int[] queueArray;
    private int size;
    private int front = -1;
    private int rear = -1;

    // Style constants for a cleaner look and easier maintenance
    private static final String CELL_STYLE_EMPTY = "-fx-background-color: #ECEFF1; -fx-border-color: #CFD8DC; -fx-padding: 12; -fx-min-width: 90px; -fx-alignment: center; -fx-background-radius: 6; -fx-border-radius: 6; -fx-font-family: 'System Bold'; -fx-font-size: 14px;";
    private static final String CELL_STYLE_FILLED = "-fx-background-color: #3498db; -fx-border-color: #90A4AE; -fx-padding: 12; -fx-min-width: 90px; -fx-alignment: center; -fx-background-radius: 6; -fx-border-radius: 6; -fx-font-family: 'System Bold'; -fx-font-size: 14px; -fx-text-fill: #ffffff;fx-font-weight: bold;";
    private static final String LOGICAL_CELL_STYLE = "-fx-background-color: #68e36a; -fx-border-color: #66BB6A; -fx-padding: 12; -fx-min-width: 90px; -fx-alignment: center; -fx-background-radius: 6; -fx-border-radius: 6; -fx-font-family: 'System Bold'; -fx-font-size: 14px; -fx-text-fill: #ffffff; fx-font-weight: bold;";
    private static final String INDEX_LABEL_STYLE = "-fx-text-fill: #78909C; -fx-font-size: 12px;";
    private static final String POINTER_LABEL_STYLE = "-fx-font-weight: bold; -fx-font-size: 12px;";

    @FXML
    public void createQueue() {
        try {
            size = Integer.parseInt(sizeField.getText());
            if (size <= 0) {
                showAlert("Queue size must be positive.");
                return;
            }
            queueArray = new int[size];
            front = rear = -1;
            updateViews();
            enqueueButton.setDisable(false);
            dequeueButton.setDisable(false);
            frontButton.setDisable(false);
            frontLabel.setText("-");
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid integer for the size.");
        }
    }

    @FXML
    public void enqueue() {
        try {
            int val = Integer.parseInt(inputField.getText());
            if ((rear + 1) % size == front) {
                showAlert("Queue is full (Overflow).");
                return;
            }

            if (front == -1) front = 0;
            rear = (rear + 1) % size;
            queueArray[rear] = val;

            updateViews();
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid integer to enqueue.");
        }
        inputField.clear();
    }

    @FXML
    public void dequeue() {
        if (front == -1) {
            showAlert("Queue is empty (Underflow).");
            return;
        }

        if (front == rear) {
            front = rear = -1; // Queue becomes empty
        } else {
            front = (front + 1) % size;
        }

        updateViews();
    }

    @FXML
    public void showFront() {
        if (front == -1) {
            frontLabel.setText("-");
            showAlert("Queue is empty.");
        } else {
            frontLabel.setText(String.valueOf(queueArray[front]));
        }
    }

    private void updateViews() {
        queueView.getChildren().clear();
        arrayView.getChildren().clear();
        arrowLabelBox.getChildren().clear();
        indexLabelBox.getChildren().clear();

        // === Logical Queue View ===
        if (front != -1) {
            int i = front;
            while (true) {
                Label label = new Label(String.valueOf(queueArray[i]));
                label.setStyle(LOGICAL_CELL_STYLE);
                queueView.getChildren().add(label);
                if (i == rear) break;
                i = (i + 1) % size;
            }
        }

        // === Array, Pointers, and Index Views ===
        for (int i = 0; i < size; i++) {
            // Create the main array cell
            Label cell = new Label("-");
            cell.setStyle(CELL_STYLE_EMPTY);

            boolean isOccupied = (front != -1) && isInQueue(i);
            if (isOccupied) {
                cell.setText(String.valueOf(queueArray[i]));
                cell.setStyle(CELL_STYLE_FILLED);
            }
            arrayView.getChildren().add(cell);

            // Create the pointer label above the cell
            Label pointerLabel = new Label();
            pointerLabel.setAlignment(Pos.CENTER);
            pointerLabel.setStyle(POINTER_LABEL_STYLE);
            pointerLabel.prefWidthProperty().bind(cell.widthProperty());

            if (front != -1) {
                if (front == rear && front == i) {
                    pointerLabel.setText("FRONT / REAR ▼");
                    pointerLabel.setMinWidth(100);
                    pointerLabel.setMaxWidth(100);
                    pointerLabel.setAlignment(Pos.CENTER);

                    pointerLabel.setTextFill(Color.web("#AD1457"));
                } else if (i == front) {
                    pointerLabel.setText("FRONT ▼");
                    pointerLabel.setTextFill(Color.web("#1565C0"));
                } else if (i == rear) {
                    pointerLabel.setText("REAR ▼");
                    pointerLabel.setTextFill(Color.web("#C62828"));
                }
            }
            arrowLabelBox.getChildren().add(pointerLabel);

            // Create the index label below the cell
            Label indexLabel = new Label("[" + i + "]");
            indexLabel.setAlignment(Pos.CENTER);
            indexLabel.setStyle(INDEX_LABEL_STYLE);
            indexLabel.prefWidthProperty().bind(cell.widthProperty());
            indexLabelBox.getChildren().add(indexLabel);
        }

        // Update the front label without showing an alert
        if (front == -1) {
            frontLabel.setText("-");
        } else {
            frontLabel.setText(String.valueOf(queueArray[front]));
        }
    }

    private boolean isInQueue(int index) {
        if (front == -1) return false;

        // Non-circular case
        if (front <= rear) {
            return index >= front && index <= rear;
        }
        // Circular case (wrapped around)
        else {
            return index >= front || index <= rear;
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Queue Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}