package oop.mony.models;

import oop.mony.dao.UserDAO;

import java.sql.SQLException;

public class User {
    private final int userId;
    private final String username;
    private final String password;

    public User(String username, String password) {
        this(0, username, password);
    }

    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username == null ? "" : username.trim();
        this.password = password == null ? "" : password;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean createAccount() {
        try {
            return UserDAO.createAccount(this);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create user account.", e);
        }
    }
}
