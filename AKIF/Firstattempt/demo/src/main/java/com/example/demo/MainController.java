package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainController{
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
    @FXML
    protected void StackButtonClick(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(getClass().getResource("stack.fxml"));
            Scene scene2 = new Scene(root);

            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void QueueButtonClick(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(getClass().getResource("queue.fxml"));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void LinkedListButtonClick(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(getClass().getResource("linkedlist.fxml"));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }








}