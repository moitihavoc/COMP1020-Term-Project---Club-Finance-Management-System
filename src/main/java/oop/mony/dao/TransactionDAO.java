package oop.mony.dao;

import oop.mony.models.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private static final Path PROOF_FOLDER = Paths.get("app-data", "proofs");

    public static Transaction createTransaction(int potId, String name, double amount,
                                                String paidBy, LocalDate transactionDate,
                                                Path proofImage, String note)
            throws SQLException, IOException {
        Transaction transaction = new Transaction(potId, name, amount, paidBy, transactionDate, "", note);
        if (!transaction.hasName()) {
            return null;
        }

        String proofPath = saveProofImage(proofImage);
        transaction = new Transaction(potId, name, amount, paidBy, transactionDate, proofPath, note);

        String sql = "INSERT INTO transactions "
                + "(pot_id, name, amount, paid_by, transaction_date, proof_path, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, transaction.getPotId());
                statement.setString(2, transaction.getTransactionName());
                statement.setDouble(3, transaction.getAmount());
                statement.setString(4, transaction.getPaidBy());
                statement.setString(5, transaction.getTransactionDate().toString());
                statement.setString(6, transaction.getProofPath());
                statement.setString(7, transaction.getNote());
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new Transaction(
                                keys.getInt(1),
                                transaction.getPotId(),
                                transaction.getTransactionName(),
                                transaction.getAmount(),
                                transaction.getPaidBy(),
                                transaction.getTransactionDate(),
                                transaction.getProofPath(),
                                transaction.getNote()
                        );
                    }
                }
            }
        } catch (SQLException e) {
            deleteProofImage(proofPath);
            throw e;
        }

        deleteProofImage(proofPath);
        return transaction;
    }

    public static List<Transaction> findTransactionsForPot(int potId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT id, pot_id, name, amount, paid_by, transaction_date, proof_path, note "
                + "FROM transactions WHERE pot_id = ? ORDER BY transaction_date DESC, id DESC";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, potId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(new Transaction(
                            resultSet.getInt("id"),
                            resultSet.getInt("pot_id"),
                            resultSet.getString("name"),
                            resultSet.getDouble("amount"),
                            resultSet.getString("paid_by"),
                            LocalDate.parse(resultSet.getString("transaction_date")),
                            resultSet.getString("proof_path"),
                            resultSet.getString("note")
                    ));
                }
            }
        }

        return transactions;
    }

    public static void deleteTransaction(int transactionId, int potId) throws SQLException {
        List<String> proofPaths = findProofPathsForTransaction(transactionId, potId);
        String sql = "DELETE FROM transactions WHERE id = ? AND pot_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transactionId);
            statement.setInt(2, potId);
            statement.executeUpdate();
        }
        deleteProofImages(proofPaths);
    }

    public static void deleteTransactionsForPot(int potId) throws SQLException {
        List<String> proofPaths = findProofPathsForPot(potId);
        String sql = "DELETE FROM transactions WHERE pot_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, potId);
            statement.executeUpdate();
        }
        deleteProofImages(proofPaths);
    }

    private static String saveProofImage(Path proofImage) throws IOException {
        if (proofImage == null) {
            return "";
        }

        Files.createDirectories(PROOF_FOLDER);
        String originalFileName = proofImage.getFileName().toString();
        String savedFileName = System.currentTimeMillis() + "_" + originalFileName;
        Path savedPath = PROOF_FOLDER.resolve(savedFileName);
        Files.copy(proofImage, savedPath, StandardCopyOption.REPLACE_EXISTING);
        return savedPath.toString();
    }

    private static List<String> findProofPathsForTransaction(int transactionId, int potId) throws SQLException {
        List<String> proofPaths = new ArrayList<>();
        String sql = "SELECT proof_path FROM transactions WHERE id = ? AND pot_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transactionId);
            statement.setInt(2, potId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    proofPaths.add(resultSet.getString("proof_path"));
                }
            }
        }
        return proofPaths;
    }

    private static List<String> findProofPathsForPot(int potId) throws SQLException {
        List<String> proofPaths = new ArrayList<>();
        String sql = "SELECT proof_path FROM transactions WHERE pot_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, potId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    proofPaths.add(resultSet.getString("proof_path"));
                }
            }
        }
        return proofPaths;
    }

    private static void deleteProofImages(List<String> proofPaths) {
        for (String proofPath : proofPaths) {
            deleteProofImage(proofPath);
        }
    }

    private static void deleteProofImage(String proofPath) {
        if (proofPath == null || proofPath.trim().isEmpty()) {
            return;
        }

        try {
            Files.deleteIfExists(Paths.get(proofPath));
        } catch (IOException e) {
            System.err.println("Could not delete proof image: " + proofPath);
        }
    }
}
