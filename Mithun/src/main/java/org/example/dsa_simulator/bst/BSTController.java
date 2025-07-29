package org.example.dsa_simulator.bst;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

public class BSTController {

    // BST Node class
    private static class BSTNode {
        int data;
        BSTNode left, right;
        Circle visualNode;
        Text visualText;
        Line leftLine, rightLine;
        double x, y;
        double targetX, targetY; // Target positions for animations

        BSTNode(int data) {
            this.data = data;
            this.left = null;
            this.right = null;
        }
    }

    // Main components
    private Stage stage;
    private Pane drawPane;
    private BSTNode root;
    private TextField inputField;
    private TextArea traversalArea;
    private Label statusLabel;

    // Visual constants
    private static final double NODE_RADIUS = 25;
    private static final double LEVEL_HEIGHT = 60;
    private static final double MIN_HORIZONTAL_GAP = 40;
    private static final Color NODE_COLOR = Color.LIGHTBLUE;
    private static final Color NODE_BORDER = Color.DARKBLUE;
    private static final Color HIGHLIGHT_COLOR = Color.LIGHTGREEN;
    private static final Color NEW_NODE_COLOR = Color.LIGHTCORAL;
    private static final Color LINE_COLOR = Color.BLACK;

    // Animation control
    private Timeline currentAnimation;
    private boolean isAnimating = false;
    private ParallelTransition currentParallelTransition;

    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Binary Search Tree Simulation with Animations");

        // Create main layout
        BorderPane mainLayout = createMainLayout();

        // Create scene with 1080p resolution
        Scene scene = new Scene(mainLayout, 1920, 1080);
        scene.getStylesheets().add(createCSS());

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private BorderPane createMainLayout() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f0f0f0;");

        // Create control panel
        VBox controlPanel = createControlPanel();
        mainLayout.setLeft(controlPanel);

        // Create drawing area
        drawPane = new Pane();
        drawPane.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 2;");
        ScrollPane scrollPane = new ScrollPane(drawPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefSize(1400, 900);
        mainLayout.setCenter(scrollPane);

        // Create status bar
        statusLabel = new Label("Ready - Enter a number and click an operation");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        mainLayout.setBottom(statusLabel);

        return mainLayout;
    }

    private VBox createControlPanel() {
        VBox controlPanel = new VBox(15);
        controlPanel.setPadding(new Insets(20));
        controlPanel.setStyle("-fx-background-color: #e8e8e8; -fx-border-color: #cccccc; -fx-border-width: 0 2 0 0;");
        controlPanel.setPrefWidth(300);

        // Title
        Label title = new Label("BST Operations");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setAlignment(Pos.CENTER);

        // Input section
        VBox inputSection = createInputSection();

        // Operation buttons
        VBox operationButtons = createOperationButtons();

        // Traversal section
        VBox traversalSection = createTraversalSection();

        // Utility buttons
        VBox utilityButtons = createUtilityButtons();

        controlPanel.getChildren().addAll(
                title,
                new Separator(),
                inputSection,
                new Separator(),
                operationButtons,
                new Separator(),
                traversalSection,
                new Separator(),
                utilityButtons
        );

        return controlPanel;
    }

    private VBox createInputSection() {
        VBox inputSection = new VBox(10);

        Label inputLabel = new Label("Enter Value:");
        inputLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        inputField = new TextField();
        inputField.setPromptText("Enter integer value");
        inputField.setPrefHeight(35);

        inputSection.getChildren().addAll(inputLabel, inputField);
        return inputSection;
    }

    private VBox createOperationButtons() {
        VBox operationButtons = new VBox(10);

        Label operationsLabel = new Label("BST Operations:");
        operationsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Button insertBtn = createStyledButton("Insert Node", "#4CAF50");
        insertBtn.setOnAction(e -> insertNode());

        Button searchBtn = createStyledButton("Search Node", "#2196F3");
        searchBtn.setOnAction(e -> searchNode());

        Button deleteBtn = createStyledButton("Delete Node", "#f44336");
        deleteBtn.setOnAction(e -> deleteNode());

        operationButtons.getChildren().addAll(operationsLabel, insertBtn, searchBtn, deleteBtn);
        return operationButtons;
    }

    private VBox createTraversalSection() {
        VBox traversalSection = new VBox(10);

        Label traversalLabel = new Label("Tree Traversals:");
        traversalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Button inorderBtn = createStyledButton("Inorder Traversal", "#FF9800");
        inorderBtn.setOnAction(e -> performInorderTraversal());

        Button preorderBtn = createStyledButton("Preorder Traversal", "#9C27B0");
        preorderBtn.setOnAction(e -> performPreorderTraversal());

        Button postorderBtn = createStyledButton("Postorder Traversal", "#607D8B");
        postorderBtn.setOnAction(e -> performPostorderTraversal());

        traversalArea = new TextArea();
        traversalArea.setEditable(false);
        traversalArea.setPrefRowCount(6);
        traversalArea.setPromptText("Traversal results will appear here");

        traversalSection.getChildren().addAll(
                traversalLabel, inorderBtn, preorderBtn, postorderBtn, traversalArea
        );
        return traversalSection;
    }

    private VBox createUtilityButtons() {
        VBox utilityButtons = new VBox(10);

        Label utilityLabel = new Label("Utilities:");
        utilityLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Button clearBtn = createStyledButton("Clear Tree", "#795548");
        clearBtn.setOnAction(e -> clearTree());

        Button randomBtn = createStyledButton("Generate Random Tree", "#009688");
        randomBtn.setOnAction(e -> generateRandomTree());

        utilityButtons.getChildren().addAll(utilityLabel, clearBtn, randomBtn);
        return utilityButtons;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(260);
        button.setPrefHeight(40);
        button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;", color
        ));

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle(String.format(
                "-fx-background-color: derive(%s, -20%%); -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;", color
        )));

        button.setOnMouseExited(e -> button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;", color
        )));

        return button;
    }

    private void insertNode() {
        if (isAnimating) return;

        try {
            int value = Integer.parseInt(inputField.getText().trim());

            // Check if node already exists
            if (searchNodeRecursive(root, value) != null) {
                updateStatus("Node " + value + " already exists!");
                inputField.clear();
                return;
            }

            BSTNode newNode = new BSTNode(value);

            if (root == null) {
                // First node - simple insertion with entrance animation
                root = newNode;
                drawInitialTree();
                animateNodeInsertion(newNode, true);
            } else {
                // Insert and animate repositioning
                root = insertNodeRecursive(root, value);
                animateTreeRestructure("Inserted: " + value);
            }

            inputField.clear();
        } catch (NumberFormatException e) {
            updateStatus("Error: Please enter a valid integer");
        }
    }

    private BSTNode insertNodeRecursive(BSTNode root, int data) {
        if (root == null) {
            return new BSTNode(data);
        }

        if (data < root.data) {
            root.left = insertNodeRecursive(root.left, data);
        } else if (data > root.data) {
            root.right = insertNodeRecursive(root.right, data);
        }

        return root;
    }

    private void deleteNode() {
        if (isAnimating) return;

        try {
            int value = Integer.parseInt(inputField.getText().trim());
            BSTNode nodeToDelete = searchNodeRecursive(root, value);

            if (nodeToDelete != null) {
                // Animate deletion with fade out and scale down
                animateNodeDeletion(nodeToDelete, () -> {
                    root = deleteNodeRecursive(root, value);
                    if (root == null) {
                        drawPane.getChildren().clear();
                        updateStatus("Tree is now empty");
                    } else {
                        animateTreeRestructure("Deleted: " + value);
                    }
                });
            } else {
                updateStatus("Cannot delete: " + value + " not found");
            }
            inputField.clear();
        } catch (NumberFormatException e) {
            updateStatus("Error: Please enter a valid integer");
        }
    }

    private BSTNode deleteNodeRecursive(BSTNode root, int data) {
        if (root == null) {
            return root;
        }

        if (data < root.data) {
            root.left = deleteNodeRecursive(root.left, data);
        } else if (data > root.data) {
            root.right = deleteNodeRecursive(root.right, data);
        } else {
            // Node to be deleted found
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }

            // Node with two children
            root.data = minValue(root.right);
            root.right = deleteNodeRecursive(root.right, root.data);
        }

        return root;
    }

    private int minValue(BSTNode root) {
        int minValue = root.data;
        while (root.left != null) {
            minValue = root.left.data;
            root = root.left;
        }
        return minValue;
    }

    // Animation Methods
    private void animateNodeInsertion(BSTNode newNode, boolean isFirstNode) {
        isAnimating = true;
        if (isFirstNode) {
            // Calculate position for first node
            calculatePositions(); // This sets targetX, targetY
            newNode.x = newNode.targetX; // Initialize logical position
            newNode.y = newNode.targetY;

            // Create visual elements at the target position
            createVisualElements(newNode); // This adds them to drawPane

            // Start with node invisible and scaled down at the correct position
            newNode.visualNode.setOpacity(0);
            newNode.visualNode.setScaleX(0.1);
            newNode.visualNode.setScaleY(0.1);
            newNode.visualText.setOpacity(0);
            // Ensure text position is consistent with scaled circle (might adjust if needed)
            newNode.visualText.setX(newNode.x - (newNode.visualText.getBoundsInLocal().getWidth() / 2) * 0.1); // Scale text position?
            // Simpler: keep text hidden, scale node, then show text at full scale position after.

            // Animate entrance
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), newNode.visualNode);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            FadeTransition textFadeIn = new FadeTransition(Duration.millis(400), newNode.visualText);
            textFadeIn.setFromValue(0);
            textFadeIn.setToValue(1);
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(400), newNode.visualNode);
            scaleUp.setFromX(0.1);
            scaleUp.setFromY(0.1);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);

            // Color animation (optional, remove if not desired initially)
            FillTransition colorTransition = new FillTransition(Duration.millis(600), newNode.visualNode, NEW_NODE_COLOR, NODE_COLOR);
            newNode.visualNode.setFill(NEW_NODE_COLOR); // Set initial color

            ParallelTransition parallelTransition = new ParallelTransition(fadeIn, textFadeIn, scaleUp, colorTransition);
            parallelTransition.setOnFinished(e -> {
                isAnimating = false;
                updateStatus("First node inserted successfully");
                // Ensure final position and text alignment are correct after scaling
                newNode.visualNode.setScaleX(1.0);
                newNode.visualNode.setScaleY(1.0);
                newNode.visualText.setX(newNode.x - newNode.visualText.getBoundsInLocal().getWidth() / 2);
                newNode.visualText.setY(newNode.y + 5);
            });
            parallelTransition.play();
        }
        // Note: For non-first nodes, insertNode calls animateTreeRestructure, so this method only handles the very first one.
    }

    private void animateNodeDeletion(BSTNode nodeToDelete, Runnable onComplete) {
        isAnimating = true;
        // Highlight the node to be deleted
        if (nodeToDelete.visualNode != null) {
            nodeToDelete.visualNode.setFill(Color.LIGHTCORAL);
        }

        // Handle potential NPE if visuals are missing
        if (nodeToDelete.visualNode == null) {
            // If visuals are missing, just run onComplete immediately (shouldn't happen ideally)
            System.err.println("Warning: Attempting to delete node with missing visuals.");
            isAnimating = false;
            onComplete.run();
            return;
        }

        // Create fade out and scale down animations
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), nodeToDelete.visualNode);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        FadeTransition textFadeOut = new FadeTransition(Duration.millis(300), nodeToDelete.visualText);
        textFadeOut.setFromValue(1);
        textFadeOut.setToValue(0);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), nodeToDelete.visualNode);
        scaleDown.setFromX(1.0);
        scaleDown.setFromY(1.0);
        scaleDown.setToX(0.1);
        scaleDown.setToY(0.1);

        // Fade out connecting lines
        List<Animation> lineAnimations = new ArrayList<>();
        if (nodeToDelete.leftLine != null && drawPane.getChildren().contains(nodeToDelete.leftLine)) {
            FadeTransition leftLineFade = new FadeTransition(Duration.millis(300), nodeToDelete.leftLine);
            leftLineFade.setFromValue(1);
            leftLineFade.setToValue(0);
            lineAnimations.add(leftLineFade);
        }
        if (nodeToDelete.rightLine != null && drawPane.getChildren().contains(nodeToDelete.rightLine)) {
            FadeTransition rightLineFade = new FadeTransition(Duration.millis(300), nodeToDelete.rightLine);
            rightLineFade.setFromValue(1);
            rightLineFade.setToValue(0);
            lineAnimations.add(rightLineFade);
        }

        ParallelTransition deleteAnimation = new ParallelTransition();
        deleteAnimation.getChildren().addAll(fadeOut, textFadeOut, scaleDown);
        deleteAnimation.getChildren().addAll(lineAnimations);

        deleteAnimation.setOnFinished(e -> {
            // Remove visual elements from the pane *after* animation
            drawPane.getChildren().removeAll(nodeToDelete.visualNode, nodeToDelete.visualText);
            // Remove lines if they exist and are still in the pane
            if (nodeToDelete.leftLine != null) {
                drawPane.getChildren().remove(nodeToDelete.leftLine);
                nodeToDelete.leftLine = null; // Clear reference
            }
            if (nodeToDelete.rightLine != null) {
                drawPane.getChildren().remove(nodeToDelete.rightLine);
                nodeToDelete.rightLine = null; // Clear reference
            }
            // Clear visual references
            nodeToDelete.visualNode = null;
            nodeToDelete.visualText = null;
            // Proceed with the structural deletion
            onComplete.run();
        });
        deleteAnimation.play();
    }

    private void animateTreeRestructure(String statusMessage) {
        isAnimating = true;

        // Calculate new target positions for all nodes
        calculatePositions();

        // Collect all existing nodes
        List<BSTNode> allNodes = new ArrayList<>();
        collectAllNodes(root, allNodes);

        // Create a single ParallelTransition for all movements/animations
        ParallelTransition repositionAnimation = new ParallelTransition();

        // Determine the center X for new nodes that don't have a previous position
        // Use a fixed point or derive from root if it exists and is stable
        double paneCenterX = drawPane.getWidth() > 1 ? drawPane.getWidth() / 2 : 600; // Fallback if width not set
        double initialY = 50;

        for (BSTNode node : allNodes) {
            // If visual elements don't exist yet (e.g., newly inserted node during restructuring after initial insert)
            // or if they were cleared (which ideally they shouldn't be in this improved version, but check for safety)
            if (node.visualNode == null || node.visualText == null || !drawPane.getChildren().contains(node.visualNode)) {
                // This handles nodes created during restructuring that weren't visually added yet
                // Or nodes whose visuals were somehow removed.
                createVisualElements(node); // Create visuals at target position initially
                node.x = node.targetX; // Sync x,y with target for future animations
                node.y = node.targetY;
                // If it's conceptually "new" in this context (hard to distinguish perfectly without flag),
                // we could apply a brief entrance animation, but it might conflict.
                // Let's assume it's part of the general restructure animation for consistency.
                // Set initial position off-screen or at a starting point if needed, but usually target is fine here.
                // Apply entrance effect if it seems like it was just added (this is tricky without a flag)
                // For now, let's just ensure it's placed correctly for the move animation.
                node.visualNode.setCenterX(node.targetX);
                node.visualNode.setCenterY(node.targetY);
                node.visualText.setX(node.targetX - node.visualText.getBoundsInLocal().getWidth() / 2);
                node.visualText.setY(node.targetY + 5); // Adjust text Y if needed relative to circle

                // Add lines if children exist and lines don't exist yet
                updateLines(node); // This will create lines if needed


                // Add to pane if not already added by createVisualElements (double-check)
                if(!drawPane.getChildren().contains(node.visualNode)) {
                    drawPane.getChildren().addAll(node.visualNode, node.visualText);
                    // Add lines if they were created
                    if(node.leftLine != null && !drawPane.getChildren().contains(node.leftLine)) {
                        drawPane.getChildren().add(0, node.leftLine); // Add lines behind nodes
                    }
                    if(node.rightLine != null && !drawPane.getChildren().contains(node.rightLine)) {
                        drawPane.getChildren().add(0, node.rightLine);
                    }
                }

                // Apply entrance animation if it seems like a genuinely new node
                // Heuristic: if it just got created and is being added now, x,y might be 0 or unset correctly
                // This is not perfect, but a reasonable attempt.
                if (node.x == 0 && node.y == 0 && node != root) { // Not the absolute first node anymore
                    // Assume it's entering, apply entrance animation
                    node.visualNode.setOpacity(0);
                    node.visualNode.setScaleX(0.1);
                    node.visualNode.setScaleY(0.1);
                    node.visualText.setOpacity(0);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), node.visualNode);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    FadeTransition textFadeIn = new FadeTransition(Duration.millis(300), node.visualText);
                    textFadeIn.setFromValue(0);
                    textFadeIn.setToValue(1);
                    ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), node.visualNode);
                    scaleUp.setFromX(0.1);
                    scaleUp.setFromY(0.1);
                    scaleUp.setToX(1.0);
                    scaleUp.setToY(1.0);

                    // Play entrance animation in parallel with potential movement
                    ParallelTransition entranceAnim = new ParallelTransition(fadeIn, textFadeIn, scaleUp);
                    repositionAnimation.getChildren().add(entranceAnim);
                }


                // Now, proceed with potential movement animation if target differs from current (though likely they are the same here)
                // This part handles if the *repositioning* itself needs animation after creation
                double moveDistanceX = node.targetX - node.x;
                double moveDistanceY = node.targetY - node.y;

                if (Math.abs(moveDistanceX) > 0.1 || Math.abs(moveDistanceY) > 0.1) {
                    TranslateTransition moveNode = new TranslateTransition(Duration.millis(500), node.visualNode);
                    moveNode.setFromX(node.x - node.targetX + moveDistanceX); // Correcting logic: from current offset
                    moveNode.setFromY(node.y - node.targetY + moveDistanceY);
                    moveNode.setToX(moveDistanceX);
                    moveNode.setToY(moveDistanceY);

                    TranslateTransition moveText = new TranslateTransition(Duration.millis(500), node.visualText);
                    moveText.setFromX(node.x - node.targetX + moveDistanceX);
                    moveText.setFromY(node.y - node.targetY + moveDistanceY);
                    moveText.setToX(moveDistanceX);
                    moveText.setToY(moveDistanceY);

                    repositionAnimation.getChildren().addAll(moveNode, moveText);
                    // Update node's logical position after animation
                    moveNode.setOnFinished(e -> {
                        node.x = node.targetX;
                        node.y = node.targetY;
                        // Reset translation to 0 after move to make x,y the true center
                        node.visualNode.setTranslateX(0);
                        node.visualNode.setTranslateY(0);
                        node.visualText.setTranslateX(0);
                        node.visualText.setTranslateY(0);
                        // Re-center text correctly after move
                        node.visualText.setX(node.x - node.visualText.getBoundsInLocal().getWidth() / 2);
                        node.visualText.setY(node.y + 5);
                        updateLines(node); // Update lines after move
                    });
                } else {
                    // No significant move, just ensure position is correct logically and visually
                    node.x = node.targetX;
                    node.y = node.targetY;
                    node.visualNode.setCenterX(node.x);
                    node.visualNode.setCenterY(node.y);
                    node.visualText.setX(node.x - node.visualText.getBoundsInLocal().getWidth() / 2);
                    node.visualText.setY(node.y + 5);
                    updateLines(node); // Ensure lines are correct even if no move
                }


            } else {
                // Visual elements exist, update them based on movement

                // --- Entrance Animation for Conceptually New Nodes (Improved Check) ---
                // This is still a bit heuristic. A better way is to pass a flag during insertion.
                // But we can check if the node was just structurally added but visuals were created in a prior step
                // that didn't involve animation (like initial insert). If x,y are still at target but visual
                // properties indicate "new", trigger entrance. Otherwise, assume it's a reposition.
                // Let's simplify: if it's not the root and seems newly placed without animation, do entrance.
                // This is tricky without explicit state. Let's rely more on the initial insert animation handling.

                // --- Repositioning Animation ---
                // Calculate the distance to move based on current *rendered* position and new target.
                // We use TranslateTransition, so we need the delta from the *current effective position*.
                // The effective position is visualNode.getCenterX() + visualNode.getTranslateX()
                // But since we reset translate after moves, it's often just visualNode.getCenterX()
                // However, during an animation, getCenterX is the base, getTranslate is the offset.
                // Let's calculate based on the logical x,y (which should represent the base position after previous moves).
                double moveDistanceX = node.targetX - node.x;
                double moveDistanceY = node.targetY - node.y;

                if (Math.abs(moveDistanceX) > 0.1 || Math.abs(moveDistanceY) > 0.1) {
                    // There is a significant move

                    // Create TranslateTransitions for smooth movement
                    // From current offset (0 if not moving) to the required delta
                    TranslateTransition moveNode = new TranslateTransition(Duration.millis(500), node.visualNode);
                    moveNode.setFromX(node.visualNode.getTranslateX()); // Start from current translation
                    moveNode.setFromY(node.visualNode.getTranslateY());
                    moveNode.setToX(node.visualNode.getTranslateX() + moveDistanceX); // Move by the delta
                    moveNode.setToY(node.visualNode.getTranslateY() + moveDistanceY);

                    TranslateTransition moveText = new TranslateTransition(Duration.millis(500), node.visualText);
                    moveText.setFromX(node.visualText.getTranslateX());
                    moveText.setFromY(node.visualText.getTranslateY());
                    moveText.setToX(node.visualText.getTranslateX() + moveDistanceX);
                    moveText.setToY(node.visualText.getTranslateY() + moveDistanceY);

                    repositionAnimation.getChildren().addAll(moveNode, moveText);

                    // Update node's logical position *after* the animation completes
                    // and reset the translation to 0 to make x,y the new base.
                    moveNode.setOnFinished(e -> {
                        node.x = node.targetX;
                        node.y = node.targetY;
                        // Reset translation to 0 after move to make x,y the true center
                        node.visualNode.setTranslateX(0);
                        node.visualNode.setTranslateY(0);
                        node.visualText.setTranslateX(0);
                        node.visualText.setTranslateY(0);
                        // Re-center text correctly after move (might be redundant if text follows node via translate)
                        // But good to ensure if translate is reset.
                        node.visualText.setX(node.x - node.visualText.getBoundsInLocal().getWidth() / 2);
                        node.visualText.setY(node.y + 5);
                        updateLines(node); // Update lines after move is finalized
                    });

                } else {
                    // No significant movement needed, but ensure lines are updated in case children moved
                    updateLines(node);
                }
            }
        }

        repositionAnimation.setOnFinished(e -> {
            // Final update of logical positions (redundant if done in moveNode.setOnFinished, but safe)
            // Mostly handled in the OnFinished of individual moves now.
            // Ensure any remaining lines are correctly positioned
            for (BSTNode node : allNodes) {
                updateLines(node); // Final line update pass
            }
            isAnimating = false;
            updateStatus(statusMessage);
        });

        repositionAnimation.play();
        currentParallelTransition = repositionAnimation;
    }

    private void collectAllNodes(BSTNode node, List<BSTNode> nodes) {
        if (node == null) return;

        nodes.add(node);
        collectAllNodes(node.left, nodes);
        collectAllNodes(node.right, nodes);
    }

    // Renamed and modified to create elements at the target position and link them correctly
    private void createVisualElements(BSTNode node) {
        if (node == null) return;

        // Create circle at the target position
        Circle nodeCircle = new Circle(node.targetX, node.targetY, NODE_RADIUS);
        nodeCircle.setFill(NODE_COLOR);
        nodeCircle.setStroke(NODE_BORDER);
        nodeCircle.setStrokeWidth(3);
        node.visualNode = nodeCircle;

        // Create text at the target position
        Text nodeText = new Text(node.targetX, node.targetY + 5, String.valueOf(node.data));
        nodeText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        // Center the text horizontally
        nodeText.setX(nodeText.getX() - nodeText.getBoundsInLocal().getWidth() / 2);
        node.visualText = nodeText;

        // Note: Lines are handled separately in drawTree or updateLines
        // Add elements to the pane
        drawPane.getChildren().addAll(node.visualNode, node.visualText);
    }

    private void drawInitialTree() {
        drawPane.getChildren().clear();

        if (root == null) {
            return;
        }

        // Calculate positions
        calculatePositions();

        // Draw the tree
        drawTree(root);
    }

    // Search and traversal methods remain the same
    private void searchNode() {
        if (isAnimating) return;

        try {
            int value = Integer.parseInt(inputField.getText().trim());
            BSTNode found = searchNodeRecursive(root, value);

            if (found != null) {
                updateStatus("Found: " + value);
                animateSearch(value);
            } else {
                updateStatus("Not Found: " + value);
            }
            inputField.clear();
        } catch (NumberFormatException e) {
            updateStatus("Error: Please enter a valid integer");
        }
    }

    private BSTNode searchNodeRecursive(BSTNode root, int data) {
        if (root == null || root.data == data) {
            return root;
        }

        if (data < root.data) {
            return searchNodeRecursive(root.left, data);
        }

        return searchNodeRecursive(root.right, data);
    }

    private void performInorderTraversal() {
        if (isAnimating) return;

        List<Integer> result = new ArrayList<>();
        inorderTraversal(root, result);

        String traversalResult = "Inorder: " + result.toString();
        traversalArea.setText(traversalResult);
        updateStatus("Inorder traversal completed");

        animateTraversal(result, "Inorder");
    }

    private void inorderTraversal(BSTNode root, List<Integer> result) {
        if (root != null) {
            inorderTraversal(root.left, result);
            result.add(root.data);
            inorderTraversal(root.right, result);
        }
    }

    private void performPreorderTraversal() {
        if (isAnimating) return;

        List<Integer> result = new ArrayList<>();
        preorderTraversal(root, result);

        String traversalResult = "Preorder: " + result.toString();
        traversalArea.setText(traversalResult);
        updateStatus("Preorder traversal completed");

        animateTraversal(result, "Preorder");
    }

    private void preorderTraversal(BSTNode root, List<Integer> result) {
        if (root != null) {
            result.add(root.data);
            preorderTraversal(root.left, result);
            preorderTraversal(root.right, result);
        }
    }

    private void performPostorderTraversal() {
        if (isAnimating) return;

        List<Integer> result = new ArrayList<>();
        postorderTraversal(root, result);

        String traversalResult = "Postorder: " + result.toString();
        traversalArea.setText(traversalResult);
        updateStatus("Postorder traversal completed");

        animateTraversal(result, "Postorder");
    }

    private void postorderTraversal(BSTNode root, List<Integer> result) {
        if (root != null) {
            postorderTraversal(root.left, result);
            postorderTraversal(root.right, result);
            result.add(root.data);
        }
    }

    private void clearTree() {
        if (isAnimating) return;

        if (root == null) {
            updateStatus("Tree is already empty");
            return;
        }

        isAnimating = true;

        // Animate all nodes fading out
        List<BSTNode> allNodes = new ArrayList<>();
        collectAllNodes(root, allNodes);

        ParallelTransition clearAnimation = new ParallelTransition();

        for (BSTNode node : allNodes) {
            if (node.visualNode != null) {
                FadeTransition nodeFade = new FadeTransition(Duration.millis(300), node.visualNode);
                nodeFade.setFromValue(1);
                nodeFade.setToValue(0);

                FadeTransition textFade = new FadeTransition(Duration.millis(300), node.visualText);
                textFade.setFromValue(1);
                textFade.setToValue(0);

                ScaleTransition nodeScale = new ScaleTransition(Duration.millis(300), node.visualNode);
                nodeScale.setFromX(1.0);
                nodeScale.setFromY(1.0);
                nodeScale.setToX(0.1);
                nodeScale.setToY(0.1);

                clearAnimation.getChildren().addAll(nodeFade, textFade, nodeScale);
            }

            if (node.leftLine != null) {
                FadeTransition lineFade = new FadeTransition(Duration.millis(300), node.leftLine);
                lineFade.setFromValue(1);
                lineFade.setToValue(0);
                clearAnimation.getChildren().add(lineFade);
            }

            if (node.rightLine != null) {
                FadeTransition lineFade = new FadeTransition(Duration.millis(300), node.rightLine);
                lineFade.setFromValue(1);
                lineFade.setToValue(0);
                clearAnimation.getChildren().add(lineFade);
            }
        }

        clearAnimation.setOnFinished(e -> {
            root = null;
            drawPane.getChildren().clear();
            traversalArea.clear();
            isAnimating = false;
            updateStatus("Tree cleared with animation");
        });

        clearAnimation.play();
    }

    private void generateRandomTree() {
        if (isAnimating) return;

        clearTree();

        // Wait for clear animation to finish, then generate
        Timeline delayedGeneration = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            Random random = new Random();
            Set<Integer> values = new HashSet<>();

            // Generate 5-8 unique random values
            int numNodes = 5 + random.nextInt(4);
            while (values.size() < numNodes) {
                values.add(random.nextInt(50) + 1);
            }

            // Convert to list and sort to create a more balanced tree
            List<Integer> valuesList = new ArrayList<>(values);
            Collections.sort(valuesList);

            // Insert in a way that creates a more balanced tree
            insertBalanced(valuesList, 0, valuesList.size() - 1);

            visualizeTree();
            updateStatus("Generated random tree with " + numNodes + " nodes");
        }));

        delayedGeneration.play();
    }

    private void insertBalanced(List<Integer> values, int start, int end) {
        if (start > end) return;

        int mid = (start + end) / 2;
        root = insertNodeRecursive(root, values.get(mid));

        insertBalanced(values, start, mid - 1);
        insertBalanced(values, mid + 1, end);
    }

    private void visualizeTree() {
        drawPane.getChildren().clear();

        if (root == null) {
            return;
        }

        // Calculate positions
        calculatePositions();

        // Draw the tree
        drawTree(root);
    }

    private void calculatePositions() {
        if (root == null) return;

        // Calculate tree dimensions more conservatively
        int depth = getTreeDepth(root);
        int nodeCount = getNodeCount(root);

        // Calculate more reasonable tree width based on actual nodes
        double baseWidth = Math.max(nodeCount * MIN_HORIZONTAL_GAP * 2, 800);
        double maxWidth = 1200; // Limit maximum width
        double treeWidth = Math.min(baseWidth, maxWidth);

        // Set pane size with reasonable bounds
        double paneWidth = Math.max(treeWidth + 100, 1000);
        double paneHeight = Math.max(depth * LEVEL_HEIGHT + 100, 600);

        drawPane.setPrefSize(paneWidth, paneHeight);

        // Start positioning from center with more conservative initial offset
        double initialOffset = Math.min(treeWidth / 6, 150);
        setNodePositions(root, paneWidth / 2, 40, initialOffset);
    }

    private int getTreeDepth(BSTNode node) {
        if (node == null) return 0;
        return 1 + Math.max(getTreeDepth(node.left), getTreeDepth(node.right));
    }

    private int getNodeCount(BSTNode node) {
        if (node == null) return 0;
        return 1 + getNodeCount(node.left) + getNodeCount(node.right);
    }

    private void setNodePositions(BSTNode node, double x, double y, double offset) {
        if (node == null) return;

        node.targetX = x;
        node.targetY = y;

        // Reduce offset more aggressively and ensure minimum spacing
        double newOffset = Math.max(offset / 1.8, MIN_HORIZONTAL_GAP);

        if (node.left != null) {
            setNodePositions(node.left, x - newOffset, y + LEVEL_HEIGHT, newOffset);
        }

        if (node.right != null) {
            setNodePositions(node.right, x + newOffset, y + LEVEL_HEIGHT, newOffset);
        }
    }

    // Modified to reuse existing visual elements and only create/update lines
    private void drawTree(BSTNode node) {
        if (node == null) return;

        // Ensure visual elements exist (should be handled before calling drawTree, e.g., in createVisualElements or animateTreeRestructure)
        // If they don't exist here, it's likely the initial draw. Create them.
        if (node.visualNode == null || node.visualText == null) {
            createVisualElements(node); // This sets x,y to targetX,targetY initially
            node.x = node.targetX; // Sync logical position
            node.y = node.targetY;
        } else {
            // If they exist, update their position to the target (initial draw scenario)
            // This might be redundant if animateTreeRestructure handles it, but safe for direct calls.
            node.visualNode.setCenterX(node.targetX);
            node.visualNode.setCenterY(node.targetY);
            node.visualText.setX(node.targetX - node.visualText.getBoundsInLocal().getWidth() / 2);
            node.visualText.setY(node.targetY + 5);
            node.x = node.targetX; // Sync logical
            node.y = node.targetY;
        }

        // Update or create lines based on current target positions of children
        updateLines(node);

        // Recursively draw children (their visuals should be handled by animateTreeRestructure or this initial call)
        drawTree(node.left);
        drawTree(node.right);
    }

    // Helper method to create/update lines for a node
    private void updateLines(BSTNode node) {
        if (node == null) return;

        // Remove old lines from pane if they exist
        if (node.leftLine != null && drawPane.getChildren().contains(node.leftLine)) {
            drawPane.getChildren().remove(node.leftLine);
        }
        if (node.rightLine != null && drawPane.getChildren().contains(node.rightLine)) {
            drawPane.getChildren().remove(node.rightLine);
        }

        // Create new lines based on target positions
        if (node.left != null) {
            Line leftLine = new Line(node.targetX, node.targetY, node.left.targetX, node.left.targetY);
            leftLine.setStroke(LINE_COLOR);
            leftLine.setStrokeWidth(2);
            node.leftLine = leftLine;
            // Add line behind the nodes
            drawPane.getChildren().add(0, leftLine);
        }
        if (node.right != null) {
            Line rightLine = new Line(node.targetX, node.targetY, node.right.targetX, node.right.targetY);
            rightLine.setStroke(LINE_COLOR);
            rightLine.setStrokeWidth(2);
            node.rightLine = rightLine;
            // Add line behind the nodes
            drawPane.getChildren().add(0, rightLine);
        }
    }

    private void animateSearch(int value) {
        isAnimating = true;

        List<BSTNode> searchPath = new ArrayList<>();
        findSearchPath(root, value, searchPath);

        if (searchPath.isEmpty()) {
            isAnimating = false;
            return;
        }

        // Create sequential animation for search path
        Timeline searchAnimation = new Timeline();

        for (int i = 0; i < searchPath.size(); i++) {
            BSTNode node = searchPath.get(i);

            // Highlight animation for each node in path
            int finalI = i;
            KeyFrame highlightFrame = new KeyFrame(
                    Duration.millis(i * 500),
                    e -> {
                        if (node.visualNode != null) {
                            // Highlight the node
                            node.visualNode.setFill(HIGHLIGHT_COLOR);

                            // Scale animation
                            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), node.visualNode);
                            scaleUp.setFromX(1.0);
                            scaleUp.setFromY(1.0);
                            scaleUp.setToX(1.3);
                            scaleUp.setToY(1.3);
                            scaleUp.setAutoReverse(true);
                            scaleUp.setCycleCount(2);
                            scaleUp.play();

                            // Reset color after animation
                            Timeline resetColor = new Timeline(new KeyFrame(
                                    Duration.millis(400),
                                    resetEvent -> {
                                        if (finalI == searchPath.size() - 1) {
                                            // Keep final node highlighted longer
                                            node.visualNode.setFill(Color.LIGHTGREEN);
                                        } else {
                                            node.visualNode.setFill(NODE_COLOR);
                                        }
                                    }
                            ));
                            resetColor.play();
                        }
                    }
            );

            searchAnimation.getKeyFrames().add(highlightFrame);
        }

        // Final cleanup
        KeyFrame finalFrame = new KeyFrame(
                Duration.millis(searchPath.size() * 500 + 1000),
                e -> {
                    // Reset all node colors
                    resetAllNodeColors();
                    isAnimating = false;
                }
        );

        searchAnimation.getKeyFrames().add(finalFrame);
        searchAnimation.play();
    }

    private void findSearchPath(BSTNode node, int value, List<BSTNode> path) {
        if (node == null) return;

        path.add(node);

        if (value == node.data) {
            return;
        } else if (value < node.data) {
            findSearchPath(node.left, value, path);
        } else {
            findSearchPath(node.right, value, path);
        }
    }

    private void animateTraversal(List<Integer> traversalOrder, String traversalType) {
        isAnimating = true;

        // Reset all colors first
        resetAllNodeColors();

        Timeline traversalAnimation = new Timeline();

        for (int i = 0; i < traversalOrder.size(); i++) {
            int value = traversalOrder.get(i);
            BSTNode node = searchNodeRecursive(root, value);

            if (node != null) {
                int finalI = i;
                KeyFrame highlightFrame = new KeyFrame(
                        Duration.millis(i * 600),
                        e -> {
                            // Highlight current node
                            node.visualNode.setFill(HIGHLIGHT_COLOR);

                            // Pulse animation
                            ScaleTransition pulse = new ScaleTransition(Duration.millis(300), node.visualNode);
                            pulse.setFromX(1.0);
                            pulse.setFromY(1.0);
                            pulse.setToX(1.4);
                            pulse.setToY(1.4);
                            pulse.setAutoReverse(true);
                            pulse.setCycleCount(2);
                            pulse.play();

                            // Update status with current step
                            updateStatus(traversalType + " - Step " + (finalI + 1) + ": Visiting node " + value);
                        }
                );

                traversalAnimation.getKeyFrames().add(highlightFrame);

                // Reset color after delay
                KeyFrame resetFrame = new KeyFrame(
                        Duration.millis(i * 600 + 500),
                        e -> node.visualNode.setFill(NODE_COLOR)
                );

                traversalAnimation.getKeyFrames().add(resetFrame);
            }
        }

        // Final completion frame
        KeyFrame completionFrame = new KeyFrame(
                Duration.millis(traversalOrder.size() * 600 + 500),
                e -> {
                    isAnimating = false;
                    updateStatus(traversalType + " traversal animation completed");
                }
        );

        traversalAnimation.getKeyFrames().add(completionFrame);
        traversalAnimation.play();
    }

    private void resetAllNodeColors() {
        List<BSTNode> allNodes = new ArrayList<>();
        collectAllNodes(root, allNodes);

        for (BSTNode node : allNodes) {
            if (node.visualNode != null) {
                node.visualNode.setFill(NODE_COLOR);
            }
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private String createCSS() {
        return "data:text/css," +
                ".button:hover { -fx-cursor: hand; }" +
                ".text-field { -fx-font-size: 14px; }" +
                ".text-area { -fx-font-size: 12px; -fx-font-family: monospace; }";
    }
}