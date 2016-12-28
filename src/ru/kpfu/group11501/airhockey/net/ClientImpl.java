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
    public static final String HOST = "localhost";
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

    public Boolean getUserIsReady() {
        return userIsReady;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOpponentName() {
        return name;
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

            temp.notifyAll();


    }

    public ClientImpl(String name, Controller controller) {
        this.setController(controller);
        // создаем поток, передавая поведение
        // нашего Client
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
        this.name = name;
        opponentName = "Ожидание игрока";
        userIsReady = new Boolean(false);
        opponentIsReady = new Boolean(false);
        startFlag = new Boolean(false);


    }

    public void run() {
        server = null;

        try {

            server = new Socket(HOST, PORT);
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
                            System.out.println("start waiting");
                            toSendFlag.wait();
                            System.out.println("sending: "+ toSend);
                            System.out.println("stop waiting");
                            printWriter.println(toSend);
                            toSend = "";
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            senderThread.setDaemon(true);
            senderThread.start();
            synchronized (controller){
                controller.wait();
                System.out.println("ITSWORK");
            }



            //main receiving data loop
            while (true) {
                //get - code of method and args values
                String[] request = bufferedReader.readLine().split("\\?");

                NetMethod method = NetMethod.getMethod(request[0]);
                System.out.print(method.name());
                Method executableMethod = Client.class.getMethod(method.name(), method.getArgsClasses());
                //try to parse arguments from string to Integer, Double and Long if they exists
                Object[] args = null;
                if (request.length > 1) {
                    args = new Object[request[1].split("&").length];
                    int i = 0;
                    try {
                        for (String stringArg : request[1].split("&")) {
                            System.out.println(" " + stringArg);
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOpponentName(String opponentName) {

        this.opponentName = opponentName;
        Platform.runLater(() -> {
            controller.setOpponentName(opponentName);
        });

    }

    @Override
    public void updatePuckDirection(Double puckX, Double puckY) {

    }

    @Override
    public void updateGameScore(Integer clientScore, Integer opponentScore) {
        Platform.runLater(() -> {
            controller.updateScore(clientScore, opponentScore);
        });

    }

    @Override
    public void updateOpponentMalletDirection(Double malletX, Double malletY) {

    }

    @Override
    public void setGameResult(Integer clientScore, Integer opponentScore) {

    }

    @Override
    public void gameStartsInTime(Long timeInstanceOfStart) {
        Platform.runLater(() -> {
            controller.gameStart();
        });
        Platform.runLater(() -> {
            controller.gameStartsInTime(timeInstanceOfStart);
        });

    }

    @Override
    public void opponentIsReady() {
        synchronized (opponentIsReady){
            opponentIsReady.notifyAll();
            opponentIsReady = true;

        }
    }

    @Override
    public void opponentLeftGame() {

    }

    @Override
    public void setReady() {
        toSend = NetMethod.clientIsReady.getCode();
        synchronized (toSendFlag){
            toSendFlag.notifyAll();
        }
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
        controller.setClient(this);
        controller.setOpponentName(opponentName);
    }

    @Override
    public void askGame(String name) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        toSend = NetMethod.clientAsksGame.getCode()+"?"+name;
        synchronized (toSendFlag){
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

    }
}

