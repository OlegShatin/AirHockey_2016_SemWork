package ru.kpfu.itis.group11501.airhockey.controller;


import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistrationController {

    private Stage stage;
    private GameController gameController;

    @FXML
    private TextField name;


    public void onClickInMethod(Event event) throws IOException {
        if(isInputNameNotNull()) {
            Parent root = FXMLLoader.load(getClass().getResource("scene/game.fxml"));
            stage.setScene(new Scene(root, 400, 600));
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please, enter your name");
            a.show();
        }
    }

    private boolean isInputNameNotNull() {
        return name.getLength() != 0;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /*public void setRegistrationScene(Scene registrationScene) {
        this.registrationScene = registrationScene;
    }*/
}
