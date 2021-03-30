package ru.geekbrains.chat;

public interface AuthenticationService {
    void start();
    void stop();
    User authenticate(String login, String password);
    String changeNickName(int id, String newNick);
}
