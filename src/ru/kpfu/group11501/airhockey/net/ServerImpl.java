package ru.kpfu.group11501.airhockey.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;

/**
 * @author Oleg Shatin
 *         11-501
 */
public class ServerImpl extends Thread implements Server {
    static class Score{
        Score (){
            value = 0;
        }
        int value;
        int get() {
            return value;
        }
        void inc(){
            value++;
        }
        void setZero(){
            value = 0;
        }
    }
    private final static int PORT = 3456;
    private static int readyCounter = 0;
    private static final Score client1Score = new Score();
    private static final Score client2Score = new Score();


    private PrintWriter clientsPrintWriter;
    private PrintWriter opponentsPrintWriter;
    private Score opponentsScore;
    private Score clientsScore;
    private BufferedReader clientBufferedReader;
    private Object opponentFlag;

    public ServerImpl(PrintWriter clientsPrintWriter,
                      Score opponentsScore, Score clientsScore, BufferedReader clientBufferedReader,
                      Object opponentFlag) {
        this.clientsPrintWriter = clientsPrintWriter;
        this.opponentsScore = opponentsScore;
        this.clientsScore = clientsScore;
        this.clientBufferedReader = clientBufferedReader;
        this.opponentFlag = opponentFlag;
    }

    @Override
    public void clientUpdatesPuckDirection(Double clientPuckX, Double clientPuckY) {
        opponentsPrintWriter.println(NetMethod.updatePuckDirection.getCode()
                + "?"+Math.abs(clientPuckX - 400)+"&"+Math.abs(clientPuckY - 600));
    }

    @Override
    public void updateClientMalletDirection(Double clientMalletX, Double clientMalletY) {
        opponentsPrintWriter.println(NetMethod.updateOpponentMalletDirection.getCode()
                + "?"+Math.abs(400 - clientMalletX)+"&"+Math.abs(clientMalletY - 600));
    }

    @Override
    public void clientIsReady() {
        readyCounter++;
        if (opponentsPrintWriter == null){
            ServerImpl self = this;
            new Thread(new Runnable() {
                ServerImpl host = self;
                @Override
                public void run() {
                    try {
                        synchronized (opponentFlag){
                            opponentFlag.wait();
                        }
                        Thread.sleep(3000);
                        host.opponentsPrintWriter.println(NetMethod.opponentIsReady.getCode());
                        System.out.println("sent from buffer trhread to opponent: im ready");

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            opponentsPrintWriter.println(NetMethod.opponentIsReady.getCode());
            System.out.println("sent from main thread to opponent: im ready");
        }
        System.out.println("readyCounter: " + readyCounter);
        if (readyCounter == 2){
            //game starts in 5 seconds after this moment
            long startTime = Instant.now().toEpochMilli() + 1000 * 5;
            opponentsPrintWriter.println(NetMethod.gameStartsInTime.getCode()+"?"+startTime);
            clientsPrintWriter.println(NetMethod.gameStartsInTime.getCode()+"?"+startTime);
            System.out.println("GAME STARTS IN 5 SEC");
        }
    }

    @Override
    public void clientAsksGame(String name) {
        System.out.println(name + " asks game");
        if (opponentsPrintWriter == null){
            ServerImpl self = this;
            new Thread(new Runnable() {
                ServerImpl host = self;
                String savedName = name;
                @Override
                public void run() {
                    try {
                        synchronized (opponentFlag){
                            opponentFlag.wait();
                        }
                        Thread.sleep(3000);
                        host.opponentsPrintWriter.println(NetMethod.setOpponentName.getCode()+"?"+savedName);
                        System.out.println("sent from buffer trhread to opponent name" + savedName);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            opponentsPrintWriter.println(NetMethod.setOpponentName.getCode()+"?"+name);
            System.out.println("sent from main thread to opponent name" + name);
        }

    }

    @Override
    public void clientLeavesGame() throws SocketException {
        throw new SocketException("Client leave game");
    }

    @Override
    public void clientLoseRound() {
        System.out.println("client lose");
        opponentsScore.inc();
        opponentsPrintWriter.println(NetMethod.updateGameScore.getCode()+"?"+opponentsScore.get()+"&"+ clientsScore.get());
        clientsPrintWriter.println(NetMethod.updateGameScore.getCode()+"?"+ clientsScore.get() +"&"+ opponentsScore.get());
        if (opponentsScore.get() > 6) {
            opponentsPrintWriter.println(NetMethod.setGameResult.getCode() + "?" + opponentsScore.get() + "&" + clientsScore.get());
            clientsPrintWriter.println(NetMethod.setGameResult.getCode() + "?" + clientsScore.get() + "&" + opponentsScore.get());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                //get - code of method and args values
                String[] request = clientBufferedReader.readLine().split("\\?");
                NetMethod method = NetMethod.getMethod(request[0]);
                try {
                    Method executableMethod = Server
                            .class
                            .getMethod(
                                    method.name()
                                    ,
                                    method.getArgsClasses());

                    //try to parse arguments from string to Integer, Double and Long if they exists
                    Object[] args = null;
                    if (request.length > 1) {
                        args = new Object[request[1].split("\\&").length];
                        int i = 0;

                        for (String stringArg : request[1].split("\\&")) {
                            if (method.getArgsClasses()[i].equals(Integer.class)) {
                                args[i] = method.getArgsClasses()[i].getMethod("parseInt", String.class)
                                        .invoke(null, stringArg);
                            } else {
                                if (method.getArgsClasses()[i].equals(String.class)) {
                                    args[i] = stringArg;
                                } else {
                                    args[i] = method.getArgsClasses()[i]
                                            .getMethod("parse" + method.getArgsClasses()[i].getSimpleName(), String.class)
                                            .invoke(null, stringArg);
                                }
                            }
                            i++;
                        }

                    }
                    //execute this method
                    executableMethod.invoke(this, args);
                } catch (NullPointerException e){
                    e.printStackTrace();
                }


            }

        } catch (SocketException e) {
            System.out.println(e.getMessage());
            readyCounter--;
            opponentsPrintWriter.println(NetMethod.opponentLeftGame.getCode());
            opponentsPrintWriter.println(NetMethod.setGameResult.getCode() + "?" + opponentsScore.get() + "&" + (-clientsScore.get()));
        } catch (IOException e) {
            e.printStackTrace();

        } catch (NumberFormatException e) {
            e.printStackTrace();

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    private static Socket client1;
    private static Socket client2;
    private static Object client1Flag = new Object();
    private static Object client2Flag = new Object();
    private static ServerImpl operator1;
    private static ServerImpl operator2;
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            client1 = null;
            client2 = null;
            operator1 = null;
            operator2 = null;
            while (true) {
                if (operator1 == null || !operator1.isAlive() || operator2 == null || !operator2.isAlive()) {
                    if (operator1 == null || !operator1.isAlive()) {
                        client1 = serverSocket.accept();
                        operator1 = new ServerImpl(
                                new PrintWriter(client1.getOutputStream(),true),
                                client2Score,
                                client1Score,
                                new BufferedReader(new InputStreamReader(client1.getInputStream())),
                                client2Flag
                        );
                        synchronized (client1Flag) {
                            client1Flag.notifyAll();
                        }
                        if (operator2 == null || !operator2.isAlive()) {
                            new Thread(() -> {
                                try {
                                    synchronized (client2Flag) {
                                        client2Flag.wait();
                                        operator2.setOpponentsPrintWriter(new PrintWriter(client1.getOutputStream(),true));
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        } else {
                            operator2.setOpponentsPrintWriter(new PrintWriter(client1.getOutputStream(), true));
                        }
                        operator1.start();
                        resetCounters();
                    }
                    if ( operator2 == null || !operator2.isAlive()) {
                        client2 = serverSocket.accept();
                        operator2= new ServerImpl(
                                new PrintWriter(client2.getOutputStream(),true),
                                client1Score,
                                client2Score,
                                new BufferedReader(new InputStreamReader(client2.getInputStream())),
                                client1Flag
                        );
                        synchronized (client2Flag) {
                            client2Flag.notifyAll();
                        }
                        if (operator1 == null || !operator1.isAlive()) {
                            new Thread(() -> {
                                try {
                                    synchronized (client1Flag) {
                                        client1Flag.wait();
                                        operator1.setOpponentsPrintWriter(new PrintWriter(client2.getOutputStream(),true));
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        } else {
                            operator1.setOpponentsPrintWriter(new PrintWriter(client2.getOutputStream(),true));
                        }
                        operator2.start();
                        resetCounters();
                    }




                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void resetCounters() {
        client1Score.setZero();
        client2Score.setZero();

    }

    public void setOpponentsPrintWriter(PrintWriter opponentPrintWriter) {
        this.opponentsPrintWriter = opponentPrintWriter;
        synchronized (opponentFlag){
            opponentFlag.notifyAll();
        }
    }
}
