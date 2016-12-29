package ru.kpfu.group11501.airhockey.controller;

import javafx.stage.Stage;
import ru.kpfu.group11501.airhockey.net.Client;

public abstract class Controller {
    //TODO Design: please override this constants
    protected String name;
    protected Stage stage;
    protected Client client;
    public void opponentIsReady() {
        //TODO: Design - this method to set opponent
    }

    public void connectToGame(){

    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public String getName() {
        return name;
    }
    public void setClient(Client client) {
        this.client = client;
        this.name = client.getName();
    }

    public void gameStart(){
    }

    public void updateMainInfo() {
    }

    public void setOpponentName() {
    }

    public void updateScore(Integer clientScore, Integer opponentScore) {
    }

    public void gameStartsInTime(Long timeInstanceOfStart) {
    }

    public void updatePuckDirection(Double puckX, Double puckY) {
    }

    public void updateOpponentMalletDirection(Double malletX, Double malletY) {
    }
}
