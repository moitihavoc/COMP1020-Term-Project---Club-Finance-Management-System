package oop.mony.dao;

import oop.mony.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public static boolean createAccount(User user) throws SQLException {
        if (!hasUserInput(user.getUsername(), user.getPassword())) {
            return false;
        }

        if (isDuplicateUsername(user.getUsername())) {
            return false;
        }

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            return statement.executeUpdate() == 1;
        }
    }

    public static boolean login(String username, String password) throws SQLException {
        return findByUsernameAndPassword(username, password) != null;
    }

    public static boolean updatePassword(int userId, String newPassword) throws SQLException {
        if (userId <= 0 || newPassword == null || newPassword.isEmpty()) {
            return false;
        }

        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newPassword);
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public static User findByUsernameAndPassword(String username, String password) throws SQLException {
        if (!hasUserInput(username, password)) {
            return null;
        }

        String sql = "SELECT id, username, password FROM users WHERE username = ? AND password = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password")
                );
            }
        }
    }

    private static boolean isDuplicateUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static boolean hasUserInput(String username, String password) {
        return username != null && !username.trim().isEmpty()
                && password != null && !password.isEmpty();
    }
}
