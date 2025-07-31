package org.example.dsa_simulator.sort;

import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class InsertionSortController implements Initializable {


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
    private int currentI = 1; // Current element being inserted
    private int currentJ = 1; // Position in sorted part for comparison
    private int arrayLength = 0;

    private PauseTransition pause;
    private Duration animationDuration = Duration.millis(500);


    private final Color UNSORTED_COLOR = Color.LIGHTBLUE;
    private final Color SORTED_COLOR = Color.LIGHTGREEN;
    private final Color COMPARING_COLOR = Color.RED;
    private final Color POINTER_COLOR = Color.PURPLE; // For the key element


    private enum Phase {
        SELECTING_KEY,
        FINDING_POSITION
    }
    private Phase currentPhase = Phase.SELECTING_KEY;


    private final String[] insertionSortPseudoCode = {
            "procedure insertionSort(A : list of sortable items)",
            "  n = length(A)",
            "  for i from 1 to n-1",
            "    j = i",
            "    while j > 0 and A[j-1] > A[j]",
            "      swap(A[j], A[j-1])",
            "      j = j - 1",
            "    end while",
            "  end for",
            "end procedure"
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        arrayInput.setText("64,34,25,12,22,11,90");
        pause = new PauseTransition(animationDuration);

        speedSlider.setMin(1);
        speedSlider.setMax(100);
        speedSlider.setValue(50);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double maxDuration = 1500.0;
            double minDuration = 50.0;
            double newDurationMillis = maxDuration - (newVal.doubleValue() / 100.0) * (maxDuration - minDuration);
            animationDuration = Duration.millis(newDurationMillis);
            pause.setDuration(animationDuration);
        });

        arrayContainer.widthProperty().addListener((obs, oldVal, newVal) -> parseAndVisualize());
        arrayContainer.heightProperty().addListener((obs, oldVal, newVal) -> parseAndVisualize());
        pseudoCodeList.getItems().addAll(insertionSortPseudoCode);
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
        currentJ = 1;
        arrayLength = array.length;
        currentPhase = Phase.SELECTING_KEY;

        updateButtonStates();
        runSortingStep();
    }

    @FXML
    private void pauseSort() {
        if (!isSorting || isPaused) return;
        isPaused = true;
        pause.pause();
        statusLabel.setText("Sorting paused.");
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
        if (!isSorting) {
            startSort();
            pauseSort();
        } else if (!isPaused) {
            pauseSort();
        } else {
            runSortingStep();
        }
    }

    @FXML
    private void resetSort() {
        isSorting = false;
        isPaused = false;
        isCompleted = false;
        pause.stop();
        currentI = 1;
        parseAndVisualize();
        statusLabel.setText("Enter an array and click 'Start'");
        updateButtonStates();
        highlightPseudoCode(-1);
    }

    private void runSortingStep() {
        if (currentI >= arrayLength) {
            finishSorting();
            return;
        }

        if (currentPhase == Phase.SELECTING_KEY) {

            currentJ = currentI;
            statusLabel.setText("Pass " + currentI + ": Inserting element " + array[currentI]);
            highlightKeyElement(currentI);
            highlightPseudoCode(2);
            currentPhase = Phase.FINDING_POSITION; // Transition to next phase

            pause.setOnFinished(e -> runSortingStep());
            pause.playFromStart();

        } else if (currentPhase == Phase.FINDING_POSITION) {

            if (currentJ > 0 && array[currentJ - 1] > array[currentJ]) {
                statusLabel.setText("Comparing " + array[currentJ] + " and " + array[currentJ - 1] + ". Swapping...");
                highlightComparison(currentJ - 1, currentJ);
                highlightPseudoCode(4);

                pause.setOnFinished(e -> animateSwap(currentJ - 1, currentJ));
                pause.playFromStart();
            } else {

                statusLabel.setText("Element " + array[currentJ] + " is in correct sorted position.");
                currentI++;
                currentPhase = Phase.SELECTING_KEY; // Reset phase for next pass
                resetBarColors();

                if (isPaused) return;
                runSortingStep();
            }
        }
    }

    private void animateSwap(int index1, int index2) {
        highlightPseudoCode(5);

        Rectangle bar1 = bars.get(index1);
        Rectangle bar2 = bars.get(index2);
        Text valueText1 = valueTexts.get(index1);
        Text valueText2 = valueTexts.get(index2);
        Text indexText1 = indexTexts.get(index1);
        Text indexText2 = indexTexts.get(index2);

        double deltaX = bar2.getX() - bar1.getX();

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

        ParallelTransition swapAnimation = new ParallelTransition(ttBar1, ttBar2, ttValue1, ttValue2, ttIndex1, ttIndex2);

        swapAnimation.setOnFinished(e -> {
            // Update data array
            int temp = array[index1];
            array[index1] = array[index2];
            array[index2] = temp;


            bar1.setTranslateX(0); bar2.setTranslateX(0);
            valueText1.setTranslateX(0); valueText2.setTranslateX(0);
            indexText1.setTranslateX(0); indexText2.setTranslateX(0);


            updateBarVisualization(index1);
            updateBarVisualization(index2);


            currentJ--;
            if (isPaused) return;
            runSortingStep();
        });
        swapAnimation.play();
    }

    private void finishSorting() {
        for (int i = 0; i < bars.size(); i++) setSorted(i);
        statusLabel.setText("Insertion sort completed!");
        highlightPseudoCode(-1);
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
            if (input.isEmpty()) array = new int[]{64, 34, 25, 12, 22, 11, 90};
            else {
                String[] parts = input.split(",");
                array = new int[parts.length];
                for (int i = 0; i < parts.length; i++) array[i] = Integer.parseInt(parts[i].trim());
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

        int maxValue = Arrays.stream(array).max().orElse(1);
        double barWidth = 60;
        double barSpacing = 10;
        double maxBarHeight = 400;
        double baseY = 550;
        double totalWidth = array.length * barWidth + (array.length - 1) * barSpacing;
        double startX = (arrayContainer.getWidth() > 0 ? (arrayContainer.getWidth() - totalWidth) / 2 : 20);

        for (int i = 0; i < array.length; i++) {
            double x = startX + i * (barWidth + barSpacing);
            double barHeight = (double) array[i] / maxValue * maxBarHeight;
            double y = baseY - barHeight;

            Rectangle bar = new Rectangle(x, y, barWidth, barHeight);
            bar.setFill(UNSORTED_COLOR); // All bars start as unsorted
            bar.setStroke(Color.BLACK);
            bar.setStrokeWidth(2);

            Text valueText = new Text(String.valueOf(array[i]));
            valueText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            valueText.setX(x + barWidth / 2 - valueText.getBoundsInLocal().getWidth() / 2);
            valueText.setY(baseY - 10);

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
        if (index < 0 || index >= bars.size()) return;
        int maxValue = Arrays.stream(array).max().orElse(1);
        double maxBarHeight = 400;
        double baseY = 550;
        Rectangle bar = bars.get(index);
        Text valueText = valueTexts.get(index);
        double barHeight = (double) array[index] / maxValue * maxBarHeight;
        bar.setHeight(barHeight);
        bar.setY(baseY - barHeight);
        valueText.setText(String.valueOf(array[index]));
        valueText.setX(bar.getX() + bar.getWidth() / 2 - valueText.getBoundsInLocal().getWidth() / 2);
    }

    private void highlightKeyElement(int keyIndex) {
        resetBarColors();
        if (keyIndex >= 0 && keyIndex < bars.size()) {
            bars.get(keyIndex).setFill(POINTER_COLOR);
        }
    }

    private void highlightComparison(int compareIndex, int keyIndex) {
        resetBarColors();
        if (compareIndex >= 0 && compareIndex < bars.size()) {
            bars.get(compareIndex).setFill(COMPARING_COLOR);
        }
        if (keyIndex >= 0 && keyIndex < bars.size()) {
            bars.get(keyIndex).setFill(POINTER_COLOR);
        }
    }

    private void resetBarColors() {
        for (int i = 0; i < bars.size(); i++) {

            if (i < currentI) bars.get(i).setFill(SORTED_COLOR);
            else bars.get(i).setFill(UNSORTED_COLOR);
        }
    }

    private void setSorted(int index) {
        if (index >= 0 && index < bars.size()) {
            bars.get(index).setFill(SORTED_COLOR);
        }
    }

    private void highlightPseudoCode(int lineNumber) {
        if (lineNumber >= 0 && lineNumber < pseudoCodeList.getItems().size()) {
            pseudoCodeList.getSelectionModel().select(lineNumber);
            pseudoCodeList.scrollTo(lineNumber);
        } else {
            pseudoCodeList.getSelectionModel().clearSelection();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
