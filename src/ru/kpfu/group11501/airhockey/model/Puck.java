package ru.kpfu.group11501.airhockey.model;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import ru.kpfu.group11501.airhockey.net.Client;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class Puck implements Controllable {
    private static final double FIELD_WIDTH = 400.0;
    private static final double FIELD_HEIGHT = 600.0;
    private static final double SPEED = 5;
    private Client client;

    private ImageView view;
    private Pane gameField;
    private AnimationTimer currentMoveTimer;
    private boolean blocked;
    private Mallet[] mallets;
    private Mallet userMallet;
    private Thread selfMover;
    private Object selfMoveSync = new Object();
    private double selfMoveX;
    private double selfMoveY;
    private Region gates;

    public Puck(ImageView view, Pane gameField, Mallet userMallet, Client client, Region userGates) {
        this.view = view;
        this.gameField = gameField;
        this.userMallet = userMallet;
        this.client = client;
        this.gates = userGates;
        selfMover = new Thread(() -> {
            while (true) {
                synchronized (selfMoveSync) {
                    try {
                        selfMoveSync.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Platform.runLater(() -> {
                    move(selfMoveX, selfMoveY, true);
                });
            }
        });
        selfMover.setDaemon(true);
        selfMover.start();
    }

    private enum Border {
        LEFT(0.0, 0.0, 0.0, FIELD_HEIGHT), RIGHT(FIELD_WIDTH, 0.0, FIELD_WIDTH, FIELD_HEIGHT), TOP(0.0, 0.0, FIELD_WIDTH, 0.0), BOTTOM(0.0, FIELD_HEIGHT, FIELD_WIDTH, FIELD_HEIGHT);
        public double x1, x2, y1, y2;

        Border(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    @Override
    public void move(double newX, double newY, boolean needSendDataToServer) {
        double currX = getX();
        double currY = getY();
        if (gameField.getChildren().contains(view) && !blocked) {
            double dX = (newX - getX());
            double dY = (newY - getY());
            Border directionBorder = null;

            if (!((Math.abs(newX - 0.0) < 10e-3 && newX <= FIELD_WIDTH && newX >= 0) || (Math.abs(newY - 0.0) < 10e-3 && newY <= FIELD_HEIGHT && newY >= 0))) {
                //define the edge of field that will be next
                if (newX / newY < currX / currY && newX / (newY - Border.LEFT.y2) > currX / (currY - Border.LEFT.y2)) {
                    directionBorder = Border.LEFT;
                } else {
                    if ((newY - Border.BOTTOM.y1) < currX / (currY - Border.BOTTOM.y1)
                            && (Border.BOTTOM.x2 - currX) / (Border.BOTTOM.y2 - currY) < (Border.BOTTOM.x2 - newX) / (Border.BOTTOM.y2 - newY)) {
                        directionBorder = Border.BOTTOM;
                    } else {
                        if (newX / newY > currX / currY
                                && (Border.TOP.x2 - currX) / (Border.TOP.y2 - currY) > (Border.TOP.x2 - newX) / (Border.TOP.y2 - newY)) {
                            directionBorder = Border.TOP;
                        } else {
                            directionBorder = Border.RIGHT;
                        }
                    }
                }
                double oldDX = dX;
                double oldDY = dY;
                switch (directionBorder) {
                    case LEFT:
                        dX = -currX;
                        dY *= dX / oldDX;
                        break;
                    case BOTTOM:
                        dY = directionBorder.y2 - currY;
                        dX *= dY / oldDY;
                        break;
                    case RIGHT:
                        dX = directionBorder.x2 - currX;
                        dY *= dX / oldDX;
                        break;
                    case TOP:
                        dY = -currY;
                        dX *= dY / oldDY;
                }
            }

            double newCorrectX = currX + dX;
            double newCorrectY = currY + dY;

            synchronized (client) {
                client.sendPuckDirection(newCorrectX, newCorrectY);
            }
            double trackLenght = Math.sqrt(dX * dX + dY * dY) / SPEED;
            double rateX = dX / trackLenght;
            double rateY = dY / trackLenght;
            if (currentMoveTimer != null) {
                currentMoveTimer.stop();
            }


            double finalDY = dY;
            currentMoveTimer = new AnimationTimer() {
                int i = 0;

                @Override
                public void handle(long now) {
                    //firstly check is intersects with userMallet
                    double actualX = getX();
                    double actualY = getY();
                    //defense from bugs
                    if (actualX > FIELD_WIDTH || actualX < 0 || actualY > FIELD_HEIGHT || actualY < 0){
                        this.stop();
                        selfMoveX = 10;
                        selfMoveY = 10;
                        setX(FIELD_WIDTH / 2);
                        setY(FIELD_HEIGHT/ 2);
                        synchronized (selfMoveSync) {
                            selfMoveSync.notifyAll();
                        }
                        return;
                    }
                    double deltaX = userMallet.getX() - actualX;
                    double deltaY = userMallet.getY() - actualY;
                    double vectorLenght =
                            Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    if (actualX < gates.getLayoutX() + gates.getWidth() && actualX > gates.getLayoutX() &&
                            actualY > FIELD_HEIGHT - 2 * view.getFitHeight()){
                        System.out.println("in gates!");
                        this.stop();
                        block();
                        setX(FIELD_WIDTH / 2);
                        setY(FIELD_HEIGHT/ 2);
                        synchronized (client) {
                            client.loseRound();
                        }
                    } else {
                        if (vectorLenght < view.getFitHeight() / 2 + userMallet.getView().getFitHeight() / 2) {

                            double intersectY = actualY + deltaY / 2;
                            double intersectX = actualX + deltaX / 2;
                            //ortogonally vector with length == 1
                            double deltaOrtoY = (finalDY / Math.abs(finalDY)) * Math.sqrt(1 / (1 + (deltaY * deltaY) / (deltaX * deltaX)));
                            double deltaOrtoX = -deltaY * deltaOrtoY / deltaX;

                            double currentVectorX = newCorrectX - intersectX;
                            double currentVectorY = newCorrectY - intersectY;


                            double scalarMult = deltaOrtoX * currentVectorX + deltaOrtoY * currentVectorY;
                            deltaOrtoX *= scalarMult;
                            deltaOrtoY *= scalarMult;

                            double antiNormalVectorX = intersectX + deltaOrtoX - newCorrectX;
                            double antiNormalVectorY = intersectY + deltaOrtoY - newCorrectY;

                            double updatedX = newCorrectX + 2 * (antiNormalVectorX);
                            double updatedY = newCorrectY + 2 * (antiNormalVectorY);
                            this.stop();
                            setX(intersectX + (updatedX - intersectX) / Math.abs((updatedX - intersectX)) * (view.getFitHeight()));
                            setY(intersectY + (updatedY - intersectY) / Math.abs((updatedY - intersectY)) * (view.getFitHeight()));
                            selfMoveX = updatedX;
                            selfMoveY = updatedY;
                            synchronized (selfMoveSync) {
                                selfMoveSync.notifyAll();
                            }
                        } else {

                            if (getX() < view.getFitWidth() / 2) {
                                this.stop();
                                setX(view.getFitWidth() + 1);
                                selfMoveX = FIELD_WIDTH;
                                selfMoveY = newCorrectY;
                                synchronized (selfMoveSync) {
                                    selfMoveSync.notifyAll();
                                }
                            } else {
                                if (getX() > FIELD_WIDTH - view.getFitWidth() / 2) {
                                    this.stop();
                                    setX(FIELD_WIDTH - view.getFitWidth() - 1);
                                    selfMoveX = 0;
                                    selfMoveY = newCorrectY;
                                    synchronized (selfMoveSync) {
                                        selfMoveSync.notifyAll();
                                    }
                                } else {
                                    if (getY() < view.getFitHeight()) {
                                        this.stop();
                                        setY(view.getFitHeight() + 1);
                                        selfMoveX = newCorrectX;
                                        selfMoveY = FIELD_HEIGHT;
                                        synchronized (selfMoveSync) {
                                            selfMoveSync.notifyAll();
                                        }
                                    } else {
                                        if (getY() > FIELD_HEIGHT - view.getFitHeight()) {
                                            this.stop();
                                            setY(FIELD_HEIGHT - view.getFitHeight() - 1);
                                            selfMoveX = newCorrectX;
                                            selfMoveY = 0;
                                            synchronized (selfMoveSync) {
                                                selfMoveSync.notifyAll();
                                            }
                                        } else {
                                            if (i < trackLenght && !blocked) {
                                                view.setX(view.getX() + rateX);
                                                view.setY(view.getY() + rateY);
                                                i++;
                                            } else {
                                                this.stop();
                                            }
                                        }
                                    }
                                }
                            }
                        }
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

    //getters and setters are centred
    @Override
    public double getX() {
        return view.getX() + view.getFitWidth() / 2 + view.getLayoutX();
    }

    @Override
    public void setX(double newX) {

        view.setX(newX - view.getLayoutX() - view.getFitWidth() / 2);
    }

    @Override
    public double getY() {
        return view.getY() + view.getFitHeight() / 2 + view.getLayoutY();
    }

    @Override
    public void setY(double newY) {

        view.setY(newY - view.getLayoutY() - view.getFitHeight() / 2);
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
    }

    public ImageView getView() {
        return view;
    }
}
