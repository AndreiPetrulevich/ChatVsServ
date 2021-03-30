package ru.geekbrains.chat;

public class User {
    private Integer id;
    private String login;
    private String nick;

    public User(Integer id, String login, String nick) {
        this.id = id;
        this.login = login;
        this.nick = nick;
    }

    public String getNick() {
        return this.nick;
    }

    public int getId() {
        return this.id;
    }
}
