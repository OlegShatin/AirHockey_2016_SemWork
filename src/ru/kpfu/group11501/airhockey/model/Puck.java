package ru.kpfu.group11501.airhockey.model;

import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class Puck implements Controllable {

    private static final double DISTANCE = 100;

    private ImageView view;
    private Pane gameField;
    private AnimationTimer currentMoveTimer;
    private boolean blocked;
    private Mallet[] mallets;

    public Puck(ImageView view, Pane gameField, Mallet userMallet, Mallet opponentMallet) {
        this.view = view;
        this.gameField = gameField;
        mallets = new Mallet[2];
        mallets[0] = userMallet;
        mallets[1] = opponentMallet;
    }

    @Override
    public void move(double newX, double newY) {
        if (gameField.getChildren().contains(view) && !blocked) {
            double dX = newX - view.getX() - view.getLayoutX() - view.getFitWidth() / 2;
            double dY = newY - view.getY() - view.getLayoutY() - view.getFitHeight() / 2;

            double rateX = dX / DISTANCE;
            double rateY = dY / DISTANCE;

            currentMoveTimer = new AnimationTimer() {
                int i = 0;

                @Override
                public void handle(long now) {
                    for (Mallet mallet : mallets) {
                        if (view.getBoundsInParent().intersects(mallet.getView().getBoundsInParent())) {
                            double deltaX = mallet.getX() - getX();
                            double deltaY = mallet.getY() - getY();
                            double vectorLenght =
                                    Math.sqrt(deltaX * deltaX + deltaY + deltaY);
                            double intersectX = getX() + deltaX / vectorLenght;
                            double intersectY = getY() + deltaY / vectorLenght;
                            double deltaOrtoX = newX - getX();
                            double deltaOrtoY = -deltaX * deltaOrtoX / deltaY;
                            double updatedX = newX + 2 * (deltaOrtoX + intersectX - newX);
                            double updatedY = newY + 2 * (deltaOrtoY + intersectY - newY);
                            this.stop();
                            move(updatedX, updatedY);
                        }
                    }
                    if (getX() < view.getFitWidth() || getX() > 200 - view.getFitWidth()) {
                        this.stop();
                        move(Math.abs(200 - newX), newY);
                    } else {
                        if (getY() < view.getFitHeight() || getY() > 400 - view.getFitHeight()) {
                            this.stop();
                            move(newX,Math.abs(400 - newY));
                        } else {
                            if (i < DISTANCE) {
                                view.setX(view.getX() + rateX);
                                view.setY(view.getY() + rateY);
                                i++;
                            } else {
                                this.stop();
                            }
                        }
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

    //getters and setters are centred
    @Override
    public double getX() {
        return view.getX() - view.getFitWidth() / 2 - view.getLayoutX();
    }

    @Override
    public void setX(double newX) {

        view.setX(newX - view.getLayoutX() - view.getFitWidth() / 2);
    }

    @Override
    public double getY() {
        return view.getY() - view.getFitHeight() / 2 - view.getLayoutY();
    }

    @Override
    public void setY(double newY) {

        view.setY(newY - view.getLayoutY() - view.getFitHeight() / 2);
    }

    public ImageView getView() {
        return view;
    }
}
