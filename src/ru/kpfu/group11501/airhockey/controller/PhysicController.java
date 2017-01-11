package ru.kpfu.group11501.airhockey.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import ru.kpfu.group11501.airhockey.model.Mallet;
import ru.kpfu.group11501.airhockey.model.Puck;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class PhysicController extends Controller{
    private static final double GAME_FIELD_CENTER_X = 200.0;
    private static final double GAME_FIELD_CENTER_Y = 300.0;
    @FXML
    public Label opponentNameLabel;
    @FXML
    public Label opponentCounter;
    @FXML
    public Label userCounter;
    @FXML
    public Region userGates;
    @FXML
    public Label currMalBounds;
    @FXML
    public Label currTarget;
    @FXML
    public ImageView vecTarget;
    @FXML
    public Line ortoLine;
    @FXML
    public Line targetLine;
    @FXML
    public GridPane root;
    private int userCurrentGameScore;
    private int opponentCurrentGameScore;
    private Puck puck;
    private Mallet userMallet;
    private Mallet opponentMallet;
    @FXML
    private ImageView malRed;
    @FXML
    private ImageView malBlue;
    @FXML
    private ImageView target;
    @FXML
    private ImageView sam;
    @FXML
    private Pane gameField;
    private Object scoreSync = new Object();

    public void initialize(){

        userMallet = new Mallet(malRed, gameField, client);
        opponentMallet = new Mallet(malBlue, gameField, client);
        puck = new Puck(sam, gameField, userMallet,  client, userGates, new Mallet(target, gameField, client),
                currMalBounds, currTarget, new Mallet(vecTarget, gameField, client), ortoLine, targetLine);
        userCurrentGameScore = 0;
        opponentCurrentGameScore = 0;
        puck.setX(GAME_FIELD_CENTER_X);
        puck.setY(GAME_FIELD_CENTER_Y);
        puck.getView().setVisible(true);
        puck.move(10, 10, false);
        root.setOnKeyPressed(event -> keyPressed(event));
        /*userMallet.block();
        opponentMallet.block();
        puck.block();*/

    }

    public void mouseMoved(MouseEvent mouseEvent) {
        userMallet.move(mouseEvent.getX(),mouseEvent.getY(),true);
    }
    @FXML
    public void keyPressed(KeyEvent keyEvent) {
        System.out.println("space");
        if(keyEvent.getCode() == KeyCode.SPACE) {
            if (puck.isBlocked()) {
                puck.unblock();
                userMallet.unblock();
            } else {
                puck.block();
                userMallet.block();
            }
        }
    }
}
