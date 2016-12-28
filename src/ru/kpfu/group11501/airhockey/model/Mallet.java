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
    private Puck puck;

    private static final double DISTANCE = 10;
    private boolean blocked;

    public ImageView getView() {
        return view;
    }

    public Mallet(ImageView view, Pane gameField) {
        this.view = view;
        this.gameField = gameField;
        blocked = false;
    }

    @Override
    public void move(double newX, double newY) {
        if(gameField.getChildren().contains(view) && !blocked) {
            double dX = newX - view.getX() - view.getLayoutX() - view.getFitWidth()/2;
            double dY = newY - view.getY() - view.getLayoutY() - view.getFitHeight()/2;

            double rateX = dX / DISTANCE;
            double rateY = dY / DISTANCE;

            if(currentMoveTimer != null) {
                currentMoveTimer.stop();
            }
            currentMoveTimer = new AnimationTimer() {
                int i = 0;
                @Override
                public void handle(long now) {
                    if (view.getBoundsInParent().intersects(puck.getView().getBoundsInParent())){
                        puck.move(getX(),getY());
                    }
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
        blocked = true;
    }

    @Override
    public void unblock() {
        blocked = false;
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    @Override
    public double getX() {
        return view.getX()- view.getFitWidth()/2 - view.getLayoutX();
    }

    @Override
    public void setX(double newX) {
        view.setX(newX - view.getLayoutX() - view.getFitWidth()/2);
    }

    @Override
    public double getY() {
        return view.getY()- view.getFitHeight()/2 - view.getLayoutY();
    }

    @Override
    public void setY(double newY) {
        view.setY(newY - view.getLayoutY() - view.getFitHeight()/2);
    }

    public void setPuck(Puck puck) {
        this.puck = puck;
    }
}
