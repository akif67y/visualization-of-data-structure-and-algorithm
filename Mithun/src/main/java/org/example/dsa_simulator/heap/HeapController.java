package org.example.dsa_simulator.heap;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;

public class HeapController {

    @FXML private TextField enqueueField;
    @FXML private Button enqueueButton;
    @FXML private Button dequeueButton;
    @FXML private RadioButton minHeapRadio;
    @FXML private RadioButton maxHeapRadio;
    @FXML private ToggleGroup heapTypeToggle;
    @FXML private Pane canvasPane;
    @FXML private TextField buildHeapField;

    private MinHeap minHeap;
    private MaxHeap maxHeap;
    private Object currentHeap;
    private Map<String, StackPane> visualNodeMap = new HashMap<>();

    @FXML
    public void initialize() {
        minHeap = new MinHeap();
        maxHeap = new MaxHeap();
        currentHeap = minHeap;
    }

    @FXML
    void clearScreen(){
        canvasPane.getChildren().clear();
        visualNodeMap.clear();
        minHeap=new MinHeap();
        maxHeap=new MaxHeap();

        System.out.println("Screen cleared");

    }

    @FXML
    void selectMinHeap(ActionEvent event){
        System.out.println("Switched to minheap");
        currentHeap=minHeap;
        clearScreen();
    }

    @FXML
    void selectMaxHeap(ActionEvent event){
        System.out.println("Switched to maxheap");
        currentHeap = maxHeap;
        clearScreen();
    }

    // The method signature must include (ActionEvent event)
    @FXML
    void handleEnqueue(ActionEvent event) {
        System.out.println("Enqueue button was clicked!"); // Diagnostic print
        try {
            int value = Integer.parseInt(enqueueField.getText());
            enqueueField.clear();

            if (currentHeap instanceof MinHeap) {
                ((MinHeap) currentHeap).insert(value);
            } else {
                ((MaxHeap) currentHeap).insert(value);
            }
            redrawHeap();
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter an integer.");
        }
    }
    @FXML
    void handleDequeue(ActionEvent event) {
        if (getHeapList().isEmpty()) return;

        if (minHeapRadio.isSelected()) {
            ((MinHeap) currentHeap).extractMin();
        }
        else {
            ((MaxHeap) currentHeap).extractMax();
        }
        redrawHeap();
    }

    @FXML void buildHeap()
    {
        String inputText = buildHeapField.getText();
        if (inputText == null || inputText.isBlank()) {
            return;
        }

        List<Integer> numberList = new ArrayList<>();
        try {
            String[] numbersAsStrings = inputText.split(",");
            // 3. Loop through the strings, convert each to an integer, and add to the list
            for (String numStr : numbersAsStrings) {
                numberList.add(Integer.parseInt(numStr.trim()));
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter only comma-separated integers.");
            return;
        }
        clearScreen();

        if (minHeapRadio.isSelected()) {
            minHeap = new MinHeap(numberList);
            currentHeap = minHeap;
        } else {
            maxHeap = new MaxHeap(numberList);
            currentHeap = maxHeap;
        }
        redrawHeap();
    }

    @FXML
    void randomButton(ActionEvent event) {
        List<Integer> numberList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            numberList.add(random.nextInt(99) + 1);
        }

        clearScreen();

        if (minHeapRadio.isSelected()) {
            minHeap = new MinHeap(numberList);
            currentHeap = minHeap;
        }
        else {
            maxHeap = new MaxHeap(numberList);
            currentHeap = maxHeap;
        }

        redrawHeap();
    }

    private void redrawHeap() {
        canvasPane.getChildren().removeIf(node -> node instanceof Line);
        List<Integer> heapList = getHeapList();
        Map<String, StackPane> nextVisualNodeMap = new HashMap<>();
        ParallelTransition parallelTransition = new ParallelTransition();

        for (int i = 0; i < heapList.size(); i++) {
            int value = heapList.get(i);
            String nodeId = "node_" + i;

            StackPane nodePane = visualNodeMap.get(nodeId);
            boolean isNewNode = (nodePane == null);

            if (isNewNode) {
                nodePane = createVisualNode(value);
                nodePane.setId(nodeId);
                nodePane.setLayoutX(20);
                nodePane.setLayoutY(20);
                nodePane.setOpacity(0.0);
                canvasPane.getChildren().add(nodePane);
                FadeTransition ft = new FadeTransition(Duration.millis(300), nodePane);
                ft.setToValue(1.0);
                parallelTransition.getChildren().add(ft);
            } else {
                ((Text)nodePane.getChildren().get(1)).setText(String.valueOf(value));
            }

            double[] pos = calculateNodePosition(i);
            TranslateTransition tt = new TranslateTransition(Duration.millis(500), nodePane);
            tt.setToX(pos[0] - nodePane.getLayoutX());
            tt.setToY(pos[1] - nodePane.getLayoutY());
            parallelTransition.getChildren().add(tt);
            nextVisualNodeMap.put(nodeId, nodePane);
        }

        // This is the new, corrected code
        for (String nodeId : visualNodeMap.keySet()) {
            if (!nextVisualNodeMap.containsKey(nodeId)) {
                StackPane nodeToRemove = visualNodeMap.get(nodeId);

                // 1. Create the animation to move it to the top-left corner
                TranslateTransition move = new TranslateTransition(Duration.millis(600), nodeToRemove);
                move.setToX(20 - nodeToRemove.getLayoutX());
                move.setToY(20 - nodeToRemove.getLayoutY());

                // 2. Create an animation to make it fade out
                FadeTransition fade = new FadeTransition(Duration.millis(600), nodeToRemove);
                fade.setToValue(0.0); // Fade to invisible

                // 3. After the animations finish, permanently remove the node from the screen
                move.setOnFinished(e -> canvasPane.getChildren().remove(nodeToRemove));

                // 4. Add both animations to the master list to be played together
                parallelTransition.getChildren().addAll(move, fade);
            }
        }

        parallelTransition.setOnFinished(e -> {
            for (String nodeId : nextVisualNodeMap.keySet()) {
                StackPane nodePane = nextVisualNodeMap.get(nodeId);
                nodePane.setTranslateX(0);

                nodePane.setTranslateY(0);
                double[] pos = calculateNodePosition(Integer.parseInt(nodeId.split("_")[1]));
                nodePane.setLayoutX(pos[0]);
                nodePane.setLayoutY(pos[1]);
            }
            drawEdges(nextVisualNodeMap);
        });
        parallelTransition.play();
        this.visualNodeMap = nextVisualNodeMap;
    }

    private void drawEdges(Map<String, StackPane> currentNodes) {
        for (int i = 1; i < getHeapList().size(); i++) {
            int parentIndex = (i - 1) / 2;
            StackPane childNode = currentNodes.get("node_" + i);
            StackPane parentNode = currentNodes.get("node_" + parentIndex);
            if (childNode != null && parentNode != null) {
                Line edge = createVisualEdge(parentNode, childNode);
                canvasPane.getChildren().add(edge);
                edge.toBack();
            }
        }
    }

    private double[] calculateNodePosition(int index) {
        double canvasWidth = canvasPane.getWidth();
        if (canvasWidth == 0) canvasWidth = 1000;
        int level = (int) (Math.log(index + 1) / Math.log(2));
        double y = 50 + level * 80;
        int nodesInLevel = 1 << level;
        int positionInLevel = index - nodesInLevel + 1;
        double levelWidth = canvasWidth * 0.6;
        double xOffset = canvasWidth * 0.2;
        double x = xOffset + (positionInLevel + 0.5) * (levelWidth / nodesInLevel);
        return new double[]{x, y};
    }

    private StackPane createVisualNode(int value) {
        Circle circle = new Circle(20, Color.STEELBLUE);
        circle.setStroke(Color.BLACK);
        Text label = new Text(String.valueOf(value));
        label.setFill(Color.WHITE);
        return new StackPane(circle, label);
    }

    private Line createVisualEdge(StackPane source, StackPane target) {
        Line line = new Line();
        line.setStrokeWidth(2);
        line.setStroke(Color.GRAY);
        line.startXProperty().bind(source.layoutXProperty().add(source.widthProperty().divide(2)));
        line.startYProperty().bind(source.layoutYProperty().add(source.heightProperty().divide(2)));
        line.endXProperty().bind(target.layoutXProperty().add(target.widthProperty().divide(2)));
        line.endYProperty().bind(target.layoutYProperty().add(target.heightProperty().divide(2)));
        return line;
    }

    private List<Integer> getHeapList() {
        return (minHeapRadio.isSelected()) ? ((MinHeap) currentHeap).getAsList() : ((MaxHeap) currentHeap).getAsList();
    }
}
