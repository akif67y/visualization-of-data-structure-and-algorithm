package org.example.dsa_simulator.heap;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.*;

public class HeapController {

    @FXML private TextField enqueueField;
    @FXML private TextField buildHeapField;
    @FXML private RadioButton minHeapRadio;
    @FXML private Pane canvasPane;

    private MinHeap minHeap;
    private MaxHeap maxHeap;
    private Object currentHeap;

    // Track visual elements by their position index
    private Map<Integer, StackPane> visualNodes = new HashMap<>();
    private Map<Integer, Text> visualTexts = new HashMap<>();

    // Animation constants
    private static final double ANIMATION_SPEED = 600; // milliseconds
    private static final double SPAWN_X = 50; // top-left corner spawn position
    private static final double SPAWN_Y = 50;

    @FXML
    public void initialize() {
        minHeap = new MinHeap();
        maxHeap = new MaxHeap();
        currentHeap = minHeap;
    }

    // --- Event Handlers ---

    @FXML
    void clearScreen() {
        canvasPane.getChildren().clear();
        visualNodes.clear();
        visualTexts.clear();
        minHeap = new MinHeap();
        maxHeap = new MaxHeap();
        currentHeap = minHeapRadio.isSelected() ? minHeap : maxHeap;
    }

    @FXML
    void selectMinHeap(ActionEvent event) {
        List<Integer> currentData = getHeapList();
        clearScreen();
        if (!currentData.isEmpty()) {
            minHeap = new MinHeap(currentData);
            currentHeap = minHeap;
            visualizeHeapState();
        }
    }

    @FXML
    void selectMaxHeap(ActionEvent event) {
        List<Integer> currentData = getHeapList();
        clearScreen();
        if (!currentData.isEmpty()) {
            maxHeap = new MaxHeap(currentData);
            currentHeap = maxHeap;
            visualizeHeapState();
        }
    }

    @FXML
    void handleEnqueue(ActionEvent event) {
        try {
            int value = Integer.parseInt(enqueueField.getText().trim());
            if (enqueueField.getText().trim().isEmpty()) return;

            List<Integer> oldState = new ArrayList<>(getHeapList());

            if (currentHeap instanceof MinHeap) {
                ((MinHeap) currentHeap).insert(value);
            } else {
                ((MaxHeap) currentHeap).insert(value);
            }

            animateEnqueue(oldState, value);
            enqueueField.clear();
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter an integer.");
        }
    }

    @FXML
    void handleDequeue(ActionEvent event) {
        if (getHeapList().isEmpty()) return;

        List<Integer> oldState = new ArrayList<>(getHeapList());
        int removedValue;

        if (currentHeap instanceof MinHeap) {
            removedValue = ((MinHeap) currentHeap).extractMin();
        } else {
            removedValue = ((MaxHeap) currentHeap).extractMax();
        }

        animateDequeue(oldState, removedValue);
    }

    @FXML
    void buildHeap() {
        List<Integer> numberList = new ArrayList<>();
        try {
            String input = buildHeapField.getText().trim();
            if (input.isEmpty()) return;

            String[] numbersAsStrings = input.split(",");
            for (String numStr : numbersAsStrings) {
                String trimmed = numStr.trim();
                if (!trimmed.isEmpty()) {
                    numberList.add(Integer.parseInt(trimmed));
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter comma-separated integers.");
            return;
        }

        if (numberList.isEmpty()) return;

        canvasPane.getChildren().clear();
        visualNodes.clear();
        visualTexts.clear();

        if (minHeapRadio.isSelected()) {
            minHeap = new MinHeap(numberList);
            currentHeap = minHeap;
        } else {
            maxHeap = new MaxHeap(numberList);
            currentHeap = maxHeap;
        }

        animateBuildHeap(numberList);
        buildHeapField.clear();
    }

    @FXML
    void randomButton(ActionEvent event) {
        List<Integer> numberList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            numberList.add(random.nextInt(99) + 1);
        }

        canvasPane.getChildren().clear();
        visualNodes.clear();
        visualTexts.clear();

        if (minHeapRadio.isSelected()) {
            minHeap = new MinHeap(numberList);
            currentHeap = minHeap;
        } else {
            maxHeap = new MaxHeap(numberList);
            currentHeap = maxHeap;
        }

        animateBuildHeap(numberList);
    }

    /**
     * Animates the insertion of a new element (flying from top-left)
     */
    private void animateEnqueue(List<Integer> oldState, int newValue) {
        List<Integer> newState = getHeapList();

        // First, update the structure for all existing elements
        updateHeapStructure(newState);

        // Find where the new value ended up
        int newValueIndex = -1;
        for (int i = 0; i < newState.size(); i++) {
            if (newState.get(i) == newValue) {
                newValueIndex = i;
                break;
            }
        }

        if (newValueIndex == -1) return;

        // Create the new number at spawn position
        Text newText = createVisualNumber(newValue, SPAWN_X, SPAWN_Y);
        canvasPane.getChildren().add(newText);
        newText.toFront(); // Ensure it's on top

        // Animate existing elements to their new positions
        ParallelTransition repositionAnimations = new ParallelTransition();

        for (int i = 0; i < oldState.size(); i++) {
            int oldValue = oldState.get(i);
            int newIndex = newState.indexOf(oldValue);

            if (newIndex != -1 && newIndex != i) {
                // This element moved to a different position
                Text textElement = visualTexts.get(i);
                if (textElement != null) {
                    double[] newPos = calculateNodePosition(newIndex, newState.size());
                    TranslateTransition moveTransition = new TranslateTransition(Duration.millis(ANIMATION_SPEED), textElement);
                    moveTransition.setToX(newPos[0] - textElement.getLayoutX());
                    moveTransition.setToY(newPos[1] - textElement.getLayoutY());
                    repositionAnimations.getChildren().add(moveTransition);
                }
            }
        }

        // Animate the new element flying to its final position
        double[] finalPos = calculateNodePosition(newValueIndex, newState.size());
        TranslateTransition flyInTransition = new TranslateTransition(Duration.millis(ANIMATION_SPEED), newText);
        flyInTransition.setToX(finalPos[0] - SPAWN_X);
        flyInTransition.setToY(finalPos[1] - SPAWN_Y);

        // Run repositioning and fly-in animations together
        ParallelTransition allAnimations = new ParallelTransition();
        allAnimations.getChildren().addAll(repositionAnimations.getChildren());
        allAnimations.getChildren().add(flyInTransition);

        // After animation, lock in final positions
        allAnimations.setOnFinished(e -> {
            lockInFinalPositions(newState);
            // Ensure all numbers are on top after animation
            bringAllNumbersToFront();
        });

        allAnimations.play();
    }

    /**
     * Animates the removal of an element (flying back to top-left)
     */
    private void animateDequeue(List<Integer> oldState, int removedValue) {
        List<Integer> newState = getHeapList();

        // Find the removed element's text
        Text removedText = visualTexts.values().stream().filter(text -> text.getText().equals(String.valueOf(removedValue))).findFirst().orElse(null);

        // Update structure for remaining elements
        updateHeapStructure(newState);

        ParallelTransition allAnimations = new ParallelTransition();

        // Animate removed element flying away
        if (removedText != null) {
            TranslateTransition flyOutTransition = new TranslateTransition(Duration.millis(ANIMATION_SPEED), removedText);
            flyOutTransition.setToX(SPAWN_X - removedText.getLayoutX());
            flyOutTransition.setToY(SPAWN_Y - removedText.getLayoutY());

            FadeTransition fadeOut = new FadeTransition(Duration.millis(ANIMATION_SPEED), removedText);
            fadeOut.setToValue(0);

            ParallelTransition removeTransition = new ParallelTransition(flyOutTransition, fadeOut);
            removeTransition.setOnFinished(e -> canvasPane.getChildren().remove(removedText));
            allAnimations.getChildren().add(removeTransition);
        }

        // Animate remaining elements to their new positions
        for (int i = 0; i < newState.size(); i++) {
            int value = newState.get(i);
            // Find this value in the old state
            int oldIndex = oldState.indexOf(value);
            if (oldIndex != -1 && oldIndex != i) {
                Text textElement = visualTexts.get(oldIndex);
                if (textElement != null) {
                    double[] newPos = calculateNodePosition(i, newState.size());
                    TranslateTransition moveTransition = new TranslateTransition(Duration.millis(ANIMATION_SPEED), textElement);
                    moveTransition.setToX(newPos[0] - textElement.getLayoutX());
                    moveTransition.setToY(newPos[1] - textElement.getLayoutY());
                    allAnimations.getChildren().add(moveTransition);
                }
            }
        }

        // After animation, lock in final positions
        allAnimations.setOnFinished(e -> {
            lockInFinalPositions(newState);
            // Ensure all numbers are on top after animation
            bringAllNumbersToFront();
        });

        allAnimations.play();
    }

    /**
     * Animates building heap from scratch (cascade effect)
     */
    private void animateBuildHeap(List<Integer> values) {
        List<Integer> finalState = getHeapList();
        updateHeapStructure(finalState);

        // Create all numbers at spawn position
        List<Text> numbersToAnimate = new ArrayList<>();
        for (int value : values) {
            Text numberText = createVisualNumber(value, SPAWN_X, SPAWN_Y);
            canvasPane.getChildren().add(numberText);
            numberText.toFront(); // Ensure numbers are on top
            numbersToAnimate.add(numberText);
        }

        // Animate them flying to their positions with staggered delays
        ParallelTransition cascadeAnimation = new ParallelTransition();

        for (int i = 0; i < numbersToAnimate.size(); i++) {
            Text numberText = numbersToAnimate.get(i);
            double[] finalPos = calculateNodePosition(i, finalState.size());

            TranslateTransition flyTransition = new TranslateTransition(Duration.millis(ANIMATION_SPEED), numberText);
            flyTransition.setDelay(Duration.millis(i * 100)); // Staggered delay
            flyTransition.setToX(finalPos[0] - SPAWN_X);
            flyTransition.setToY(finalPos[1] - SPAWN_Y);

            cascadeAnimation.getChildren().add(flyTransition);
        }

        cascadeAnimation.setOnFinished(e -> {
            lockInFinalPositions(finalState);
            // Ensure all numbers are on top after animation
            bringAllNumbersToFront();
        });

        cascadeAnimation.play();
    }

    /**
     * Draws the heap instantly without animation.
     */
    private void visualizeHeapState() {
        List<Integer> heapList = getHeapList();
        if (heapList.isEmpty()) return;

        if (canvasPane.getWidth() == 0 || canvasPane.getHeight() == 0) {
            canvasPane.setPrefSize(1000, 600);
        }

        drawHeapStructure(heapList);

        // Ensure all numbers are visible on top
        bringAllNumbersToFront();

        // Ensure all numbers are visible on top
        bringAllNumbersToFront();
    }

    /**
     * Updates the heap structure (circles and lines) without animation
     */
    private void updateHeapStructure(List<Integer> heapList) {
        // Remove old structure elements
        canvasPane.getChildren().removeIf(node ->
                node instanceof StackPane || node instanceof Line);

        // Clear old tracking
        visualNodes.clear();

        drawHeapStructure(heapList);
    }

    /**
     * Locks in final positions after animation
     */
    private void lockInFinalPositions(List<Integer> finalState) {
        // Reset all transforms and set correct layout positions
        Map<Integer, Text> newTextMap = new HashMap<>();

        for (int i = 0; i < finalState.size(); i++) {
            int value = finalState.get(i);

            // Find the text element for this value
            Text textElement = null;
            for (Text text : canvasPane.getChildren().filtered(node -> node instanceof Text)
                    .toArray(new Text[0])) {
                if (text.getText().equals(String.valueOf(value))) {
                    textElement = text;
                    break;
                }
            }

            if (textElement != null) {
                double[] pos = calculateNodePosition(i, finalState.size());
                textElement.setTranslateX(0);
                textElement.setTranslateY(0);
                textElement.setLayoutX(pos[0] - textElement.getBoundsInLocal().getWidth() / 2);
                textElement.setLayoutY(pos[1] + textElement.getBoundsInLocal().getHeight() / 4);
                newTextMap.put(i, textElement);
            }
        }

        visualTexts = newTextMap;
    }

    private void drawHeapStructure(List<Integer> heapList) {
        if (heapList.isEmpty()) return;

        // Draw edges first (they should be at the back)
        for (int i = 1; i < heapList.size(); i++) {
            int parentIndex = (i - 1) / 2;
            double[] childPos = calculateNodePosition(i, heapList.size());
            double[] parentPos = calculateNodePosition(parentIndex, heapList.size());

            Line edge = createVisualEdge(parentPos[0], parentPos[1], childPos[0], childPos[1]);
            canvasPane.getChildren().add(edge);
            edge.toBack(); // Ensure edges are at the back
        }

        // Draw nodes (circles should be in the middle)
        for (int i = 0; i < heapList.size(); i++) {
            double[] pos = calculateNodePosition(i, heapList.size());

            StackPane circleNode = createVisualCircle(pos[0], pos[1]);
            canvasPane.getChildren().add(circleNode);
            visualNodes.put(i, circleNode);
        }
    }

    private StackPane createVisualCircle(double x, double y) {
        Circle circle = new Circle(25, Color.LIGHTBLUE);
        circle.setStroke(Color.DARKBLUE);
        circle.setStrokeWidth(2);

        StackPane pane = new StackPane(circle);
        pane.setLayoutX(x - 25);
        pane.setLayoutY(y - 25);
        return pane;
    }

    private Text createVisualNumber(int value, double x, double y) {
        Text text = new Text(String.valueOf(value));
        text.setFill(Color.DARKBLUE);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        text.setLayoutX(x - text.getBoundsInLocal().getWidth() / 2);
        text.setLayoutY(y + text.getBoundsInLocal().getHeight() / 4);

        // Ensure text is always on top
        text.setMouseTransparent(true); // Prevent interference with interactions

        return text;
    }

    private Line createVisualEdge(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStrokeWidth(2);
        line.setStroke(Color.GRAY);
        return line;
    }

    private double[] calculateNodePosition(int index, int totalNodes) {
        double canvasWidth = canvasPane.getWidth();
        double canvasHeight = canvasPane.getHeight();

        if (canvasWidth <= 0) canvasWidth = 1000;
        if (canvasHeight <= 0) canvasHeight = 600;

        int level = (int) (Math.log(index + 1) / Math.log(2));
        int nodesInLevel = 1 << level;
        int positionInLevel = index - (nodesInLevel - 1);

        double levelHeight = Math.max(60, canvasHeight * 0.8 / (getMaxLevels(totalNodes) + 1));
        double y = 60 + level * levelHeight;

        double levelWidth = canvasWidth * 0.8;
        double xOffset = canvasWidth * 0.1;
        double spacing = levelWidth / nodesInLevel;
        double x = xOffset + (positionInLevel + 0.5) * spacing;

        return new double[]{x, y};
    }

    private int getMaxLevels(int totalNodes) {
        if (totalNodes == 0) return 0;
        return (int) (Math.log(totalNodes) / Math.log(2)) + 1;
    }

    /**
     * Ensures all number text elements are brought to the front
     */
    private void bringAllNumbersToFront() {
        // Bring all text elements to the front
        for (javafx.scene.Node node : new ArrayList<>(canvasPane.getChildren())) {
            if (node instanceof Text) {
                node.toFront();
            }
        }
    }

    private List<Integer> getHeapList() {
        if (currentHeap instanceof MinHeap) {
            return minHeap.getAsList();
        } else {
            return maxHeap.getAsList();
        }
    }
}