package org.example.dsa_simulator.dynamicProgramming;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.IOException;
import java.util.*;

public class knapsack{

    @FXML
    private TextField weightsField;
    @FXML
    private TextField valuesField;
    @FXML
    private TextField capacityField;
    @FXML
    private Pane drawingPane;
    @FXML
    private Label resultLabel;
    @FXML
    private Label status;
    @FXML
    private Label pickedItems;
    @FXML
    private Button pauseResumeButton;
    @FXML
    private Slider speedSlider;
    @FXML
    private Label speedLabel;

    private List<Integer> weights = new ArrayList<>();
    private List<Integer> values = new ArrayList<>();
    private int capacity = 0;

    private int[][] dpTable;
    private int n = 0; // number of items

    private static final double CELL_WIDTH = 60.0;
    private static final double CELL_HEIGHT = 30.0;
    private static final double GRID_POS_X = 10.0;
    private static final double GRID_POS_Y = 10.0;

    private final Map<Pair<Integer, Integer>, Label> cellMap = new HashMap<>();

    // Animation speed - now variable
    private int ANIM_SPEED = 600;

    private int getAnimSpeed() {
        return ANIM_SPEED;
    }

    // Animation control variables
    private SequentialTransition currentAnimation;
    private boolean isPaused = false;
    private boolean isAnimationRunning = false;

    @FXML
    public void initialize() {
        if (speedSlider != null && speedLabel != null) {
            // Set initial value
            speedSlider.setValue(ANIM_SPEED);
            speedLabel.setText(ANIM_SPEED + "ms");

            // Add listener for real-time speed changes
            speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                ANIM_SPEED = newValue.intValue();
                speedLabel.setText(ANIM_SPEED + "ms");

                // If animation is currently running, update its rate
                if (currentAnimation != null && currentAnimation.getStatus() == Animation.Status.RUNNING) {
                    // Calculate rate multiplier (inverse relationship)
                    double rateMultiplier = 600.0 / ANIM_SPEED; // 600 is the default speed
                    currentAnimation.setRate(rateMultiplier);
                }
            });
        }
    }

    @FXML
    public void onWeightsEnter(ActionEvent event) {
        parseIntegerList(weightsField.getText(), weights, "Weights");
    }

    @FXML
    public void onValuesEnter(ActionEvent event) {
        parseIntegerList(valuesField.getText(), values, "Values");
    }

    @FXML
    public void onCapacityEnter(ActionEvent event) {
        try {
            capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) {
                throw new NumberFormatException();
            }
            status.setText("Capacity set: " + capacity);
            showAlert("Success", "Knapsack capacity set to " + capacity);
            capacityField.clear();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid positive integer for capacity");
            capacityField.clear();
        }
    }

    @FXML
    private void onPauseResume(ActionEvent event) {
        if (currentAnimation == null || !isAnimationRunning) {
            return;
        }

        if (isPaused) {
            currentAnimation.play();
            isPaused = false;
        } else {
            currentAnimation.pause();
            isPaused = true;
        }
        updatePauseButtonState();
    }

    private void updatePauseButtonState() {
        if (pauseResumeButton != null) {
            if (!isAnimationRunning) {
                pauseResumeButton.setText("Pause");
                pauseResumeButton.setDisable(true);
                pauseResumeButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8;");
            } else if (isPaused) {
                pauseResumeButton.setText("Resume");
                pauseResumeButton.setDisable(false);
                pauseResumeButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8;");
            } else {
                pauseResumeButton.setText("Pause");
                pauseResumeButton.setDisable(false);
                pauseResumeButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8;");
            }
        }
    }

    private void parseIntegerList(String input, List<Integer> list, String name) {
        list.clear();
        try {
            String[] parts = input.trim().split(",");
            for (String part : parts) {
                list.add(Integer.parseInt(part.trim()));
            }
            if (list.isEmpty()) {
                throw new NumberFormatException();
            }
            status.setText(name + " set: " + list);
            showAlert("Success", name + " set: " + list);
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid input for " + name + ". Use comma-separated integers.");
            list.clear();
        }
    }

    @FXML
    private void onGenerate(ActionEvent event) {
        if (drawingPane == null) {
            System.err.println("Error: drawingPane is not initialized.");
            showAlert("Error", "Drawing pane not initialized");
            return;
        }

        if (weights.isEmpty() || values.isEmpty()) {
            showAlert("Error", "Please set both weights and values.");
            return;
        }

        if (weights.size() != values.size()) {
            showAlert("Error", "Number of weights and values must be equal.");
            return;
        }

        if (capacity <= 0) {
            showAlert("Error", "Please set a valid capacity (> 0).");
            return;
        }

        // Stop any current animation
        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        // Reset animation state
        isAnimationRunning = false;
        isPaused = false;
        updatePauseButtonState();

        n = weights.size();
        drawingPane.getChildren().clear();
        cellMap.clear();

        generateKnapsackTable();
    }

    private void generateKnapsackTable() {
        int W = capacity;
        int N = n;
        System.out.println("weights: " + weights);
        System.out.println("items count: " + N);

        // Create GridPane
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setStyle("-fx-border-color: black; -fx-border-width: 2px;");

        for (int col = 0; col <= W + 3; col++) {
            ColumnConstraints colConstraint = new ColumnConstraints(CELL_WIDTH);
            grid.getColumnConstraints().add(colConstraint);
        }

        for (int row = 0; row <= N + 1; row++) {
            RowConstraints rowConstraint = new RowConstraints(CELL_HEIGHT);
            grid.getRowConstraints().add(rowConstraint);
        }

        cellMap.clear();

        Label valHeader = new Label("val");
        styleHeaderCell(valHeader, grid, 0, 0);
        cellMap.put(new Pair<>(0, 0), valHeader);

        Label wtHeader = new Label("wt");
        styleHeaderCell(wtHeader, grid, 1, 0);
        cellMap.put(new Pair<>(0, 1), wtHeader);

        Label indexHeader = new Label("i");
        styleHeaderCell(indexHeader, grid, 2, 0);
        cellMap.put(new Pair<>(0, 2), indexHeader);

        // Weight capacity headers (columns 3 to 3+W)
        for (int w = 0; w <= W; w++) {
            Label capacityLabel = new Label(String.valueOf(w));
            styleHeaderCell(capacityLabel, grid, 3 + w, 0);
            cellMap.put(new Pair<>(0, 3 + w), capacityLabel);
        }

        Label emptyVal = new Label("");
        styleDataCell(emptyVal, grid, 0, 1);
        cellMap.put(new Pair<>(1, 0), emptyVal);

        Label emptyWt = new Label("");
        styleDataCell(emptyWt, grid, 1, 1);
        cellMap.put(new Pair<>(1, 1), emptyWt);

        Label indexZero = new Label("0");
        styleDataCell(indexZero, grid, 2, 1);
        cellMap.put(new Pair<>(1, 2), indexZero);

        dpTable = new int[N + 2][W + 4];

        for (int w = 0; w <= W; w++) {
            dpTable[0][w] = 0;
            Label baseCell = new Label("0");
            styleDataCell(baseCell, grid, 3 + w, 1);
            cellMap.put(new Pair<>(1, 3 + w), baseCell);
        }

        for (int i = 1; i <= N; i++) {
            Label valueLabel = new Label(String.valueOf(values.get(i - 1)));
            styleDataCell(valueLabel, grid, 0, i + 1);
            cellMap.put(new Pair<>(i + 1, 0), valueLabel);

            Label weightLabel = new Label(String.valueOf(weights.get(i - 1)));
            styleDataCell(weightLabel, grid, 1, i + 1);
            cellMap.put(new Pair<>(i + 1, 1), weightLabel);

            Label indexLabel = new Label(String.valueOf(i));
            styleDataCell(indexLabel, grid, 2, i + 1);
            cellMap.put(new Pair<>(i + 1, 2), indexLabel);
        }

        SequentialTransition sequence = new SequentialTransition();

        for (int i = 1; i <= N; i++) { // Items 1 to N
            for (int w = 0; w <= W; w++) { // Capacities 0 to W
                final int itemIndex = i;
                final int currentCapacity = w;
                final int gridRow = i + 1;
                final int gridCol = 3 + w;

                int currentWeight = weights.get(i - 1);
                int currentValue = values.get(i - 1);

                if (currentWeight > w) {
                    dpTable[i][w] = dpTable[i - 1][w];
                } else {
                    dpTable[i][w] = Math.max(
                            dpTable[i - 1][w],
                            dpTable[i - 1][w - currentWeight] + currentValue
                    );
                }

                // Create cell for this position
                Label cell = new Label("");
                styleDataCell(cell, grid, gridCol, gridRow);
                cellMap.put(new Pair<>(gridRow, gridCol), cell);

                sequence.getChildren().add(createHighlightAnimation(cell));

                ParallelTransition headerHighlight = new ParallelTransition();
                headerHighlight.getChildren().addAll(
                        createHighlightAnimation(cellMap.get(new Pair<>(0, gridCol))), // Capacity header
                        createHighlightAnimation(cellMap.get(new Pair<>(gridRow, 2)))  // Item index
                );
                sequence.getChildren().add(headerHighlight);

                if (currentWeight > w) {
                    sequence.getChildren().add(createHighlightAnimation(cellMap.get(new Pair<>(gridRow - 1, gridCol))));
                } else {
                    ParallelTransition depHighlight = new ParallelTransition();
                    Label upCell = cellMap.get(new Pair<>(gridRow - 1, gridCol));
                    Label diagCell = cellMap.get(new Pair<>(gridRow - 1, 3 + (w - currentWeight)));

                    if (upCell != null) {
                        depHighlight.getChildren().add(createHighlightAnimation(upCell));
                    }
                    if (diagCell != null) {
                        depHighlight.getChildren().add(createHighlightAnimation(diagCell));
                    }
                    sequence.getChildren().add(depHighlight);
                }

                ParallelTransition cellHighlight = new ParallelTransition();
                Timeline updateStatus = new Timeline(
                        new KeyFrame(Duration.millis(getAnimSpeed()), e -> {
                            if (currentWeight > currentCapacity) {
                                status.setText("Item " + itemIndex + " too heavy (" + currentWeight + " > " + currentCapacity + ")");
                            } else {
                                status.setText("Item " + itemIndex + ": max(skip=" + dpTable[itemIndex - 1][currentCapacity] +
                                        ", take=" + (dpTable[itemIndex - 1][currentCapacity - currentWeight] + currentValue) + ")");
                            }
                        })
                );
                cellHighlight.getChildren().addAll(updateStatus);
                cellHighlight.getChildren().addAll(createHighlightAnimation(status));
                sequence.getChildren().add(cellHighlight);

                Timeline setValue = new Timeline(
                        new KeyFrame(Duration.millis(getAnimSpeed() / 2), e -> cell.setText(String.valueOf(dpTable[itemIndex][currentCapacity])))
                );
                sequence.getChildren().add(setValue);
                sequence.getChildren().add(new PauseTransition(Duration.millis(getAnimSpeed() / 4)));
            }
        }

        grid.setLayoutX(GRID_POS_X);
        grid.setLayoutY(GRID_POS_Y);
        drawingPane.getChildren().add(grid);

        // Set up animation control
        currentAnimation = sequence;

        if (!sequence.getChildren().isEmpty()) {
            System.out.println("Starting Knapsack animation...");
            isAnimationRunning = true;
            updatePauseButtonState();

            sequence.setOnFinished(e -> {
                resultLabel.setText(String.valueOf(dpTable[N][W]));
                status.setText("DP Table Complete. Max value: " + dpTable[N][W]);
                isAnimationRunning = false;
                isPaused = false;
                updatePauseButtonState();
            });

            sequence.play();
        }
    }

    @FXML
    private void extractItems() {
        if (dpTable == null || n == 0 || capacity == 0) {
            showAlert("Error", "Generate the table first!");
            return;
        }

        // Stop any current animation before starting item extraction
        if (currentAnimation != null && isAnimationRunning) {
            currentAnimation.stop();
        }

        List<Integer> selected = new ArrayList<>();
        String trace = "";
        SequentialTransition sequence = new SequentialTransition();

        int i = n;
        int w = capacity;
        while (i > 0 && w > 0) {
            int gridRow = i + 1;
            int gridCol = 3 + w;

            sequence.getChildren().add(createHighlightAnimation(cellMap.get(new Pair<>(gridRow, gridCol))));

            final int itemIndex = i;
            final int weight = weights.get(i - 1);
            final int value = values.get(i - 1);

            if (w >= weight && dpTable[i][w] == dpTable[i - 1][w - weight] + value) {
                selected.add(itemIndex);
                trace = trace +  "Item " + itemIndex + "(wt=" + weight + ", val=" + value + ") \n";
                String finalTrace = trace;
                Timeline tl = new Timeline(
                        new KeyFrame(Duration.millis(getAnimSpeed()), e -> {
                            pickedItems.setText(finalTrace);
                            status.setText("Selected item " + itemIndex);
                        })
                );
                sequence.getChildren().add(tl);
                w -= weight;
            } else {
                Timeline tl = new Timeline(
                        new KeyFrame(Duration.millis(getAnimSpeed()), e -> {
                            status.setText("Skipped item " + itemIndex);
                        })
                );
                sequence.getChildren().add(tl);
            }
            i--;
        }

        if (selected.isEmpty()) {
            Timeline finalStatus = new Timeline(
                    new KeyFrame(Duration.millis(getAnimSpeed()), e -> pickedItems.setText("No items selected"))
            );
            sequence.getChildren().add(finalStatus);
        }

        // Set up animation control for item extraction
        currentAnimation = sequence;

        if (!sequence.getChildren().isEmpty()) {
            System.out.println("Starting item extraction animation...");
            isAnimationRunning = true;
            updatePauseButtonState();

            sequence.setOnFinished(e -> {
                isAnimationRunning = false;
                isPaused = false;
                updatePauseButtonState();
            });

            sequence.play();
        } else {
            pickedItems.setText("No items selected");
        }
    }

    private void styleHeaderCell(Label cell, GridPane grid, int col, int row) {
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
        cell.setStyle("-fx-alignment: center; -fx-border-color: black; -fx-border-width: 1px; " +
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        grid.add(cell, col, row);
    }

    private void styleDataCell(Label cell, GridPane grid, int col, int row) {
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
        cell.setStyle("-fx-alignment: center; -fx-border-color: gray; -fx-border-width: 1px; -fx-background-color: #f9f9f9;");
        grid.add(cell, col, row);
    }

    private SequentialTransition createHighlightAnimation(Label label) {
        if (label == null) return new SequentialTransition();

        String originalStyle = label.getStyle();
        String highlightBg = "#ffeb3b";
        String highlightBorder = "#f57c00";

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(getAnimSpeed() / 4), label);
        scaleUp.setFromX(1.0);
        scaleUp.setFromY(1.0);
        scaleUp.setToX(1.2);
        scaleUp.setToY(1.2);
        scaleUp.setOnFinished(e -> label.setStyle(originalStyle + " -fx-background-color: " + highlightBg +
                "; -fx-border-color: " + highlightBorder + "; -fx-border-width: 2px; -fx-font-weight: bold;"));

        PauseTransition pause = new PauseTransition(Duration.millis(getAnimSpeed() / 2));

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(getAnimSpeed() / 4), label);
        scaleDown.setFromX(1.2);
        scaleDown.setFromY(1.2);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setOnFinished(e -> label.setStyle(originalStyle));

        return new SequentialTransition(scaleUp, pause, scaleDown);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void returnHome(ActionEvent event) {
        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        try {
            Parent homeScreenRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/Home-screen.fxml")));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homeScreenRoot));
            stage.setTitle("DSA Simulator");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}