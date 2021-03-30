package ru.geekbrains.chat;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryLogger {
    private String filename;
    private static final int MAX_ROWS_COUNT = 100;

    public HistoryLogger(int userId) {
        assert(userId > 0);

        filename = userId + ".txt";
    }

    public List<String> getLastMessages() {
        List<String> messageList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String str;
            while ((str = reader.readLine()) != null && messageList.size() < MAX_ROWS_COUNT) {
                messageList.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messageList;
    }

    public void logMessage(String message) {
        if (!message.isEmpty()) {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(filename, true))) {
                out.append(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
