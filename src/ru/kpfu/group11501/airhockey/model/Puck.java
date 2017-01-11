package ru.kpfu.group11501.airhockey.model;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import ru.kpfu.group11501.airhockey.net.Client;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class Puck implements Controllable {
    private static final double FIELD_WIDTH = 400.0;
    private static final double FIELD_HEIGHT = 600.0;
    private static final double SPEED = 8;
    private static final double EPS = 10e-6;
    private Line ortoLine;
    private Line targetLine;
    private Mallet vectTarget;
    private Label currTargetLabes;
    private Label curBoundsLabes;
    private Mallet target;
    private Client client;

    private ImageView view;
    private Pane gameField;
    private AnimationTimer currentMoveTimer;
    private boolean blocked;
    private Mallet userMallet;
    private Thread selfMover;
    private Object selfMoveSync = new Object();
    private double selfMoveX;
    private double selfMoveY;
    private Region gates;
    private double startX;
    private double startY;

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

    public Puck(ImageView sam, Pane gameField, Mallet userMallet, Client client,
                Region userGates, Mallet target, Label currMalBounds, Label currTarget, Mallet vectTarget, Line ortoLine, Line targetLine) {
        this(sam, gameField,userMallet,client,userGates);
        this.target = target;
        this.curBoundsLabes = currMalBounds;
        this.currTargetLabes = currTarget;
        this.vectTarget = vectTarget;
        this.ortoLine = ortoLine;
        this.targetLine = targetLine;
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
        if (currentMoveTimer != null) {
            currentMoveTimer.stop();
        }
        startX = getX();
        startY = getY();
        if (gameField.getChildren().contains(view) && !blocked) {
            if (newX < 0) {
                newX = 0;
            } else {
                if (newX > FIELD_WIDTH)
                    newX = FIELD_WIDTH;
            }
            if (newY < 0) {
                newY = 0;
            } else {
                if (newY > FIELD_HEIGHT)
                    newY = FIELD_HEIGHT;
            }
            double dX = (newX - getX());
            double dY = (newY - getY());
            Border directionBorder = null;

            if (!((Math.abs(newX - 0.0) < EPS || Math.abs(newX - FIELD_WIDTH) < EPS)
                    || (Math.abs(newY - 0.0) < EPS  || Math.abs(newY - FIELD_HEIGHT)< EPS))) {
                
                //define the edge of field that will be next
                if (newX / newY < startX / startY && newX / (newY - Border.LEFT.y2) > startX / (startY - Border.LEFT.y2)) {
                    directionBorder = Border.LEFT;
                } else {
                    if ((newY - Border.BOTTOM.y1) < startX / (startY - Border.BOTTOM.y1)
                            && (Border.BOTTOM.x2 - startX) / (Border.BOTTOM.y2 - startY) < (Border.BOTTOM.x2 - newX) / (Border.BOTTOM.y2 - newY)) {
                        directionBorder = Border.BOTTOM;
                    } else {
                        if (newX / newY > startX / startY
                                && (Border.TOP.x2 - startX) / (Border.TOP.y2 - startY) > (Border.TOP.x2 - newX) / (Border.TOP.y2 - newY)) {
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
                        dX = -startX;
                        dY *= dX / oldDX;
                        break;
                    case BOTTOM:
                        dY = directionBorder.y2 - startY;
                        dX *= dY / oldDY;
                        break;
                    case RIGHT:
                        dX = directionBorder.x2 - startX;
                        dY *= dX / oldDX;
                        break;
                    case TOP:
                        dY = -startY;
                        dX *= dY / oldDY;
                }
            }

            double newCorrectX = startX + dX;
            double newCorrectY = startY + dY;
            //currTargetLabes.setText((int)newCorrectX + "; " + (int)newCorrectY);
            target.setX(newCorrectX);
            target.setY(newCorrectY);
            synchronized (client) {
                client.sendPuckDirection(newCorrectX, newCorrectY);
            }
            double trackLenght = Math.sqrt(dX * dX + dY * dY) / SPEED;
            double rateX = dX / trackLenght;
            double rateY = dY / trackLenght;



            double finalDY = dY;
            currentMoveTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (!blocked) {
                        setX(getX() + rateX);
                        setY(getY() + rateY);
                    }
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
                            actualY > FIELD_HEIGHT - view.getFitHeight() / 2  - 5){
                        System.out.println("in gates!");
                        this.stop();

                        //block();
                        setX(FIELD_WIDTH / 2);
                        setY(FIELD_HEIGHT/ 2);
                        synchronized (client) {
                            client.loseRound();
                        }
                        selfMoveY = 0;
                        selfMoveX = 0;
                        synchronized (selfMoveSync) {
                            selfMoveSync.notifyAll();
                        }
                        return;
                    } else {
                        vectTarget.setX(actualX + deltaX/2);
                        vectTarget.setY(actualY + deltaY/2);
                        ortoLine.setStartX(actualX + deltaX/2);
                        ortoLine.setStartY(actualY + deltaY/2);

                        //ortogonally vector with length == 1
                        double deltaOrtoY = (finalDY / Math.abs(finalDY)) * Math.sqrt(1 / (1 + (deltaY * deltaY) / (deltaX * deltaX)));
                        double deltaOrtoX = -deltaY * deltaOrtoY / deltaX;

                        double currentVectorX = newCorrectX - actualX;
                        double currentVectorY = newCorrectY - actualY;


                        double scalarMult = deltaOrtoX * currentVectorX + deltaOrtoY * currentVectorY;
                        deltaOrtoX *= scalarMult;
                        deltaOrtoY *= scalarMult;

                        ortoLine.setEndX(ortoLine.getStartX() + deltaOrtoX);
                        ortoLine.setEndY(ortoLine.getStartY() + deltaOrtoY);

                        double antiNormalVectorX = deltaOrtoX - currentVectorX;
                        double antiNormalVectorY = deltaOrtoY - currentVectorY;
                        double updatedX = newCorrectX + 2 * (antiNormalVectorX);
                        double updatedY = newCorrectY + 2 * (antiNormalVectorY);

                        targetLine.setStartX(ortoLine.getEndX());
                        targetLine.setStartY(ortoLine.getEndY());
                        targetLine.setEndX(updatedX);
                        targetLine.setEndY(updatedY);


                        if (vectorLenght < userMallet.getView().getFitHeight() + 1) {

                            this.stop();
                            setX(actualX + (updatedX - actualX) / (Math.abs(updatedX - actualX)) * userMallet.getView().getFitHeight() / 2);
                            setY(actualY + (updatedY - actualY) / (Math.abs(updatedY - actualY)) * userMallet.getView().getFitHeight() / 2);
                            selfMoveX = updatedX;
                            selfMoveY = updatedY;
                            if (collaidsWithBorders(actualX, actualY)){
                                new Thread(()->{
                                    Platform.runLater(() -> {
                                        userMallet.block();
                                        userMallet.setX(userMallet.getX() - (updatedX - actualX) / (Math.abs(updatedX - actualX))
                                                * (Math.abs(vectorLenght - userMallet.getView().getFitWidth()) + 2));
                                        userMallet.setY(userMallet.getY() - (updatedY - actualY) / (Math.abs(updatedY - actualY))
                                                * (Math.abs(vectorLenght - userMallet.getView().getFitHeight()) + 2));
                                    });
                                    synchronized (this) {
                                        try {
                                            while (collaidsWithBorders(getX(), getY())){
                                                Thread.sleep(300);
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Platform.runLater(() -> {
                                        userMallet.unblock();
                                    });
                                }).start();
                                if (getX() < 0 + view.getFitWidth()/2) {
                                    selfMoveX = 0 + view.getFitWidth()/2;
                                    if (deltaY > 0) {
                                        selfMoveY = 0;
                                    }  else {
                                        selfMoveY = FIELD_HEIGHT;
                                    }
                                } else {
                                    if (getX() > FIELD_WIDTH - view.getFitWidth()/2) {
                                        selfMoveX = FIELD_WIDTH - view.getFitWidth()/2;
                                        if (deltaY > 0) {
                                            selfMoveY = 0;
                                        }  else {
                                            selfMoveY = FIELD_HEIGHT;
                                        }
                                    }
                                }
                                if (getY() < 0 + view.getFitHeight()/2) {
                                    selfMoveY = 0 + view.getFitHeight()/2;
                                    if (deltaX > 0) {
                                        selfMoveX = 0;
                                    }  else {
                                        selfMoveX = FIELD_WIDTH;
                                    }
                                } else {
                                    if (getY() > FIELD_HEIGHT - view.getFitHeight()/2){
                                        selfMoveY = FIELD_HEIGHT - view.getFitHeight()/2;
                                        if (deltaX > 0) {
                                            selfMoveX = 0;
                                        }  else {
                                            selfMoveX = FIELD_WIDTH;
                                        }
                                    }
                                }
                                setY(getY() + (selfMoveY - getY()) / 10);
                                setX(getX() + (selfMoveX - getX()) / 10);
                            }
                            synchronized (selfMoveSync) {
                                selfMoveSync.notifyAll();
                            }
                            return;
                        } else {
                            if (collaidsWithBorders(actualX,actualY)){
                                this.stop();
                                synchronized (selfMoveSync) {
                                    selfMoveSync.notifyAll();
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
    private boolean collaidsWithBorders(double actualX, double actualY){

        //handle impact with borders
        if (actualX < view.getFitWidth() / 2) {
            toSetX(view.getFitWidth() / 2 + 1);
            selfMoveX = startX;
            selfMoveY = actualY - (startY - actualY);
            return true;
        } else {
            if (actualX > FIELD_WIDTH - view.getFitWidth() / 2) {
                toSetX(FIELD_WIDTH - view.getFitWidth() / 2 - 1);
                selfMoveX = startX;
                selfMoveY = actualY - (startY - actualY);
                return true;
            } else {
                if (actualY < view.getFitHeight() / 2) {
                    toSetY(view.getFitHeight() / 2 + 1);
                    selfMoveX = actualX - (startX - actualX);
                    selfMoveY = startY;
                    return true;
                } else {
                    if (actualY > FIELD_HEIGHT - view.getFitHeight() / 2) {
                        toSetY(FIELD_HEIGHT - view.getFitHeight() / 2 - 1);
                        selfMoveX = actualX - (startX - actualX);
                        selfMoveY = startY;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void toSetY(double newY) {
        Platform.runLater(() -> {
            setY(newY);
        });
    }

    private void toSetX(double newX) {
        Platform.runLater(() -> {
            setX(newX);
        });
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
        //curBoundsLabes.setText((int)getX() + "; " + (int)getY());
        if (newX <= FIELD_WIDTH) {
            if (newX >= 0) {
                view.setX(newX - view.getLayoutX() - view.getFitHeight() / 2);
            } else {
                view.setX(0 - view.getLayoutX() - view.getFitHeight() / 2);
            }
        } else {
            view.setX(FIELD_WIDTH - view.getLayoutX() - view.getFitHeight() / 2);
        }
    }

    @Override
    public double getY() {
        return view.getY() + view.getFitHeight() / 2 + view.getLayoutY();
    }

    @Override
    public void setY(double newY) {
        //curBoundsLabes.setText((int)getX() + "; " + (int)getY());
        if (newY <= FIELD_HEIGHT) {
            if (newY >= 0) {
                view.setY(newY - view.getLayoutY() - view.getFitHeight() / 2);
            } else {
                view.setY(0 - view.getLayoutY() - view.getFitHeight() / 2);
            }
        } else {
            view.setY(FIELD_HEIGHT - view.getLayoutY() - view.getFitHeight() / 2);
        }
    }

    @Override
    public void setClient(Client client)
    {
        this.client = client;
    }

    public ImageView getView() {
        return view;
    }
}
