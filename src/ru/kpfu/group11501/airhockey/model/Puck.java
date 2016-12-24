package ru.kpfu.group11501.airhockey.model;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class Puck implements Controllable {
    @Override
    public void move(double newX, double newY) {

    }

    @Override
    public void block() {

    }

    @Override
    public void unblock() {

    }

    @Override
    public boolean isBlocked() {
        return false;
    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double setX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public double setY() {
        return 0;
    }
}
