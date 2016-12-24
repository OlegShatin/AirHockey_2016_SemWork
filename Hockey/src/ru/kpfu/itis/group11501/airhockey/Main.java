package ru.kpfu.itis.group11501.airhockey;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.kpfu.itis.group11501.airhockey.controller.GameController;
import ru.kpfu.itis.group11501.airhockey.controller.RegistrationController;

public class Main extends Application {

    /*private Stage mainStage;*/

    @Override
    public void start(Stage primaryStage) throws Exception{

        /*this.mainStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("scene/registration.fxml"));
        primaryStage.setTitle("AirHockey");

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("scene/registration.fxml"));
        loader.load();
        registrationController = root.cont;
        registrationController.setStage(primaryStage);
        //controller.initialize();*/
        FXMLLoader loader = new FXMLLoader(getClass().getResource("scene/registration.fxml"));
        Parent root = loader.load();
        RegistrationController registrationController = loader.getController();
        registrationController.setStage(primaryStage);
        primaryStage.setScene(new Scene(root, 400, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

