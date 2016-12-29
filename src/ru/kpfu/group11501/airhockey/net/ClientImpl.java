package ru.kpfu.group11501.airhockey.net;

import javafx.application.Platform;
import ru.kpfu.group11501.airhockey.controller.Controller;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class ClientImpl implements Runnable, Client {
    public static final int PORT = 3456;
    private String toSend;
    private Thread thread;
    private Thread senderThread;
    private String name;
    private String opponentName;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private boolean isConnected = false;
    private Socket server;
    private Controller controller;
    private Boolean userIsReady;
    private Boolean opponentIsReady;
    private Boolean startFlag;
    private final Object toSendFlag = new Object();
    private String hostname;

    public Boolean getUserIsReady() {
        return userIsReady;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOpponentName() {
        return opponentName;
    }

    public void setUserIsReady(Boolean userIsReady) {
        this.userIsReady = userIsReady;

    }

    public Boolean getOpponentIsReady() {
        return opponentIsReady;
    }

    public void setOpponentIsReady(Boolean opponentIsReady) {
        this.opponentIsReady = opponentIsReady;
    }

    public Boolean getStartFlag() {
        return startFlag;
    }

    public void setStartFlag(Boolean startFlag) {
        Boolean temp = this.startFlag;
        this.startFlag = startFlag;
        synchronized (temp) {
            temp.notifyAll();
        }


    }

    public ClientImpl(String name, Controller controller, String hostname) {
        this.hostname = hostname;
        this.name = name;
        userIsReady = new Boolean(false);
        startFlag = new Boolean(false);
        opponentName = "Ожидание игрока";
        opponentIsReady = new Boolean(false);
        this.setController(controller);
        // создаем поток, передавая поведение
        // нашего Client
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();




    }

    public void run() {
        server = null;

        try {

            server = new Socket(hostname, PORT);
            printWriter = new PrintWriter(server.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(server.getInputStream()));
            isConnected = true;
            //notify for controller;
            //notify();

            //thread to send data to server
            senderThread = new Thread(() -> {
                try {
                    toSend = "";
                    while (true) {
                        synchronized (toSendFlag) {
                            toSendFlag.wait();

                            if (!toSend.equals("")) {

                                printWriter.println(toSend);
                            } else {
                                System.out.println("TRY TO SEND NOTHING!");
                            }
                            toSend = "";
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            senderThread.setDaemon(true);
            senderThread.start();




            //main receiving data loop
            while (true) {
                //get - code of method and args values
                String[] request = bufferedReader.readLine().split("\\?");

                NetMethod method = NetMethod.getMethod(request[0]);
                Method executableMethod = Client.class.getMethod(method.name(), method.getArgsClasses());
                //try to parse arguments from string to Integer, Double and Long if they exists
                Object[] args = null;
                if (request.length > 1) {
                    args = new Object[request[1].split("\\&").length];
                    int i = 0;
                    try {
                        for (String stringArg : request[1].split("\\&")) {
                            if (method.getArgsClasses()[i].equals(Integer.class)) {
                                args[i] = method.getArgsClasses()[i].getMethod("parseInt", String.class)
                                        .invoke(null, stringArg);
                            } else {
                                if (method.getArgsClasses()[i].equals(String.class)) {
                                    args[i]  = stringArg;
                                } else {
                                    args[i] = method.getArgsClasses()[i]
                                            .getMethod("parse" + method.getArgsClasses()[i].getSimpleName(), String.class)
                                            .invoke(null, stringArg);
                                }
                            }
                            i++;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();

                    }
                }
                //execute this method
                executableMethod.invoke(this, args);

            }
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOpponentName(String opponentName) {

        this.opponentName = opponentName;
        Platform.runLater(() -> {
            controller.setOpponentName();
        });

    }

    @Override
    public void updatePuckDirection(Double puckX, Double puckY) {
        Platform.runLater(() -> {
            controller.updatePuckDirection(puckX, puckY);
        });
    }

    @Override
    public void updateGameScore(Integer clientScore, Integer opponentScore) {
        Platform.runLater(() -> {
            controller.updateScore(clientScore, opponentScore);
        });

    }

    @Override
    public void updateOpponentMalletDirection(Double malletX, Double malletY) {
        Platform.runLater(() -> {
            controller.updateOpponentMalletDirection(malletX, malletY);
        });
    }

    @Override
    public void setGameResult(Integer clientScore, Integer opponentScore) {
        startFlag = false;
        Platform.runLater(() -> {
            controller.updateScore(clientScore, opponentScore);
        });

    }

    @Override
    public void gameStartsInTime(Long timeInstanceOfStart) {
        Platform.runLater(() -> {
            controller.gameStart();
            controller.setOpponentName();
        });
        Platform.runLater(() -> {
            controller.gameStartsInTime(timeInstanceOfStart);
        });

    }

    @Override
    public void opponentIsReady() {
        System.out.println("opponentIsReady GOT FROM SERVER");
        synchronized (opponentIsReady){
            opponentIsReady.notifyAll();
            opponentIsReady = true;

        }
    }

    @Override
    public void opponentLeftGame() {

    }

    @Override
    public void sendPuckDirection(Double puckX, Double puckY) {

        synchronized (toSendFlag){
            if (puckX.toString().length() >= 7 && puckY.toString().length() >= 7) {
                toSend = NetMethod.clientUpdatesPuckDirection.getCode() + "?" + puckX.toString().substring(0, 7) + "&" + puckY.toString().substring(0, 7);
            } else {
                toSend = NetMethod.clientUpdatesPuckDirection.getCode() + "?" + puckX.toString() + "&" + puckY.toString();
            }
            toSendFlag.notifyAll();
        }
    }

    @Override
    public void sendMalletDirection(Double malletX, Double malletY) {

        synchronized (toSendFlag){
            if (malletX.toString().length() >= 7 && malletY.toString().length() >= 7) {
                toSend = NetMethod.updateClientMalletDirection.getCode() + "?" + malletX.toString().substring(0, 7) + "&" + malletY.toString().substring(0, 7);
            } else {
                toSend = NetMethod.updateClientMalletDirection.getCode() + "?" + malletX.toString() + "&" + malletY.toString();
            }
            toSendFlag.notifyAll();
        }
    }

    @Override
    public void setReady() {
        toSend = NetMethod.clientIsReady.getCode();
        synchronized (toSendFlag){
            toSendFlag.notifyAll();
        }
        synchronized (opponentIsReady) {
            if (!opponentIsReady) {
                try {
                    System.out.println("waiting opponent is ready");
                    opponentIsReady.wait();
                    System.out.println("waiting opponent is ready off");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
        controller.setClient(this);
        Platform.runLater(()->{
            controller.setOpponentName();
        });

    }

    @Override
    public void askGame(String name) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (toSendFlag){
            toSend = NetMethod.clientAsksGame.getCode()+"?"+name;
            toSendFlag.notifyAll();
        }
        synchronized (controller) {
            controller.notifyAll();
        }

    }

    @Override
    public void leaveGame() {

    }

    @Override
    public void loseRound() {
        synchronized (toSendFlag){
            System.out.println("lose");
            toSend = NetMethod.clientLoseRound.getCode();
            toSendFlag.notifyAll();
        }
    }
}

