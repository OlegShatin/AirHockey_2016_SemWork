package ru.kpfu.group11501.airhockey.controller;

import ru.kpfu.group11501.airhockey.net.Client;

public class Controller implements Client {
    @Override
    public void updatePuckDirection(Double puckX, Double puckY) {

    }

    @Override
    public void updateGameScore(Integer clientScore, Integer opponentScore) {

    }

    @Override
    public void updateOpponentMalletDirection(Double malletX, Double malletY) {

    }

    @Override
    public void setGameResult(Integer clientScore, Integer opponentScore) {

    }

    @Override
    public void gameStartsInTime(Long timeInstanceOfStart) {

    }

    @Override
    public void opponentIsReady() {

    }

    @Override
    public void opponentLeftGame() {

    }
}
