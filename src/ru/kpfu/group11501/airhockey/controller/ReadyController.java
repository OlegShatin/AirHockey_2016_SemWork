package ru.kpfu.group11501.airhockey.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import ru.kpfu.group11501.airhockey.net.ClientImpl;

import java.io.IOException;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class ReadyController extends Controller {

    public void initialize(){

    }
    @Override
    public synchronized void connectToGame(){

        try {

            if (client.getUserIsReady()){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../game.fxml"));
                Parent root = loader.load();
                stage.setScene(new Scene(root, 400, 600));
                stage.show();
                Controller controller = loader.getController();
                client.setController(controller);
                controller.setStage(stage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized void userIsReady(MouseEvent mouseEvent) {
        client.setUserIsReady(true);
        client.setReady();
        connectToGame();

    }
}
