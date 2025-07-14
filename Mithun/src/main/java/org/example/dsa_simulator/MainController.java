package org.example.dsa_simulator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainController {

    @FXML
    public void openGraphWindow(ActionEvent event) {
        try {
            // Create a new loader for the graph FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dsa_simulator/GraphView.fxml"));
            Parent root = loader.load();

            // Get the current stage from the button that triggered the event
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

// Set the scene of that stage to our new graph view
            stage.setScene(new Scene(root));
//            stage.setTitle("Graph Simulator"); // Optional: Update the window title
            stage.show();

        } catch (java.io.IOException e) {
            System.out.println("Failed to open the graph window.");

        }
    }
    @FXML
    public void openHeapWindow(ActionEvent event) throws IOException {
        FXMLLoader loader= new FXMLLoader(getClass().getResource("/org/example/dsa_simulator/PriorityQueue.fxml"));
        Parent root=loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    // Add this method to your MainController.java
    @FXML
    void openAiAssistantWindow(ActionEvent event) {
        System.out.println("AI was called");
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/ChatView.fxml")));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AI Assistant");
        } catch (java.io.IOException e) {
            System.out.println("Error opening AI chat");
        }
    }



}