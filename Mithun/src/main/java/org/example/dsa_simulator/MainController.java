package org.example.dsa_simulator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.dsa_simulator.bst.BSTController;

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

    @FXML
    void openSelectionSortWindow(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dsa_simulator/SelectionSort.fxml"));
        Parent root = loader.load();

        // Create a new scene with the loaded root
        Scene scene = new Scene(root);

        // Add the CSS stylesheet to the scene
        String css = Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/SortStyles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        // Get the current stage and set the new scene
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void openBubbleSortWindow(ActionEvent event) throws IOException {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dsa_simulator/BubbleSort.fxml"));
        Parent root = loader.load();

        // Create a new scene with the loaded root
        Scene scene = new Scene(root);

        // Add the CSS stylesheet to the scene
        String css = Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/SortStyles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        // Get the current stage and set the new scene
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void openInsertionSortWindow(ActionEvent event) throws IOException {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dsa_simulator/InsertionSort.fxml"));
        Parent root = loader.load();

        // Create a new scene with the loaded root
        Scene scene = new Scene(root);

        // Add the CSS stylesheet to the scene
        String css = Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/SortStyles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        // Get the current stage and set the new scene
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void openMergeSortWindow(ActionEvent event) throws IOException {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dsa_simulator/MergeSort.fxml"));
        Parent root = loader.load();

        // Create a new scene with the loaded root
        Scene scene = new Scene(root);

        // Add the CSS stylesheet to the scene
        String css = Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/SortStyles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        // Get the current stage and set the new scene
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void openArrayWindow(ActionEvent event) throws IOException {
        FXMLLoader loader= new FXMLLoader(getClass().getResource("/org/example/dsa_simulator/Array.fxml"));
        Parent root=loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Method to launch BST simulation in new window
    public void launchBSTSimulation() {
        try {
            // Create new stage for BST simulation
            Stage bstStage = new Stage();
            bstStage.setTitle("BST Simulation");

            // Create and start BST controller
            BSTController bstController = new BSTController();
            bstController.start(bstStage);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error launching BST simulation: " + e.getMessage());
        }
    }
    @FXML
    // If you have a button in your main UI to launch BST
    public void onBSTButtonClicked(ActionEvent event) {
        launchBSTSimulation();
    }



}