package com.example.demo.queue;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class queuelist {

    /* ─────────────────────────────  CONSTANTS  ───────────────────────────── */
    private static final double BASE_X       = 70;   // where the first node appears
    private static final double BASE_Y       = 50;
    private static final double NODE_WIDTH   = 50;
    private static final double NODE_SPACING = 80;   // NODE_WIDTH + horizontal gap

    /* ─────────────────────────────  FXML HOOKS  ──────────────────────────── */
    @FXML private Label status;
    @FXML private HBox  buttonContainer;
    @FXML private TextField inputField;
    @FXML private AnchorPane linkedListContainer;
    @FXML private Label nullLabel;

    @FXML private Label frontPointer;
    @FXML private Label rearPointer;

    /* ─────────────────────────────  DATA STRUCTURES  ─────────────────────── */
    private final Queue<Button> dynamicButtons   = new LinkedList<>();

    private final List<Button>  linkedListNodes  = new ArrayList<>();
    private final List<Line>    connectionLines  = new ArrayList<>();
    private final List<Polygon> arrowHeads       = new ArrayList<>();

    /* ─────────────────────────────  INITIALISE  ──────────────────────────── */
    @FXML
    public void initialize() {
        frontPointer.setVisible(false);
        rearPointer.setVisible(false);

        /* place the NULL marker at its baseline position */
        nullLabel.setLayoutX(BASE_X);
        nullLabel.setLayoutY(BASE_Y + 15);
        nullLabel.setVisible(true);

        buttonContainer.setSpacing(15);
    }

    /* ─────────────────────────────  INPUT HANDLERS  ──────────────────────── */
    @FXML
    private void handleTextFieldKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEnqueueClicked();
        }
    }

    @FXML
    private void onEnqueueClicked() {
        String label = inputField.getText().trim();
        if (label.isEmpty()) return;

        /* ---------- visual button in the queue strip ---------- */
        Button btn = new Button(label);
        btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2196F3, #1976D2);" +
                        "-fx-background-radius: 12;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 15 25;" +
                        "-fx-min-width: 70;" +
                        "-fx-max-width: 70;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        );
        /* simple hover effect */
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #64B5F6, #42A5F5);" +
                        "-fx-background-radius: 12;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 15 25;" +
                        "-fx-min-width: 70;" +
                        "-fx-max-width: 70;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 6);" +
                        "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2196F3, #1976D2);" +
                        "-fx-background-radius: 12;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 15 25;" +
                        "-fx-min-width: 70;" +
                        "-fx-max-width: 70;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        ));

        /* enqueue-strip animation */
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), btn);
        scaleIn.setFromX(0.8); scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);   scaleIn.setToY(1.0);
        FadeTransition  fadeIn = new FadeTransition(Duration.millis(200), btn);
        fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);

        dynamicButtons.add(btn);
        buttonContainer.getChildren().add(btn);
        scaleIn.play(); fadeIn.play();

        /* ---------- linked-list canvas ---------- */
        addToLinkedListEnd(label);
        inputField.clear();
        updateQueuePointers();               // moves FRONT / REAR labels
        updateNullPointerPosition();         // keeps “NULL” correctly placed
    }

    @FXML
    private void onDequeueClicked() {
        if (dynamicButtons.isEmpty()) return;

        Button frontBtn = dynamicButtons.peek();          // don't remove yet

        ParallelTransition anim = new ParallelTransition();

        /* fade + shrink the front button */
        FadeTransition  fadeOut  = new FadeTransition(Duration.millis(300), frontBtn);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(300), frontBtn);
        fadeOut.setToValue(0.0);
        scaleOut.setToX(0.5); scaleOut.setToY(0.5);
        anim.getChildren().addAll(fadeOut, scaleOut);

        /* shift remaining buttons in the strip */
        double shift = -(frontBtn.getWidth() + buttonContainer.getSpacing());
        List<Node> children = buttonContainer.getChildren();
        for (int i = 1; i < children.size(); i++) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(300), children.get(i));
            tt.setByX(shift);
            anim.getChildren().add(tt);
        }

        /* after animation finishes, update data + linked list */
        anim.setOnFinished(e -> {
            dynamicButtons.remove();
            buttonContainer.getChildren().remove(frontBtn);

            // reset any residual translate so HBox can lay out naturally
            for (Node node : buttonContainer.getChildren()) node.setTranslateX(0);

            removeFromLinkedListFront();     // visual list + lines
            updateQueuePointers();
            updateNullPointerPosition();
        });

        anim.play();
    }

    /* ─────────────────────────────  POINTER LABELS  ──────────────────────── */
    private void updateQueuePointers() {
        int size = dynamicButtons.size();
        if (size == 0) {
            frontPointer.setVisible(false);
            rearPointer.setVisible(false);
            return;
        }

        double frontX = buttonContainer.getLayoutX();
        double rearX  = frontX + (size - 1) * (70 + buttonContainer.getSpacing());

        rearPointer.setText("REAR ▼");
        frontPointer.setVisible(true);
        rearPointer.setVisible(true);

        if (size == 1) {
            frontPointer.setText("FRONT / REAR ▼");
            rearPointer.setVisible(false);
        } else {
            frontPointer.setText("FRONT ▼");
        }
        animatePointer(frontPointer, frontX);
        animatePointer(rearPointer,  rearX);
    }
    private void animatePointer(Label pointer, double targetX) {
        Timeline tl = new Timeline(new KeyFrame(
                Duration.millis(300),
                new KeyValue(pointer.layoutXProperty(), targetX, Interpolator.EASE_BOTH)
        ));
        tl.play();
    }

    /* ─────────────────────────────  NULL MARKER  ─────────────────────────── */
    private void updateNullPointerPosition() {
        if (linkedListNodes.isEmpty()) {
            nullLabel.setVisible(true);
            animatePointer(nullLabel, BASE_X);          // move back to baseline
            return;
        }
        Button last = linkedListNodes.get(linkedListNodes.size() - 1);
        double targetX = last.getLayoutX() + NODE_SPACING;
        nullLabel.setVisible(true);                     // ensure it’s on
        animatePointer(nullLabel, targetX);
    }

    /* ─────────────────────────────  LINKED-LIST OPS  ─────────────────────── */
    private void addToLinkedListEnd(String value) {

        /* create and position the node */
        Button node = new Button(value);
        node.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049);" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 12;" +
                        "-fx-min-width: 50;" +
                        "-fx-max-width: 50;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );
        double nodeX = BASE_X + linkedListNodes.size() * NODE_SPACING;
        node.setLayoutX(nodeX);
        node.setLayoutY(BASE_Y);

        linkedListContainer.getChildren().add(node);
        linkedListNodes.add(node);

        nullLabel.setVisible(false);                    // pushed further right
        redrawConnections();                            // refresh all arrows
    }

    private void removeFromLinkedListFront() {
        if (linkedListNodes.isEmpty()) {
            nullLabel.setVisible(true);
            return;
        }

        /* elements to be removed */
        Button  nodeToRemove = linkedListNodes.get(0);
        Line    lineToRemove = connectionLines.isEmpty() ? null : connectionLines.get(0);
        Polygon arrowToRemove = arrowHeads.isEmpty() ? null : arrowHeads.get(0);

        ParallelTransition all = new ParallelTransition();

        /* fade-out front node */
        FadeTransition fadeNode = new FadeTransition(Duration.millis(20), nodeToRemove);
        fadeNode.setToValue(0.0);
        all.getChildren().add(fadeNode);

// fade-out the outgoing connector (if any)
        if (lineToRemove != null && arrowToRemove != null) {
            FadeTransition fadeLine  = new FadeTransition(Duration.millis(20), lineToRemove);
            fadeLine.setToValue(0.0);

            FadeTransition fadeArrow = new FadeTransition(Duration.millis(20), arrowToRemove);
            fadeArrow.setToValue(0.0);

            all.getChildren().addAll(fadeLine, fadeArrow);
        }
        /* shift everything else left by one slot */
        for (int i = 1; i < linkedListNodes.size(); i++) {
            TranslateTransition t = new TranslateTransition(Duration.millis(400), linkedListNodes.get(i));
            t.setByX(-NODE_SPACING);
            t.setInterpolator(Interpolator.EASE_BOTH);
            all.getChildren().add(t);
        }
        for (int i = 1; i < connectionLines.size(); i++) {
            TranslateTransition tL = new TranslateTransition(Duration.millis(400), connectionLines.get(i));
            TranslateTransition tA = new TranslateTransition(Duration.millis(400), arrowHeads.get(i));
            tL.setByX(-NODE_SPACING); tA.setByX(-NODE_SPACING);
            tL.setInterpolator(Interpolator.EASE_BOTH);
            tA.setInterpolator(Interpolator.EASE_BOTH);
            all.getChildren().addAll(tL, tA);
        }

        /* clean-up & redraw */
        all.setOnFinished(e -> {
            linkedListContainer.getChildren().remove(nodeToRemove);
            if (lineToRemove != null)  linkedListContainer.getChildren().remove(lineToRemove);
            if (arrowToRemove != null) linkedListContainer.getChildren().remove(arrowToRemove);

            linkedListNodes.remove(0);
            if (!connectionLines.isEmpty()) connectionLines.remove(0);
            if (!arrowHeads.isEmpty())     arrowHeads.remove(0);

            /* bake translation into layoutX for remaining nodes */
            for (Node n : linkedListNodes) {
                n.setLayoutX(n.getLayoutX() + n.getTranslateX());
                n.setTranslateX(0);
            }
            redrawConnections();
            updateNullPointerPosition();
        });

        all.play();
    }

    private void redrawConnections() {
        /* erase everything first */
        linkedListContainer.getChildren().removeAll(connectionLines);
        linkedListContainer.getChildren().removeAll(arrowHeads);
        connectionLines.clear();
        arrowHeads.clear();

        /* rebuild */
        for (int i = 0; i < linkedListNodes.size() - 1; i++) {
            Button current = linkedListNodes.get(i);
            Button next    = linkedListNodes.get(i + 1);

            double startX = current.getLayoutX() + NODE_WIDTH;
            double startY = current.getLayoutY() + 15;
            double endX   = next.getLayoutX();
            double endY   = next.getLayoutY() + 15;

            Line line = new Line(startX, startY, endX, endY);
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(2);

            Polygon arrow = new Polygon(
                    endX - 8, endY - 5,
                    endX - 8, endY + 5,
                    endX+3,     endY
            );
            arrow.setFill(Color.BLACK);

            linkedListContainer.getChildren().addAll(line, arrow);
            connectionLines.add(line);
            arrowHeads.add(arrow);
        }
    }
}
