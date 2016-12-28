package ru.kpfu.group11501.airhockey.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import ru.kpfu.group11501.airhockey.model.Mallet;
import ru.kpfu.group11501.airhockey.model.Puck;

import java.time.Instant;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class GameController extends Controller {
    private static final double GAME_FIELD_CENTER_X = 200.0;
    private static final double GAME_FIELD_CENTER_Y = 300.0;
    @FXML
    public Label opponentNameLabel;
    @FXML
    public Label opponentCounter;
    @FXML
    public Label userCounter;
    private Integer userCurrentGameScore;
    private Integer opponentCurrentGameScore;
    private Puck puck;
    private Mallet userMallet;
    private Mallet opponentMallet;
    @FXML
    private ImageView malRed;
    @FXML
    private ImageView malBlue;
    @FXML
    private ImageView sam;
    @FXML
    private Pane gameField;
    public void initialize(){

        userMallet = new Mallet(malRed, gameField);
        opponentMallet = new Mallet(malBlue, gameField);
        puck = new Puck(sam, gameField, userMallet, opponentMallet);
        userMallet.setPuck(puck);
        opponentMallet.setPuck(puck);
        userCurrentGameScore = 0;
        opponentCurrentGameScore = 0;
        userMallet.block();
        opponentMallet.block();
        puck.block();

    }
    @Override
    public void gameStart() {
        if (!client.getOpponentIsReady()){
            try {
                synchronized (client.getOpponentIsReady()){
                    System.out.println("wOIR");
                    client.getOpponentIsReady().wait();
                    System.out.println("wOIR_DONE");

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Thread roundThread = new Thread(() -> {
            try {

                System.out.println("WRR");
                synchronized (client.getStartFlag()){
                    client.getStartFlag().wait();
                }

                while (client.getStartFlag()) {
                    roundRun();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        roundThread.setDaemon(true);
        roundThread.start();

    }
    public void keyPressed(KeyEvent event) {

        if (event.getCode() == KeyCode.RIGHT) {
            userMallet.getView().setScaleX(1);
            userMallet.move(20, 0);
        }
        if (event.getCode() == KeyCode.DOWN) {
            userMallet.move(0, 20);
        }
        if (event.getCode() == KeyCode.LEFT) {
            userMallet.getView().setScaleX(-1);
            userMallet.move(-20, 0);
        }
        if (event.getCode() == KeyCode.UP) {
            userMallet.move(0, -20);
        }
        if(event.getCode() == KeyCode.SPACE) {
            puck.move(10, 0);
        }
    }

    public void mouseMoved(MouseEvent mouseEvent) {
        userMallet.move(mouseEvent.getX(),mouseEvent.getY());
    }

    private void roundRun() {
        puck.setX(GAME_FIELD_CENTER_X);
        puck.setY(GAME_FIELD_CENTER_Y);
        puck.move(0,0);
        System.out.println("RoundRun!");
        int userBeforeStartScore = userCurrentGameScore;
        userMallet.unblock();
        opponentMallet.unblock();
        puck.unblock();
        while (true) {
            if (userBeforeStartScore != userCurrentGameScore)
                break;
            if (puckInUserGates()) {
                synchronized (client) {
                    client.loseRound();
                }
                break;
            }
        }
        userMallet.block();
        opponentMallet.block();
        puck.block();
        puck.setX(GAME_FIELD_CENTER_X);
        puck.setY(GAME_FIELD_CENTER_Y);
        client.getStartFlag().notifyAll();


    }

    private boolean puckInUserGates() {
        //TODO: Design - this predicate method needed to be overrided
        return false;
    }


    public void updatePuckDirection(Double puckX, Double puckY) {
        puck.move(puckX, puckY);
    }


    public void updateOpponentMalletDirection(Double malletX, Double malletY) {
        opponentMallet.move(malletX, malletY);
    }


    public void setGameResult(Integer clientScore, Integer opponentScore) {
        //TODO: Design - this method describes behavior by result of game
    }

    //time offset to synchronize start time between clients
    @Override
    public void gameStartsInTime(Long timeInstanceOfStart) {
        if (client.getStartFlag() == null) {
            client.setStartFlag(false);
        }
        Timer timer = new Timer();
        System.out.println("START IN: " + Date.from(Instant.ofEpochMilli(timeInstanceOfStart)));
        timer.schedule(new TimerTask() {

            public void run() {
                synchronized (client.getStartFlag()) {
                    client.setStartFlag(true);
                    System.out.println("DONE");
                }
            }
        }, Date.from(Instant.ofEpochMilli(timeInstanceOfStart)));
    }
    public void opponentLeftGame() {
        //TODO: Design - this method describes behavior by leaving opponent, without any messages about result of game
    }
    @Override
    public void setOpponentName(String opponentName) {
        opponentNameLabel.setText(opponentName);
    }
    @Override
    public void updateScore(Integer clientScore, Integer opponentScore) {
        userCounter.setText(clientScore.toString());
        opponentCounter.setText(opponentScore.toString());
        userCurrentGameScore = clientScore;
        opponentCurrentGameScore = opponentScore;
    }

}
