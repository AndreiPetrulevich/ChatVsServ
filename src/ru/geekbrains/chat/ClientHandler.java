package ru.geekbrains.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    public enum ConnectionType {
        ANONYMOUS,
        AUTHENTICATED
    }
    private String name;
    private ConnectionType connectionType;
    private EchoServer echoServer;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private long connectionTimestamp;

    public ClientHandler(EchoServer echoServer, Socket socket) {
        try {
            this.echoServer = echoServer;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            this.connectionType = ConnectionType.ANONYMOUS;
            this.connectionTimestamp = System.currentTimeMillis();

            new Thread(() -> {
                try {
                    while (System.currentTimeMillis() - this.connectionTimestamp < 2 * 60 * 1000) {
                        if (this.connectionType != ConnectionType.ANONYMOUS) {
                            break;
                        }
                        Thread.sleep(1000);
                    }
                    if (this.connectionType == ConnectionType.ANONYMOUS) {
                        System.out.println("Dropping anonumous user due to authentication timeout");
                        this.closeConnection();
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }).start();

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

            this.echoServer.subscribe(this);
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
                if (nick != null && !nick.isEmpty()) {
                    if (!echoServer.isNickBusy(nick)) {
                        sendMessage("/authok " + nick);
                        name = nick;
                        connectionType = ConnectionType.AUTHENTICATED;
                        echoServer.sendMessageToClients(nick + " Joined to chat");
                        echoServer.subscribe(this);
                        return;
                    } else {
                        sendMessage("This " + nick + " is busy!");
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
            if (messageFromClient.startsWith("/")) {
                if (messageFromClient.startsWith("/w")) {
                    String [] array = messageFromClient.trim().split("\\s",3);
                    echoServer.sendPrivateMessage(this, array[1], array[2]);
                    continue;
                }
                if (messageFromClient.startsWith("/ou")) {
                    echoServer.showOnlineClientsList(this);
                    continue;
                }

                if (messageFromClient.equals("/q")) {
                    sendMessage(messageFromClient);
                    break;
                }
            }
            echoServer.sendMessageToClients(name + ": " + messageFromClient);
        }
    }

    private void closeConnection() {
        echoServer.unsubscribe(this);
        if (connectionType != ConnectionType.ANONYMOUS) {
            echoServer.sendMessageToClients(name + " leave the chat.");
        }
        connectionType = ConnectionType.ANONYMOUS;
        try {
            socket.close();
        } catch (IOException ignored) {
            System.out.println("Unable to close client handler socket");;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
