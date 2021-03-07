package ru.geekbrains.chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
    public static void main(String[] args) {
        Socket socket = null;
        try (ServerSocket serverSocket = new ServerSocket(8081)) {
            System.out.println("Server is run, waiting for  connection...");
            socket = serverSocket.accept();
            System.out.println("User connected");
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String str = in.readUTF();
                if (str.equals("/q")) {
                    socket.close();
                    break;
                } else {
                    out.writeUTF("User: " + str);
                    System.out.println(str);
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
