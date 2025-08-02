package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class MainController{
    @FXML
    private Label welcomeText;
 //   @FXML
  //  private Label mousepos;
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
    @FXML
    protected void StackButtonClick(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(getClass().getResource("stackbyArray.fxml"));
            Scene scene2 = new Scene(root);

            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void Stack2ButtonClick(ActionEvent event) {
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
            Parent root = FXMLLoader.load(getClass().getResource("queuelist.fxml"));
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
    @FXML
    protected void Queue2ButtonClick(ActionEvent event) {
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
    protected void lcsButtonClick(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(getClass().getResource("hey.fxml"));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void edtButtonClick(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(getClass().getResource("edit.fxml"));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void knapsackButtonClick(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(getClass().getResource("knapsackview.fxml"));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void dijkstraButtonClick(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(getClass().getResource("dik.fxml"));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void primButtonClick(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(getClass().getResource("primview.fxml"));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }








}