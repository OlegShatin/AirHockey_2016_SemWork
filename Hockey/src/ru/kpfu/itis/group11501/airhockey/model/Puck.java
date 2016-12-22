package ru.kpfu.itis.group11501.airhockey.model;


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

            double rateX = dX / DISTANCE;
            double rateY = dY / DISTANCE;

            if(currentMoveTimer != null) {
                currentMoveTimer.stop();
            }

            currentMoveTimer = new AnimationTimer() {
                double wasX = view.getLayoutX();
                int d = (int) view.getScaleX();
                @Override
                public void handle(long now) {
                    int k = -1;
                    /*while (view.getX() < wasX) {
                        view.setX(view.getX() + d*DISTANCE);
                        view.setY(view.getY() - d*DISTANCE);
                        System.out.println(view.getX());
                    }*/
                    while (view.getY() > (-1)*wasX) {
                        view.setX(view.getX() + d*DISTANCE);
                        view.setY(view.getY() + d*DISTANCE);
                        System.out.println(view.getX());
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
}
