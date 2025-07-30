package org.example.dsa_simulator.graph;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EditGraph {

    // --- FXML Injected Fields ---
    @FXML private TextField sourceNodeField;
    @FXML private TextField destNodeField;
    @FXML private TextField weightField;
    @FXML private ListView<String> edgeListView;
    @FXML private Button doneButton;

    // --- Data & Communication ---
    private MST mainController; // Reference to the main controller
    private final List<MST.Edge> newEdges = new ArrayList<>();
    private final ObservableList<String> edgeListItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        edgeListView.setItems(edgeListItems);
    }

    /**
     * Sets the reference to the main controller to allow data passing.
     * @param mainController The instance of the main MST controller.
     */
    public void setMainController(MST mainController) {
        this.mainController = mainController;
    }

    /**
     * Called when the "Add Edge" button is clicked.
     * Validates input and adds the new edge to the list.
     */
    @FXML
    private void addEdge() {
        String source = sourceNodeField.getText().trim().toUpperCase();
        String dest = destNodeField.getText().trim().toUpperCase();

        if (source.isEmpty() || dest.isEmpty() || weightField.getText().trim().isEmpty()) {
            System.err.println("All fields must be filled.");
            return;
        }
        if (source.equals(dest)) {
            System.err.println("Source and destination cannot be the same.");
            return;
        }

        try {
            int weight = Integer.parseInt(weightField.getText().trim());
            // Create an instance of the public inner class from MST
            MST.Edge newEdge = new MST.Edge(source, dest, weight);

            // Add to data structures
            newEdges.add(newEdge);
            edgeListItems.add(newEdge.toString());

            // Clear fields for next entry
            sourceNodeField.clear();
            destNodeField.clear();
            weightField.clear();
            sourceNodeField.requestFocus();

        } catch (NumberFormatException e) {
            System.err.println("Weight must be a valid integer.");
        }
    }

    /**
     * Called when the "Done" button is clicked.
     * Passes the new graph data back to the main controller and closes the window.
     */
    @FXML
    private void closeWindow() {
        if (mainController != null) {
            mainController.updateGraphFromData(newEdges);
        }
        // Get the stage and close it
        Stage stage = (Stage) doneButton.getScene().getWindow();
        stage.close();
    }
}
