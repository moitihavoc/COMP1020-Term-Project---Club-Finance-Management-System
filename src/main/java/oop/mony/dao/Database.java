package oop.mony.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Database {
    private static final Path APP_DATA_FOLDER = Paths.get("app-data");
    private static final String DATABASE_URL = "jdbc:sqlite:app-data/mony.db";

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        createAppDataFolder();
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        initialize(connection);
        return connection;
    }

    private static void createAppDataFolder() throws SQLException {
        try {
            Files.createDirectories(APP_DATA_FOLDER);
        } catch (java.io.IOException e) {
            throw new SQLException("Could not create app data folder.", e);
        }
    }

    private static void initialize(Connection connection) throws SQLException {
        String usersSql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ")";

        String clubsSql = "CREATE TABLE IF NOT EXISTS clubs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL UNIQUE,"
                + "name TEXT NOT NULL,"
                + "total_balance REAL NOT NULL DEFAULT 0,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(user_id) REFERENCES users(id)"
                + ")";

        String projectsSql = "CREATE TABLE IF NOT EXISTS projects ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "name TEXT NOT NULL,"
                + "allocated_amount REAL NOT NULL DEFAULT 0,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(user_id) REFERENCES users(id)"
                + ")";

        String potsSql = "CREATE TABLE IF NOT EXISTS pots ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "project_id INTEGER NOT NULL,"
                + "name TEXT NOT NULL,"
                + "allocated_amount REAL NOT NULL DEFAULT 0,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(project_id) REFERENCES projects(id)"
                + ")";

        String transactionsSql = "CREATE TABLE IF NOT EXISTS transactions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "pot_id INTEGER NOT NULL,"
                + "name TEXT NOT NULL,"
                + "amount REAL NOT NULL,"
                + "paid_by TEXT NOT NULL,"
                + "transaction_date TEXT NOT NULL,"
                + "proof_path TEXT,"
                + "note TEXT,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(pot_id) REFERENCES pots(id)"
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.execute(usersSql);
            statement.execute(clubsSql);
            statement.execute(projectsSql);
            statement.execute(potsSql);
            statement.execute(transactionsSql);
        }

        addColumnIfMissing(connection, "projects", "allocated_amount",
                "allocated_amount REAL NOT NULL DEFAULT 0");
    }

    private static void addColumnIfMissing(Connection connection, String tableName,
                                           String columnName, String columnDefinition) throws SQLException {
        if (hasColumn(connection, tableName, columnName)) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnDefinition);
        }
    }

    private static boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (resultSet.next()) {
                if (columnName.equals(resultSet.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }
}
