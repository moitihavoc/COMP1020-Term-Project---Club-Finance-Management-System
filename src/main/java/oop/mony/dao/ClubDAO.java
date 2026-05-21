package oop.mony.dao;

import oop.mony.models.Club;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClubDAO {
    public static Club getOrCreateClubForUser(int userId, String username) throws SQLException {
        Club club = findByUserId(userId);
        if (club != null) {
            return club;
        }

        String clubName = username == null || username.trim().isEmpty()
                ? "My Club"
                : username.trim() + "'s Club";

        String sql = "INSERT INTO clubs (user_id, name, total_balance) VALUES (?, ?, 0)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, clubName);
            statement.executeUpdate();
        }

        return findByUserId(userId);
    }

    public static boolean updateTotalBalance(int userId, double totalBalance) throws SQLException {
        double safeTotalBalance = Math.max(0.0, totalBalance);
        String sql = "UPDATE clubs SET total_balance = ? WHERE user_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, safeTotalBalance);
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public static double getTotalBalanceForUser(int userId) throws SQLException {
        String sql = "SELECT total_balance FROM clubs WHERE user_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0.0;
                }

                return resultSet.getDouble("total_balance");
            }
        }
    }

    public static Club findByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, name, total_balance FROM clubs WHERE user_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return new Club(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("total_balance")
                );
            }
        }
    }
}
