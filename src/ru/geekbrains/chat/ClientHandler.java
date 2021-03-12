package ru.geekbrains.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private String name;
    private EchoServer echoServer;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public ClientHandler(EchoServer echoServer, Socket socket) {
        try {
            this.echoServer = echoServer;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.name = "";

            new Thread(() -> {
                autentication();
                readMessage();
            });
        } catch (IOException e) {
            System.out.println(e.getMessage());;
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(String message) {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
