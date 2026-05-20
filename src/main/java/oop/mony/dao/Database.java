package oop.mony.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String DATABASE_URL = "jdbc:sqlite:data/mony.db";

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        initialize(connection);
        return connection;
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
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(user_id) REFERENCES users(id)"
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.execute(usersSql);
            statement.execute(clubsSql);
            statement.execute(projectsSql);
        }
    }
}
