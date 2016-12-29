package ru.kpfu.group11501.airhockey.model;

import ru.kpfu.group11501.airhockey.net.Client;

/**
 * @author Oleg Shatin
 *         11-501
 *
 * interface decribes controlabed object by Controller on game field.
 */
public interface Controllable {
    /**
     * set new point on game field, to which object will move.
     * @param newX
     * @param newY
     */
    void move(double newX, double newY, boolean needSendDataToServer);

    /**
     * set this object's state not controllable more - block it
     */
    void block();

    /**
     * set this object's state controllable again
     */
    void unblock();

    /**
     *
     * @return is this object controllable now or no
     */
    boolean isBlocked();

    /**
     *
     * getters and setters of coordinates on game directly
     */
    double getX();
    void setX(double newX);
    double getY();
    void setY(double newY);
    void setClient(Client client);
}
