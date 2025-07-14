package com.example.demo;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class tempcontroller {

    @FXML
    private VBox buttonContainer;
    @FXML
    private TextField inputField;
    @FXML
    private Stack<Button> dynamicButtons = new Stack<>();
    @FXML
    private HBox dynamicHBox;
    @FXML
    private Pane overlayPan;



    @FXML
    private void handleTextFieldKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onAddClicked();
        }
    }

    @FXML
    private void onAddClicked() {
        String label = inputField.getText().trim();
        if (label.isEmpty()) return;

        // Create a beautifully styled button
        Button btn = new Button(label);
        Button btn2 = new Button(label);

        // Apply beautiful styling
        btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ff7b7b, #ff5252);" +
                        "-fx-background-radius: 12;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 15 25;" +
                        "-fx-min-width: 200;" +
                        "-fx-max-width: 200;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        );

        // Add hover effects
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #ff9999, #ff7777);" +
                            "-fx-background-radius: 12;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 15 25;" +
                            "-fx-min-width: 200;" +
                            "-fx-max-width: 200;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 6);" +
                            "-fx-cursor: hand;"
            );
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #ff7b7b, #ff5252);" +
                            "-fx-background-radius: 12;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 15 25;" +
                            "-fx-min-width: 200;" +
                            "-fx-max-width: 200;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);" +
                            "-fx-cursor: hand;"
            );
        });

        // Add push animation
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), btn);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), btn);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Add to stack and container
        dynamicButtons.push(btn);
        buttonContainer.getChildren().add(0, btn);
        dynamicHBox.getChildren().add(btn2);

        // Play animations
        scaleIn.play();
        fadeIn.play();

        // Clear input
        inputField.clear();
    }

    @FXML
    private void poppedCalled() {
        if (dynamicButtons.isEmpty()) return;

        Button topButton = dynamicButtons.peek();

        // Add pop animation
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), topButton);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), topButton);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Remove after animation completes
        fadeOut.setOnFinished(e -> {
            buttonContainer.getChildren().remove(topButton);
            dynamicButtons.pop();
        });

        // Play animations
        scaleOut.play();
        fadeOut.play();

        System.out.println("Button removed: " + topButton.getText());
    }
}