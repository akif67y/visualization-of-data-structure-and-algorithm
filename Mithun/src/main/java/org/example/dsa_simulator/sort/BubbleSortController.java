package org.example.dsa_simulator.sort;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BubbleSortController implements Initializable {

    // FXML Components
    @FXML private TextField arrayInput;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button resumeButton;
    @FXML private Button nextStepButton;
    @FXML private Button resetButton;
    @FXML private Pane arrayContainer;
    @FXML private Label statusLabel;

    // Sorting and Visualization State
    private int[] array;
    private List<Rectangle> bars;
    private List<Text> valueTexts;
    private List<Text> indexTexts;

    private boolean isSorting = false;
    private boolean isPaused = false;
    private boolean isCompleted = false; // Add this flag to track completion
    private int currentI = 0;
    private int currentJ = 0;
    private int arrayLength = 0;

    private PauseTransition pause;
    private final double barWidth = 60;
    private final double barSpacing = 10;
    private final double maxBarHeight = 300;
    private final double baseY = 310; // Base line for bars
    private final Duration animationDuration = Duration.millis(1000);
    private boolean showingPurpleBar = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        arrayInput.setText("64,34,25,12,22,11,90");
        pause = new PauseTransition(animationDuration);

        parseAndVisualize();
        updateButtonStates();
    }

    @FXML
    private void startSort() {
        if (isSorting) return;

        if (!parseAndVisualize()) return;

        isSorting = true;
        isPaused = false;
        isCompleted = false; // Reset completion flag
        statusLabel.setText("Sorting started...");

        currentI = 0;
        currentJ = 0;
        arrayLength = array.length;
        showingPurpleBar = false;

        updateButtonStates();
        runSortingStep();
    }

    @FXML
    private void pauseSort() {
        if (!isSorting || isPaused) return;
        isPaused = true;
        pause.pause();
        statusLabel.setText("Sorting paused. Press 'Resume' or 'Next Step'.");
        updateButtonStates();
    }

    @FXML
    private void resumeSort() {
        if (!isSorting || !isPaused) return;
        isPaused = false;
        statusLabel.setText("Resuming sort...");
        updateButtonStates();
        pause.play();
    }

    @FXML
    private void doNextStep() {
        // Don't allow next step if sorting is completed
        if (isCompleted) return;

        if (isSorting && !isPaused) {
            pauseSort();
            return;
        }

        if (!isSorting) {
            if (!parseAndVisualize()) return;
            isSorting = true;
            isPaused = true;
            isCompleted = false; // Reset completion flag
            currentI = 0;
            currentJ = 0;
            arrayLength = array.length;
            showingPurpleBar = false;
            updateButtonStates();
        }

        statusLabel.setText("Manual step...");
        performSingleStepManual();
    }

    @FXML
    private void resetSort() {
        isSorting = false;
        isPaused = false;
        isCompleted = false; // Reset completion flag
        pause.stop();
        showingPurpleBar = false;

        currentI = 0;
        currentJ = 0;

        parseAndVisualize();
        statusLabel.setText("Enter an array and click 'Start'");
        updateButtonStates();
    }

    private void runSortingStep() {
        if (!isSorting) return;

        if (currentI >= arrayLength - 1) {
            finishSorting();
            return;
        }

        if (currentJ >= arrayLength - currentI - 1) {
            setSorted(arrayLength - currentI - 1);
            currentI++;
            currentJ = 0;
            resetBarColors();
            showingPurpleBar = false;

            if (!isPaused) {
                pause.setOnFinished(e -> runSortingStep());
                pause.playFromStart();
            }
            return;
        }

        performSingleStep();
    }

    private void performSingleStep() {
        statusLabel.setText("Comparing elements at index " + currentJ + " and " + (currentJ + 1));

        // Highlight bars being compared
        highlightBars(currentJ, currentJ + 1);

        pause.setOnFinished(e -> {
            compareAndSwap();
            resetBarColors();
            currentJ++;
            if (!isPaused) {
                runSortingStep();
            }
        });
        pause.playFromStart();
    }

    private void compareAndSwap() {
        if (array[currentJ] > array[currentJ + 1]) {
            statusLabel.setText("Swapping " + array[currentJ] + " and " + array[currentJ + 1]);

            // Swap in the array
            int temp = array[currentJ];
            array[currentJ] = array[currentJ + 1];
            array[currentJ + 1] = temp;

            // Update the visualization
            updateBarVisualization(currentJ);
            updateBarVisualization(currentJ + 1);
        } else {
            statusLabel.setText("No swap needed.");
        }
    }

    private void finishSorting() {
        // Set ALL bars to green since sorting is complete
        for (int i = 0; i < bars.size(); i++) {
            setSorted(i);
        }

        statusLabel.setText("Sorting completed!");

        isSorting = false;
        isPaused = false;
        isCompleted = true; // Mark as completed
        updateButtonStates();
    }

    private void updateButtonStates() {
        startButton.setDisable(isSorting);
        resetButton.setDisable(!isSorting && currentI == 0 && !isCompleted);
        arrayInput.setDisable(isSorting);

        pauseButton.setDisable(!isSorting || isPaused || isCompleted);
        resumeButton.setDisable(!isSorting || !isPaused || isCompleted);
        nextStepButton.setDisable((isSorting && !isPaused) || isCompleted); // Disable when completed
    }

    private boolean parseAndVisualize() {
        try {
            String input = arrayInput.getText().trim();
            if (input.isEmpty()) {
                array = new int[]{64, 34, 25, 12, 22, 11, 90};
            } else {
                String[] parts = input.split(",");
                array = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    array[i] = Integer.parseInt(parts[i].trim());
                }
            }
            createVisualization();
            return true;
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid, comma-separated numbers.");
            return false;
        }
    }

    private void createVisualization() {
        arrayContainer.getChildren().clear();
        bars = new ArrayList<>();
        valueTexts = new ArrayList<>();
        indexTexts = new ArrayList<>();

        if (array == null) return;

        // Find max value for scaling
        int maxValue = array[0];
        for (int value : array) {
            if (value > maxValue) maxValue = value;
        }

        double totalWidth = array.length * barWidth + (array.length - 1) * barSpacing;
        double startX = (arrayContainer.getPrefWidth() - totalWidth) / 2;

        for (int i = 0; i < array.length; i++) {
            double x = startX + i * (barWidth + barSpacing);
            double barHeight = (double) array[i] / maxValue * maxBarHeight;
            double y = baseY - barHeight;

            // Create bar
            Rectangle bar = new Rectangle(x, y, barWidth, barHeight);
            bar.setFill(Color.LIGHTBLUE);
            bar.setStroke(Color.BLACK);
            bar.setStrokeWidth(2);

            // Create value text (at bottom of bar)
            Text valueText = new Text(String.valueOf(array[i]));
            valueText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            valueText.setX(x + barWidth / 2 - valueText.getBoundsInLocal().getWidth() / 2);
            valueText.setY(baseY - 5);

            // Create index text (below value text)
            Text indexText = new Text(String.valueOf(i));
            indexText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            indexText.setFill(Color.GRAY);
            indexText.setX(x + barWidth / 2 - indexText.getBoundsInLocal().getWidth() / 2);
            indexText.setY(baseY + 20);

            arrayContainer.getChildren().addAll(bar, valueText, indexText);
            bars.add(bar);
            valueTexts.add(valueText);
            indexTexts.add(indexText);
        }
    }

    private void updateBarVisualization(int index) {
        if (index >= 0 && index < bars.size()) {
            // Find max value for scaling
            int maxValue = array[0];
            for (int value : array) {
                if (value > maxValue) maxValue = value;
            }

            Rectangle bar = bars.get(index);
            Text valueText = valueTexts.get(index);

            // Update bar height
            double barHeight = (double) array[index] / maxValue * maxBarHeight;
            bar.setHeight(barHeight);
            bar.setY(baseY - barHeight);

            // Update value text
            valueText.setText(String.valueOf(array[index]));
            valueText.setX(bar.getX() + barWidth / 2 - valueText.getBoundsInLocal().getWidth() / 2);
        }
    }

    private void highlightBars(int index1, int index2) {
        resetBarColors();
        if (index1 >= 0 && index1 < bars.size()) {
            bars.get(index1).setFill(Color.RED);
        }
        if (index2 >= 0 && index2 < bars.size()) {
            bars.get(index2).setFill(Color.PURPLE);
        }
    }

    private void resetBarColors() {
        for (int i = 0; i < bars.size(); i++) {
            // Check if this bar is in sorted position
            if (i >= arrayLength - currentI) {
                bars.get(i).setFill(Color.LIGHTGREEN);
            } else {
                bars.get(i).setFill(Color.LIGHTBLUE);
            }
        }
    }

    private void setSorted(int index) {
        if (index >= 0 && index < bars.size()) {
            bars.get(index).setFill(Color.LIGHTGREEN);
        }
    }

    private void performSingleStepManual() {
        if (!isSorting) return;

        if (currentI >= arrayLength - 1) {
            finishSorting();
            return;
        }

        if (currentJ >= arrayLength - currentI - 1) {
            setSorted(arrayLength - currentI - 1);
            currentI++;
            currentJ = 0;
            resetBarColors();
            showingPurpleBar = false;
            return;
        }

        if (!showingPurpleBar) {
            // First click: highlight bars being compared
            statusLabel.setText("Comparing elements at index " + currentJ + " and " + (currentJ + 1));
            highlightBars(currentJ, currentJ + 1);
            showingPurpleBar = true;
        } else {
            // Second click: perform comparison and move to next
            compareAndSwap();
            resetBarColors();
            currentJ++;
            showingPurpleBar = false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}