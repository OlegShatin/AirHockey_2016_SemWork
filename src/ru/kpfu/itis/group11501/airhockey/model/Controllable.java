package ru.kpfu.itis.group11501.airhockey.model;

public interface Controllable {
    void move(double newX, double newY);

    void block();

    void unblock();

    boolean isBlocked();
}
