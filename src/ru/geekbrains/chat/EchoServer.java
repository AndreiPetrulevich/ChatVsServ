package ru.geekbrains.chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class EchoServer extends Thread {
    private Socket socket;
    private ServerSocket serverSocket;
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
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            clientsList = new ArrayList<>();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
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

    public void listen() {
        System.out.println("Server is run, waiting for  connection...");
        try {
            socket = serverSocket.accept();
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("User connected");

        Thread clientMessage = new Thread(this::clientMessagesRoutine);
        Thread serverMessage = new Thread(this::serverMessageRoutine);

        clientMessage.start();
        serverMessage.start();

        try {
            clientMessage.join();
            serverMessage.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void clientMessagesRoutine() {
        while (true) {
            String str = "";
            try {
                str = in.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (str.equals("/q")) {
                try {
                    System.out.println("Quit command received. Closing sockets");
                    socket.close();
                    serverSocket.close();
                    synchronized(this) {
                        shouldShutdown = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            } else {
                writeOut("User: " + str);
                System.out.println(str);
            }
        }
    }

    private void serverMessageRoutine() {
        while(true) {
            synchronized(this) {
                if (shouldShutdown) {
                    System.out.println("Terminating server message routine");
                    break;
                }
            }
            BufferedReader sm = new BufferedReader(new InputStreamReader(System.in));
            String str;
            try {
                str = sm.readLine();
                synchronized(this) {
                    if (shouldShutdown) {
                        System.out.println("Terminating server message routine");
                        break;
                    }
                }
                writeOut("Server: " + str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void writeOut(String text) {
        try {
            out.writeUTF(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EchoServer server = new EchoServer();
        server.listen();
    }
}
