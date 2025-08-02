package org.example.dsa_simulator.heap;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

public class HeapTry {


    private final List<Integer> heap;
    private final List<Button> visualNodes;
    private final List<Line> visualEdges;

    @FXML private TextField enqueueField;
    @FXML private TextField buildHeapField;
    @FXML private RadioButton minHeapRadio;
    @FXML private RadioButton maxHeapRadio;
    @FXML private Pane canvasPane;

    public HeapTry() {
        heap = new ArrayList<>();
        visualNodes = new ArrayList<>();
        visualEdges = new ArrayList<>();
    }


    @FXML
    public void initialize() {

        maxHeapRadio.setSelected(true);
    }



    @FXML
    void handleEnqueue(ActionEvent event) {
        try {
            int value = Integer.parseInt(enqueueField.getText().trim());
            heap.add(value);

            double[] initialPos = calculateNodePosition(heap.size() - 1, heap.size());
            Button button = createVisualNode(initialPos[0], initialPos[1], value);
            visualNodes.add(button);
            canvasPane.getChildren().add(button);

            drawEdges();


            if (maxHeapRadio.isSelected()) {
                animateMaxEnqueue();
            } else {
                animateMinEnqueue();
            }
            enqueueField.clear();
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter an integer.");
        }
    }

    @FXML
    void handleDequeue(ActionEvent event) {
        if (heap.isEmpty()) {
            System.err.println("Heap is empty, cannot dequeue.");
            return;
        }

        if (maxHeapRadio.isSelected()) {
            animateMaxDequeue();
        } else {
            animateMinDequeue();
        }
    }

    @FXML
    void buildHeap(ActionEvent event) {
        String inputText = buildHeapField.getText().trim();
        if (inputText.isEmpty()) return;
        List<Integer> numbers = new ArrayList<>();
        try {
            for (String numStr : inputText.split(",")) {
                if (!numStr.trim().isEmpty()) numbers.add(Integer.parseInt(numStr.trim()));
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter comma-separated integers.");
            return;
        }

        clearScreen(null);
        heap.addAll(numbers);


        if (maxHeapRadio.isSelected()) {
            for (int i = (heap.size() / 2) - 1; i >= 0; i--) maxHeapify(i);
        } else {
            for (int i = (heap.size() / 2) - 1; i >= 0; i--) minHeapify(i);
        }

        for (int i = 0; i < heap.size(); i++) {
            double[] pos = calculateNodePosition(i, heap.size());
            Button button = createVisualNode(pos[0], pos[1], heap.get(i));
            visualNodes.add(button);
            canvasPane.getChildren().add(button);
        }
        drawEdges();
        buildHeapField.clear();
    }

    @FXML
    void randomButton(ActionEvent event) {
        List<Integer> numbers = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 15; i++) numbers.add(random.nextInt(99) + 1);

        buildHeapField.setText(String.join(",", numbers.stream().map(Object::toString).toArray(String[]::new)));

        buildHeap(event);
    }

    @FXML
    void clearScreen(ActionEvent event) {
        heap.clear();
        visualNodes.clear();
        visualEdges.clear();
        canvasPane.getChildren().clear();
    }




    void animateMinEnqueue() {
        SequentialTransition sequentialAnimation = new SequentialTransition();
        int i = heap.size() - 1;
        Button mainNode = visualNodes.get(i);
        setButtonColor(mainNode, Color.RED);
        while (i > 0) {
            int parentIndex = (i - 1) / 2;
            if (heap.get(i) >= heap.get(parentIndex)) break;

            sequentialAnimation.getChildren().add(createSwapAnimation(i, parentIndex));
            Collections.swap(heap, i, parentIndex);
            Collections.swap(visualNodes, i, parentIndex);
            i = parentIndex;
        }
        if (sequentialAnimation.getChildren().isEmpty()) {
            sequentialAnimation.getChildren().add(new PauseTransition(Duration.millis(600)));
        }
        playAnimationWithFinalization(sequentialAnimation);
    }

    void animateMaxEnqueue() {
        SequentialTransition sequentialAnimation = new SequentialTransition();
        int i = heap.size() - 1;
        Button mainNode = visualNodes.get(i);
        setButtonColor(mainNode, Color.RED);
        while (i > 0) {
            int parentIndex = (i - 1) / 2;
            if (heap.get(i) <= heap.get(parentIndex)) break;

            sequentialAnimation.getChildren().add(createSwapAnimation(i, parentIndex));
            Collections.swap(heap, i, parentIndex);
            Collections.swap(visualNodes, i, parentIndex);
            i = parentIndex;
        }
        if (sequentialAnimation.getChildren().isEmpty()) {
            sequentialAnimation.getChildren().add(new PauseTransition(Duration.millis(600)));
        }
        playAnimationWithFinalization(sequentialAnimation);
    }



    private void animateMinDequeue() {
        SequentialTransition masterAnimation = new SequentialTransition();
        Button rootButton = visualNodes.getFirst();
        masterAnimation.getChildren().add(createFadeOutAnimation(rootButton));

        if (heap.size() > 1) {

            int lastNodeIndex = heap.size() - 1;
            Line edgeToRemove = null;
            if (lastNodeIndex > 0 && lastNodeIndex -1 < visualEdges.size()) {

                edgeToRemove = visualEdges.get(lastNodeIndex - 1);
            }

            Button lastButton = visualNodes.get(lastNodeIndex);
            TranslateTransition moveToRoot = createMoveToRootAnimation(lastButton);


            if (edgeToRemove != null) {
                final Line finalEdgeToRemove = edgeToRemove;
                moveToRoot.setOnFinished(e -> {
                    canvasPane.getChildren().remove(finalEdgeToRemove);
                    visualEdges.remove(finalEdgeToRemove);
                });
            }
            masterAnimation.getChildren().add(moveToRoot);


            setButtonColor(lastButton, Color.RED);
            heap.set(0, heap.getLast());
            visualNodes.set(0, lastButton);
            heap.removeLast();
            visualNodes.removeLast();

            int currentIndex = 0;
            while (true) {
                int leftChildIndex = 2 * currentIndex + 1;
                int rightChildIndex = 2 * currentIndex + 2;
                int swapCandidateIndex = currentIndex;
                if (leftChildIndex < heap.size() && heap.get(leftChildIndex) < heap.get(swapCandidateIndex)) swapCandidateIndex = leftChildIndex;
                if (rightChildIndex < heap.size() && heap.get(rightChildIndex) < heap.get(swapCandidateIndex)) swapCandidateIndex = rightChildIndex;
                if (swapCandidateIndex == currentIndex) break;

                masterAnimation.getChildren().add(createSwapAnimation(currentIndex, swapCandidateIndex));
                Collections.swap(heap, currentIndex, swapCandidateIndex);
                Collections.swap(visualNodes, currentIndex, swapCandidateIndex);
                currentIndex = swapCandidateIndex;
            }
        } else {
            heap.clear();
            visualNodes.clear();
        }
        playAnimationWithFinalization(masterAnimation);
    }

    private void animateMaxDequeue() {
        SequentialTransition masterAnimation = new SequentialTransition();
        Button rootButton = visualNodes.getFirst();
        masterAnimation.getChildren().add(createFadeOutAnimation(rootButton));

        if (heap.size() > 1) {

            int lastNodeIndex = heap.size() - 1;
            Line edgeToRemove = null;
            if (lastNodeIndex > 0 && lastNodeIndex - 1 < visualEdges.size()) {
                edgeToRemove = visualEdges.get(lastNodeIndex - 1);
            }

            Button lastButton = visualNodes.get(lastNodeIndex);
            TranslateTransition moveToRoot = createMoveToRootAnimation(lastButton);


            if (edgeToRemove != null) {
                final Line finalEdgeToRemove = edgeToRemove;
                moveToRoot.setOnFinished(e -> {
                    canvasPane.getChildren().remove(finalEdgeToRemove);
                    visualEdges.remove(finalEdgeToRemove);
                });
            }
            masterAnimation.getChildren().add(moveToRoot);


            setButtonColor(lastButton, Color.RED);
            heap.set(0, heap.getLast());
            visualNodes.set(0, lastButton);
            heap.removeLast();
            visualNodes.removeLast();

            int currentIndex = 0;
            while (true) {
                int leftChildIndex = 2 * currentIndex + 1;
                int rightChildIndex = 2 * currentIndex + 2;
                int swapCandidateIndex = currentIndex;
                if (leftChildIndex < heap.size() && heap.get(leftChildIndex) > heap.get(swapCandidateIndex)) swapCandidateIndex = leftChildIndex;
                if (rightChildIndex < heap.size() && heap.get(rightChildIndex) > heap.get(swapCandidateIndex)) swapCandidateIndex = rightChildIndex;
                if (swapCandidateIndex == currentIndex) break;

                masterAnimation.getChildren().add(createSwapAnimation(currentIndex, swapCandidateIndex));
                Collections.swap(heap, currentIndex, swapCandidateIndex);
                Collections.swap(visualNodes, currentIndex, swapCandidateIndex);
                currentIndex = swapCandidateIndex;
            }
        } else {
            heap.clear();
            visualNodes.clear();
        }
        playAnimationWithFinalization(masterAnimation);
    }



    private ParallelTransition createSwapAnimation(int index1, int index2) {
        Button button1 = visualNodes.get(index1);
        Button button2 = visualNodes.get(index2);
        Button otherButton = button1.getBackground().getFills().getFirst().getFill().equals(Color.RED) ? button2 : button1;
        setButtonColor(otherButton, Color.YELLOW);
        double[] pos1 = calculateNodePosition(index1, heap.size());
        double[] pos2 = calculateNodePosition(index2, heap.size());
        double deltaX = pos2[0] - pos1[0];
        double deltaY = pos2[1] - pos1[1];
        TranslateTransition move1 = new TranslateTransition(Duration.millis(1000), button1);
        move1.setByX(deltaX);
        move1.setByY(deltaY);
        TranslateTransition move2 = new TranslateTransition(Duration.millis(1000), button2);
        move2.setByX(-deltaX);
        move2.setByY(-deltaY);
        ParallelTransition swapAnimation = new ParallelTransition(move1, move2);


        swapAnimation.setOnFinished(e -> {
            setButtonColor(otherButton, Color.LIGHTCYAN);
        });

        return swapAnimation;
    }

    private FadeTransition createFadeOutAnimation(Button button) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), button);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> canvasPane.getChildren().remove(button));
        return fadeOut;
    }

    private TranslateTransition createMoveToRootAnimation(Button lastButton) {
        double[] rootPos = calculateNodePosition(0, heap.size() - 1);
        double[] lastPos = calculateNodePosition(heap.size() - 1, heap.size());
        TranslateTransition moveToRoot = new TranslateTransition(Duration.millis(1000), lastButton);
        moveToRoot.setToX(rootPos[0] - lastPos[0]);
        moveToRoot.setToY(rootPos[1] - lastPos[1]);
        return moveToRoot;
    }

    private void playAnimationWithFinalization(SequentialTransition animation) {
        animation.setOnFinished(e -> {
            drawEdges();
            for (Button button : visualNodes) {
                button.setLayoutX(button.getLayoutX() + button.getTranslateX());
                button.setLayoutY(button.getLayoutY() + button.getTranslateY());
                button.setTranslateX(0);
                button.setTranslateY(0);
                setButtonColor(button, Color.LIGHTCYAN);
            }
        });
        animation.play();
    }




    private void minHeapify(int i) {
        int smallest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        if (left < heap.size() && heap.get(left) < heap.get(smallest)) smallest = left;
        if (right < heap.size() && heap.get(right) < heap.get(smallest)) smallest = right;
        if (smallest != i) {
            Collections.swap(heap, i, smallest);
            minHeapify(smallest);
        }
    }

    private void maxHeapify(int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        if (left < heap.size() && heap.get(left) > heap.get(largest)) largest = left;
        if (right < heap.size() && heap.get(right) > heap.get(largest)) largest = right;
        if (largest != i) {
            Collections.swap(heap, i, largest);
            maxHeapify(largest);
        }
    }

    private void drawEdges() {
        canvasPane.getChildren().removeAll(visualEdges);
        visualEdges.clear();
        for (int i = 1; i < heap.size(); i++) {
            double[] childPos = calculateNodePosition(i, heap.size());
            double[] parentPos = calculateNodePosition((i - 1) / 2, heap.size());
            Line edge = new Line(childPos[0], childPos[1], parentPos[0], parentPos[1]);
            edge.setStroke(Color.GRAY);
            edge.setStrokeWidth(2.0);
            canvasPane.getChildren().add(edge);
            visualEdges.add(edge);
            edge.toBack();
        }
    }

    private Button createVisualNode(double x, double y, int value) {
        Button button = new Button(String.valueOf(value));
        button.setTextFill(Color.DARKBLUE);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        double diameter = 50.0;
        button.setMinSize(diameter, diameter);
        button.setPrefSize(diameter, diameter);
        button.setMaxSize(diameter, diameter);
        button.setBackground(new Background(new BackgroundFill(Color.LIGHTCYAN, new CornerRadii(diameter / 2.0), Insets.EMPTY)));
        button.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(diameter / 2.0), new BorderWidths(2.0), Insets.EMPTY)));
        button.setLayoutX(x - diameter / 2.0);
        button.setLayoutY(y - diameter / 2.0);
        return button;
    }

    private double[] calculateNodePosition(int index, int totalNodes) {

        double canvasWidth = 1500;
        if (canvasWidth <= 0) canvasWidth = 1200;


        final double LEVEL_HEIGHT = 90.0;
        final double TOP_OFFSET = 60.0;
        int level = (int) (Math.log(index + 1) / Math.log(2));
        double y = TOP_OFFSET + level * LEVEL_HEIGHT;




        double x = canvasWidth / 2.0;


        if (index == 0) {
            return new double[]{x, y};
        }


        double horizontalGap = canvasWidth / 6;


        LinkedList<String> path = new LinkedList<>();
        int currentIndex = index;
        while (currentIndex > 0) {
            int parentIndex = (currentIndex - 1) / 2;
            if (currentIndex == 2 * parentIndex + 1) {
                path.addFirst("left");
            } else {
                path.addFirst("right");
            }
            currentIndex = parentIndex;
        }


        for (int i = 0; i < path.size(); i++) {

            double currentLevelGap = horizontalGap / (1 << i);
            if (path.get(i).equals("left")) {
                x -= currentLevelGap;
            } else { // "right"
                x += currentLevelGap;
            }
        }

        return new double[]{x, y};
    }

    private void setButtonColor(Button button, Color color) {
        double diameter = 50.0;
        button.setBackground(new Background(new BackgroundFill(color, new CornerRadii(diameter / 2.0), Insets.EMPTY)));
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