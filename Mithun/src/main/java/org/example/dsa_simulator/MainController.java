package org.example.dsa_simulator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;


import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Objects;

public class MainController {

    @FXML
    public void BFS(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dsa_simulator/EnhancedBFS.fxml"));
            Parent root = loader.load();


            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();


            stage.setScene(new Scene(root));

            stage.show();

        } catch (java.io.IOException e) {
            System.out.println("Failed to open the graph window.");

        }
    }
    @FXML
    public void DFS(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dsa_simulator/ChangedDFS.fxml"));
            Parent root = loader.load();


            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();


            stage.setScene(new Scene(root));

            stage.show();

        } catch (java.io.IOException e) {
            System.out.println("Failed to open the DFS window.");

        }
    }

    @FXML
    void openAiAssistantWindow(ActionEvent event) {
        System.out.println("AI was called");
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/ChatView.fxml")));
            Stage newStage = new Stage();
            newStage.setTitle("AI Assistant");
            newStage.setScene(new Scene(root));
            newStage.show();

        } catch (java.io.IOException e) {
            System.out.println("Error opening AI chat");
            e.printStackTrace();
        }
    }

    @FXML
    void openSelectionSortWindow(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dsa_simulator/SelectionSort.fxml"));
        Parent root = loader.load();


        Scene scene = new Scene(root);


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
        stage.setWidth(1920);
        stage.setHeight(1080);
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

    public void heapTry(ActionEvent event )
    {
        System.out.println("Heap Try was called");
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/HeapTry.fxml")));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PQ");
            stage.setWidth(1920);
            stage.setHeight(1080);
//            stage.setFullScreen(true);
        } catch (java.io.IOException e) {
            System.out.println("Error opening heap try");
        }
    }

    @FXML
    public void TryBST(ActionEvent event )
    {
        System.out.println("BST was called");
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/TryBST.fxml")));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BST");
            stage.setWidth(1920);
            stage.setHeight(1080);
//            stage.setFullScreen(true);
        } catch (java.io.IOException e) {
            System.out.println("Error opening BST");
        }
    }


    @FXML
    protected void Dijkstra(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/dik.fxml")));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void PracticeKruskal(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/Kruskal.fxml")));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void LinkedList(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/linkedlist.fxml")));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void Prim(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/primview.fxml")));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void LCS(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/hey.fxml")));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void Edit(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/edit.fxml")));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void Knapsack(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/knapsackview.fxml")));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    protected void StackL(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/stack.fxml")));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void StackA(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/stackbyArray.fxml")));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void QueueL(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/queuelist.fxml")));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void QueueA(ActionEvent event) {
        try {
            // Load Scene 2
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/queue.fxml")));
            Scene scene2 = new Scene(root);
            // Get the current stage and switch the scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}