package ru.geekbrains.chat;

import ru.geekbrains.chat.connection.SQLiteConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationServiceImpl implements AuthenticationService {
    static final String getNickQuery = "SELECT * FROM users WHERE login = ? AND password = ?";
    static final String changeNickQuery = "UPDATE users SET nick = ? WHERE id = ?";

    static Connection connection;

    static {
        try {
            connection = SQLiteConnection.getSQLiteConnection("chat.sqlite");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        System.out.println("Start");
    }

    @Override
    public void stop() {
        System.out.println("Stop");
    }

    @Override
    public User authenticate(String login, String password) {
        if (login == null || password == null) {
            return null;
        }
        login = login.trim();
        password = password.trim();

        try {
            PreparedStatement statement = connection.prepareStatement(getNickQuery);
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            User result = new User(
                resultSet.getInt("id"),
                resultSet.getString("login"),
                resultSet.getString("nick")
            );
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String changeNickName(int id, String newNick) {
        try {
            PreparedStatement statement = connection.prepareStatement(changeNickQuery);
            statement.setString(1, newNick);
            statement.setInt(2, id);
            if(statement.execute()) {
                return newNick;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
