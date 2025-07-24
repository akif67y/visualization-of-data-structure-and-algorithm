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

public class InsertionSortController implements Initializable {

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
    private int currentI = 1; // Current element being inserted
    private int currentJ = 0; // Current position in sorted part for comparison
    private int key = 0; // Current element value being inserted
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
    private final Color POINTER_COLOR = Color.PURPLE;        // Purple for current key element

    // Insertion sort phases
    private enum Phase {
        SELECTING_KEY,   // Selecting the key element to insert
        FINDING_POSITION, // Finding the correct position for the key
        INSERTING        // Inserting the key at the correct position
    }
    private Phase currentPhase = Phase.SELECTING_KEY;

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
        statusLabel.setText("Insertion sort started...");

        currentI = 1;
        currentJ = 0;
        key = 0;
        arrayLength = array.length;
        currentPhase = Phase.SELECTING_KEY;
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
            currentI = 1;
            currentJ = 0;
            key = 0;
            arrayLength = array.length;
            currentPhase = Phase.SELECTING_KEY;
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

        currentI = 1;
        currentJ = 0;
        key = 0;
        currentPhase = Phase.SELECTING_KEY;

        parseAndVisualize();
        statusLabel.setText("Enter an array and click 'Start'");
        updateButtonStates();
    }

    private void runSortingStep() {
        if (!isSorting) return;

        if (currentI >= arrayLength) {
            finishSorting();
            return;
        }

        if (currentPhase == Phase.SELECTING_KEY) {
            // Select the key element
            key = array[currentI];
            currentJ = currentI - 1;
            statusLabel.setText("Pass " + currentI + ": Selecting key element " + key + " at index " + currentI);
            highlightKeyElement();
            currentPhase = Phase.FINDING_POSITION;

            if (!isPaused) {
                pause.setOnFinished(e -> runSortingStep());
                pause.playFromStart();
            }
            return;
        }

        if (currentPhase == Phase.FINDING_POSITION) {
            if (currentJ >= 0 && array[currentJ] > key) {
                // Need to shift element to the right
                performShift();
            } else {
                // Found correct position, insert key
                currentPhase = Phase.INSERTING;
                performInsertion();
            }
        }
    }

    private void performShift() {
        statusLabel.setText("Comparing key " + key + " with element " + array[currentJ] + " at index " + currentJ + ". Shifting right...");

        highlightComparison(currentJ, currentI);

        pause.setOnFinished(e -> {
            // Shift element to the right
            array[currentJ + 1] = array[currentJ];
            updateBarVisualization(currentJ + 1);

            statusLabel.setText("Shifted element " + array[currentJ + 1] + " to index " + (currentJ + 1));
            currentJ--;
            resetBarColors();

            if (!isPaused) {
                runSortingStep();
            }
        });
        pause.playFromStart();
    }

    private void performInsertion() {
        int insertPos = currentJ + 1;
        array[insertPos] = key;
        updateBarVisualization(insertPos);

        statusLabel.setText("Inserted key " + key + " at index " + insertPos + ". Position " + insertPos + " is now sorted.");

        highlightInsertion(insertPos);

        pause.setOnFinished(e -> {
            // Move to next element
            currentI++;
            currentJ = 0;
            currentPhase = Phase.SELECTING_KEY;
            resetBarColors();

            if (!isPaused) {
                runSortingStep();
            }
        });
        pause.playFromStart();
    }

    private void finishSorting() {
        // Set all bars to green since sorting is complete
        for (int i = 0; i < bars.size(); i++) {
            setSorted(i);
        }

        statusLabel.setText("Insertion sort completed!");

        isSorting = false;
        isPaused = false;
        isCompleted = true;
        updateButtonStates();
    }

    private void updateButtonStates() {
        startButton.setDisable(isSorting);
        resetButton.setDisable(!isSorting && currentI == 1 && !isCompleted);
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
            bar.setFill(i == 0 ? SORTED_COLOR : UNSORTED_COLOR); // First element is already sorted
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

    private void highlightKeyElement() {
        resetBarColors();
        // Highlight the key element being inserted
        if (currentI >= 0 && currentI < bars.size()) {
            bars.get(currentI).setFill(POINTER_COLOR);
            bars.get(currentI).setStrokeWidth(3);
        }
    }

    private void highlightComparison(int compareIndex, int keyIndex) {
        resetBarColors();
        // Highlight element being compared with red
        if (compareIndex >= 0 && compareIndex < bars.size()) {
            bars.get(compareIndex).setFill(COMPARING_COLOR);
            bars.get(compareIndex).setStrokeWidth(3);
        }
        // Highlight key element with purple
        if (keyIndex >= 0 && keyIndex < bars.size()) {
            bars.get(keyIndex).setFill(POINTER_COLOR);
            bars.get(keyIndex).setStrokeWidth(3);
        }
    }

    private void highlightInsertion(int insertIndex) {
        resetBarColors();
        // Highlight the position where key is being inserted
        if (insertIndex >= 0 && insertIndex < bars.size()) {
            bars.get(insertIndex).setFill(POINTER_COLOR);
            bars.get(insertIndex).setStrokeWidth(4);
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

        if (currentI >= arrayLength) {
            finishSorting();
            return;
        }

        if (currentPhase == Phase.SELECTING_KEY) {
            // Select the key element
            key = array[currentI];
            currentJ = currentI - 1;
            statusLabel.setText("Pass " + currentI + ": Selecting key element " + key + " at index " + currentI);
            highlightKeyElement();
            currentPhase = Phase.FINDING_POSITION;
            return;
        }

        if (currentPhase == Phase.FINDING_POSITION) {
            if (currentJ >= 0 && array[currentJ] > key) {
                if (!showingComparisonBar) {
                    // First click: show comparison
                    statusLabel.setText("Comparing key " + key + " with element " + array[currentJ] + " at index " + currentJ);
                    highlightComparison(currentJ, currentI);
                    showingComparisonBar = true;
                } else {
                    // Second click: perform shift
                    statusLabel.setText("Element " + array[currentJ] + " > key " + key + ". Shifting right...");
                    array[currentJ + 1] = array[currentJ];
                    updateBarVisualization(currentJ + 1);
                    currentJ--;
                    resetBarColors();
                    showingComparisonBar = false;
                }
            } else {
                // Found correct position, insert key
                currentPhase = Phase.INSERTING;
                int insertPos = currentJ + 1;
                array[insertPos] = key;
                updateBarVisualization(insertPos);

                statusLabel.setText("Inserted key " + key + " at index " + insertPos + ". Position " + insertPos + " is now sorted.");
                highlightInsertion(insertPos);

                // Move to next element
                currentI++;
                currentJ = 0;
                currentPhase = Phase.SELECTING_KEY;
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