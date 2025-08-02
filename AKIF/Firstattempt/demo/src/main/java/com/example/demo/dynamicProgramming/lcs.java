package com.example.demo.dynamicProgramming;

// --- Necessary Imports ---
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class lcs {

    // --- FXML Fields ---
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
    private Label answer;
    private String ans = "";
    private final int SECONDS = 200;


    // --- Internal Fields ---
    private String t1 = "";
    private String t2 = "";
    private int m = 0;
    private int n = 0;

    // --- Grid Configuration ---
    private static final double CELL_WIDTH = 50.0;
    private static final double CELL_HEIGHT = 30.0;
    private int GRID_ROWS = 0;
    private int GRID_COLS = 0;
    private static final double GRID_POS_X = 10.0;
    private static final double GRID_POS_Y = 10.0;

    // --- Cell Map for Animation ---
    private Map<Pair<Integer, Integer>, Label> cellMap;
   // private ArrayList<ArrayList<Integer>> dp2;
    private int[][]dp2;

    // --- FXML Event Handlers ---

    @FXML
    public void onString1Enter(ActionEvent event) {
        if (s1 != null && !s1.getText().trim().isEmpty()) {
            t1 = s1.getText().trim();
            m = t1.length();
            GRID_ROWS = m + 2;
            System.out.println("String 1 set: " + t1 + " (Length: " + m + ")");
            showAlert("Success", "String 1 set: " + t1);
            s1.clear(); // Clear the text field after setting the string
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
            System.out.println("String 2 set: " + t2 + " (Length: " + n + ")");
            showAlert("Success", "String 2 set: " + t2);
            s2.clear(); // Clear the text field after setting the string
        } else {
            showAlert("Error", "Please enter a valid string for String 2");
        }
    }

    @FXML
    private void onGenerate(ActionEvent event) {
        if (drawingPane == null) {
            System.err.println("Error: drawingPane is not initialized. Check FXML linkage.");
            showAlert("Error", "Drawing pane not initialized");
            return;
        }

        // Validate that both strings are set
        if (t1.isEmpty() || t2.isEmpty()) {
            showAlert("Error", "Please set both strings before generating the table");
            return;
        }

        // Clear previous content
        drawingPane.getChildren().clear();
        cellMap = new HashMap<>();
        generateDpTable();
    }

    // --- Utility Method ---
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Core Logic Methods ---

    /**
     * Generates the LCS DP table, populates the GridPane, and initiates the animation sequence.
     */
    private void generateDpTable() {
        // --- 1. Create and Configure the GridPane ---
        GridPane arrayGrid = new GridPane();
        arrayGrid.setHgap(0);
        arrayGrid.setVgap(0);
        arrayGrid.setStyle("-fx-border-color: black; -fx-border-width: 2px;");

        // Define column and row constraints
        for (int i = 0; i < GRID_COLS; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPrefWidth(CELL_WIDTH);
            arrayGrid.getColumnConstraints().add(colConst);
        }
        for (int i = 0; i < GRID_ROWS; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPrefHeight(CELL_HEIGHT);
            arrayGrid.getRowConstraints().add(rowConst);
        }

        // --- 2. Add Header Row and Column ---
        // Add empty cell at (0,0)
        Label emptyCell = new Label("lcs");
        styleAndAddCell(emptyCell, arrayGrid, 0, 0);
        cellMap.put(new Pair<>(0, 0), emptyCell);

        // Add character headers for string 2 (columns)
        for (int j = 2; j < GRID_COLS; j++) {
            Label headerCell = new Label(String.valueOf(t2.charAt(j - 2)));
            headerCell.setStyle("-fx-alignment: center; " +
                    "-fx-border-color: gray; " +
                    "-fx-border-width: 0.5px; " +
                    "-fx-background-color: #e0e0e0; " +
                    "-fx-font-weight: bold;");
            headerCell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
            cellMap.put(new Pair<>(0, j), headerCell);
            arrayGrid.add(headerCell, j, 0);
        }

       //  Add character headers for string 1 (rows)
        for (int i = 2; i < GRID_ROWS; i++) {
            Label headerCell = new Label(String.valueOf(t1.charAt(i - 2)));
            headerCell.setStyle("-fx-alignment: center; " +
                    "-fx-border-color: gray; " +
                    "-fx-border-width: 0.5px; " +
                    "-fx-background-color: #e0e0e0; " +
                    "-fx-font-weight: bold;");
            headerCell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
            cellMap.put(new Pair<>(i,0), headerCell);
            arrayGrid.add(headerCell, 0, i);
        }

        // --- 3. Initialize DP Table ---
        int[][] dp = new int[GRID_ROWS][GRID_COLS];

        // First row (j from 1 to n)
        for (int j = 2; j < GRID_COLS; j++) {
            dp[1][j] = 0;
            Label cell = new Label("0");
            styleAndAddCell(cell, arrayGrid, j, 1);
            cellMap.put(new Pair<>(1, j), cell);
        }

        // First column (i from 1 to m)
        for (int i = 2; i < GRID_ROWS; i++) {
            dp[i][1] = 0;
            Label cell = new Label("0");
            styleAndAddCell(cell, arrayGrid, 1, i);
            cellMap.put(new Pair<>(i, 1), cell);
        }
        dp[0][1] = 0;
        Label cellse2 = new Label("null");
        styleAndAddCell(cellse2, arrayGrid, 1, 0);
        cellMap.put(new Pair<>(0, 1), cellse2);
        dp[1][0] = 0;
        Label cellse3 = new Label("null");
        styleAndAddCell(cellse3, arrayGrid, 0, 1);
        cellMap.put(new Pair<>(1, 0), cellse3);
        dp[1][1] = 0;
        Label cellse = new Label("0");
        styleAndAddCell(cellse, arrayGrid, 1, 1);
        cellMap.put(new Pair<>(1, 1), cellse);

        // --- 4. Fill DP Table and Create Animation ---
        SequentialTransition sequence = new SequentialTransition();

        for (int i = 2; i < GRID_ROWS; i++) {
            for (int j = 2; j < GRID_COLS; j++) {
                final int ifinal = i;
                final int jfinal = j;
                char char1 = t1.charAt(i - 2);
                char char2 = t2.charAt(j - 2);


                if (char1 == char2) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }

                // Create label for the cell
                Label cell = new Label("");
                cell.setOpacity(1); // Start invisible for animation
                styleAndAddCell(cell, arrayGrid, j, i);
                cellMap.put(new Pair<>(i, j), cell);
                sequence.getChildren().addAll(createHighlightAnimation(cell)) ;
                sequence.getChildren().addAll(createHighlightAnimation(cellMap.get(new Pair<>(0,j))));
                sequence.getChildren().addAll(createHighlightAnimation(cellMap.get(new Pair<>(i,0))));
                ParallelTransition parallelTransition = new ParallelTransition();
                Timeline finalTimeline = new Timeline();
                KeyFrame finalFrame = new KeyFrame(Duration.millis(SECONDS), e -> {
                  if(char1 == char2)  comparison.setText( char1 + " equals " + char2);
                  else{
                      comparison.setText( char1 + " is not equal to " + char2);
                  }
                    //System.out.println("Sequence with Timelines completed!");
                });
                finalTimeline.getKeyFrames().addAll(finalFrame);
                parallelTransition.getChildren().addAll(finalTimeline);
                parallelTransition.getChildren().addAll(createHighlightAnimation(comparison));
                sequence.getChildren().addAll(parallelTransition);
                // Create fade-in animation


                // Highlight the influencing cell
                if (char1 == char2) {
                    Label diagonalCell = cellMap.get(new Pair<>(i - 1, j - 1));
                    if (diagonalCell != null) {
                        sequence.getChildren().addAll(createHighlightAnimation(diagonalCell));
                    }
                } else {
                    Label maxInfluencingCell;
                    if (dp[i - 1][j] >= dp[i][j - 1]) {
                        maxInfluencingCell = cellMap.get(new Pair<>(i - 1, j));
                    } else {
                        maxInfluencingCell = cellMap.get(new Pair<>(i, j - 1));
                    }
                    if (maxInfluencingCell != null) {

                        sequence.getChildren().addAll(createHighlightAnimation(maxInfluencingCell));

                    }
                }

                // Add small pause between animations

                sequence.getChildren().add(new PauseTransition(Duration.millis(SECONDS)));
                Timeline finalTimeline2 = new Timeline();
                KeyFrame finalFrame2 = new KeyFrame(Duration.millis(SECONDS), e -> {
                    cell.setText(dp[ifinal][jfinal] + "");
                    //System.out.println("Sequence with Timelines completed!");
                });
                finalTimeline2.getKeyFrames().addAll(finalFrame2);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(SECONDS), cell);
                fadeIn.setFromValue(1.0);
                fadeIn.setToValue(1.5);
                sequence.getChildren().addAll( finalTimeline2, fadeIn);
            }
        }

        // --- 5. Position Grid and Add to Pane ---
        arrayGrid.setLayoutX(GRID_POS_X);
        arrayGrid.setLayoutY(GRID_POS_Y);

        drawingPane.getChildren().add(arrayGrid);

        // --- 6. Play Animation ---
        if (!sequence.getChildren().isEmpty()) {
            System.out.println("Starting animation sequence...");
            sequence.play();
            System.out.println("animation sequence finished.");
        }

        // Show final result
        sequence.setOnFinished(e -> {
            resultLabel.setText(dp[m+1][n+1]+"");
            //showAlert("LCS Complete", "Longest Common Subsequence length: " + dp[m+1][n+1]);
        });
        dp2 = new int[GRID_ROWS][GRID_COLS];
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
              dp2[i][j] = dp[i][j];
            }
        }
    }
    @FXML
    private void extractLcs(){
        ans = "";
        SequentialTransition squel = new SequentialTransition();
        int i1 = m+1;
        int j1 = n+1;

        while(i1 > 1 && j1 > 1){
            squel.getChildren().add(createHighlightAnimation(cellMap.get(new Pair<>(i1, j1))));
            if(t1.charAt(i1-2) == t2.charAt(j1-2)){
                final int ifinal = i1;
                final int jfinal = j1;
                Timeline finalTimeline2 = new Timeline();
                KeyFrame finalFrame2 = new KeyFrame(Duration.millis(SECONDS), e -> {
                    ans = t1.charAt(ifinal-2) + ans;
//                    System.out.println(t1);
//                    System.out.println(t2);
                    answer.setText(ans);
                    //System.out.println("Sequence with Timelines completed!");
                });
                finalTimeline2.getKeyFrames().addAll(finalFrame2);
                squel.getChildren().addAll(finalTimeline2);
                i1--;
                j1--;

            }
            else{
                if(dp2[i1-1][j1] > dp2[i1][j1-1]){
                    i1--;
                }
                else{
                    j1--;
                }

            }
        }
        if (!squel.getChildren().isEmpty()) {
            System.out.println("Starting animation sequence...");
            squel.play();
            System.out.println("animation sequence finished.");
        }
        if(ans.equals("")){
            answer.setText("No LCS found");
        }
    }

    /**
     * Styles a label cell and adds it to the grid.
     */
    private void styleAndAddCell(Label cell, GridPane grid, int col, int row) {
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
        cell.setStyle("-fx-alignment: center; " +
                "-fx-border-color: gray; " +
                "-fx-border-width: 1px; " +
                "-fx-background-color: #f9f9f9;");
        grid.add(cell, col, row);
    }

    /**
     * Creates a highlight animation for a given label.
     */
    private String extractColorFromStyle(String style, String key, String defaultColor) {
        if (style == null || style.isEmpty()) return defaultColor;
        String[] parts = style.split(";");
        for (String part : parts) {
            if (part.contains(key)) {
                String[] kv = part.trim().split(":");
                if (kv.length == 2) return kv[1].trim();
            }
        }
        return defaultColor;
    }
    private SequentialTransition createHighlightAnimation(Label label) {
        if (label == null) return new SequentialTransition();

        // Store original style properties
        String originalStyle = label.getStyle();
        String baseStyle = originalStyle != null ? originalStyle : "";

        // Extract or define fallback colors
        String bgColor = extractColorFromStyle(baseStyle, "-fx-background-color", "#f9f9f9");
        String borderColor = extractColorFromStyle(baseStyle, "-fx-border-color", "gray");

        // Temp highlight colors
        String highlightBg = "#ffcccb"; // Light red
        String highlightBorder = "darkred";

        // --- 1. Scale Up + Change Style ---
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(SECONDS), label);
        scaleUp.setFromX(1.0);
        scaleUp.setFromY(1.0);
        scaleUp.setToX(1.15);
        scaleUp.setToY(1.15);

        // Apply highlight style immediately after scale-up starts
        scaleUp.setOnFinished(e -> label.setStyle(
                baseStyle +
                        " -fx-background-color: " + highlightBg + ";" +
                        " -fx-border-color: " + highlightBorder + ";" +
                        " -fx-border-width: 2px;" +
                        " -fx-font-weight: bold;"
        ));

        // --- 2. Pause to hold highlight ---
        PauseTransition pause = new PauseTransition(Duration.millis(SECONDS));

        // --- 3. Scale Down + Restore Original Style ---
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(SECONDS), label);
        scaleDown.setFromX(1.15);
        scaleDown.setFromY(1.15);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        scaleDown.setOnFinished(e -> label.setStyle(originalStyle));

        // Build sequence
        SequentialTransition sequence = new SequentialTransition();
        sequence.getChildren().addAll(scaleUp, pause, scaleDown);
        return sequence;
    }
}