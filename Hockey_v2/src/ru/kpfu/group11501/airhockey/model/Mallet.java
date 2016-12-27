package ru.kpfu.group11501.airhockey.model;

import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class Mallet implements Controllable {

    private ImageView view;
    private Pane gameField;
    private AnimationTimer currentMoveTimer;

    private static final double DISTANCE = 10;

    public ImageView getView() {
        return view;
    }

    public Mallet(ImageView view, Pane gameField) {
        this.view = view;
        this.gameField = gameField;
    }

    @Override
    public void move(double newX, double newY) {
        if(gameField.getChildren().contains(view)) {
            double dX = newX - view.getX() - view.getLayoutX();
            double dY = newY - view.getY() - view.getLayoutY();

            double rateX = dX / DISTANCE;
            double rateY = dY / DISTANCE;

            if(currentMoveTimer != null) {
                currentMoveTimer.stop();
            }
            currentMoveTimer = new AnimationTimer() {
                int i = 0;
                @Override
                public void handle(long now) {
                    if (i < DISTANCE) {
                        view.setX(view.getX() + rateX);
                        view.setY(view.getY() + rateY);
                        i++;
                    } else {
                        this.stop();
                    }
                }
            };
            currentMoveTimer.start();
        }
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
        return view.getX();
    }

    @Override
    public void setX(double newX) {
        view.setX(newX);
    }

    @Override
    public double getY() {
        return view.getY();
    }

    @Override
    public void setY(double newY) {
        view.setY(newY);
    }

}
