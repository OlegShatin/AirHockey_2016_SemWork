package ru.kpfu.group11501.airhockey.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import ru.kpfu.group11501.airhockey.model.Mallet;
import ru.kpfu.group11501.airhockey.model.Puck;
import ru.kpfu.group11501.airhockey.net.Client;

import java.io.IOException;
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
    @FXML
    public Region userGates;
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
    private ImageView sam;
    @FXML
    private Pane gameField;
    private Object scoreSync = new Object();

    public void initialize(){

        userMallet = new Mallet(malRed, gameField, client);
        opponentMallet = new Mallet(malBlue, gameField, client);
        puck = new Puck(sam, gameField, userMallet,  client, userGates);
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
                    Thread.sleep(2000);
                }
                if (userCurrentGameScore > opponentCurrentGameScore){
                    Platform.runLater(()->{
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("../winner.fxml"));
                        Parent root = null;
                        try {
                            root = loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        stage.setScene(new Scene(root, 400, 600));
                        stage.show();
                    });
                } else {
                    Platform.runLater(()->{
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("../looser.fxml"));
                        Parent root = null;
                        try {
                            root = loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        stage.setScene(new Scene(root, 400, 600));
                        stage.show();
                    });

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        roundThread.setDaemon(true);
        roundThread.start();


    }


    public void mouseMoved(MouseEvent mouseEvent) {
        userMallet.move(mouseEvent.getX(),mouseEvent.getY(),true);
    }

    private void roundRun() {
        puck.setX(GAME_FIELD_CENTER_X);
        puck.setY(GAME_FIELD_CENTER_Y);
        System.out.println("RoundRun!");
        userMallet.unblock();
        opponentMallet.unblock();
        puck.unblock();
        puck.move(10, 100, true);
        synchronized (scoreSync) {
            try {
                scoreSync.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("round breaked");
        userMallet.block();
        opponentMallet.block();
        puck.block();
        puck.setX(GAME_FIELD_CENTER_X);
        puck.setY(GAME_FIELD_CENTER_Y);



    }

    private boolean puckInUserGates() {
        return puck.getView().getBoundsInLocal().intersects(userGates.getBoundsInLocal());
    }


    public void updatePuckDirection(Double puckX, Double puckY) {
        puck.move(puckX, puckY, false);
    }


    public void updateOpponentMalletDirection(Double malletX, Double malletY) {
        opponentMallet.move(malletX, malletY, false);
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
        setOpponentName();
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
    public void setOpponentName() {
        opponentNameLabel.setText(client.getOpponentName());
    }
    @Override
    public void updateScore(Integer clientScore, Integer opponentScore) {
        System.out.println("score updated by serv");
        userCounter.setText(clientScore.toString());
        opponentCounter.setText(opponentScore.toString());
        userCurrentGameScore = clientScore;
        opponentCurrentGameScore = opponentScore;
        synchronized (scoreSync) {
            scoreSync.notifyAll();
        }
    }
    @Override
    public void setClient(Client client) {
        this.client = client;
        this.name = client.getName();
        this.puck.setClient(client);
        this.userMallet.setClient(client);
        this.opponentMallet.setClient(client);
    }

}
