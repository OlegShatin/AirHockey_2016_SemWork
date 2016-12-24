package ru.kpfu.group11501.airhockey;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.kpfu.group11501.airhockey.controller.Controller;

public class Main extends Application {

    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("reg.fxml"));
        primaryStage.setTitle("Airhockey");
        primaryStage.setScene(new Scene(root, 400, 600));
        primaryStage.show();

        //controller.initialize();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
