package org.example.dsa_simulator.ai;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ChatController {

    @FXML private VBox chatBox;
    @FXML private TextField inputField;
    @FXML private Button sendButton;
    @FXML private ScrollPane scrollPane;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    public void initialize() {
        chatBox.heightProperty().addListener(observable -> scrollPane.setVvalue(1.0));
        addMessage("Hello! I'm your AI assistant. How can I help you with data structures and algorithms today?", Pos.CENTER_LEFT, "#F0F0F0");
    }

    @FXML
    void handleSendMessage(ActionEvent event) {
        String userMessage = inputField.getText();
        if (userMessage.isBlank()) {
            return;
        }

        addMessage(userMessage, Pos.CENTER_RIGHT, "#E1F5FE");
        inputField.clear();

        Text typingIndicator = new Text("Gemini is typing...");
        addNodeToChat(typingIndicator, Pos.CENTER_LEFT);

        Task<String> apiCallTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                // =================================================================
                // IMPORTANT: You MUST get a free API key and paste it here.
                // Get one from Google AI Studio: https://aistudio.google.com/app/apikey
                // =================================================================
                String apiKey = "AIzaSyBclwscsx6FIb3FKZwcaJ4u_SRH-j3EjXU"; // <-- PASTE YOUR API KEY HERE

                if (apiKey.isBlank()) {
                    throw new Exception("API Key is missing. Please get a key from Google AI Studio and add it to ChatController.java.");
                }

                String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

                String jsonPayload = new JSONObject()
                        .put("contents", new JSONObject[] {
                                new JSONObject().put("parts", new JSONObject[] {
                                        new JSONObject().put("text", userMessage)
                                })
                        })
                        .toString();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject jsonResponse = new JSONObject(response.body());

                // --- THIS IS THE FIX ---
                // First, check if the response from the API contains an error object.
                if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.getJSONObject("error").getString("message");
                    throw new Exception("API Error: " + errorMessage);
                }
                // -----------------------

                // If there is no error, proceed to get the candidate text.
                return jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            }
        };

        apiCallTask.setOnSucceeded(e -> {
            chatBox.getChildren().remove(typingIndicator);
            String aiResponse = apiCallTask.getValue();
            addMessage(aiResponse, Pos.CENTER_LEFT, "#F0F0F0");
        });

        apiCallTask.setOnFailed(e -> {
            chatBox.getChildren().remove(typingIndicator);
            // This will now display the specific error from the API call.
            String errorMessage = apiCallTask.getException().getMessage();
            addMessage(errorMessage, Pos.CENTER_LEFT, "#FFEBEE");
            apiCallTask.getException().printStackTrace();
        });

        new Thread(apiCallTask).start();
    }

    private void addMessage(String message, Pos alignment, String color) {
        Text text = new Text(message);
        text.setWrappingWidth(500);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; -fx-padding: 10;");
        addNodeToChat(textFlow, alignment);
    }

    private void addNodeToChat(javafx.scene.Node node, Pos alignment) {
        HBox hbox = new HBox();
        hbox.setAlignment(alignment);
        hbox.getChildren().add(node);
        chatBox.getChildren().add(hbox);
    }
}
