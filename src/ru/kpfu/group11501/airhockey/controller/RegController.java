package ru.kpfu.group11501.airhockey.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import ru.kpfu.group11501.airhockey.net.Client;
import ru.kpfu.group11501.airhockey.net.ClientImpl;

import java.io.IOException;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class RegController extends Controller {
    @FXML
    private TextField namePlayer;
    private boolean isInputNameNotNull() {
        return namePlayer.getLength() != 0;
    }

    //todo - Design: passed click on "connect to game" button event - needed uploading of game main scene to form
    public void onClickInMethod(Event event) throws IOException {
        if(isInputNameNotNull()) {
            name = namePlayer.getText();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../newgame.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, 400, 600));
            stage.show();
            Controller controller = loader.getController();
            Client client = new ClientImpl(name, controller);
            controller.setStage(stage);
            client.askGame(name);
            synchronized (this){
                this.notifyAll();
            }




        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please, enter your name");
            a.show();
        }
    }


}
