package ru.kpfu.group11501.airhockey.net;

import java.net.SocketException;

/**
 * @author Oleg Shatin
 *         11-501
 */
public interface Server {
    void updatePuckDirection(Double clientPuckX, Double clientPuckY);
    void updateClientMalletDirection(Double clientMalletX, Double clientMalletY);
    void clientIsReady();
    void clientAsksGame();
    void clientLeavesGame() throws SocketException;
    void clientLoseRound();
}
