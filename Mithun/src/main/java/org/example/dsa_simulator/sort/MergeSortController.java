package org.example.dsa_simulator.sort;

import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.ParallelTransition;
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

public class MergeSortController implements Initializable {


    @FXML private TextField arrayInput;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button resumeButton;
    @FXML private Button nextStepButton;
    @FXML private Button resetButton;
    @FXML private Pane arrayContainer;
    @FXML private Label statusLabel;
    @FXML private Slider speedSlider; // Add this to your FXML file


    private int[] originalArray;
    private List<MergeLevel> mergeLevels;
    private int currentLevel = 0;
    private int currentPairIndex = 0; // Index of the *parent* subarray in the level above the current one being merged
    private boolean isSorting = false;
    private boolean isPaused = false;
    private boolean isCompleted = false;
    private PauseTransition pause;
    private final double smallBoxWidth = 60;
    private final double smallBoxHeight = 60;
    private final double boxSpacing = 10;
    private final double levelSpacing = 100;
    private final double baseY = 10;
    private Duration animationDuration = Duration.millis(1200); // Will be updated by slider


    private SequentialTransition currentMergeAnimation;
    private boolean isAnimating = false;


    private final Color ORIGINAL_COLOR = Color.LIGHTBLUE;
    private final Color LEFT_SUBARRAY_COLOR = Color.LIGHTCORAL;
    private final Color RIGHT_SUBARRAY_COLOR = Color.CYAN;
    private final Color MERGING_PARENT_COLOR = Color.YELLOW;
    private final Color COMPARE_COLOR = Color.DARKORANGE;
    private final Color PLACED_COLOR = Color.LIGHTSEAGREEN;   // Elements placed in parent
    private final Color MERGED_CHILD_COLOR = Color.GRAY.brighter(); // Children after merge
    private final Color COMPLETED_COLOR = Color.LIGHTGREEN;
    private final Color INACTIVE_LEVEL_COLOR = Color.GRAY.deriveColor(0, 1, 1, 0.4); // Dimmed levels


    private enum Phase {
        DIVIDING,
        MERGING
    }
    private Phase currentPhase = Phase.DIVIDING;


    private static class MergeLevel {
        List<SubArray> subArrays;
        int levelNumber;

        MergeLevel(int levelNumber) {
            this.levelNumber = levelNumber;
            this.subArrays = new ArrayList<>();
        }
    }


    private static class SubArray {
        int[] elements;
        int startIndex;
        int endIndex;
        Rectangle[] boxes;
        Text[] valueTexts;
        Text[] indexTexts;
        double x, y;


        SubArray leftChild = null;
        SubArray rightChild = null;
        SubArray parent = null;

        SubArray(int[] elements, int startIndex, int endIndex) {
            this.elements = Arrays.copyOf(elements, elements.length); // Safer copy
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.boxes = new Rectangle[elements.length];
            this.valueTexts = new Text[elements.length];
            this.indexTexts = new Text[elements.length];
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        arrayInput.setText("12,23,14,16,18,67,34,65");
        pause = new PauseTransition(animationDuration);


        if (speedSlider != null) {
            speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {

                double newDurationMillis = 2500 - (newVal.doubleValue() * 24.5);
                animationDuration = Duration.millis(Math.max(50, newDurationMillis));
                pause.setDuration(animationDuration);
            });
            speedSlider.setValue(50);
        }

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
        statusLabel.setText("Merge sort started - Dividing phase...");
        currentLevel = 0;
        currentPairIndex = 0;
        currentPhase = Phase.DIVIDING;
        prepareMergeLevels();
        updateButtonStates();
        runSortingStep();
    }

    @FXML
    private void pauseSort() {
        if (!isSorting || isPaused) return;
        isPaused = true;


        if (pause != null) pause.pause();
        if (currentMergeAnimation != null && isAnimating) {
            currentMergeAnimation.pause();
        }

        statusLabel.setText("Sorting paused. Press 'Resume' or 'Next Step'.");
        updateButtonStates();
    }

    @FXML
    private void resumeSort() {
        if (!isSorting || !isPaused) return;
        isPaused = false;
        statusLabel.setText("Resuming sort...");
        updateButtonStates();


        if (currentMergeAnimation != null && isAnimating) {
            currentMergeAnimation.play();
        } else if (pause != null) {
            pause.play();
        }
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
            currentLevel = 0;
            currentPairIndex = 0;
            currentPhase = Phase.DIVIDING;
            prepareMergeLevels();
            updateButtonStates();
            statusLabel.setText("Manual mode - Click 'Next Step'.");
        }
        performSingleStepManual();
    }

    @FXML
    private void resetSort() {
        isSorting = false;
        isPaused = false;
        isCompleted = false;
        isAnimating = false;


        if (pause != null) pause.stop();
        if (currentMergeAnimation != null) {
            currentMergeAnimation.stop();
            currentMergeAnimation = null;
        }

        currentLevel = 0;
        currentPairIndex = 0;
        currentPhase = Phase.DIVIDING;
        parseAndVisualize();
        statusLabel.setText("Enter an array and click 'Start'");
        updateButtonStates();
    }

    private void prepareMergeLevels() {
        mergeLevels = new ArrayList<>();
        if (originalArray == null || originalArray.length == 0) return;


        MergeLevel initialLevel = new MergeLevel(0);
        SubArray initialSubArray = new SubArray(originalArray, 0, originalArray.length - 1);
        initialLevel.subArrays.add(initialSubArray);
        mergeLevels.add(initialLevel);


        generateDivisionLevels();
    }

    private void generateDivisionLevels() {
        int levelNumber = 0;
        while (true) {
            MergeLevel currentLevelObj = mergeLevels.get(levelNumber);
            boolean hasDivisions = false;
            MergeLevel nextLevel = new MergeLevel(levelNumber + 1);

            for (SubArray subArray : currentLevelObj.subArrays) {
                if (subArray.elements.length > 1) {
                    hasDivisions = true;
                    int mid = subArray.elements.length / 2;


                    int[] leftElements = Arrays.copyOfRange(subArray.elements, 0, mid);
                    SubArray leftSubArray = new SubArray(leftElements, subArray.startIndex, subArray.startIndex + mid - 1);
                    leftSubArray.parent = subArray;


                    int[] rightElements = Arrays.copyOfRange(subArray.elements, mid, subArray.elements.length);
                    SubArray rightSubArray = new SubArray(rightElements, subArray.startIndex + mid, subArray.endIndex);
                    rightSubArray.parent = subArray;


                    subArray.leftChild = leftSubArray;
                    subArray.rightChild = rightSubArray;

                    nextLevel.subArrays.add(leftSubArray);
                    nextLevel.subArrays.add(rightSubArray);
                } else {

                    nextLevel.subArrays.add(subArray);
                }
            }

            if (!hasDivisions) {

                break;
            }

            mergeLevels.add(nextLevel);
            levelNumber++;
        }
    }

    private void runSortingStep() {
        if (!isSorting) return;

        if (currentPhase == Phase.DIVIDING) {
            if (currentLevel < mergeLevels.size() - 1) {
                performDivisionStep();
            } else {

                currentPhase = Phase.MERGING;

                currentLevel = mergeLevels.size() - 1;
                currentPairIndex = 0;
                statusLabel.setText("Division complete. Starting merge phase...");

                runSortingStep();
            }
        } else if (currentPhase == Phase.MERGING) {
            if (currentLevel > 0) {
                performMergingStep();
            } else {

                finishSorting();
            }
        }
    }

    private void performDivisionStep() {
        MergeLevel currentLevelObj = mergeLevels.get(currentLevel);
        statusLabel.setText("Level " + currentLevel + ": Dividing subarrays...");
        visualizeAllLevelsUpTo(currentLevel);

        pause.setOnFinished(e -> {
            currentLevel++;
            if (!isPaused) {
                runSortingStep();
            }

        });
        pause.playFromStart();
    }

    private void performMergingStep() {
        // Safety check
        if (currentLevel <= 0 || currentLevel >= mergeLevels.size()) {
            finishSorting();
            return;
        }


        MergeLevel parentLevel = mergeLevels.get(currentLevel - 1);
        MergeLevel childLevel = mergeLevels.get(currentLevel); // Level containing children


        if (currentPairIndex >= parentLevel.subArrays.size()) {

            currentLevel--;
            currentPairIndex = 0;
            if (!isPaused) {
                runSortingStep();
            }
            return;
        }

        SubArray parentSubArray = parentLevel.subArrays.get(currentPairIndex);
        SubArray leftChild = parentSubArray.leftChild;
        SubArray rightChild = parentSubArray.rightChild;


        if (leftChild == null || rightChild == null) {

            currentPairIndex++;
            if (!isPaused) {
                runSortingStep();
            }
            return;
        }

        statusLabel.setText("Level " + (currentLevel - 1) + ": Merging into " + Arrays.toString(parentSubArray.elements));


        visualizeAllLevelsUpTo(mergeLevels.size() - 1, parentSubArray, leftChild, rightChild);


        highlightSubArray(parentSubArray, MERGING_PARENT_COLOR);
        highlightSubArray(leftChild, LEFT_SUBARRAY_COLOR);
        highlightSubArray(rightChild, RIGHT_SUBARRAY_COLOR);


        pause.setOnFinished(e -> {

            performAnimatedMerge(parentSubArray, leftChild, rightChild, () -> {



                highlightSubArray(leftChild, MERGED_CHILD_COLOR);
                highlightSubArray(rightChild, MERGED_CHILD_COLOR);


                currentPairIndex++;


                if (currentPairIndex >= parentLevel.subArrays.size()) {
                    currentLevel--;
                    currentPairIndex = 0;
                }

                if (!isPaused) {
                    runSortingStep();
                }

            });
        });
        pause.playFromStart();
    }


    private void performAnimatedMerge(SubArray parent, SubArray leftChild, SubArray rightChild, Runnable onComplete) {
        isAnimating = true;
        currentMergeAnimation = new SequentialTransition();

        int i = 0, j = 0, k = 0;
        int[] leftArr = leftChild.elements;
        int[] rightArr = rightChild.elements;
        int[] parentArr = parent.elements;


        while (i < leftArr.length && j < rightArr.length) {
            final int finalI = i, finalJ = j, finalK = k;

            if (leftArr[i] <= rightArr[j]) {

                parentArr[k] = leftArr[i];
                ParallelTransition moveAnimation = createMoveAnimation(leftChild, finalI, parent, finalK, leftArr[i]);
                currentMergeAnimation.getChildren().add(moveAnimation);
                i++;
            } else {

                parentArr[k] = rightArr[j];
                ParallelTransition moveAnimation = createMoveAnimation(rightChild, finalJ, parent, finalK, rightArr[j]);
                currentMergeAnimation.getChildren().add(moveAnimation);
                j++;
            }
            k++;
        }


        while (i < leftArr.length) {
            final int finalI = i, finalK = k;
            parentArr[k] = leftArr[i];
            ParallelTransition moveAnimation = createMoveAnimation(leftChild, finalI, parent, finalK, leftArr[i]);
            currentMergeAnimation.getChildren().add(moveAnimation);
            i++; k++;
        }


        while (j < rightArr.length) {
            final int finalJ = j, finalK = k;
            parentArr[k] = rightArr[j];
            ParallelTransition moveAnimation = createMoveAnimation(rightChild, finalJ, parent, finalK, rightArr[j]);
            currentMergeAnimation.getChildren().add(moveAnimation);
            j++; k++;
        }


        currentMergeAnimation.setOnFinished(e -> {
            isAnimating = false;
            currentMergeAnimation = null;
            onComplete.run();
        });


        currentMergeAnimation.play();
    }


    private ParallelTransition createMoveAnimation(SubArray sourceArray, int sourceIndex,
                                                   SubArray destArray, int destIndex, int value) {
        ParallelTransition moveAnimation = new ParallelTransition();


        Rectangle sourceBox = sourceArray.boxes[sourceIndex];
        Text sourceText = sourceArray.valueTexts[sourceIndex];
        Rectangle destBox = destArray.boxes[destIndex];
        Text destText = destArray.valueTexts[destIndex];

        if (sourceBox == null || destBox == null) return moveAnimation;


        double deltaX = destBox.getX() - sourceBox.getX();
        double deltaY = destBox.getY() - sourceBox.getY();


        Rectangle animBox = new Rectangle(sourceBox.getX(), sourceBox.getY(),
                sourceBox.getWidth(), sourceBox.getHeight());
        animBox.setFill(COMPARE_COLOR);
        animBox.setStroke(Color.BLACK);
        animBox.setStrokeWidth(2);

        Text animText = new Text(sourceBox.getX() + smallBoxWidth / 2 - 5,
                sourceBox.getY() + smallBoxHeight / 2 + 4,
                String.valueOf(value));
        animText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        animText.setFill(Color.BLACK);


        arrayContainer.getChildren().addAll(animBox, animText);


        TranslateTransition boxTransition = new TranslateTransition(
                Duration.millis(animationDuration.toMillis() * 0.6), animBox);
        boxTransition.setByX(deltaX);
        boxTransition.setByY(deltaY);

        TranslateTransition textTransition = new TranslateTransition(
                Duration.millis(animationDuration.toMillis() * 0.6), animText);
        textTransition.setByX(deltaX);
        textTransition.setByY(deltaY);

        moveAnimation.getChildren().addAll(boxTransition, textTransition);


        moveAnimation.setOnFinished(e -> {

            updateParentElement(destArray, destIndex, value);


            arrayContainer.getChildren().removeAll(animBox, animText);


            highlightElement(destArray, destIndex, PLACED_COLOR);


            PauseTransition briefPause = new PauseTransition(Duration.millis(100));
            briefPause.play();
        });

        return moveAnimation;
    }


    private void visualizeAllLevelsUpTo(int maxLevelIndex) {
        visualizeAllLevelsUpTo(maxLevelIndex, null, null, null);
    }


    private void visualizeAllLevelsUpTo(int maxLevelIndex, SubArray highlightParent, SubArray highlightLeft, SubArray highlightRight) {
        arrayContainer.getChildren().clear();
        if (mergeLevels == null || mergeLevels.isEmpty()) return;

        for (int i = 0; i <= maxLevelIndex && i < mergeLevels.size(); i++) {
            MergeLevel level = mergeLevels.get(i);
            double totalWidth = calculateTotalWidth(level);

            double containerWidth = arrayContainer.getWidth() > 10 ? arrayContainer.getWidth() : arrayContainer.getPrefWidth();
            double startX = (containerWidth - totalWidth) / 2;
            if (Double.isNaN(startX) || startX < 0) startX = 10;
            double currentX = startX;
            double currentY = baseY + i * levelSpacing;


            boolean isDimmed = (i != maxLevelIndex);

            for (SubArray subArray : level.subArrays) {
                boolean isHighlighted = (subArray == highlightParent || subArray == highlightLeft || subArray == highlightRight);

                createSubArrayVisualization(subArray, currentX, currentY, isDimmed && !isHighlighted);
                currentX += subArray.elements.length * (smallBoxWidth + boxSpacing) + 20;
            }
        }
    }

    private void createSubArrayVisualization(SubArray subArray, double startX, double y, boolean isDimmed) {
        subArray.x = startX;
        subArray.y = y;
        for (int i = 0; i < subArray.elements.length; i++) {
            double x = startX + i * (smallBoxWidth + boxSpacing);
            Rectangle box = new Rectangle(x, y, smallBoxWidth, smallBoxHeight);
            box.setFill(ORIGINAL_COLOR);
            if (isDimmed) {
                box.setFill(INACTIVE_LEVEL_COLOR);
            }
            box.setStroke(Color.BLACK);
            box.setStrokeWidth(1);

            Text valueText = new Text(String.valueOf(subArray.elements[i]));
            valueText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            if (isDimmed) {
                valueText.setFill(Color.GRAY.brighter());
            } else {
                valueText.setFill(Color.BLACK);
            }
            valueText.setX(x + smallBoxWidth / 2 - valueText.getBoundsInLocal().getWidth() / 2);
            valueText.setY(y + smallBoxHeight / 2 + 4);

            Text indexText = new Text(String.valueOf(subArray.startIndex + i));
            indexText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            indexText.setFill(Color.GRAY);
            if (isDimmed) {
                indexText.setFill(Color.GRAY.darker());
            }
            indexText.setX(x + smallBoxWidth / 2 - indexText.getBoundsInLocal().getWidth() / 2);
            indexText.setY(y + smallBoxHeight + 15);

            subArray.boxes[i] = box;
            subArray.valueTexts[i] = valueText;
            subArray.indexTexts[i] = indexText;
            arrayContainer.getChildren().addAll(box, valueText, indexText);
        }
    }

    private void highlightSubArray(SubArray subArray, Color color) {
        if (subArray == null || subArray.boxes == null) return;
        for (Rectangle box : subArray.boxes) {
            if (box != null) {
                box.setFill(color);
            }
        }
        if (subArray.valueTexts != null) {
            for(Text text : subArray.valueTexts) {
                if (text != null) text.setFill(Color.BLACK);
            }
        }
    }

    private void highlightElement(SubArray subArray, int index, Color color) {
        if (subArray == null || index < 0 || index >= subArray.boxes.length) return;
        Rectangle box = subArray.boxes[index];
        if (box != null) {
            box.setFill(color);
        }
        Text text = subArray.valueTexts[index];
        if (text != null) {
            text.setFill(Color.BLACK); // Keep text visible
        }
    }


    private void updateParentElement(SubArray parentSubArray, int index, int value) {
        if (parentSubArray == null || index < 0 || index >= parentSubArray.boxes.length) return;

        parentSubArray.elements[index] = value; // Update data model

        Text valueText = parentSubArray.valueTexts[index];
        if (valueText != null) {
            valueText.setText(String.valueOf(value));
            double boxX = parentSubArray.boxes[index].getX();
            valueText.setX(boxX + smallBoxWidth / 2 - valueText.getBoundsInLocal().getWidth() / 2);
        }
    }

    private double calculateTotalWidth(MergeLevel level) {
        double totalWidth = 0;
        for (SubArray subArray : level.subArrays) {
            totalWidth += subArray.elements.length * (smallBoxWidth + boxSpacing);
        }
        totalWidth += (level.subArrays.size() - 1) * 20; // Add space between subarrays
        return Math.max(0, totalWidth);
    }

    private void finishSorting() {
        if (mergeLevels != null && !mergeLevels.isEmpty()) {
            MergeLevel finalLevel = mergeLevels.get(0);
            if (!finalLevel.subArrays.isEmpty()) {
                SubArray finalSubArray = finalLevel.subArrays.get(0);
                if (finalSubArray != null) {
                    highlightSubArray(finalSubArray, COMPLETED_COLOR);
                }
            }
        }
        statusLabel.setText("Merge sort completed!");
        isSorting = false;
        isPaused = false;
        isCompleted = true;
        isAnimating = false;
        updateButtonStates();
    }

    private void updateButtonStates() {
        if (startButton != null) startButton.setDisable(isSorting);
        if (resetButton != null) resetButton.setDisable(!isSorting && !isCompleted);
        if (arrayInput != null) arrayInput.setDisable(isSorting);
        if (pauseButton != null) pauseButton.setDisable(!isSorting || isPaused || isCompleted);
        if (resumeButton != null) resumeButton.setDisable(!isSorting || !isPaused || isCompleted);
        if (nextStepButton != null) nextStepButton.setDisable((isSorting && !isPaused) || isCompleted || isAnimating);
        // if (speedSlider != null) speedSlider.setDisable(isSorting && !isPaused); // Optional
    }

    private boolean parseAndVisualize() {
        try {
            String input = arrayInput.getText().trim();
            if (input.isEmpty()) {
                originalArray = new int[]{12,23,14,16,18,67,34,65};
            } else {
                String[] parts = input.split(",");
                originalArray = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    originalArray[i] = Integer.parseInt(parts[i].trim());
                }
            }
            createInitialVisualization();
            return true;
        } catch (NumberFormatException e) {
            showAlert();
            return false;
        }
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

    private void createInitialVisualization() {
        arrayContainer.getChildren().clear();
        if (originalArray == null) return;

        double totalWidth = originalArray.length * (smallBoxWidth + boxSpacing) - boxSpacing;

        double containerWidth = arrayContainer.getWidth() > 10 ? arrayContainer.getWidth() : arrayContainer.getPrefWidth();
        double startX = (containerWidth - totalWidth) / 2;
        if (Double.isNaN(startX) || startX < 0) startX = 10;

        for (int i = 0; i < originalArray.length; i++) {
            double x = startX + i * (smallBoxWidth + boxSpacing);
            double y = baseY;

            Rectangle box = new Rectangle(x, y, smallBoxWidth, smallBoxHeight);
            box.setFill(ORIGINAL_COLOR);
            box.setStroke(Color.BLACK);
            box.setStrokeWidth(1);

            Text valueText = new Text(String.valueOf(originalArray[i]));
            valueText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            valueText.setFill(Color.BLACK);
            valueText.setX(x + smallBoxWidth / 2 - valueText.getBoundsInLocal().getWidth() / 2);
            valueText.setY(y + smallBoxHeight / 2 + 4);

            Text indexText = new Text(String.valueOf(i));
            indexText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            indexText.setFill(Color.GRAY);
            indexText.setX(x + smallBoxWidth / 2 - indexText.getBoundsInLocal().getWidth() / 2);
            indexText.setY(y + smallBoxHeight + 15);

            arrayContainer.getChildren().addAll(box, valueText, indexText);
        }
    }

    private void performSingleStepManual() {

        runSortingStep();

    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText("Please enter valid, comma-separated numbers.");
        alert.showAndWait();
    }

}