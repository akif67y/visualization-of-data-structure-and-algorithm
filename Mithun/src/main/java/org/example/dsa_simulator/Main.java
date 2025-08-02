package org.example.dsa_simulator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/dsa_simulator/Home-screen.fxml")));
        Platform.runLater(root::requestFocus);
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/dsa_simulator/icons/app_icon.png")));
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("DSA Simulator");
        primaryStage.setScene(new Scene(root));
        primaryStage.setHeight(1060);
        primaryStage.setWidth(1900);
        primaryStage.setResizable(false);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
