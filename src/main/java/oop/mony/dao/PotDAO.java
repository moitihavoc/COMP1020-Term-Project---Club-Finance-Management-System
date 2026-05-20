package oop.mony.dao;

import oop.mony.models.Pot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PotDAO {
    public static Pot createPot(int projectId, String name, double allocatedAmount) throws SQLException {
        Pot pot = new Pot(projectId, name, allocatedAmount);
        if (!pot.hasName()) {
            return null;
        }

        if (!canAllocatePotAmount(projectId, pot.getAllocatedAmount())) {
            return null;
        }

        String sql = "INSERT INTO pots (project_id, name, allocated_amount) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, projectId);
            statement.setString(2, pot.getPotName());
            statement.setDouble(3, pot.getAllocatedAmount());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Pot(keys.getInt(1), projectId, pot.getPotName(), pot.getAllocatedAmount(), 0.0);
                }
            }
        }

        return pot;
    }

    public static List<Pot> findPotsForProject(int projectId) throws SQLException {
        List<Pot> pots = new ArrayList<>();
        String sql = "SELECT id, project_id, name, allocated_amount FROM pots WHERE project_id = ? ORDER BY id DESC";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, projectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    pots.add(new Pot(
                            resultSet.getInt("id"),
                            resultSet.getInt("project_id"),
                            resultSet.getString("name"),
                            resultSet.getDouble("allocated_amount"),
                            TransactionDAO.getTotalSpentForPot(resultSet.getInt("id"))
                    ));
                }
            }
        }

        return pots;
    }

    public static void deletePot(int potId, int projectId) throws SQLException {
        if (!potBelongsToProject(potId, projectId)) {
            return;
        }

        TransactionDAO.deleteTransactionsForPot(potId);

        String sql = "DELETE FROM pots WHERE id = ? AND project_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, potId);
            statement.setInt(2, projectId);
            statement.executeUpdate();
        }
    }

    public static void deletePotsForProject(int projectId) throws SQLException {
        List<Pot> pots = findPotsForProject(projectId);
        for (Pot pot : pots) {
            deletePot(pot.getPotId(), projectId);
        }
    }

    public static double getTotalAllocatedForProject(int projectId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(allocated_amount), 0) AS total_allocated FROM pots WHERE project_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, projectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.getDouble("total_allocated");
            }
        }
    }

    private static boolean canAllocatePotAmount(int projectId, double newAllocatedAmount) throws SQLException {
        double projectAllocatedAmount = ProjectDAO.getProjectAllocatedAmount(projectId);
        double currentPotAllocatedAmount = getTotalAllocatedForProject(projectId);
        return currentPotAllocatedAmount + newAllocatedAmount <= projectAllocatedAmount;
    }

    private static boolean potBelongsToProject(int potId, int projectId) throws SQLException {
        String sql = "SELECT 1 FROM pots WHERE id = ? AND project_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, potId);
            statement.setInt(2, projectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
