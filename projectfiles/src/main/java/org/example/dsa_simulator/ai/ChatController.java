package org.example.dsa_simulator.ai;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    private HBox typingIndicatorBox;

    @FXML
    public void initialize() {

        chatBox.setStyle("-fx-background-color: #f0f2f5;");
        scrollPane.setStyle("-fx-background: #f0f2f5; -fx-background-color: #f0f2f5;");


        inputField.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 15;");

        chatBox.heightProperty().addListener(observable ->
                Platform.runLater(() -> scrollPane.setVvalue(1.0))
        );

        // Welcome message from Panda AI
        addAIMessage("Hello! I'm Panda AI, your friendly assistant for learning data structures and algorithms! 🐼 How can I help you today?");
    }

    @FXML
    void handleSendMessage(ActionEvent event) {
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        // Add user message
        addUserMessage(userMessage);
        inputField.clear();


        showTypingIndicator();


        Task<String> apiCallTask = new Task<>() {
            @Override
            protected String call() throws Exception {

                String apiKey = "AIzaSyBclwscsx6FIb3FKZwcaJ4u_SRH-j3EjXU";

                if (apiKey.isBlank()) {
                    throw new Exception("API Key is missing. Please get a key from Google AI Studio and add it to ChatController.java.");
                }

                String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;


                String enhancedMessage = "You are Panda AI, a DSA learning assistant. Give concise, to-the-point answers. " +
                        "Keep responses brief and practical. Focus on key concepts and avoid lengthy explanations unless specifically asked for details. " +
                        "User question: " + userMessage;

                String jsonPayload = new JSONObject()
                        .put("contents", new JSONObject[] {
                                new JSONObject().put("parts", new JSONObject[] {
                                        new JSONObject().put("text", enhancedMessage)
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

                // Check for API errors
                if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.getJSONObject("error").getString("message");
                    throw new Exception("API Error: " + errorMessage);
                }


                return jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            }
        };

        apiCallTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                hideTypingIndicator();
                String aiResponse = apiCallTask.getValue();
                addAIMessage(aiResponse);
            });
        });

        apiCallTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                hideTypingIndicator();
                String errorMessage = apiCallTask.getException().getMessage();
                addErrorMessage("Sorry, I encountered an error: " + errorMessage);
                apiCallTask.getException().printStackTrace();
            });
        });

        new Thread(apiCallTask).start();
    }

    private void addUserMessage(String message) {
        // Create user message container
        VBox messageContainer = new VBox(5);
        messageContainer.setAlignment(Pos.CENTER_RIGHT);
        messageContainer.setMaxWidth(500);


        Text text = new Text(message);
        text.setFont(Font.font("System", 14));
        text.setFill(Color.WHITE);

        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: #0084ff; " +  // Changed to solid blue
                "-fx-background-radius: 20 20 5 20; " +
                "-fx-padding: 12 16 12 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        textFlow.setMaxWidth(450);

        messageContainer.getChildren().add(textFlow);


        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.getChildren().add(messageContainer);
        hbox.setPadding(new Insets(5, 15, 5, 50));

        chatBox.getChildren().add(hbox);
    }

    private void addAIMessage(String message) {

        HBox messageContainer = new HBox(10);
        messageContainer.setAlignment(Pos.CENTER_LEFT);
        messageContainer.setMaxWidth(550);


        Label pandaEmoji = new Label("🐼");
        pandaEmoji.setStyle("-fx-font-size: 24px;");
        pandaEmoji.setMinWidth(40);
        pandaEmoji.setAlignment(Pos.TOP_LEFT);


        VBox messageBubble = new VBox(5);
        messageBubble.setMaxWidth(450);


        Label nameLabel = new Label("Panda AI");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        nameLabel.setTextFill(Color.web("#2c3e50"));


        Text text = new Text(message);
        text.setFont(Font.font("System", 14));
        text.setFill(Color.web("#2c3e50"));

        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: white; " +  // Pure white for AI messages
                "-fx-background-radius: 5 20 20 20; " +
                "-fx-padding: 12 16 12 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        messageBubble.getChildren().addAll(nameLabel, textFlow);
        messageContainer.getChildren().addAll(pandaEmoji, messageBubble);


        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().add(messageContainer);
        hbox.setPadding(new Insets(5, 50, 5, 15));

        chatBox.getChildren().add(hbox);
    }

    private void addErrorMessage(String message) {

        HBox messageContainer = new HBox(10);
        messageContainer.setAlignment(Pos.CENTER_LEFT);
        messageContainer.setMaxWidth(550);


        Label pandaEmoji = new Label("🐼");
        pandaEmoji.setStyle("-fx-font-size: 24px;");
        pandaEmoji.setMinWidth(40);
        pandaEmoji.setAlignment(Pos.TOP_LEFT);


        VBox messageBubble = new VBox(5);
        messageBubble.setMaxWidth(450);


        Label nameLabel = new Label("Panda AI");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        nameLabel.setTextFill(Color.web("#e74c3c"));


        Text text = new Text(message);
        text.setFont(Font.font("System", 14));
        text.setFill(Color.web("#c0392b"));

        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: #ffebee; " +  // Light red background
                "-fx-background-radius: 5 20 20 20; " +
                "-fx-padding: 12 16 12 16; " +
                "-fx-border-color: #e74c3c; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 5 20 20 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.2), 5, 0, 0, 2);");

        messageBubble.getChildren().addAll(nameLabel, textFlow);
        messageContainer.getChildren().addAll(pandaEmoji, messageBubble);


        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().add(messageContainer);
        hbox.setPadding(new Insets(5, 50, 5, 15));

        chatBox.getChildren().add(hbox);
    }

    private void showTypingIndicator() {

        HBox messageContainer = new HBox(10);
        messageContainer.setAlignment(Pos.CENTER_LEFT);


        Label pandaEmoji = new Label("🐼");
        pandaEmoji.setStyle("-fx-font-size: 20px;");
        pandaEmoji.setMinWidth(35);
        pandaEmoji.setAlignment(Pos.CENTER_LEFT);


        Label typingLabel = new Label("Panda is helping you...");
        typingLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        typingLabel.setTextFill(Color.web("#7f8c8d"));
        typingLabel.setStyle("-fx-background-color: #e9ecef; " +  // Light gray for typing indicator
                "-fx-background-radius: 15; " +
                "-fx-padding: 8 12 8 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        messageContainer.getChildren().addAll(pandaEmoji, typingLabel);


        typingIndicatorBox = new HBox();
        typingIndicatorBox.setAlignment(Pos.CENTER_LEFT);
        typingIndicatorBox.getChildren().add(messageContainer);
        typingIndicatorBox.setPadding(new Insets(5, 50, 5, 15));

        chatBox.getChildren().add(typingIndicatorBox);
    }

    private void hideTypingIndicator() {
        if (typingIndicatorBox != null) {
            chatBox.getChildren().remove(typingIndicatorBox);
            typingIndicatorBox = null;
        }
    }
}