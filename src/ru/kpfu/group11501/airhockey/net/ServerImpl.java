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
    private final static int PORT = 3456;
    private static Boolean client1IsReady = false;
    private static Boolean client2IsReady = false;
    private static Integer client1Score = 0;
    private static Integer client2Score = 0;

    private Boolean clientIsReady;
    private PrintWriter clientsPrintWriter;
    private PrintWriter opponentsPrintWriter;
    private Integer opponentsScore;
    private Integer clientsScore;
    private BufferedReader clientBufferedReader;

    public ServerImpl(PrintWriter clientsPrintWriter, PrintWriter opponentsPrintWriter,
                      Integer opponentsScore, Integer clientsScore, BufferedReader clientBufferedReader,
                      Boolean clientIsReady) {
        this.clientsPrintWriter = clientsPrintWriter;
        this.opponentsPrintWriter = opponentsPrintWriter;
        this.opponentsScore = opponentsScore;
        this.clientsScore = clientsScore;
        this.clientBufferedReader = clientBufferedReader;
        this.clientIsReady = clientIsReady;
    }

    @Override
    public void updatePuckDirection(Double clientPuckX, Double clientPuckY) {
        opponentsPrintWriter.println(NetMethod.updatePuckDirection.getCode()
                + "?"+clientPuckX.toString().substring(0, 7)+"&"+clientPuckY.toString().substring(0, 7));
    }

    @Override
    public void updateClientMalletDirection(Double clientMalletX, Double clientMalletY) {
        opponentsPrintWriter.println(NetMethod.updateOpponentMalletDirection.getCode()
                + "?"+clientMalletX.toString().substring(0, 7)+"&"+clientMalletY.toString().substring(0, 7));
    }

    @Override
    public void clientIsReady() {
        clientIsReady = true;
        opponentsPrintWriter.println(NetMethod.opponentIsReady.getCode());
        if (client1IsReady && client2IsReady){
            //game starts in 5 seconds after this moment
            long startTime = Instant.now().toEpochMilli() + 1000 * 5;
            opponentsPrintWriter.println(NetMethod.gameStartsInTime.getCode()+"?"+startTime);
        }
    }

    @Override
    public void clientAsksGame() {
        //?
    }

    @Override
    public void clientLeavesGame() throws SocketException {
        clientsPrintWriter.println(NetMethod.setGameResult.getCode() + "?" + (-clientsScore) + "&" + opponentsScore);
        throw new SocketException("Client leave game");
    }

    @Override
    public void clientLoseRound() {
        opponentsScore++;
        opponentsPrintWriter.println(NetMethod.updateGameScore.getCode()+"?"+opponentsScore+"&"+ clientsScore);
        clientsPrintWriter.println(NetMethod.updateGameScore.getCode()+"?"+ clientsScore +"&"+ opponentsScore);
    }

    @Override
    public void run() {
        try {
            while (true) {
                //get - code of method and args values
                String[] request = clientBufferedReader.readLine().split("\\?");
                NetMethod method = NetMethod.getMethod(request[0]);
                Method executableMethod = Server.class.getMethod(method.name(), method.getArgsClasses());
                //try to parse arguments from string to Integer, Double and Long if they exists
                Object[] args = null;
                if (request.length > 1) {
                    args = new Object[request[1].split("&").length];
                    int i = 0;

                    for (String stringArg : request[1].split("&")) {
                        if (method.getArgsClasses()[i].equals(Integer.class)) {
                            args[i] = method.getArgsClasses()[i].getMethod("parseInt", String.class)
                                    .invoke(null, stringArg);
                        } else {
                            if (method.getArgsClasses()[i].equals(String.class)) {
                                args[i] = stringArg;
                            } else {
                                args[i] = method.getArgsClasses()[i]
                                        .getMethod("parse" + method.getArgsClasses()[i].getName(), String.class)
                                        .invoke(null, stringArg);
                            }
                        }
                    }

                }
                //execute this method
                executableMethod.invoke(this, args);

            }

        } catch (SocketException e) {
            System.out.println(e.getMessage());
            opponentsPrintWriter.println(NetMethod.opponentLeftGame.getCode());
            opponentsPrintWriter.println(NetMethod.setGameResult.getCode() + "?" + opponentsScore + "&" + (-clientsScore));
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

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            Socket client1 = null;
            Socket client2 = null;
            ServerImpl operator1 = null;
            ServerImpl operator2 = null;
            while (true) {
                if (operator1 == null || !operator1.isAlive() || operator2 == null || !operator2.isAlive()) {
                    if (operator1 == null || !operator1.isAlive()) {
                        client1 = serverSocket.accept();
                        resetCounters();
                    }
                    if ( operator2 == null || !operator2.isAlive()) {
                        client2 = serverSocket.accept();
                        resetCounters();
                    }
                    operator1 = new ServerImpl(
                            new PrintWriter(client1.getOutputStream(),true),
                            new PrintWriter(client2.getOutputStream(),true),
                            client1Score,
                            client2Score,
                            new BufferedReader(new InputStreamReader(client1.getInputStream())),
                            client1IsReady
                    );
                    operator2= new ServerImpl(
                            new PrintWriter(client2.getOutputStream(),true),
                            new PrintWriter(client1.getOutputStream(),true),
                            client1Score,
                            client2Score,
                            new BufferedReader(new InputStreamReader(client2.getInputStream())),
                            client2IsReady
                    );
                    operator2.start();
                    operator1.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void resetCounters() {
        client1Score = 0;
        client2Score = 0;
        client2IsReady = false;
        client1IsReady = false;
    }

}
