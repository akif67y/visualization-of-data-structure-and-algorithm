package com.example.demo;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class StackController {

    @FXML
    private VBox buttonContainer;
    @FXML
    private TextField inputField;
    @FXML
    private Stack<Button> dynamicButtons = new Stack<>();

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
        // 1) new up any Node you like:
        Button btn = new Button(label);
        dynamicButtons.push(btn);
        buttonContainer.getChildren().add(0, btn);
        // clear the input for next time
        inputField.clear();
    }
    @FXML
    private void poppedCalled(){
        buttonContainer.getChildren().remove(dynamicButtons.peek());
        dynamicButtons.pop();
        System.out.println("Button removed: ");
    }

}
