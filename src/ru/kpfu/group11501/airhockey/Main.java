package ru.kpfu.group11501.airhockey;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.kpfu.group11501.airhockey.controller.Controller;
import ru.kpfu.group11501.airhockey.net.Client;
import ru.kpfu.group11501.airhockey.net.ClientImpl;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("reg.fxml"));
        Parent root = loader.load();
        Controller registrationController = loader.getController();
        registrationController.setStage(primaryStage);
        primaryStage.setScene(new Scene(root, 400, 600));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
