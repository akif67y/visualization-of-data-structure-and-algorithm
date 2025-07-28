package org.example.dsa_simulator.heap;

import javafx.animation.*;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HeapController {
    @FXML private TextField enqueueField;
    @FXML private TextField buildHeapField;
    @FXML private RadioButton minHeapRadio;
    @FXML private RadioButton maxHeapRadio;
    @FXML private Pane canvasPane;
    private MinHeap minHeap;
    private MaxHeap maxHeap;
    private Object currentHeap;
    // Track visual elements by their position index
    private final Map<Integer, StackPane> visualNodes = new HashMap<>();
    private Map<Integer, Text> visualTexts = new HashMap<>();

    // --- Animation constants for clarity and control ---
    private static final double BASE_SPEED = 1800; // Base time for actions (ms)
    private static final double HIGHLIGHT_SPEED = BASE_SPEED / 3; // Color change
    private static final double SWAP_SPEED = BASE_SPEED;          // Node movement
    private static final double STEP_PAUSE = BASE_SPEED / 3;      // Pause between steps
    private static final double FLY_SPEED = BASE_SPEED / 2;       // Fly in/out
    private static final double BUILD_CASCADE_DELAY = 50;         // Delay between elements in build
    private static final double SPAWN_X = 50; // top-left corner spawn position
    private static final double SPAWN_Y = 50;

    // Colors for visualization
    private static final Color COMPARE_COLOR = Color.ORANGE; // For comparing/swapping nodes
    private static final Color NORMAL_CIRCLE_COLOR = Color.LIGHTBLUE; // Default circle color
    private static final Color NORMAL_TEXT_COLOR = Color.DARKBLUE; // Default text color

    // Helper class for heapify steps
    private static class SwapStep {
        int index1;
        int index2;
        SwapStep(int index1, int index2) {
            this.index1 = index1;
            this.index2 = index2;
        }
    }

    private List<Integer> getHeapList() {
        if (currentHeap instanceof MinHeap) {
            return minHeap.getAsList();
        } else {
            return maxHeap.getAsList();
        }
    }

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
            visualizeHeapState(); // Use instant visualization for type switch
        }
    }

    @FXML
    void selectMaxHeap(ActionEvent event) {
        List<Integer> currentData = getHeapList();
        clearScreen();
        if (!currentData.isEmpty()) {
            maxHeap = new MaxHeap(currentData);
            currentHeap = maxHeap;
            visualizeHeapState(); // Use instant visualization for type switch
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
            animateEnqueue(oldState, value);     // abar ager element animate korar dorkar ache?????
            enqueueField.clear();
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter an integer.");   // find a way to print this on the screen
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
     * Animates the insertion of a new element (flying from top-left, then bubbling up with swaps)
     */
    private void animateEnqueue(List<Integer> oldState, int newValue) {
        List<Integer> newState = getHeapList();
        if (newState.isEmpty()) return;

        // 1. Update the structure to include the new node position (circles and lines)
        updateHeapStructure(newState);

        // 2. Create the new number at spawn position
        Text newText = createVisualNumber(newValue);
        canvasPane.getChildren().add(newText);
        newText.toFront();

        // 3. Sequentially animate the entire process: fly-in -> bubble-up steps
        SequentialTransition overallAnimation = new SequentialTransition();

        // 4a. Fly-in animation to the initial (last) position
        int initialInsertIndex = newState.size() - 1;
        double[] initialPos = calculateNodePosition(initialInsertIndex, newState.size());

        TranslateTransition flyInTransition = new TranslateTransition(Duration.millis(FLY_SPEED), newText);
        flyInTransition.setToX(initialPos[0] - SPAWN_X);
        flyInTransition.setToY(initialPos[1] - SPAWN_Y);
        overallAnimation.getChildren().add(flyInTransition);

        // Add a brief pause after fly-in for clarity
        overallAnimation.getChildren().add(new PauseTransition(Duration.millis(STEP_PAUSE / 2)));

        // 4b. Bubble-up swap animations - one step at a time
        // We simulate the heap property check to determine the path
        int currentIndex = initialInsertIndex;
        List<Integer> workingState = new ArrayList<>(newState); // Copy to simulate swaps

        while (currentIndex > 0) {
            int parentIndex = (currentIndex - 1) / 2;
            boolean needsSwap = false;

            if (currentHeap instanceof MinHeap) {
                needsSwap = (workingState.get(parentIndex) > workingState.get(currentIndex));
            } else { // MaxHeap
                needsSwap = (workingState.get(parentIndex) < workingState.get(currentIndex));
            }

            if (needsSwap) {
                // Add animation for this swap step
                overallAnimation.getChildren().add(createBubbleUpSwapStep(parentIndex, currentIndex, newState.size()));
                // Simulate the swap in the working state for the next iteration
                Collections.swap(workingState, parentIndex, currentIndex);
                currentIndex = parentIndex;
            } else {
                break; // Heap property satisfied
            }
        }

        // 5. Finalize positions and text map after all animations
        overallAnimation.setOnFinished(e -> {
            lockInFinalPositions(newState);
            bringAllNumbersToFront();
        });

        overallAnimation.play();
    }

    // Helper for a single bubble-up swap step animation
    private SequentialTransition createBubbleUpSwapStep(int parentIndex, int childIndex, int heapSize) {
        SequentialTransition swapStep = new SequentialTransition();

        Text parentText = visualTexts.get(parentIndex);
        Text childText = visualTexts.get(childIndex);
        StackPane parentNode = visualNodes.get(parentIndex);
        StackPane childNode = visualNodes.get(childIndex);

        if (parentText != null && childText != null && parentNode != null && childNode != null) {
            // a. Highlight nodes being compared
            ParallelTransition highlightOn = new ParallelTransition();
            highlightOn.getChildren().addAll(
                    createColorChangeTransition(parentNode, COMPARE_COLOR),
                    createTextColorChangeTransition(parentText, COMPARE_COLOR),
                    createColorChangeTransition(childNode, COMPARE_COLOR),
                    createTextColorChangeTransition(childText, COMPARE_COLOR)
            );

            // b. Pause to show the highlighted state
            PauseTransition pauseAfterHighlight = new PauseTransition(Duration.millis(STEP_PAUSE));

            // c. Swap animation (move both text and circles)
            ParallelTransition swapMove = createSwapAnimation(parentIndex, childIndex, heapSize);

            // d. Pause after swap
            PauseTransition pauseAfterSwap = new PauseTransition(Duration.millis(STEP_PAUSE / 2));

            // e. De-highlight nodes after swap
            ParallelTransition highlightOff = new ParallelTransition();
            highlightOff.getChildren().addAll(
                    createColorChangeTransition(parentNode, NORMAL_CIRCLE_COLOR),
                    createTextColorChangeTransition(parentText, NORMAL_TEXT_COLOR),
                    createColorChangeTransition(childNode, NORMAL_CIRCLE_COLOR),
                    createTextColorChangeTransition(childText, NORMAL_TEXT_COLOR)
            );

            swapStep.getChildren().addAll(highlightOn, pauseAfterHighlight, swapMove, pauseAfterSwap, highlightOff);
        } else {
            swapStep.getChildren().add(new PauseTransition(Duration.millis(BASE_SPEED / 2)));
        }
        return swapStep;
    }

    /**
     * Animates the removal of an element (flying back to top-left) and heapify (sink down with swaps)
     */
    private void animateDequeue(List<Integer> oldState, int removedValue) {
        List<Integer> newState = getHeapList(); // State after removal

        // Sequentially animate the dequeue and heapify
        SequentialTransition overallAnimation = new SequentialTransition();

        // 1. Animate removed element (root) flying away
        Text removedText = visualTexts.get(0); // Root text is at index 0
        StackPane removedNode = visualNodes.get(0); // Root circle is at index 0

        if (removedText != null) {
            TranslateTransition flyOutTextTransition = new TranslateTransition(Duration.millis(FLY_SPEED), removedText);
            flyOutTextTransition.setToX(SPAWN_X - (removedText.getLayoutX() + removedText.getTranslateX()));
            flyOutTextTransition.setToY(SPAWN_Y - (removedText.getLayoutY() + removedText.getTranslateY()));
            FadeTransition fadeOutText = new FadeTransition(Duration.millis(FLY_SPEED), removedText);
            fadeOutText.setToValue(0);

            ParallelTransition removeTransition;
            if (removedNode != null) {
                removeTransition = getParallelTransition(removedNode, flyOutTextTransition, fadeOutText);
                removeTransition.setOnFinished(e -> {
                    canvasPane.getChildren().remove(removedText);
                    canvasPane.getChildren().remove(removedNode);
                    visualTexts.remove(0);
                    visualNodes.remove(0);
                });
            } else {
                removeTransition = new ParallelTransition(flyOutTextTransition, fadeOutText);
                removeTransition.setOnFinished(e -> {
                    canvasPane.getChildren().remove(removedText);
                    visualTexts.remove(0);
                    visualNodes.remove(0);
                });
            }
            overallAnimation.getChildren().add(removeTransition);
        }

        // 2. If heap is not empty after removal, perform heapify (sink down)
        if (!newState.isEmpty()) {
            // 2b. Identify the element that will be moved to the root (the last element in the old state)
            int lastElementOldIndex = oldState.size() - 1;
            Text lastElementText = visualTexts.get(lastElementOldIndex);
            StackPane lastElementNode = visualNodes.get(lastElementOldIndex);

            // Move this last element's visuals to the root position (index 0)
            if (lastElementText != null && lastElementNode != null) {
                double[] rootPos = calculateNodePosition(0, oldState.size() - 1); // Size after removal

                ParallelTransition moveToRootTransition = getParallelTransition(rootPos, lastElementText, lastElementNode);
                moveToRootTransition.setOnFinished(e -> {
                    // Update maps: last element is now logically at index 0
                    visualTexts.put(0, lastElementText);
                    visualNodes.put(0, lastElementNode);
                    visualTexts.remove(lastElementOldIndex);
                    visualNodes.remove(lastElementOldIndex);
                });
                overallAnimation.getChildren().add(moveToRootTransition);
                overallAnimation.getChildren().add(new PauseTransition(Duration.millis(STEP_PAUSE / 2))); // Pause after move
            }

            // 2c. Heapify (sink down) animations
            List<SwapStep> sinkPath = calculateSinkPath(new ArrayList<>(newState)); // Use a copy

            if (!sinkPath.isEmpty()) {
                for (SwapStep step : sinkPath) {
                    int parentIndex = step.index1;
                    int childIndex = step.index2;
                    // Add animation for this swap step
                    overallAnimation.getChildren().add(createHeapifySwapStep(parentIndex, childIndex, newState.size()));
                    // Simulate the swap in the list for the next iteration
                    Collections.swap(newState, parentIndex, childIndex);
                }
            }
        }

        // 3. Finalize positions and text map after all animations
        overallAnimation.setOnFinished(e -> {
            List<Integer> trulyFinalState = getHeapList(); // Get final state from heap
            lockInFinalPositions(trulyFinalState);
            bringAllNumbersToFront();
            updateHeapStructure(trulyFinalState); // Redraw structure
        });

        overallAnimation.play();
    }

    @NotNull
    private static ParallelTransition getParallelTransition(double[] rootPos, Text lastElementText, StackPane lastElementNode) {
        double translateLastTextX = (rootPos[0] - lastElementText.getBoundsInLocal().getWidth() / 2) - (lastElementText.getLayoutX() + lastElementText.getTranslateX());
        double translateLastTextY = (rootPos[1] + lastElementText.getBoundsInLocal().getHeight() / 4) - (lastElementText.getLayoutY() + lastElementText.getTranslateY());
        double translateLastNodeX = (rootPos[0] - 25) - (lastElementNode.getLayoutX() + lastElementNode.getTranslateX());
        double translateLastNodeY = (rootPos[1] - 25) - (lastElementNode.getLayoutY() + lastElementNode.getTranslateY());

        TranslateTransition moveLastTextToRoot = new TranslateTransition(Duration.millis(FLY_SPEED), lastElementText);
        moveLastTextToRoot.setToX(translateLastTextX);
        moveLastTextToRoot.setToY(translateLastTextY);
        TranslateTransition moveLastNodeToRoot = new TranslateTransition(Duration.millis(FLY_SPEED), lastElementNode);
        moveLastNodeToRoot.setToX(translateLastNodeX);
        moveLastNodeToRoot.setToY(translateLastNodeY);

        return new ParallelTransition(moveLastTextToRoot, moveLastNodeToRoot);
    }

    @NotNull
    private static ParallelTransition getParallelTransition(StackPane removedNode, TranslateTransition flyOutTextTransition, FadeTransition fadeOutText) {
        TranslateTransition flyOutNodeTransition = new TranslateTransition(Duration.millis(FLY_SPEED), removedNode);
        flyOutNodeTransition.setToX(SPAWN_X - (removedNode.getLayoutX() + removedNode.getTranslateX()));
        flyOutNodeTransition.setToY(SPAWN_Y - (removedNode.getLayoutY() + removedNode.getTranslateY()));
        FadeTransition fadeOutNode = new FadeTransition(Duration.millis(FLY_SPEED), removedNode);
        fadeOutNode.setToValue(0);

        return new ParallelTransition(flyOutTextTransition, fadeOutText, flyOutNodeTransition, fadeOutNode);
    }

    // Helper for a single heapify swap step animation
    private SequentialTransition createHeapifySwapStep(int parentIndex, int childIndex, int heapSize) {
        SequentialTransition swapStep = new SequentialTransition();

        Text parentText = visualTexts.get(parentIndex);
        Text childText = visualTexts.get(childIndex);
        StackPane parentNode = visualNodes.get(parentIndex);
        StackPane childNode = visualNodes.get(childIndex);

        if (parentText != null && childText != null && parentNode != null && childNode != null) {
            // a. Highlight nodes being compared
            ParallelTransition highlightOn = new ParallelTransition();
            highlightOn.getChildren().addAll(
                    createColorChangeTransition(parentNode, COMPARE_COLOR),
                    createTextColorChangeTransition(parentText, COMPARE_COLOR),
                    createColorChangeTransition(childNode, COMPARE_COLOR),
                    createTextColorChangeTransition(childText, COMPARE_COLOR)
            );

            // b. Pause to show the highlighted state
            PauseTransition pauseAfterHighlight = new PauseTransition(Duration.millis(STEP_PAUSE));

            // c. Swap animation (move both text and circles)
            ParallelTransition swapMove = createSwapAnimation(parentIndex, childIndex, heapSize);

            // d. Pause after swap
            PauseTransition pauseAfterSwap = new PauseTransition(Duration.millis(STEP_PAUSE / 2));

            // e. De-highlight nodes after swap
            ParallelTransition highlightOff = new ParallelTransition();
            highlightOff.getChildren().addAll(
                    createColorChangeTransition(parentNode, NORMAL_CIRCLE_COLOR),
                    createTextColorChangeTransition(parentText, NORMAL_TEXT_COLOR),
                    createColorChangeTransition(childNode, NORMAL_CIRCLE_COLOR),
                    createTextColorChangeTransition(childText, NORMAL_TEXT_COLOR)
            );

            swapStep.getChildren().addAll(highlightOn, pauseAfterHighlight, swapMove, pauseAfterSwap, highlightOff);
        } else {
            swapStep.getChildren().add(new PauseTransition(Duration.millis(BASE_SPEED / 2)));
        }
        return swapStep;
    }

    // Helper method to calculate the sequence of swaps for heapify (sink down)
    private List<SwapStep> calculateSinkPath(List<Integer> heapList) {
        List<SwapStep> path = new ArrayList<>();
        if (heapList == null || heapList.size() <= 1) {
            return path;
        }

        boolean isMaxHeap = (currentHeap instanceof MaxHeap);
        int index = 0;
        int size = heapList.size();

        while (true) {
            int leftChildIndex = 2 * index + 1;
            int rightChildIndex = 2 * index + 2;
            int swapIndex = index;

            if (leftChildIndex < size) {
                if (isMaxHeap) {
                    if (heapList.get(leftChildIndex) > heapList.get(swapIndex)) {
                        swapIndex = leftChildIndex;
                    }
                } else { // HeapTry
                    if (heapList.get(leftChildIndex) < heapList.get(swapIndex)) {
                        swapIndex = leftChildIndex;
                    }
                }
            }

            if (rightChildIndex < size) {
                if (isMaxHeap) {
                    if (heapList.get(rightChildIndex) > heapList.get(swapIndex)) {
                        swapIndex = rightChildIndex;
                    }
                } else { // HeapTry
                    if (heapList.get(rightChildIndex) < heapList.get(swapIndex)) {
                        swapIndex = rightChildIndex;
                    }
                }
            }

            if (swapIndex == index) {
                break;
            }

            path.add(new SwapStep(index, swapIndex));
            index = swapIndex;
        }
        return path;
    }

    /**
     * Animates building heap from scratch (elements appear at final positions with fade-in)
     */
    private void animateBuildHeap(List<Integer> values) {
        List<Integer> finalState = getHeapList();
        if (finalState.isEmpty()) return;

        updateHeapStructure(finalState);

        // Create all number texts directly at their final positions
        List<Text> numbersToAnimate = new ArrayList<>();
        for (int i = 0; i < finalState.size(); i++) {
            int value = finalState.get(i);
            double[] finalPos = calculateNodePosition(i, finalState.size());
            Text numberText = createVisualNumberAtFinalPosition(value, finalPos[0], finalPos[1]);
            numberText.setOpacity(0.0); // Start invisible for fade-in
            canvasPane.getChildren().add(numberText);
            numberText.toFront();
            numbersToAnimate.add(numberText);
        }

        // Animate them appearing with staggered delays
        ParallelTransition buildAnimation = new ParallelTransition();
        for (int i = 0; i < numbersToAnimate.size(); i++) {
            Text numberText = numbersToAnimate.get(i);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(FLY_SPEED), numberText);
            fadeIn.setDelay(Duration.millis(i * BUILD_CASCADE_DELAY));
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            buildAnimation.getChildren().add(fadeIn);
        }

        buildAnimation.setOnFinished(e -> {
            lockInFinalPositions(finalState);
            bringAllNumbersToFront();
        });

        buildAnimation.play();
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
        bringAllNumbersToFront();
    }

    // --- Helper Methods ---

    // Helper to create text at the final position for build animation
    private Text createVisualNumberAtFinalPosition(int value, double x, double y) {
        Text text = new Text(String.valueOf(value));
        text.setFill(Color.DARKBLUE);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        text.setLayoutX(x - text.getBoundsInLocal().getWidth() / 2);
        text.setLayoutY(y + text.getBoundsInLocal().getHeight() / 4);
        text.setMouseTransparent(true);
        return text;
    }

    // Helper to create a color change animation for a StackPane containing a Circle
    private FillTransition createColorChangeTransition(StackPane node, Color targetColor) {
        if (node == null || node.getChildren().isEmpty() || !(node.getChildren().getFirst() instanceof Circle circle)) {
            return new FillTransition(Duration.ZERO);
        }
        FillTransition ft = new FillTransition(Duration.millis(HeapController.HIGHLIGHT_SPEED), circle);
        ft.setToValue(targetColor);
        return ft;
    }

    // Helper to create a color change animation for Text
    private FillTransition createTextColorChangeTransition(Text textNode, Color targetColor) {
        if (textNode == null) {
            return new FillTransition(Duration.ZERO);
        }
        FillTransition ft = new FillTransition(Duration.millis(HeapController.HIGHLIGHT_SPEED), textNode);
        ft.setToValue(targetColor);
        return ft;
    }

    // Helper to create the animation for swapping two nodes (visual elements)
    // Standardized signature: (index1, index2, heapSize, durationMillis)
    private ParallelTransition createSwapAnimation(int index1, int index2, int heapSize) {
        ParallelTransition swapTransition = new ParallelTransition();

        Text text1 = visualTexts.get(index1);
        Text text2 = visualTexts.get(index2);
        StackPane node1 = visualNodes.get(index1);
        StackPane node2 = visualNodes.get(index2);

        if (text1 != null && text2 != null && node1 != null && node2 != null) {
            double[] pos1 = calculateNodePosition(index1, heapSize);
            double[] pos2 = calculateNodePosition(index2, heapSize);

            // Calculate required translate for text1 to move to pos2
            double translateX1 = (pos2[0] - text1.getBoundsInLocal().getWidth() / 2) - (text1.getLayoutX() + text1.getTranslateX());
            double translateY1 = (pos2[1] + text1.getBoundsInLocal().getHeight() / 4) - (text1.getLayoutY() + text1.getTranslateY());

            // Calculate required translate for text2 to move to pos1
            double translateX2 = (pos1[0] - text2.getBoundsInLocal().getWidth() / 2) - (text2.getLayoutX() + text2.getTranslateX());
            double translateY2 = (pos1[1] + text2.getBoundsInLocal().getHeight() / 4) - (text2.getLayoutY() + text2.getTranslateY());

            // Calculate required translate for circles
            double translateCircleX1 = (pos2[0] - 25) - (node1.getLayoutX() + node1.getTranslateX());
            double translateCircleY1 = (pos2[1] - 25) - (node1.getLayoutY() + node1.getTranslateY());
            double translateCircleX2 = (pos1[0] - 25) - (node2.getLayoutX() + node2.getTranslateX());
            double translateCircleY2 = (pos1[1] - 25) - (node2.getLayoutY() + node2.getTranslateY());

            TranslateTransition moveText1 = new TranslateTransition(Duration.millis(HeapController.SWAP_SPEED), text1);
            moveText1.setToX(translateX1);
            moveText1.setToY(translateY1);
            TranslateTransition moveText2 = new TranslateTransition(Duration.millis(HeapController.SWAP_SPEED), text2);
            moveText2.setToX(translateX2);
            moveText2.setToY(translateY2);
            TranslateTransition moveNode1 = new TranslateTransition(Duration.millis(HeapController.SWAP_SPEED), node1);
            moveNode1.setToX(translateCircleX1);
            moveNode1.setToY(translateCircleY1);
            TranslateTransition moveNode2 = new TranslateTransition(Duration.millis(HeapController.SWAP_SPEED), node2);
            moveNode2.setToX(translateCircleX2);
            moveNode2.setToY(translateCircleY2);

            swapTransition.getChildren().addAll(moveText1, moveText2, moveNode1, moveNode2);

            // Update the visualTexts/Nodes map to reflect the swap for subsequent animations
            swapTransition.setOnFinished(e -> {
                visualTexts.put(index1, text2);
                visualTexts.put(index2, text1);
                visualNodes.put(index1, node2);
                visualNodes.put(index2, node1);
            });
        }
        return swapTransition;
    }

    // --- Structure Management ---

    private void updateHeapStructure(List<Integer> heapList) {
        canvasPane.getChildren().removeIf(node -> node instanceof StackPane || node instanceof Line);
        visualNodes.clear();
        drawHeapStructure(heapList);
    }

    private void lockInFinalPositions(List<Integer> finalState) {
        visualTexts.clear();
        Map<Integer, Text> newTextMap = new HashMap<>();
        Map<Integer, List<Text>> valueToTextMap = new HashMap<>();

        for (javafx.scene.Node node : new ArrayList<>(canvasPane.getChildren())) {
            if (node instanceof Text textElement) {
                String textValueStr = textElement.getText();
                try {
                    int textValue = Integer.parseInt(textValueStr);
                    valueToTextMap.computeIfAbsent(textValue, k -> new ArrayList<>()).add(textElement);
                } catch (NumberFormatException ex) {
                    // Ignore non-numeric text
                }
            }
        }

        for (int i = 0; i < finalState.size(); i++) {
            int value = finalState.get(i);
            List<Text> candidates = valueToTextMap.getOrDefault(value, new ArrayList<>());
            if (!candidates.isEmpty()) {
                Text textElement = candidates.removeFirst();
                textElement.setTranslateX(0);
                textElement.setTranslateY(0);
                double[] pos = calculateNodePosition(i, finalState.size());
                textElement.setLayoutX(pos[0] - textElement.getBoundsInLocal().getWidth() / 2);
                textElement.setLayoutY(pos[1] + textElement.getBoundsInLocal().getHeight() / 4);
                textElement.setOpacity(1.0);
                textElement.setScaleX(1.0);
                textElement.setScaleY(1.0);
                newTextMap.put(i, textElement);
            }
        }
        visualTexts = newTextMap;
    }

    private void drawHeapStructure(List<Integer> heapList) {
        if (heapList.isEmpty()) return;
        for (int i = 1; i < heapList.size(); i++) {
            int parentIndex = (i - 1) / 2;
            double[] childPos = calculateNodePosition(i, heapList.size());
            double[] parentPos = calculateNodePosition(parentIndex, heapList.size());
            Line edge = createVisualEdge(parentPos[0], parentPos[1], childPos[0], childPos[1]);
            canvasPane.getChildren().add(edge);
            edge.toBack();
        }
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

    private Text createVisualNumber(int value) {
        Text text = new Text(String.valueOf(value));
        text.setFill(Color.DARKBLUE);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        text.setLayoutX(HeapController.SPAWN_X - text.getBoundsInLocal().getWidth() / 2);
        text.setLayoutY(HeapController.SPAWN_Y + text.getBoundsInLocal().getHeight() / 4);
        text.setMouseTransparent(true);
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

    private void bringAllNumbersToFront() {
        for (javafx.scene.Node node : new ArrayList<>(canvasPane.getChildren())) {
            if (node instanceof Text) {
                node.toFront();
            }
        }
    }


}