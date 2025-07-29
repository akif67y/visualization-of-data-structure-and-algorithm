package org.example.dsa_simulator;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

// This class is your controller for the FXML file.
public class ArrayViewController implements Initializable {

    // 1. Your pre-defined array of numbers
    private final int[] numbers = {73, 12, 90, 44, 28, 65};
    List<StackPane> nodes= new ArrayList<>();
    Text statusText;

    // 2. This HBox is injected from your FXML file.
    //    Make sure the fx:id in the FXML matches this variable name.
    @FXML
    private HBox arrayDisplayContainer;

    /**
     * This method is automatically called by the FXMLLoader after the FXML is loaded.
     * All initialization logic goes here.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Center the content of the HBox
        arrayDisplayContainer.setAlignment(Pos.CENTER);

        // Loop through the array to create and add visual nodes
        for (int number : numbers) {
            // Create a visual box (cell) for the number
            Rectangle cell = new Rectangle(60, 60);
            cell.setFill(Color.LIGHTSKYBLUE);
            cell.setStroke(Color.BLACK);
            cell.setArcWidth(10); // Adds rounded corners
            cell.setArcHeight(10);

            // Create the text node for the number
            Text numberText = new Text(String.valueOf(number));
            numberText.setFont(Font.font("Arial", 18));
            numberText.setFill(Color.DARKSLATEBLUE);

            // Use a StackPane to center the text on top of the rectangle
            StackPane stack = new StackPane();
            stack.getChildren().addAll(cell, numberText);
            nodes.add(stack);

            // Add the final visual element directly to the HBox from the FXML
            arrayDisplayContainer.getChildren().add(stack);


        }
        statusText = new Text("Animation will start soon...");
        statusText.setFont(Font.font(16));

        // 2. Get the main layout (the BorderPane)
        BorderPane rootPane = (BorderPane) arrayDisplayContainer.getParent();

        // 3. Place the text at the bottom of the layout
        StackPane textContainer = new StackPane(statusText); // Use a StackPane for centering and padding
        textContainer.setPadding(new Insets(10, 0, 20, 0));
        rootPane.setBottom(textContainer);

        PauseTransition delay = new PauseTransition(Duration.millis(1000));

        // Call your method from inside the setOnFinished lambda
        delay.setOnFinished(event -> animate());

        delay.play();

    }

    private void animate() {
        // Create a new timeline to manage the animation
        Timeline timeline = new Timeline();

        // Loop through your nodes to create an animation step for each one
        for (int i = 0; i < nodes.size(); i++) {
            final int index = i; // Use a final variable for the lambda

            // A KeyFrame defines a point in time and an action to run.
            KeyFrame keyFrame = new KeyFrame(
                    // When this frame should happen (e.g., 500ms, 1000ms, 1500ms...)
                    Duration.millis(2000 * (index + 1)),
                    // What should happen at that time
                    event -> {
                        if(index > 0) {
                            // Get the text nodes from current and previous positions
                            Text currentText = (Text) ((StackPane) arrayDisplayContainer.getChildren().get(index)).getChildren().get(1);
                            Text previousText = (Text) ((StackPane) arrayDisplayContainer.getChildren().get(index - 1)).getChildren().get(1);

                            // Get the current values
                            String currentValue = currentText.getText();
                            String previousValue = previousText.getText();

                            // Update status text
                            statusText.setText("Swapping " + currentValue + " with " + previousValue);

                            // Swap the text values
                            currentText.setText(previousValue);
                            previousText.setText(currentValue);
                        }
                    }
            );

            // Add this step to the timeline
            timeline.getKeyFrames().add(keyFrame);
        }

        // Start the animation
        timeline.play();
    }

    public void changeCellColor(int index, Color color) {
        if (index >= 0 && index < nodes.size()) {
            StackPane cellNode = nodes.get(index);

            // The Rectangle is the first child we added to the StackPane.
            // We get it and cast it from a generic Node to a Rectangle.
            Rectangle box = (Rectangle) cellNode.getChildren().getFirst();

            box.setFill(color);
        }
    }


}
