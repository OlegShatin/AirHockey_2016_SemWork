package ru.kpfu.group11501.airhockey.net;

import ru.kpfu.group11501.airhockey.controller.Controller;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class ClientImpl implements Runnable, Client {
    public static final int PORT = 3456;
    public static final String HOST = "localhost";
    private String toSend;
    //todo: remove it after testing
    private Scanner scanner;
    private Thread thread;
    private Thread senderThread;
    private String name;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private boolean isConnected = false;
    private Socket server;
    private Controller controller;

    public ClientImpl(String name, Controller controller) {
        this.controller = controller;
        // создаем поток, передавая поведение
        // нашего Client
        thread = new Thread(this);
        scanner = new Scanner(System.in);
        thread.start();
        this.name = name;

    }

    public void run() {
        server = null;
        try {
            server = new Socket(HOST, PORT);
            printWriter = new PrintWriter(server.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(server.getInputStream()));
            isConnected = true;
            //notify for controller;
            notify();

            //thread to send data to server
            senderThread = new Thread(() -> {
                try {
                    while (true) {
                        synchronized (toSend) {
                            toSend.wait();
                            printWriter.println(toSend);
                            toSend = "";
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
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
                    args = new Object[request[1].split("&").length];
                    int i = 0;
                    try {
                        for (String stringArg : request[1].split("&")) {
                            if (method.getArgsClasses()[i].equals(Integer.class)) {
                                args[i] = method.getArgsClasses()[i].getMethod("parseInt", String.class)
                                        .invoke(null, stringArg);
                            } else {
                                if (method.getArgsClasses()[i].equals(String.class)) {
                                    args[i]  = stringArg;
                                } else {
                                    args[i] = method.getArgsClasses()[i]
                                            .getMethod("parse" + method.getArgsClasses()[i].getName(), String.class)
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
        }
    }

    @Override
    public void updatePuckDirection(Double puckX, Double puckY) {

    }

    @Override
    public void updateGameScore(Integer clientScore, Integer opponentScore) {

    }

    @Override
    public void updateOpponentMalletDirection(Double malletX, Double malletY) {

    }

    @Override
    public void setGameResult(Integer clientScore, Integer opponentScore) {

    }

    @Override
    public void gameStartsInTime(Long timeInstanceOfStart) {

    }

    @Override
    public void opponentIsReady() {

    }

    @Override
    public void opponentLeftGame() {

    }

    @Override
    public void askGame() {

    }

    @Override
    public void leaveGame() {

    }

    @Override
    public void loseRound() {

    }
}

