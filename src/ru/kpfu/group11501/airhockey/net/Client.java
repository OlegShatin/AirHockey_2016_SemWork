package ru.kpfu.group11501.airhockey.net;

import ru.kpfu.group11501.airhockey.controller.Controller;

/**
 * @author Oleg Shatin
 *         11-501
 */
public interface Client {
    void setOpponentName(String opponentName);
    void updatePuckDirection(Double puckX, Double puckY);
    void updateGameScore(Integer clientScore,Integer opponentScore);
    void updateOpponentMalletDirection(Double malletX, Double malletY);
    void setGameResult(Integer clientScore,Integer opponentScore);
    void gameStartsInTime(Long timeInstanceOfStart);
    void opponentIsReady();
    void opponentLeftGame();
    void sendPuckDirection(Double puckX, Double puckY);
    void sendMalletDirection(Double malletX, Double malletY);
    void setReady();
    void setController(Controller controller);
    void askGame(String name);
    void leaveGame();
    void loseRound();

    void setStartFlag(Boolean b);

    Boolean getStartFlag();

    Boolean getUserIsReady();

    String getName();
    String getOpponentName();

    Boolean getOpponentIsReady();

    void setUserIsReady(Boolean b);


}
