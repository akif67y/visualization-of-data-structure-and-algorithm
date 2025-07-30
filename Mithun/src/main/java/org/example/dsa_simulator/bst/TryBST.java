package org.example.dsa_simulator.bst;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

// Your existing Node class
class Node {
    int data;
    Node left, right;
    double targetX, targetY;

    Node(int data) {
        this.data = data;
        this.left = null;
        this.right = null;
    }
}

// This class can now act as your FXML controller
public class TryBST implements Initializable {

    // --- FXML INJECTED FIELDS ---
    @FXML private Pane drawPane;
    @FXML private TextField valueTextField;
    @FXML private Button insertButton;
    @FXML private Button deleteButton;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private ComboBox<String> traversalTypeComboBox;
    @FXML private Button traverseButton;


    // --- CLASS FIELDS ---
    private Node root;
    private Map<Node, Button> visualNodes = new HashMap<>();
    private Map<Node, Line> parentLines = new HashMap<>();
    private double anchoredRootX = -1; // Stores the fixed X position of the root.
    private Text traversalResultText; // A single Text object for traversal results.

    // Visual constants
    private static final double NODE_DIAMETER = 50;
    private static final double LEVEL_HEIGHT = 80;
    private static final double HORIZONTAL_GAP = 60; // Gap between nodes horizontally
    private static final Color NODE_COLOR = Color.LIGHTCYAN;
    private static final Color NODE_BORDER_COLOR = Color.BLACK;
    private static final Color HIGHLIGHT_COLOR = Color.LIGHTGREEN;
    private static final Color FOUND_COLOR = Color.ORANGE;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        traversalTypeComboBox.getItems().addAll(
                "Inorder",
                "Preorder",
                "Postorder",
                "Level Order"
        );
    }

    @FXML
    void clear() {
        drawPane.getChildren().clear();
        if (traversalResultText != null) {
            traversalResultText = null;
        }
        visualNodes.clear();
        parentLines.clear();
        root = null;
        anchoredRootX = -1;
        System.out.println("Tree cleared.");
    }

    @FXML
    void insert() {
        setButtonsDisabled(true);
        try {
            int value = Integer.parseInt(valueTextField.getText().trim());
            if (search(root, value) != null) {
                System.out.println("Node " + value + " already exists.");
                setButtonsDisabled(false);
                return;
            }
            animateInsertion(value);
            valueTextField.clear();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
            setButtonsDisabled(false);
        }
    }

    @FXML
    void delete() {
        setButtonsDisabled(true);
        try {
            int value = Integer.parseInt(valueTextField.getText().trim());
            Node nodeToDelete = search(root, value);
            if (nodeToDelete == null) {
                System.out.println("Node " + value + " not found.");
                setButtonsDisabled(false);
                return;
            }
            animateDeletion(nodeToDelete);
            valueTextField.clear();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
            setButtonsDisabled(false);
        }
    }

    @FXML
    void searchNode() {
        setButtonsDisabled(true);
        try {
            int value = Integer.parseInt(valueTextField.getText().trim());
            animateSearch(value);
            valueTextField.clear();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
            setButtonsDisabled(false);
        }
    }

    @FXML
    void performTraversal() {
        String selectedTraversal = traversalTypeComboBox.getValue();
        if (selectedTraversal == null || root == null) {
            System.out.println("Please select a traversal type or insert nodes first.");
            return;
        }

        if (traversalResultText != null) {
            drawPane.getChildren().remove(traversalResultText);
            traversalResultText = null;
        }

        List<Node> traversalOrder = new ArrayList<>();
        switch (selectedTraversal) {
            case "Inorder": getInorder(root, traversalOrder); break;
            case "Preorder": getPreorder(root, traversalOrder); break;
            case "Postorder": getPostorder(root, traversalOrder); break;
            case "Level Order": traversalOrder = getLevelOrder(); break;
        }
        animateTraversal(traversalOrder, selectedTraversal);
    }


    // --- TRAVERSAL LOGIC & ANIMATION ---

    private void getInorder(Node node, List<Node> order) {
        if (node == null) return;
        getInorder(node.left, order);
        order.add(node);
        getInorder(node.right, order);
    }

    private void getPreorder(Node node, List<Node> order) {
        if (node == null) return;
        order.add(node);
        getPreorder(node.left, order);
        getPreorder(node.right, order);
    }

    private void getPostorder(Node node, List<Node> order) {
        if (node == null) return;
        getPostorder(node.left, order);
        getPostorder(node.right, order);
        order.add(node);
    }

    private List<Node> getLevelOrder() {
        List<Node> order = new ArrayList<>();
        if (root == null) return order;
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            order.add(current);
            if (current.left != null) queue.add(current.left);
            if (current.right != null) queue.add(current.right);
        }
        return order;
    }

    private void animateTraversal(List<Node> order, String traversalName) {
        setButtonsDisabled(true);
        SequentialTransition sequence = new SequentialTransition();

        traversalResultText = new Text(traversalName + ": ");
        traversalResultText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        traversalResultText.setX(20);
        traversalResultText.setY(drawPane.getHeight() - 200);
        drawPane.getChildren().add(traversalResultText);

        for (Node node : order) {
            Button button = visualNodes.get(node);
            if (button == null) continue;
            Timeline stepAnimation = new Timeline();
            KeyFrame highlightAndShowFrame = new KeyFrame(Duration.ZERO, e -> {
                setButtonColor(button, HIGHLIGHT_COLOR);
                String currentText = traversalResultText.getText();
                traversalResultText.setText(currentText + node.data + "   ");
            });
            KeyFrame unhighlightFrame = new KeyFrame(Duration.millis(500), e -> {
                setButtonColor(button, NODE_COLOR);
            });
            stepAnimation.getKeyFrames().addAll(highlightAndShowFrame, unhighlightFrame);
            sequence.getChildren().add(new SequentialTransition(stepAnimation, new PauseTransition(Duration.millis(100))));
        }
        sequence.setOnFinished(e -> setButtonsDisabled(false));
        sequence.play();
    }


    // --- SEARCH LOGIC ---

    private void animateSearch(int value) {
        List<Node> path = new ArrayList<>();
        findSearchPath(root, value, path);
        Node foundNode = path.isEmpty() ? null : path.get(path.size() - 1);
        if (foundNode == null || foundNode.data != value) {
            foundNode = null;
        }
        SequentialTransition sequence = new SequentialTransition();
        for (Node pathNode : path) {
            sequence.getChildren().add(highlightNode(pathNode));
        }
        Node finalFoundNode = foundNode;
        sequence.setOnFinished(e -> {
            if (finalFoundNode != null) {
                Button foundButton = visualNodes.get(finalFoundNode);
                Timeline foundTimeline = new Timeline(
                        new KeyFrame(Duration.millis(1), event -> {
                            setButtonColor(foundButton, FOUND_COLOR);
                            displayTextMessage("Found!", foundButton.getLayoutX() + NODE_DIAMETER / 2, foundButton.getLayoutY() - 20, true);
                        }),
                        new KeyFrame(Duration.seconds(2)),
                        new KeyFrame(Duration.seconds(2.5), event -> setButtonColor(foundButton, NODE_COLOR))
                );
                foundTimeline.setOnFinished(event -> setButtonsDisabled(false));
                foundTimeline.play();
            } else {
                Node lastNodeInPath = path.isEmpty() ? null : path.get(path.size() - 1);
                Button lastButton = (lastNodeInPath != null) ? visualNodes.get(lastNodeInPath) : null;
                double x = (lastButton != null) ? lastButton.getLayoutX() + NODE_DIAMETER / 2 : drawPane.getWidth() / 2;
                double y = (lastButton != null) ? lastButton.getLayoutY() + NODE_DIAMETER + 20 : drawPane.getHeight() / 2;
                displayTextMessage(value + " Not Found", x, y, false);
            }
        });
        sequence.play();
    }

    private void findSearchPath(Node current, int value, List<Node> path) {
        if (current == null) return;
        path.add(current);
        if (value == current.data) return;
        else if (value < current.data) findSearchPath(current.left, value, path);
        else findSearchPath(current.right, value, path);
    }

    private void displayTextMessage(String message, double x, double y, boolean isSuccess) {
        Text text = new Text(message);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        text.setFill(isSuccess ? Color.DARKORANGE : Color.FIREBRICK);
        text.setX(x - text.getLayoutBounds().getWidth() / 2);
        text.setY(y);
        text.setOpacity(0);
        drawPane.getChildren().add(text);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), text);
        fadeIn.setToValue(1.0);
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), text);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> drawPane.getChildren().remove(text));
        SequentialTransition textAnimation = new SequentialTransition(fadeIn, pause, fadeOut);
        if (!isSuccess) textAnimation.setOnFinished(e -> setButtonsDisabled(false));
        textAnimation.play();
    }


    // --- INSERTION LOGIC ---

    private void animateInsertion(int value) {
        List<Node> path = new ArrayList<>();
        Node parent = findPath(root, value, path);
        SequentialTransition sequence = new SequentialTransition();
        for (Node pathNode : path) {
            sequence.getChildren().add(highlightNode(pathNode));
        }
        sequence.setOnFinished(e -> {
            Node newNode = new Node(value);
            if (parent == null) root = newNode;
            else if (value < parent.data) parent.left = newNode;
            else parent.right = newNode;
            calculatePositions();
            animateTreeRestructure(newNode, parent, true);
        });
        sequence.play();
    }

    // --- DELETION LOGIC ---

    private void animateDeletion(Node nodeToDelete) {
        if (nodeToDelete.left == null || nodeToDelete.right == null) {
            Button buttonToDelete = visualNodes.get(nodeToDelete);
            setButtonColor(buttonToDelete, Color.RED);
            Line lineToDelete = parentLines.get(nodeToDelete);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(600), buttonToDelete);
            fadeOut.setToValue(0);
            if (lineToDelete != null) {
                FadeTransition lineFade = new FadeTransition(Duration.millis(600), lineToDelete);
                lineFade.setToValue(0);
                lineFade.play();
            }
            fadeOut.setOnFinished(e -> {
                drawPane.getChildren().remove(buttonToDelete);
                if (lineToDelete != null) drawPane.getChildren().remove(lineToDelete);
                visualNodes.remove(nodeToDelete);
                parentLines.remove(nodeToDelete);
                root = deleteNodeRecursive(root, nodeToDelete.data);
                calculatePositions();
                animateTreeRestructure(null, null, false);
            });
            fadeOut.play();
        } else {
            Node successor = findInorderSuccessor(nodeToDelete.right);
            Button successorButton = visualNodes.get(successor);
            Button targetButton = visualNodes.get(nodeToDelete);
            Text movingText = new Text(successorButton.getLayoutX(), successorButton.getLayoutY(), String.valueOf(successor.data));
            movingText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            drawPane.getChildren().add(movingText);
            TranslateTransition tt = new TranslateTransition(Duration.millis(700), movingText);
            tt.setToX(targetButton.getLayoutX() - successorButton.getLayoutX());
            tt.setToY(targetButton.getLayoutY() - successorButton.getLayoutY());
            tt.setOnFinished(e -> {
                drawPane.getChildren().remove(movingText);
                targetButton.setText(String.valueOf(successor.data));
                nodeToDelete.data = successor.data;
                animateDeletion(successor);
            });
            tt.play();
        }
    }

    private Node deleteNodeRecursive(Node current, int value) {
        if (current == null) return null;
        if (value < current.data) current.left = deleteNodeRecursive(current.left, value);
        else if (value > current.data) current.right = deleteNodeRecursive(current.right, value);
        else {
            if (current.left == null) return current.right;
            if (current.right == null) return current.left;
            Node successor = findInorderSuccessor(current.right);
            current.data = successor.data;
            current.right = deleteNodeRecursive(current.right, successor.data);
        }
        return current;
    }

    private Node findInorderSuccessor(Node node) {
        while (node.left != null) node = node.left;
        return node;
    }


    // --- REFACTORED: SHARED ANIMATION & HELPER METHODS ---

    private void animateTreeRestructure(Node newNode, Node parent, boolean isInsert) {
        ParallelTransition parallelTransition = new ParallelTransition();

        // Animate existing nodes moving to new positions
        visualNodes.forEach((node, button) -> {
            if (node == newNode) return; // Don't animate the node being inserted

            double targetLayoutX = node.targetX - NODE_DIAMETER / 2.0;
            double targetLayoutY = node.targetY - NODE_DIAMETER / 2.0;

            if (Math.abs(button.getLayoutX() - targetLayoutX) > 0.1 || Math.abs(button.getLayoutY() - targetLayoutY) > 0.1) {
                TranslateTransition move = new TranslateTransition(Duration.millis(600), button);
                move.setToX(targetLayoutX - button.getLayoutX());
                move.setToY(targetLayoutY - button.getLayoutY());
                parallelTransition.getChildren().add(move);

                // This custom animation moves the lines in sync with the node
                Animation lineAnimation = createLineAnimation(node, move);
                parallelTransition.getChildren().add(lineAnimation);
            }
        });

        // If this is an insertion, create and fade in the new node and its line
        if (isInsert) {
            Button newButton = createVisualNode(newNode.targetX, newNode.targetY, newNode.data);
            newButton.setOpacity(0);
            drawPane.getChildren().add(newButton);
            visualNodes.put(newNode, newButton);
            FadeTransition nodeFadeIn = new FadeTransition(Duration.millis(400), newButton);
            nodeFadeIn.setDelay(Duration.millis(200));
            nodeFadeIn.setToValue(1.0);
            parallelTransition.getChildren().add(nodeFadeIn);
            if (parent != null) {
                Line newLine = new Line(parent.targetX, parent.targetY, newNode.targetX, newNode.targetY);
                newLine.setOpacity(0);
                drawPane.getChildren().add(0, newLine);
                parentLines.put(newNode, newLine);
                FadeTransition lineFadeIn = new FadeTransition(Duration.millis(400), newLine);
                lineFadeIn.setDelay(Duration.millis(200));
                lineFadeIn.setToValue(1.0);
                parallelTransition.getChildren().add(lineFadeIn);
            }
        }

        // After all animations are complete, clean up and finalize positions
        parallelTransition.setOnFinished(e -> {
            visualNodes.forEach((node, button) -> {
                button.setLayoutX(node.targetX - NODE_DIAMETER / 2.0);
                button.setLayoutY(node.targetY - NODE_DIAMETER / 2.0);
                button.setTranslateX(0);
                button.setTranslateY(0);
            });
            parentLines.values().forEach(line -> drawPane.getChildren().remove(line));
            parentLines.clear();
            if (root != null) drawLinesRecursive(root);
            setButtonsDisabled(false);
        });
        parallelTransition.play();
    }

    private Animation createLineAnimation(Node node, TranslateTransition move) {
        Button button = visualNodes.get(node);
        Line parentLine = parentLines.get(node);
        Line leftChildLine = (node.left != null) ? parentLines.get(node.left) : null;
        Line rightChildLine = (node.right != null) ? parentLines.get(node.right) : null;

        return new Transition() {
            { setCycleDuration(Duration.millis(600)); }
            @Override
            protected void interpolate(double frac) {
                double currentCenterX = button.getLayoutX() + NODE_DIAMETER / 2.0 + (move.getToX() * frac);
                double currentCenterY = button.getLayoutY() + NODE_DIAMETER / 2.0 + (move.getToY() * frac);
                if (parentLine != null) {
                    parentLine.setEndX(currentCenterX);
                    parentLine.setEndY(currentCenterY);
                }
                if (leftChildLine != null) {
                    leftChildLine.setStartX(currentCenterX);
                    leftChildLine.setStartY(currentCenterY);
                }
                if (rightChildLine != null) {
                    rightChildLine.setStartX(currentCenterX);
                    rightChildLine.setStartY(currentCenterY);
                }
            }
        };
    }

    private Animation highlightNode(Node node) {
        Button button = visualNodes.get(node);
        if (button == null) return new PauseTransition(Duration.ZERO);
        Timeline timeline = new Timeline();
        KeyFrame kf1 = new KeyFrame(Duration.millis(1), e -> setButtonColor(button, HIGHLIGHT_COLOR));
        KeyFrame kf2 = new KeyFrame(Duration.millis(400));
        KeyFrame kf3 = new KeyFrame(Duration.millis(600), e -> setButtonColor(button, NODE_COLOR));
        timeline.getKeyFrames().addAll(kf1, kf2, kf3);
        return timeline;
    }

    private void calculatePositions() {
        if (root == null) {
            anchoredRootX = -1;
            return;
        }
        if (anchoredRootX == -1) anchoredRootX = drawPane.getWidth() / 2.0;
        List<Node> inOrderNodes = new ArrayList<>();
        setYPositionsAndGetInOrder(root, 0, inOrderNodes);
        for (int i = 0; i < inOrderNodes.size(); i++) {
            inOrderNodes.get(i).targetX = (i + 1) * HORIZONTAL_GAP;
        }
        double calculatedRootX = -1;
        for (Node node : inOrderNodes) {
            if (node == root) {
                calculatedRootX = node.targetX;
                break;
            }
        }
        if (calculatedRootX != -1) {
            double offsetX = anchoredRootX - calculatedRootX;
            for (Node node : inOrderNodes) node.targetX += offsetX;
        }
    }

    private void setYPositionsAndGetInOrder(Node node, int depth, List<Node> inOrderNodes) {
        if (node == null) return;
        setYPositionsAndGetInOrder(node.left, depth + 1, inOrderNodes);
        node.targetY = (depth + 1) * LEVEL_HEIGHT;
        inOrderNodes.add(node);
        setYPositionsAndGetInOrder(node.right, depth + 1, inOrderNodes);
    }

    private void drawLinesRecursive(Node node) {
        if (node == null) return;
        if (node.left != null) {
            Line line = new Line(node.targetX, node.targetY, node.left.targetX, node.left.targetY);
            parentLines.put(node.left, line);
            drawPane.getChildren().add(0, line);
            drawLinesRecursive(node.left);
        }
        if (node.right != null) {
            Line line = new Line(node.targetX, node.targetY, node.right.targetX, node.right.targetY);
            parentLines.put(node.right, line);
            drawPane.getChildren().add(0, line);
            drawLinesRecursive(node.right);
        }
    }

    private Button createVisualNode(double x, double y, int value) {
        Button button = new Button(String.valueOf(value));
        button.setTextFill(Color.DARKBLUE);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        button.setMinSize(NODE_DIAMETER, NODE_DIAMETER);
        button.setPrefSize(NODE_DIAMETER, NODE_DIAMETER);
        button.setMaxSize(NODE_DIAMETER, NODE_DIAMETER);
        button.setBackground(new Background(new BackgroundFill(NODE_COLOR, new CornerRadii(NODE_DIAMETER / 2.0), Insets.EMPTY)));
        button.setBorder(new Border(new BorderStroke(NODE_BORDER_COLOR, BorderStrokeStyle.SOLID, new CornerRadii(NODE_DIAMETER / 2.0), new BorderWidths(2.0), Insets.EMPTY)));
        button.setLayoutX(x - NODE_DIAMETER / 2.0);
        button.setLayoutY(y - NODE_DIAMETER / 2.0);
        return button;
    }

    private Node findPath(Node current, int value, List<Node> path) {
        if (current == null) return null;
        path.add(current);
        if (value < current.data) {
            if (current.left == null) return current;
            return findPath(current.left, value, path);
        } else {
            if (current.right == null) return current;
            return findPath(current.right, value, path);
        }
    }

    private Node search(Node current, int value) {
        if (current == null || current.data == value) return current;
        if (value < current.data) return search(current.left, value);
        return search(current.right, value);
    }

    private void setButtonsDisabled(boolean disabled) {
        if (insertButton != null) insertButton.setDisable(disabled);
        if (deleteButton != null) deleteButton.setDisable(disabled);
        if (searchButton != null) searchButton.setDisable(disabled);
        if (clearButton != null) clearButton.setDisable(disabled);
        if (traverseButton != null) traverseButton.setDisable(disabled);
    }

    private void setButtonColor(Button button, Color color) {
        button.setBackground(new Background(new BackgroundFill(color, new CornerRadii(NODE_DIAMETER / 2.0), Insets.EMPTY)));
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
