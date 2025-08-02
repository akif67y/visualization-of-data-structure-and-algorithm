package com.example.demo.dynamicProgramming;

import javafx.animation.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class editdistance {

    @FXML
    private TextField s1;
    @FXML
    private TextField s2;
    @FXML
    private Pane drawingPane;
    @FXML
    private Label resultLabel;
    @FXML
    private Label comparison;
    @FXML
    private Label answer1;
    @FXML
    private Label answer2;
    String k1 = "";
    String k2 = "";

    private String t1 = "";
    private String t2 = "";
    private int m = 0;
    private int n = 0;

    private static final double CELL_WIDTH = 50.0;
    private static final double CELL_HEIGHT = 30.0;
    private int GRID_ROWS = 0;
    private int GRID_COLS = 0;
    private static final double GRID_POS_X = 10.0;
    private static final double GRID_POS_Y = 10.0;

    private Map<Pair<Integer, Integer>, Label> cellMap;
    private int[][] dpTable;

    private final int ANIM_SPEED = 500; // milliseconds

    @FXML
    public void onString1Enter(ActionEvent event) {
        if (s1 != null && !s1.getText().trim().isEmpty()) {
            t1 = s1.getText().trim();
            m = t1.length();
            GRID_ROWS = m + 2;
            showAlert("Success", "String 1 set: " + t1);
            s1.clear();
        } else {
            showAlert("Error", "Please enter a valid string for String 1");
        }
    }

    @FXML
    public void onString2Enter(ActionEvent event) {
        if (s2 != null && !s2.getText().trim().isEmpty()) {
            t2 = s2.getText().trim();
            n = t2.length();
            GRID_COLS = n + 2;
            showAlert("Success", "String 2 set: " + t2);
            s2.clear();
        } else {
            showAlert("Error", "Please enter a valid string for String 2");
        }
    }

    @FXML
    private void onGenerate(ActionEvent event) {
        if (drawingPane == null) {
            System.err.println("Error: drawingPane is not initialized.");
            showAlert("Error", "Drawing pane not initialized");
            return;
        }

        if (t1.isEmpty() || t2.isEmpty()) {
            showAlert("Error", "Please set both strings before generating the table");
            return;
        }

        drawingPane.getChildren().clear();
        cellMap = new HashMap<>();
        generateEditDistanceTable();
    }

    @FXML
    private void extractOperations() {
        k1 = "";
        k2 = "";
        if (dpTable == null) {
            showAlert("Error", "Generate the table first!");
            return;
        }

        StringBuilder path = new StringBuilder();
        SequentialTransition sequence = new SequentialTransition();
        int i = m + 1;
        int j = n + 1;

        while (i > 1 || j > 1) {
            sequence.getChildren().add(createHighlightAnimation(cellMap.get(new Pair<>(i, j))));
            final int finali = i;
            final int finalj = j;
            if (i > 1 && j > 1 && t1.charAt(i - 2) == t2.charAt(j - 2)) {
                //final String ch = String.valueOf(t1.charAt(i - 2));
                Timeline tl = new Timeline(
                        new KeyFrame(Duration.millis(ANIM_SPEED), e -> {
                            k1 = t1.charAt(finali - 2) + k1;
                            answer1.setText(k1);
                            k2 = t2.charAt(finalj - 2) + k2;
                            answer2.setText(k2);
                        })
                );
                sequence.getChildren().add(tl);
                i--; j--;
            } else if (i > 1 && j > 1 && dpTable[i][j] == dpTable[i-1][j-1] + 1) {
                Timeline tl = new Timeline(
                        new KeyFrame(Duration.millis(ANIM_SPEED), e -> {
                            k1 = t1.charAt(finali - 2) + k1;
                            answer1.setText(k1);
                            k2 = t2.charAt(finalj - 2) + k2;
                            answer2.setText(k2);
                        })
                );
                sequence.getChildren().add(tl);
                i--; j--;
            } else if (i > 1 && dpTable[i][j] == dpTable[i-1][j] + 1) {
                Timeline tl = new Timeline(
                        new KeyFrame(Duration.millis(ANIM_SPEED), e -> {
                            k1 = t1.charAt(finali - 2) + k1;
                            answer1.setText(k1);
                            k2 = "_" + k2;
                            answer2.setText(k2);
                        })
                );
                sequence.getChildren().add(tl);
                i--;
            } else if (j > 1 && dpTable[i][j] == dpTable[i][j-1] + 1) {
                Timeline tl = new Timeline(
                        new KeyFrame(Duration.millis(ANIM_SPEED), e -> {
                            k1 = "_" + k1;
                            answer1.setText(k1);
                            k2 = t2.charAt(finalj-2) + k2;
                            answer2.setText(k2);
                        })
                );
                sequence.getChildren().add(tl);
                j--;
            } else {
                break;
            }
        }

        if (!sequence.getChildren().isEmpty()) {
            sequence.play();
        }
    }

    private void generateEditDistanceTable() {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setStyle("-fx-border-color: black; -fx-border-width: 2px;");

        for (int i = 0; i < GRID_COLS; i++) {
            ColumnConstraints col = new ColumnConstraints(CELL_WIDTH);
            grid.getColumnConstraints().add(col);
        }
        for (int i = 0; i < GRID_ROWS; i++) {
            RowConstraints row = new RowConstraints(CELL_HEIGHT);
            grid.getRowConstraints().add(row);
        }

        // Initialize cell map
        cellMap = new HashMap<>();

        // Top-left cell
        Label emptyCell = new Label("ed");
        styleAndAddCell(emptyCell, grid, 0, 0);
        cellMap.put(new Pair<>(0, 0), emptyCell);

        // Column headers (t2)
        for (int j = 2; j < GRID_COLS; j++) {
            Label lbl = new Label(String.valueOf(t2.charAt(j - 2)));
            lbl.setStyle("-fx-alignment: center; -fx-border-color: gray; -fx-border-width: 0.5px; -fx-background-color: #e0e0e0; -fx-font-weight: bold;");
            lbl.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
            cellMap.put(new Pair<>(0, j), lbl);
            grid.add(lbl, j, 0);
        }

        // Row headers (t1)
        for (int i = 2; i < GRID_ROWS; i++) {
            Label lbl = new Label(String.valueOf(t1.charAt(i - 2)));
            lbl.setStyle("-fx-alignment: center; -fx-border-color: gray; -fx-border-width: 0.5px; -fx-background-color: #e0e0e0; -fx-font-weight: bold;");
            lbl.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
            cellMap.put(new Pair<>(i, 0), lbl);
            grid.add(lbl, 0, i);
        }

        // Create DP table
        dpTable = new int[GRID_ROWS][GRID_COLS];

        // Initialize base cases
        for (int j = 1; j < GRID_COLS; j++) {
            dpTable[1][j] = j - 1;
            Label lbl = new Label(String.valueOf(j - 1));
            styleAndAddCell(lbl, grid, j, 1);
            cellMap.put(new Pair<>(1, j), lbl);
        }
        for (int i = 1; i < GRID_ROWS; i++) {
            dpTable[i][1] = i - 1;
            Label lbl = new Label(String.valueOf(i - 1));
            styleAndAddCell(lbl, grid, 1, i);
            cellMap.put(new Pair<>(i, 1), lbl);
        }

        // Null cells
        Label null1 = new Label("null");
        styleAndAddCell(null1, grid, 1, 0);
        cellMap.put(new Pair<>(0, 1), null1);

        Label null2 = new Label("null");
        styleAndAddCell(null2, grid, 0, 1);
        cellMap.put(new Pair<>(1, 0), null2);

        // Animation sequence
        SequentialTransition sequence = new SequentialTransition();

        for (int i = 2; i < GRID_ROWS; i++) {
            for (int j = 2; j < GRID_COLS; j++) {
                final int ifinal = i, jfinal = j;
                char c1 = t1.charAt(i - 2);
                char c2 = t2.charAt(j - 2);

                if (c1 == c2) {
                    dpTable[i][j] = dpTable[i-1][j-1]; // no cost
                    Timeline update = new Timeline(
                            new KeyFrame(Duration.millis(ANIM_SPEED), e ->comparison.setText(c1 + " == " + c2 + " → no cost")));
                    sequence.getChildren().add(update);

                } else {
                    dpTable[i][j] = 1 + Math.min(
                            Math.min(dpTable[i-1][j], dpTable[i][j-1]),
                            dpTable[i-1][j-1]
                    );
                    Timeline update = new Timeline(
                            new KeyFrame(Duration.millis(ANIM_SPEED), e ->comparison.setText("Replace/Delete/Insert to turn '" + c1 + "' into '" + c2 + "'")));
                            sequence.getChildren().add(update);
                   // comparison.setText("Replace/Delete/Insert to turn '" + c1 + "' into '" + c2 + "'");
                }

                Label cell = new Label("");
                cell.setOpacity(1.0);
                styleAndAddCell(cell, grid, j, i);
                cellMap.put(new Pair<>(i, j), cell);

                sequence.getChildren().addAll(createHighlightAnimation(cell));
                sequence.getChildren().addAll(createHighlightAnimation(cellMap.get(new Pair<>(0, j))));
                sequence.getChildren().addAll(createHighlightAnimation(cellMap.get(new Pair<>(i, 0))));

//                ParallelTransition pt = new ParallelTransition();
//                pt.getChildren().addAll();
                sequence.getChildren().add(createHighlightAnimation(comparison));

                // Highlight dependency cells
                if (c1 == c2) {
                    sequence.getChildren().addAll(createHighlightAnimation(cellMap.get(new Pair<>(i-1, j-1))));
                } else {
                    int up = dpTable[i-1][j];
                    int left = dpTable[i][j-1];
                    int diag = dpTable[i-1][j-1];

                    int min = Math.min(Math.min(up, left), diag);
                    if (min == up) {
                        sequence.getChildren().addAll(createHighlightAnimation(cellMap.get(new Pair<>(i-1, j))));
                    } else if (min == left) {
                        sequence.getChildren().addAll(createHighlightAnimation(cellMap.get(new Pair<>(i, j-1))));
                    } else {
                        sequence.getChildren().addAll(createHighlightAnimation(cellMap.get(new Pair<>(i-1, j-1))));
                    }
                }



                // Finalize value
                Timeline update = new Timeline(
                        new KeyFrame(Duration.millis(ANIM_SPEED), e -> cell.setText(String.valueOf(dpTable[ifinal][jfinal])))
                );
                sequence.getChildren().add(update);
                sequence.getChildren().add(new PauseTransition(Duration.millis(ANIM_SPEED)));
            }
        }

        grid.setLayoutX(GRID_POS_X);
        grid.setLayoutY(GRID_POS_Y);
        drawingPane.getChildren().add(grid);

        sequence.setOnFinished(e -> {
            resultLabel.setText(String.valueOf(dpTable[m+1][n+1]));
        });

        if (!sequence.getChildren().isEmpty()) {
            System.out.println("Starting Edit Distance animation...");
            sequence.play();
        }
    }

    private void styleAndAddCell(Label cell, GridPane grid, int col, int row) {
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
        cell.setStyle("-fx-alignment: center; -fx-border-color: gray; -fx-border-width: 1px; -fx-background-color: #f9f9f9;");
        grid.add(cell, col, row);
    }

    private SequentialTransition createHighlightAnimation(Label label) {
        if (label == null) return new SequentialTransition();

        String originalStyle = label.getStyle();
        String highlightBg = "#ffeb3b";
        String highlightBorder = "#f57c00";

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(ANIM_SPEED / 2), label);
        scaleUp.setFromX(1.0);
        scaleUp.setFromY(1.0);
        scaleUp.setToX(1.2);
        scaleUp.setToY(1.2);

        scaleUp.setOnFinished(e -> label.setStyle(originalStyle + " -fx-background-color: " + highlightBg + "; -fx-border-color: " + highlightBorder + "; -fx-border-width: 2px; -fx-font-weight: bold;"));

        PauseTransition pause = new PauseTransition(Duration.millis(ANIM_SPEED / 2));

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(ANIM_SPEED / 2), label);
        scaleDown.setFromX(1.2);
        scaleDown.setFromY(1.2);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setOnFinished(e -> label.setStyle(originalStyle));

        SequentialTransition seq = new SequentialTransition(scaleUp, pause, scaleDown);
        return seq;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
