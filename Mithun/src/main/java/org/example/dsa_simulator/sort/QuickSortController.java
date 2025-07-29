package org.example.dsa_simulator.sort;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class QuickSortController {

    // --- FXML Components ---
    @FXML private TextField arrayInput;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button resumeButton;
    @FXML private Button resetButton;
    @FXML private Slider speedSlider;
    @FXML private Pane arrayContainer;
    @FXML private Label statusLabel;

    // --- Visual Constants ---
    private static final double BAR_WIDTH = 50.0, BAR_GAP = 10.0, CONTAINER_HEIGHT = 400.0;
    private static final double START_X = 50.0, START_Y = 50.0;

    // --- Colors ---
    private static final Color C_DEFAULT = Color.LIGHTBLUE;
    private static final Color C_PIVOT = Color.RED;
    private static final Color C_COMPARE = Color.YELLOW;
    private static final Color C_WALL = Color.PURPLE;
    private static final Color C_SORTED = Color.GREEN;

    // --- State Variables ---
    private List<Integer> array;
    private List<Button> buttons;
    private SequentialTransition mainAnimation;
    private double animationSpeed = 1000;

    // A simple struct to hold partition ranges for the iterative approach
    private static class Range {
        int low, high;
        Range(int low, int high) { this.low = low; this.high = high; }
    }

    @FXML
    public void initialize() {
        array = new ArrayList<>();
        buttons = new ArrayList<>();
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                animationSpeed = 2000 - (newVal.doubleValue() * 18)); // Slower max, faster min
        resetControls();
    }

    // --- Event Handlers ---

    @FXML
    void startSort(ActionEvent event) {
        try {
            parseInput();
            if (array.isEmpty()) {
                statusLabel.setText("Please enter some numbers.");
                return;
            }
            createVisualization();
            buildAndPlayAnimation();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid input! Please enter numbers separated by commas.");
        }
    }

    @FXML
    void pauseSort(ActionEvent event) {
        if (mainAnimation != null) {
            mainAnimation.pause();
            statusLabel.setText("Animation paused.");
            pauseButton.setDisable(true);
            resumeButton.setDisable(false);
        }
    }

    @FXML
    void resumeSort(ActionEvent event) {
        if (mainAnimation != null) {
            mainAnimation.play();
            statusLabel.setText("Animation resumed.");
            pauseButton.setDisable(false);
            resumeButton.setDisable(true);
        }
    }

    @FXML
    void resetSort(ActionEvent event) {
        if (mainAnimation != null) {
            mainAnimation.stop();
        }
        mainAnimation = null;
        arrayContainer.getChildren().clear();
        array.clear();
        buttons.clear();
        statusLabel.setText("Enter numbers separated by commas.");
        resetControls();
    }
    @FXML
    void doNextStep(ActionEvent event)
    {

    }

    // --- Core Logic ---

    private void buildAndPlayAnimation() {
        mainAnimation = new SequentialTransition();

        // Use a simulation array to plan the steps without modifying the real one prematurely
        List<Integer> simArray = new ArrayList<>(array);
        Stack<Range> stack = new Stack<>();
        stack.push(new Range(0, simArray.size() - 1));

        while (!stack.isEmpty()) {
            Range current = stack.pop();
            if (current.low < current.high) {
                int pivotIndex = addPartitionAnimation(simArray, current.low, current.high);
                stack.push(new Range(current.low, pivotIndex - 1));
                stack.push(new Range(pivotIndex + 1, current.high));
            }
        }

        // After all steps are planned, add a final master cleanup handler
        mainAnimation.setOnFinished(e -> {
            statusLabel.setText("Sorting complete!");
            // Finalize all positions and set final color
            for (Button b : buttons) {
                b.setLayoutX(b.getLayoutX() + b.getTranslateX());
                b.setLayoutY(b.getLayoutY() + b.getTranslateY());
                b.setTranslateX(0);
                b.setTranslateY(0);
                setButtonColor(b, C_SORTED);
            }
            resetControls();
        });

        // Disable/Enable controls and play
        startButton.setDisable(true);
        resetButton.setDisable(true);
        pauseButton.setDisable(false);
        mainAnimation.play();
    }

    /**
     * Plans and adds all animation steps for a single partition to the main sequence.
     * This uses the Lomuto partition scheme.
     *
     * @param simArray The simulation array to base decisions on.
     * @param low      The low index of the partition.
     * @param high     The high index of the partition.
     * @return The final index of the pivot after partitioning.
     */
    private int addPartitionAnimation(List<Integer> simArray, int low, int high) {
        // --- Setup Phase ---
        addStatus("Partitioning [" + low + "," + high + "]");
        addColor(buttons.subList(low, high + 1), C_DEFAULT);
        addPause(50);
        int pivotValue = simArray.get(high);
        addStatus("Pivot is " + pivotValue);
        addColor(buttons.get(high), C_PIVOT);
        addPause(animationSpeed / 2);

        int wall = low - 1;
        // --- Main Partition Loop ---
        for (int j = low; j < high; j++) {
            addStatus("Comparing " + simArray.get(j) + " with pivot " + pivotValue);
            addColor(buttons.get(j), C_COMPARE);
            addPause(animationSpeed / 2);

            if (simArray.get(j) < pivotValue) {
                wall++;
                addStatus(simArray.get(j) + " < pivot. Swapping with element at wall (" + wall + ")");
                addColor(buttons.get(wall), C_WALL); // Highlight the wall position
                addPause(animationSpeed / 2);

                addSwapAnimation(wall, j);
                Collections.swap(simArray, wall, j); // Update simulation array

                // After swap, the item at wall is confirmed "less than", so keep it purple
                // The item at j goes back to default for the next iteration
                addColor(buttons.get(j), C_DEFAULT);
            } else {
                addStatus(simArray.get(j) + " >= pivot. No swap.");
                addColor(buttons.get(j), C_DEFAULT);
            }
            addPause(animationSpeed / 2);
        }

        // --- Final Pivot Swap ---
        int finalPivotPos = wall + 1;
        addStatus("Placing pivot in final position: " + finalPivotPos);
        addSwapAnimation(finalPivotPos, high);
        Collections.swap(simArray, finalPivotPos, high);

        // --- Cleanup Phase for this Partition ---
        addStatus("Partition complete. Pivot " + pivotValue + " is sorted.");
        addColor(buttons.subList(low, high + 1), C_DEFAULT); // Reset all to default
        addColor(buttons.get(finalPivotPos), C_SORTED); // Mark the now-sorted pivot
        addPause(animationSpeed);

        return finalPivotPos;
    }

    /**
     * Creates and adds a robust, synchronized swap animation to the main sequence.
     */
    private void addSwapAnimation(int i, int j) {
        if (i == j) return;
        Button b1 = buttons.get(i);
        Button b2 = buttons.get(j);

        // The animation will be based on the buttons' final resting places (layout)
        double x1 = b1.getLayoutX();
        double x2 = b2.getLayoutX();

        // Create transitions that move the buttons relative to their current visual position
        TranslateTransition t1 = new TranslateTransition(Duration.millis(animationSpeed), b1);
        t1.setToX(x2 - x1 + b2.getTranslateX() - b1.getTranslateX());

        TranslateTransition t2 = new TranslateTransition(Duration.millis(animationSpeed), b2);
        t2.setToX(x1 - x2 + b1.getTranslateX() - b2.getTranslateX());

        ParallelTransition swapVisuals = new ParallelTransition(t1, t2);

        // Add the visual animation to the main sequence
        mainAnimation.getChildren().add(swapVisuals);

        // **CRUCIAL**: Add a subsequent step to swap the data structures.
        // This runs only AFTER the visual swap animation is complete.
        PauseTransition syncData = new PauseTransition(Duration.millis(1));
        syncData.setOnFinished(e -> {
            Collections.swap(array, i, j);
            Collections.swap(buttons, i, j);
        });
        mainAnimation.getChildren().add(syncData);
    }

    // --- Animation & UI Helper Methods ---

    private void addStatus(String message) {
        PauseTransition pt = new PauseTransition(Duration.millis(1));
        pt.setOnFinished(e -> statusLabel.setText(message));
        mainAnimation.getChildren().add(pt);
    }

    private void addColor(Button b, Color c) {
        PauseTransition pt = new PauseTransition(Duration.millis(1));
        pt.setOnFinished(e -> setButtonColor(b, c));
        mainAnimation.getChildren().add(pt);
    }

    private void addColor(List<Button> buttonList, Color c) {
        ParallelTransition pt = new ParallelTransition();
        for (Button b : buttonList) {
            pt.getChildren().add(createColorTransition(b, c));
        }
        mainAnimation.getChildren().add(pt);
    }

    private PauseTransition createColorTransition(Button b, Color c) {
        PauseTransition pt = new PauseTransition(Duration.millis(1));
        pt.setOnFinished(e -> setButtonColor(b, c));
        return pt;
    }

    private void addPause(double duration) {
        mainAnimation.getChildren().add(new PauseTransition(Duration.millis(duration)));
    }

    private void setButtonColor(Button button, Color color) {
        button.setBackground(new Background(new BackgroundFill(color, null, null)));
    }

    private void resetControls() {
        startButton.setDisable(false);
        resetButton.setDisable(false);
        pauseButton.setDisable(true);
        resumeButton.setDisable(true);
        arrayInput.setDisable(false);
    }

    private void parseInput() {
        array.clear();
        String input = arrayInput.getText().trim();
        if (input.isEmpty()) return;
        for (String part : input.split(",")) {
            part = part.trim();
            if (!part.isEmpty()) array.add(Integer.parseInt(part));
        }
    }

    private void createVisualization() {
        arrayContainer.getChildren().clear();
        buttons.clear();
        if (array.isEmpty()) return;

        int maxValue = array.stream().mapToInt(Integer::intValue).max().orElse(1);
        int minValue = array.stream().mapToInt(Integer::intValue).min().orElse(1);
        double minHeight = 40.0, maxHeight = 300.0;
        double baseY = START_Y + CONTAINER_HEIGHT - minHeight;

        for (int i = 0; i < array.size(); i++) {
            int value = array.get(i);
            double height = (maxValue == minValue) ? minHeight :
                    minHeight + ((double)(value - minValue) / (maxValue - minValue)) * (maxHeight - minHeight);

            Button button = new Button(String.valueOf(value));
            button.setPrefWidth(BAR_WIDTH);
            button.setPrefHeight(height);
            button.setLayoutX(START_X + i * (BAR_WIDTH + BAR_GAP));
            button.setLayoutY(baseY - (height - minHeight));
            button.setStyle("-fx-background-radius: 0; -fx-border-radius: 0;");
            setButtonColor(button, C_DEFAULT);
            button.setFont(Font.font(14));

            Label indexLabel = new Label(String.valueOf(i));
            indexLabel.setLayoutX(START_X + i * (BAR_WIDTH + BAR_GAP) + BAR_WIDTH / 2 - 5);
            indexLabel.setLayoutY(START_Y + CONTAINER_HEIGHT + 10);

            buttons.add(button);
            arrayContainer.getChildren().addAll(button, indexLabel);
        }
    }
}