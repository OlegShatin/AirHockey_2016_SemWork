package ru.kpfu.group11501.airhockey.model;

import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import ru.kpfu.group11501.airhockey.net.Client;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class Mallet implements Controllable {

    private ImageView view;
    private Pane gameField;
    private AnimationTimer currentMoveTimer;
    private Puck puck;
    private Client client;

    private static final double DISTANCE = 10;
    private boolean blocked;

    public ImageView getView() {
        return view;
    }

    public Mallet(ImageView view, Pane gameField, Client client) {
        this.view = view;
        this.gameField = gameField;
        blocked = false;
        this.client = client;
    }

    @Override
    public void move(double newX, double newY, boolean needSendDataToServer) {
        if(gameField.getChildren().contains(view) && !blocked) {
            if (needSendDataToServer) {
                if (newY < 300 + view.getFitWidth()/2) {
                    newY = 300 + view.getFitWidth()/2;
                }
                synchronized (client) {
                    client.sendMalletDirection(newX, newY);
                }
            }
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
                    double deltaX = puck.getX() - getX();
                    double deltaY = puck.getY() - getY();

                    double vectorLenght =
                            Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    if (i < DISTANCE && !blocked && vectorLenght > view.getFitWidth() + Math.max(rateX, rateY)) {

                        view.setX(view.getX() + rateX);
                        view.setY(view.getY() + rateY);
                        i++;
                    } else {

                        this.stop();
                    }
                }
            };
            currentMoveTimer.start();
        } else {
            if (currentMoveTimer != null) {
                currentMoveTimer.stop();
            }
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
        return view.getX()+ view.getFitWidth()/2 + view.getLayoutX();
    }

    @Override
    public void setX(double newX) {
        view.setX(newX - view.getLayoutX() - view.getFitWidth()/2);
    }

    @Override
    public double getY() {
        return view.getY() + view.getFitHeight()/2 + view.getLayoutY();
    }

    @Override
    public void setY(double newY) {
        view.setY(newY - view.getLayoutY() - view.getFitHeight()/2);
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
    }
    public void setPuck(Puck puck) {
        this.puck = puck;
    }


}
