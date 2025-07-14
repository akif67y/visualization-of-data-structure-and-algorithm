package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class StartController {

    @FXML
    protected void NextButtonClicked(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(getClass().getResource("new-view.fxml"));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
