package ru.kpfu.group11501.airhockey;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.kpfu.group11501.airhockey.controller.Controller;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("registration.fxml"));
        Parent root = loader.load();
        Controller registrationController = loader.getController();
        registrationController.setStage(primaryStage);
        primaryStage.setScene(new Scene(root, 400, 600));
        primaryStage.show();

        //controller.initialize();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
