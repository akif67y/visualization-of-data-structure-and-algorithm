package com.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.application.Platform;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the Sorting Visualization UI
 * Handles user interactions and manages the visual representation of sorting algorithms
 */
public class SortingController implements Initializable {

    @FXML private HBox barsContainer;
    @FXML private ComboBox<String> algorithmComboBox;
    @FXML private Button sortButton;
    @FXML private Button randomizeButton;
    @FXML private Button resetButton;
    @FXML private Label statusLabel;

    private static final int NUM_BARS = 50;
    private static final int MAX_HEIGHT = 300;
    private static final int BAR_WIDTH = 12;
    private static final int BAR_SPACING = 2;

    private int[] dataArray;
    private List<Rectangle> bars;
    private boolean isSorting = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBox();
        initializeBars();
        randomizeArray();
        updateStatus("Ready to sort");
    }

    private void setupComboBox() {
        algorithmComboBox.getItems().addAll(
                "Selection Sort",
                "Bubble Sort",
                "Insertion Sort",
                "Merge Sort",
                "Quick Sort",
                "Heap Sort"
        );
        algorithmComboBox.setValue("Selection Sort");
    }

    private void initializeBars() {
        dataArray = new int[NUM_BARS];
        bars = new ArrayList<>();

        barsContainer.getChildren().clear();
        barsContainer.setSpacing(BAR_SPACING);

        for (int i = 0; i < NUM_BARS; i++) {
            Rectangle bar = new Rectangle();
            bar.setWidth(BAR_WIDTH);
            bar.setHeight(10); // Initial height
            bar.setFill(Color.LIGHTBLUE);
            bar.setStroke(Color.DARKBLUE);
            bar.setStrokeWidth(0.5);

            // Position bars from bottom
            bar.setY(MAX_HEIGHT - bar.getHeight());

            bars.add(bar);
            barsContainer.getChildren().add(bar);
        }
    }

    @FXML
    private void handleRandomize() {
        if (isSorting) return;

        randomizeArray();
        updateBarsDisplay();
        updateStatus("Array randomized");
    }

    @FXML
    private void handleReset() {
        if (isSorting) return;

        // Reset colors
        for (Rectangle bar : bars) {
            bar.setFill(Color.LIGHTBLUE);
        }

        updateStatus("Array reset");
    }

    @FXML
    private void handleSort() {
        if (isSorting) return;

        String selectedAlgorithm = algorithmComboBox.getValue();
        if (selectedAlgorithm == null) {
            updateStatus("Please select an algorithm");
            return;
        }

        isSorting = true;
        sortButton.setDisable(true);
        randomizeButton.setDisable(true);
        resetButton.setDisable(true);
        algorithmComboBox.setDisable(true);

        updateStatus("Sorting with " + selectedAlgorithm + "...");

        // Reset bar colors before sorting
        Platform.runLater(() -> {
            for (Rectangle bar : bars) {
                bar.setFill(Color.LIGHTBLUE);
            }
        });

        CompletableFuture<Void> sortingFuture = null;

        switch (selectedAlgorithm) {
            case "Selection Sort":
                sortingFuture = SortingAlgorithms.selectionSort(dataArray.clone(), bars);
                break;
            case "Bubble Sort":
                sortingFuture = SortingAlgorithms.bubbleSort(dataArray.clone(), bars);
                break;
            case "Insertion Sort":
                sortingFuture = SortingAlgorithms.insertionSort(dataArray.clone(), bars);
                break;
            case "Merge Sort":
                sortingFuture = SortingAlgorithms.mergeSort(dataArray.clone(), bars);
                break;
            case "Quick Sort":
                sortingFuture = SortingAlgorithms.quickSort(dataArray.clone(), bars);
                break;
            case "Heap Sort":
                sortingFuture = SortingAlgorithms.heapSort(dataArray.clone(), bars);
                break;
            default:
                updateStatus("Unknown algorithm selected");
                enableControls();
                return;
        }

        if (sortingFuture != null) {
            sortingFuture.thenRun(() -> {
                Platform.runLater(() -> {
                    updateStatus("Sorting completed with " + selectedAlgorithm);
                    enableControls();
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    updateStatus("Error occurred during sorting");
                    enableControls();
                });
                throwable.printStackTrace();
                return null;
            });
        }
    }

    private void randomizeArray() {
        Random random = new Random();
        for (int i = 0; i < NUM_BARS; i++) {
            dataArray[i] = random.nextInt(75) + 5; // Height between 5 and 80
        }
    }

    private void updateBarsDisplay() {
        for (int i = 0; i < NUM_BARS; i++) {
            Rectangle bar = bars.get(i);
            double height = dataArray[i] * 4; // Scale factor
            bar.setHeight(height);
            bar.setFill(Color.LIGHTBLUE);
        }
    }

    private void enableControls() {
        isSorting = false;
        sortButton.setDisable(false);
        randomizeButton.setDisable(false);
        resetButton.setDisable(false);
        algorithmComboBox.setDisable(false);
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }
}