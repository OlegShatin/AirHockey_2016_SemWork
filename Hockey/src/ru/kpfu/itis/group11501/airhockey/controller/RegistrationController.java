package ru.kpfu.itis.group11501.airhockey.controller;


import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class RegistrationController {

    @FXML
    private TextField name;

    public void onClickInMethod(Event event) {
        if(isInputNameNotNull()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("You enter name, change your scene");
            a.show();
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please, enter your name");
            a.show();
        }
    }

    private boolean isInputNameNotNull() {
        if(name.getLength() == 0){
            return false;
        } else {
            return true;
        }
    }

}
