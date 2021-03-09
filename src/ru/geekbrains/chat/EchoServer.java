package ru.geekbrains.chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer extends Thread {
    public static void main(String[] args) {
        Socket socket = null;
        try (ServerSocket serverSocket = new ServerSocket(8081)) {
            System.out.println("Server is run, waiting for  connection...");
            socket = serverSocket.accept();
            System.out.println("User connected");
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            Socket finalSocket = socket;
            Thread clientMessage = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String str = null;
                        try {
                            str = in.readUTF();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (str.equals("/q")) {
                            try {
                                finalSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        } else {
                            try {
                                out.writeUTF("User: " + str);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println(str);
                        }
                    }
                }
            });

            Thread serverMessage = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        BufferedReader sm = new BufferedReader(new InputStreamReader(System.in));
                        String str = null;
                        try {
                            str = sm.readLine();
                            out.writeUTF("Server: " + str);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            clientMessage.start();
            serverMessage.start();

            try {
                clientMessage.join();
                serverMessage.join();
            } catch (InterruptedException e){
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
