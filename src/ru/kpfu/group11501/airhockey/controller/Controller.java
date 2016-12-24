package ru.kpfu.group11501.airhockey.controller;

import ru.kpfu.group11501.airhockey.model.Mallet;
import ru.kpfu.group11501.airhockey.model.Puck;
import ru.kpfu.group11501.airhockey.net.Client;
import ru.kpfu.group11501.airhockey.net.ClientImpl;

import java.time.Instant;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Client {
    //TODO Design: please override this constants
    private static final double GAME_FIELD_CENTER_X = 300;
    private static final double GAME_FIELD_CENTER_Y = 300;

    private Puck puck;
    private Mallet userMallet;
    private Mallet opponentMallet;
    private String name;
    private Client client;
    private Boolean userIsReady;
    private Boolean opponentIsReady;
    private Integer userCurrentGameScore;
    private Integer opponentCurrentGameScore;
    private Boolean startFlag;


    public void initialize(){
        //TODO - Design: initialize first scene - main menu + get name field value from form.

        client = new ClientImpl(name, this);
    }


    //todo - Design: passed click on "connect to game" button event - needed uploading of game main scene to form
    private synchronized void connectToGame(){
        userCurrentGameScore = 0;
        opponentCurrentGameScore = 0;
        userMallet.block();
        opponentMallet.block();
        puck.block();
        puck.setX(GAME_FIELD_CENTER_X);
        puck.setY(GAME_FIELD_CENTER_Y);
        startFlag = false;

        client.askGame();
        try {
            //needed to notify by event
            userIsReady.wait();
            opponentIsReady.wait();
            if (userIsReady && opponentIsReady){
                //notified by gameStartsInTime
                startFlag.wait();
                while (startFlag){
                    roundRun();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void roundRun() {
        new Thread(() -> {
            int userBeforeStartScore = userCurrentGameScore;
            userMallet.unblock();
            opponentMallet.unblock();
            puck.unblock();
            while (true){
                if (userBeforeStartScore != userCurrentGameScore)
                    break;
                if (puckInUserGates()){
                    synchronized (client){
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
        }).start();

    }

    private boolean puckInUserGates() {
        //TODO: Design - this predicate method needed to be overrided
        return false;
    }

    @Override
    public void updatePuckDirection(Double puckX, Double puckY) {

    }

    @Override
    public void updateGameScore(Integer clientScore, Integer opponentScore) {

    }

    @Override
    public void updateOpponentMalletDirection(Double malletX, Double malletY) {
        opponentMallet.move(malletX, malletY);
    }

    @Override
    public void setGameResult(Integer clientScore, Integer opponentScore) {
        //TODO: Design - this method describes behavior by result of game
    }

    //time offset to synchronize start time between clients
    @Override
    public void gameStartsInTime(Long timeInstanceOfStart) {
        if (startFlag == null){
            startFlag = false;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startFlag = true;
                startFlag.notifyAll();
            }
        }, Date.from(Instant.ofEpochMilli(timeInstanceOfStart)));
    }

    @Override
    public void opponentIsReady() {
        //TODO: Design - this method to set opponent
    }

    @Override
    public void opponentLeftGame() {
        //TODO: Design - this method describes behavior by leaving opponent, without any messages about result of game
    }

    @Override
    public void askGame() {
        //plug
    }

    @Override
    public void leaveGame() {
        //plug
    }

    @Override
    public void loseRound() {
        //plug
    }
}
