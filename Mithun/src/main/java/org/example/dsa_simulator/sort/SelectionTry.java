package org.example.dsa_simulator.sort;

import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class SelectionTry implements Initializable {

    // FXML Components
    @FXML private TextField arrayInput;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button resumeButton;
    @FXML private Button nextStepButton;
    @FXML private Button resetButton;
    @FXML private Pane arrayContainer;
    @FXML private Label statusLabel;
    @FXML private ListView<String> pseudoCodeList;

    @FXML private Button backButton;
    @FXML private Slider speedSlider;


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
    private final double maxBarHeight = 500;
    private final double baseY = 550; // Base line for bars
    private  Duration animationDuration = Duration.millis(1000);


    // Color scheme - simplified to 4 colors
    private final Color UNSORTED_COLOR = Color.LIGHTBLUE;    // Light blue for unsorted elements
    private final Color SORTED_COLOR = Color.LIGHTGREEN;     // Light green for sorted elements
    private final Color COMPARING_COLOR = Color.RED;         // Red for element being compared
    private final Color POINTER_COLOR = Color.PURPLE;        // Purple for pointer going through array

    // Selection sort phases
    private enum Phase {
        FINDING_MIN,
        SWAPPING
    }
    private Phase currentPhase = Phase.FINDING_MIN;

    private final String[] selectionSortPseudoCode = {
            "procedure selectionSort(A : list of sortable items)",
            "   n = length(A)",
            "   for i from 0 to n-2",
            "      minIndex = i",
            "      for j from i+1 to n-1",
            "         if A[j] < A[minIndex]",
            "            minIndex = j",
            "      end if",
            "   end for",
            "   swap(A[i], A[minIndex])",
            "  end for",
            "end procedure"
    };
    private void highlightPseudoCode(int lineNumber) {
        if (lineNumber >= 0 && lineNumber < pseudoCodeList.getItems().size()) {

            pseudoCodeList.getSelectionModel().select(lineNumber);

            pseudoCodeList.scrollTo(lineNumber);
        } else {

            pseudoCodeList.getSelectionModel().clearSelection();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        arrayInput.setText("64,34,25,12,22,11,90");
        pause = new PauseTransition(animationDuration);
        speedSlider.setMin(1);
        speedSlider.setMax(100);
        speedSlider.setValue(50); // Start in the middle


        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {

            double maxDuration = 2000.0; // Slowest
            double minDuration = 50.0;   // Fastest


            double newDurationMillis = maxDuration - (newVal.doubleValue() / speedSlider.getMax()) * (maxDuration - minDuration);


            animationDuration = Duration.millis(newDurationMillis);


            pause.setDuration(animationDuration);
        });
        arrayContainer.widthProperty().addListener((obs, oldVal, newVal) -> parseAndVisualize());
        arrayContainer.heightProperty().addListener((obs, oldVal, newVal) -> parseAndVisualize());
       pseudoCodeList.getItems().addAll(selectionSortPseudoCode);
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

        // This handles the very first click on "Next Step"
        if (!isSorting) {
            if (!parseAndVisualize()) return;
            isSorting = true;
            isPaused = true;
            currentI = 0;
            currentJ = 0;
            minIndex = 0;
            arrayLength = array.length;
            currentPhase = Phase.FINDING_MIN;
            updateButtonStates();

            // Start the first pass
            minIndex = currentI;
            currentJ = currentI + 1;
            statusLabel.setText("Pass " + (currentI + 1) + ": Finding minimum in unsorted part.");
            highlightCurrentPass();
            return;
        }

        // If the last step was setting up for a swap, this click executes it
        if (currentPhase == Phase.SWAPPING) {
            if (minIndex != currentI) {
                statusLabel.setText("Swapping elements at index " + currentI + " and " + minIndex + ".");
                // Perform data swap and update visuals instantly for manual mode
                int temp = array[currentI];
                array[currentI] = array[minIndex];
                array[minIndex] = temp;
                updateBarVisualization(currentI);
                updateBarVisualization(minIndex);
            } else {
                statusLabel.setText("Element is already in the correct sorted position.");
            }

            // Move to the next outer loop pass
            currentI++;
            if (currentI >= arrayLength - 1) {
                finishSorting();
                return;
            }

            currentJ = currentI;
            currentPhase = Phase.FINDING_MIN;
            minIndex = currentI; // Reset minIndex for the new pass
            currentJ = currentI + 1;
            statusLabel.setText("Pass " + (currentI + 1) + ": Finding minimum.");
            highlightCurrentPass();
            return;
        }

        // --- We are in the FINDING_MIN phase ---

        // Check if the inner loop (j) has finished for this pass
        if (currentJ >= arrayLength) {
            // Set up for the swap on the *next* click
            currentPhase = Phase.SWAPPING;
            statusLabel.setText("Minimum found at index " + minIndex + ". Click 'Next Step' to swap.");
            highlightSwap(currentI, minIndex);
        } else {
            // This is a normal comparison step
            statusLabel.setText("Comparing element at index " + currentJ + " with minimum at index " + minIndex + ".");
            highlightComparison(currentJ, minIndex);

            // Perform the comparison logic
            if (array[currentJ] < array[minIndex]) {
                minIndex = currentJ;
            }

            // Advance j for the next step
            currentJ++;
        }
    }

    @FXML
    private void resetSort() {
        isSorting = false;
        isPaused = false;
        isCompleted = false;
        pause.stop();
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
                highlightPseudoCode(2);

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
        highlightPseudoCode(5);

        pause.setOnFinished(e -> {
            if (array[currentJ] < array[minIndex]) {
                minIndex = currentJ;
                highlightPseudoCode(6);
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
            highlightSwap(currentI, minIndex);
            highlightPseudoCode(9);

            // Get the nodes to be swapped
            Rectangle bar1 = bars.get(currentI);
            Rectangle bar2 = bars.get(minIndex);
            Text valueText1 = valueTexts.get(currentI);
            Text valueText2 = valueTexts.get(minIndex);
            Text indexText1 = indexTexts.get(currentI);
            Text indexText2 = indexTexts.get(minIndex);

            // Calculate distance to move
            double deltaX = bar2.getX() - bar1.getX();

            // Create transitions for all nodes
            TranslateTransition ttBar1 = new TranslateTransition(animationDuration, bar1);
            ttBar1.setByX(deltaX);
            TranslateTransition ttBar2 = new TranslateTransition(animationDuration, bar2);
            ttBar2.setByX(-deltaX);

            TranslateTransition ttValue1 = new TranslateTransition(animationDuration, valueText1);
            ttValue1.setByX(deltaX);
            TranslateTransition ttValue2 = new TranslateTransition(animationDuration, valueText2);
            ttValue2.setByX(-deltaX);

            TranslateTransition ttIndex1 = new TranslateTransition(animationDuration, indexText1);
            ttIndex1.setByX(deltaX);
            TranslateTransition ttIndex2 = new TranslateTransition(animationDuration, indexText2);
            ttIndex2.setByX(-deltaX);

            // Combine into one parallel transition
            ParallelTransition parallelTransition = new ParallelTransition(
                    ttBar1, ttBar2, ttValue1, ttValue2, ttIndex1, ttIndex2
            );

            // Define what happens AFTER the animation finishes
            parallelTransition.setOnFinished(e -> {
                // 1. Swap the data in the underlying array
                int temp = array[currentI];
                array[currentI] = array[minIndex];
                array[minIndex] = temp;

                // 2. IMPORTANT: Reset translation to 0 BEFORE redrawing
                bar1.setTranslateX(0);
                bar2.setTranslateX(0);
                valueText1.setTranslateX(0);
                valueText2.setTranslateX(0);
                indexText1.setTranslateX(0);
                indexText2.setTranslateX(0);

                // 3. Update the visualization of the two bars to reflect their new heights/values
                updateBarVisualization(currentI);
                updateBarVisualization(minIndex);

                // 4. Update status and continue sorting
                statusLabel.setText("Swap completed. Position " + currentI + " is now sorted.");
                currentI++;
                currentJ = currentI;
                currentPhase = Phase.FINDING_MIN;
                resetBarColors();

                if (!isPaused) {
                    runSortingStep();
                }
            });

            // Play the swap animation
            parallelTransition.play();

        } else {
            // This part remains the same
            statusLabel.setText("Element " + array[currentI] + " at index " + currentI + " is already in correct position.");
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
        highlightPseudoCode(-1);

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
        double startX = (arrayContainer.getWidth() - totalWidth) / 2-200;

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
            valueText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            valueText.setX(x + barWidth / 2 - valueText.getBoundsInLocal().getWidth() / 2);
            valueText.setY(baseY - 10);

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
        if (compareIndex >= 0 && compareIndex < bars.size()) {
            Rectangle bar = bars.get(compareIndex);
            bar.setFill(COMPARING_COLOR);
            bar.setStrokeWidth(3);
//            new Pulse(bar).setSpeed(2).play(); // AnimateFX Pulse effect
        }
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



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void returnHome(ActionEvent event) {
        try {
            Parent homeScreenRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/Home-screen.fxml")));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homeScreenRoot));
            stage.setTitle("DSA Simulator");
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}