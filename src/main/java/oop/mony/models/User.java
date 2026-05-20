package oop.mony.models;

import oop.mony.dao.UserDAO;

import java.sql.SQLException;

public class User {
    private final int id;
    private final String username;
    private final String password;

    public User(String username, String password) {
        this(0, username, password);
    }

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username == null ? "" : username.trim();
        this.password = password == null ? "" : password;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean login() {
        try {
            return UserDAO.login(username, password);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not log in user.", e);
        }
    }

    public boolean createAccount() {
        try {
            return UserDAO.createAccount(this);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create user account.", e);
        }
    }

    public static boolean login(String username, String password) {
        return new User(username, password).login();
    }

    public static boolean createAccount(String username, String password) {
        return new User(username, password).createAccount();
    }
}
