package oop.mony.dao;

import oop.mony.models.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {
    public static Project createProject(int userId, String name) throws SQLException {
        return createProject(userId, name, 0.0);
    }

    public static Project createProject(int userId, String name, double allocatedAmount) throws SQLException {
        Project project = new Project(userId, name, allocatedAmount);
        if (!project.hasName()) {
            return null;
        }

        String sql = "INSERT INTO projects (user_id, name, allocated_amount) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setString(2, project.getProjectName());
            statement.setDouble(3, project.getAllocatedAmount());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Project(
                            keys.getInt(1),
                            userId,
                            project.getProjectName(),
                            project.getAllocatedAmount(),
                            0.0
                    );
                }
            }
        }

        return project;
    }

    public static List<Project> findProjectsForUser(int userId) throws SQLException {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT id, user_id, name, allocated_amount FROM projects WHERE user_id = ? ORDER BY id DESC";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    projects.add(new Project(
                            resultSet.getInt("id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("name"),
                            resultSet.getDouble("allocated_amount"),
                            0.0
                    ));
                }
            }
        }

        return projects;
    }

    public static void deleteProject(int projectId, int userId) throws SQLException {
        if (!projectBelongsToUser(projectId, userId)) {
            return;
        }

        PotDAO.deletePotsForProject(projectId);

        String sql = "DELETE FROM projects WHERE id = ? AND user_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, projectId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    public static double getProjectAllocatedAmount(int projectId) throws SQLException {
        String sql = "SELECT allocated_amount FROM projects WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, projectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0.0;
                }

                return resultSet.getDouble("allocated_amount");
            }
        }
    }

    public static void updateProject(int projectId, int userId, String name, double allocatedAmount) throws SQLException {
        if (!projectBelongsToUser(projectId, userId)) {
            return;
        }

        String sql = "UPDATE projects SET name = ?, allocated_amount = ? WHERE id = ? AND user_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setDouble(2, allocatedAmount);
            statement.setInt(3, projectId);
            statement.setInt(4, userId);
            statement.executeUpdate();
        }
    }

    private static boolean projectBelongsToUser(int projectId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM projects WHERE id = ? AND user_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, projectId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
