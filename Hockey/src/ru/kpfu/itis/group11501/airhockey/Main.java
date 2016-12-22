package ru.kpfu.itis.group11501.airhockey;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.kpfu.itis.group11501.airhockey.controller.GameController;
import ru.kpfu.itis.group11501.airhockey.controller.RegistrationController;

public class Main extends Application {
    private final static int SCREEN_WIDTH = 400;
    private final static int SCREEN_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("scene/game.fxml"));
        primaryStage.setTitle("AirHockey");
        primaryStage.setScene(new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT));
        primaryStage.show();
        primaryStage.getScene().getRoot().requestFocus();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
