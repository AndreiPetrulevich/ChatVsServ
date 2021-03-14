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
                try {
                    authentication();
                    readMessage();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            System.out.println("Server problem");;
        }
    }

    private void authentication() throws IOException {
        while (true) {
            String authStr = dis.readUTF();
            if (authStr.startsWith("/auth")) {
                String [] arr = authStr.split("\\s");
                String nick = echoServer
                        .getAuthService()
                        .getNickByLoginAndPassword(arr[1], arr[2]);
                if (!nick.isEmpty()) {
                    if (!echoServer.isNickBusy(nick)) {
                        sendMessage("/authok " + nick);
                        name = nick;
                        echoServer.sendMessageToClients(nick + "Joined to chat");
                        echoServer.subscribe(this);
                        return;
                    } else {
                        sendMessage("This " + name + " is busy!");
                    }
                } else {
                    sendMessage("Wrong login/password");
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException ignored) {
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            String messageFromClient = dis.readUTF();
            if (messageFromClient.equals("/q")) {
                sendMessage(messageFromClient);
                break;
            }
            echoServer.sendMessageToClients(name + ": " + messageFromClient);

        }
    }

    private void closeConnection() {
        echoServer.subscribe(this);
        echoServer.sendMessageToClients(name + " leave the chat.");
    }

}
