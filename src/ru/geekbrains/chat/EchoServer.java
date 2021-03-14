package ru.geekbrains.chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class EchoServer extends Thread {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean shouldShutdown;

    private final int PORT = 8081;

    private List<ClientHandler> clientsList;
    private AuthenticationService authService;

    public AuthenticationService getAuthService() {
        return this.authService;
    }


    public EchoServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            this.authService = new AuthenticationServiceImpl();
            authService.start();

            while (true) {
                socket = serverSocket.accept();
                new ClientHandler(this, socket);
                clientsList = new ArrayList<>();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public static void main(String[] args) {
        new EchoServer();
    }

    public synchronized void sendMessageToClients(String message) {
        for (ClientHandler c : clientsList) {
            c.sendMessage(message);
        }
    }

    public synchronized void subscribe(ClientHandler c) {
        clientsList.add(c);
    }

    public synchronized void unSubscribe(ClientHandler c) {
        clientsList.remove(c);
    }

    public synchronized boolean isNickBusy (String nick) {
        return clientsList.stream().anyMatch(a -> a.getName().equals(nick));
    }

    public synchronized void sendPrivateMessage(ClientHandler clientHandler, String toNick, String message) {
        for (ClientHandler u : clientsList) {
            if (u.getName().equalsIgnoreCase(toNick)) {
                u.sendMessage(clientHandler.getName() + " send private message to: " + message);
                clientHandler.sendMessage("You send to " + toNick + " private message: " + message);
                return;
            }
        }
        clientHandler.sendMessage(toNick + ": offline.");
    }

    public synchronized void showOnlineClientsList(ClientHandler clientHandler) {
        clientHandler.sendMessage("Online: " + clientsList.toString());
    }
}
