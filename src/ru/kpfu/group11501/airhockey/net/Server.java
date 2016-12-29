package ru.kpfu.group11501.airhockey.net;

import java.net.SocketException;

/**
 * @author Oleg Shatin
 *         11-501
 */
public interface Server {
    void clientUpdatesPuckDirection(Double clientPuckX, Double clientPuckY);
    void updateClientMalletDirection(Double clientMalletX, Double clientMalletY);
    void clientIsReady();
    void clientAsksGame(String name);
    void clientLeavesGame() throws SocketException;
    void clientLoseRound();
}
