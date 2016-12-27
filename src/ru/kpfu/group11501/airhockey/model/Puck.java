package ru.kpfu.group11501.airhockey.model;

import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class Puck implements Controllable {

    private static final double DISTANCE = 10;

    private ImageView view;
    private Pane gameField;
    private AnimationTimer currentMoveTimer;

    public Puck(ImageView view, Pane gameField) {
        this.view = view;
        this.gameField = gameField;
    }

    @Override
    public void move(double newX, double newY) {
        if(gameField.getChildren().contains(view)) {
            double dX = newX - view.getX() - view.getLayoutX();
            double dY = newY - view.getY() - view.getLayoutY();

            if(currentMoveTimer != null) {
                currentMoveTimer.stop();
            }

            currentMoveTimer = new AnimationTimer() {
                double wasX = view.getLayoutX();
                int d = (int) view.getScaleX();
                @Override
                public void handle(long now) {
                    int k = -1;
                    if (view.getX() < 100) {
                        view.setX(view.getX() + d*DISTANCE);
                        view.setY(view.getY() + d*DISTANCE);
                        System.out.println(view.getX());
                    }
                    /* влево вверх
                    if (view.getY() > (-1)*wasX) {
                        view.setX(view.getX() - d*DISTANCE);
                        view.setY(view.getY() - d*DISTANCE);
                        System.out.println(view.getX());
                    }*/
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
