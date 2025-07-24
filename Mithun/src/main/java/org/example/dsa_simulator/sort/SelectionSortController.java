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

public class SelectionSortController implements Initializable {

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
    private boolean isCompleted = false;
    private int currentI = 0; // Current position to place minimum
    private int currentJ = 0; // Current position being checked
    private int minIndex = 0; // Index of current minimum found
    private int arrayLength = 0;

    private PauseTransition pause;
    private final double barWidth = 60;
    private final double barSpacing = 10;
    private final double maxBarHeight = 300;
    private final double baseY = 310; // Base line for bars
    private final Duration animationDuration = Duration.millis(1000);
    private boolean showingComparisonBar = false;

    // Color scheme - simplified to 4 colors
    private final Color UNSORTED_COLOR = Color.LIGHTBLUE;    // Light blue for unsorted elements
    private final Color SORTED_COLOR = Color.LIGHTGREEN;     // Light green for sorted elements
    private final Color COMPARING_COLOR = Color.RED;         // Red for element being compared
    private final Color POINTER_COLOR = Color.PURPLE;        // Purple for pointer going through array

    // Selection sort phases
    private enum Phase {
        FINDING_MIN,    // Currently finding minimum in unsorted part
        SWAPPING        // Swapping minimum to correct position
    }
    private Phase currentPhase = Phase.FINDING_MIN;

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
        isCompleted = false;
        statusLabel.setText("Selection sort started...");

        currentI = 0;
        currentJ = 0;
        minIndex = 0;
        arrayLength = array.length;
        currentPhase = Phase.FINDING_MIN;
        showingComparisonBar = false;

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
        if (isCompleted) return;

        if (isSorting && !isPaused) {
            pauseSort();
            return;
        }

        if (!isSorting) {
            if (!parseAndVisualize()) return;
            isSorting = true;
            isPaused = true;
            isCompleted = false;
            currentI = 0;
            currentJ = 0;
            minIndex = 0;
            arrayLength = array.length;
            currentPhase = Phase.FINDING_MIN;
            showingComparisonBar = false;
            updateButtonStates();
        }

        statusLabel.setText("Manual step...");
        performSingleStepManual();
    }

    @FXML
    private void resetSort() {
        isSorting = false;
        isPaused = false;
        isCompleted = false;
        pause.stop();
        showingComparisonBar = false;

        currentI = 0;
        currentJ = 0;
        minIndex = 0;
        currentPhase = Phase.FINDING_MIN;

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

        if (currentPhase == Phase.FINDING_MIN) {
            if (currentJ == currentI) {
                // Start of new pass - initialize minimum
                minIndex = currentI;
                currentJ = currentI + 1;
                statusLabel.setText("Pass " + (currentI + 1) + ": Finding minimum in unsorted part starting from index " + currentI);
                highlightCurrentPass();

                if (!isPaused) {
                    pause.setOnFinished(e -> runSortingStep());
                    pause.playFromStart();
                }
                return;
            }

            if (currentJ >= arrayLength) {
                // Found minimum for this pass, now swap
                currentPhase = Phase.SWAPPING;
                performSwap();
                return;
            }

            // Compare current element with minimum
            performComparison();
        }
    }

    private void performComparison() {
        statusLabel.setText("Comparing element at index " + currentJ + " (" + array[currentJ] + ") with current minimum at index " + minIndex + " (" + array[minIndex] + ")");

        highlightComparison(currentJ, minIndex);

        pause.setOnFinished(e -> {
            if (array[currentJ] < array[minIndex]) {
                minIndex = currentJ;
                statusLabel.setText("New minimum found at index " + minIndex + " (" + array[minIndex] + ")");
            } else {
                statusLabel.setText("Current minimum remains at index " + minIndex + " (" + array[minIndex] + ")");
            }

            currentJ++;
            resetBarColors();

            if (!isPaused) {
                runSortingStep();
            }
        });
        pause.playFromStart();
    }

    private void performSwap() {
        if (minIndex != currentI) {
            statusLabel.setText("Swapping minimum element " + array[minIndex] + " at index " + minIndex + " with element " + array[currentI] + " at index " + currentI);

            // Highlight bars being swapped
            highlightSwap(currentI, minIndex);

            pause.setOnFinished(e -> {
                // Perform the swap
                int temp = array[currentI];
                array[currentI] = array[minIndex];
                array[minIndex] = temp;

                // Update visualization
                updateBarVisualization(currentI);
                updateBarVisualization(minIndex);

                statusLabel.setText("Swap completed. Position " + currentI + " is now sorted.");

                // Move to next position
                currentI++;
                currentJ = currentI;
                currentPhase = Phase.FINDING_MIN;
                resetBarColors();

                if (!isPaused) {
                    runSortingStep();
                }
            });
            pause.playFromStart();
        } else {
            statusLabel.setText("Element " + array[currentI] + " at index " + currentI + " is already in correct position.");

            // Move to next position
            currentI++;
            currentJ = currentI;
            currentPhase = Phase.FINDING_MIN;
            resetBarColors();

            if (!isPaused) {
                pause.setOnFinished(e -> runSortingStep());
                pause.playFromStart();
            }
        }
    }

    private void finishSorting() {
        // Set all bars to green since sorting is complete
        for (int i = 0; i < bars.size(); i++) {
            setSorted(i);
        }

        statusLabel.setText("Selection sort completed!");

        isSorting = false;
        isPaused = false;
        isCompleted = true;
        updateButtonStates();
    }

    private void updateButtonStates() {
        startButton.setDisable(isSorting);
        resetButton.setDisable(!isSorting && currentI == 0 && !isCompleted);
        arrayInput.setDisable(isSorting);

        pauseButton.setDisable(!isSorting || isPaused || isCompleted);
        resumeButton.setDisable(!isSorting || !isPaused || isCompleted);
        nextStepButton.setDisable((isSorting && !isPaused) || isCompleted);
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
            bar.setFill(UNSORTED_COLOR);
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

    private void highlightCurrentPass() {
        resetBarColors();
        // Highlight the current minimum position with pointer color
        if (minIndex >= 0 && minIndex < bars.size()) {
            bars.get(minIndex).setFill(POINTER_COLOR);
            bars.get(minIndex).setStrokeWidth(3);
        }
    }

    private void highlightComparison(int compareIndex, int minIndex) {
        resetBarColors();
        // Highlight element being compared with red
        if (compareIndex >= 0 && compareIndex < bars.size()) {
            bars.get(compareIndex).setFill(COMPARING_COLOR);
            bars.get(compareIndex).setStrokeWidth(3);
        }
        // Highlight current minimum with purple (pointer)
        if (minIndex >= 0 && minIndex < bars.size()) {
            bars.get(minIndex).setFill(POINTER_COLOR);
            bars.get(minIndex).setStrokeWidth(3);
        }
    }

    private void highlightSwap(int index1, int index2) {
        resetBarColors();
        // During swap, show one element as comparing (red) and one as pointer (purple)
        if (index1 >= 0 && index1 < bars.size()) {
            bars.get(index1).setFill(COMPARING_COLOR);
            bars.get(index1).setStrokeWidth(4);
        }
        if (index2 >= 0 && index2 < bars.size()) {
            bars.get(index2).setFill(POINTER_COLOR);
            bars.get(index2).setStrokeWidth(4);
        }
    }

    private void resetBarColors() {
        for (int i = 0; i < bars.size(); i++) {
            if (i < currentI) {
                // Sorted part - light green
                bars.get(i).setFill(SORTED_COLOR);
                bars.get(i).setStrokeWidth(2);
            } else {
                // Unsorted part - light blue
                bars.get(i).setFill(UNSORTED_COLOR);
                bars.get(i).setStrokeWidth(2);
            }
        }
    }

    private void setSorted(int index) {
        if (index >= 0 && index < bars.size()) {
            bars.get(index).setFill(SORTED_COLOR);
        }
    }

    private void performSingleStepManual() {
        if (!isSorting) return;

        if (currentI >= arrayLength - 1) {
            finishSorting();
            return;
        }

        if (currentPhase == Phase.FINDING_MIN) {
            if (currentJ == currentI) {
                // Start of new pass
                minIndex = currentI;
                currentJ = currentI + 1;
                statusLabel.setText("Pass " + (currentI + 1) + ": Finding minimum in unsorted part starting from index " + currentI);
                highlightCurrentPass();
                return;
            }

            if (currentJ >= arrayLength) {
                // Found minimum for this pass, now swap
                currentPhase = Phase.SWAPPING;
                performSwap();
                return;
            }

            if (!showingComparisonBar) {
                // First click: show comparison
                statusLabel.setText("Comparing element at index " + currentJ + " (" + array[currentJ] + ") with current minimum at index " + minIndex + " (" + array[minIndex] + ")");
                highlightComparison(currentJ, minIndex);
                showingComparisonBar = true;
            } else {
                // Second click: perform comparison
                if (array[currentJ] < array[minIndex]) {
                    minIndex = currentJ;
                    statusLabel.setText("New minimum found at index " + minIndex + " (" + array[minIndex] + ")");
                } else {
                    statusLabel.setText("Current minimum remains at index " + minIndex + " (" + array[minIndex] + ")");
                }

                currentJ++;
                resetBarColors();
                showingComparisonBar = false;
            }
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