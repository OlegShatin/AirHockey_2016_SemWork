package ru.kpfu.group11501.airhockey.net;

/**
 * @author Oleg Shatin
 *         11-501
 */
public interface Client {
    void updatePuckDirection(Double puckX, Double puckY);
    void updateGameScore(Integer clientScore,Integer opponentScore);
    void updateOpponentMalletDirection(Double malletX, Double malletY);
    void setGameResult(Integer clientScore,Integer opponentScore);
    void gameStartsInTime(Long timeInstanceOfStart);
    void opponentIsReady();
    void opponentLeftGame();

    void askGame();
    void leaveGame();
    void loseRound();

}
