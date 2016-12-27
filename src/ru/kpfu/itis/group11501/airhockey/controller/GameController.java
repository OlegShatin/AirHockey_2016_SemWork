package ru.kpfu.itis.group11501.airhockey.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ru.kpfu.itis.group11501.airhockey.model.Mallet;
import ru.kpfu.itis.group11501.airhockey.model.Puck;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Svetlana on 18.12.2016.
 */
public class GameController {

    @FXML
    private ImageView malRed;
    @FXML
    private Pane gameField;
    @FXML
    private ImageView sam;
    private Puck puck;
    private Mallet mallet;
    private Stage stage;

    public void initialize() {
        mallet = new Mallet(malRed, gameField);
        puck = new Puck(sam, gameField);
    }

    public void keyPressed(KeyEvent event) {

        if (event.getCode() == KeyCode.RIGHT) {
            mallet.getView().setScaleX(1);
            mallet.move(20, 0);
        }
        if (event.getCode() == KeyCode.DOWN) {
            mallet.move(0, 20);
        }
        if (event.getCode() == KeyCode.LEFT) {
            mallet.getView().setScaleX(-1);
            mallet.move(-20, 0);
        }
        if (event.getCode() == KeyCode.UP) {
            mallet.move(0, -20);
        }
        if(event.getCode() == KeyCode.SPACE) {
            puck.move(10, 0);
        }
    }

    public void mouseMoved(MouseEvent mouseEvent) {
        mallet.move(mouseEvent.getX(),mouseEvent.getY());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
