package oop.mony.models;

import java.time.LocalDate;

public class TransactionRecord {
    private final int transactionId;
    private final int projectId;
    private final int potId;
    private final String transactionName;
    private final String projectName;
    private final String potName;
    private final double amount;
    private final String paidBy;
    private final LocalDate transactionDate;
    private final String proofPath;
    private final String note;

    public TransactionRecord(int transactionId, int projectId, int potId,
                             String transactionName, String projectName, String potName,
                             double amount, String paidBy, LocalDate transactionDate,
                             String proofPath, String note) {
        this.transactionId = transactionId;
        this.projectId = projectId;
        this.potId = potId;
        this.transactionName = transactionName == null ? "" : transactionName.trim();
        this.projectName = projectName == null ? "" : projectName.trim();
        this.potName = potName == null ? "" : potName.trim();
        this.amount = Math.max(0.0, amount);
        this.paidBy = paidBy == null ? "" : paidBy.trim();
        this.transactionDate = transactionDate == null ? LocalDate.now() : transactionDate;
        this.proofPath = proofPath == null ? "" : proofPath.trim();
        this.note = note == null ? "" : note.trim();
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getProjectId() {
        return projectId;
    }

    public int getPotId() {
        return potId;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getPotName() {
        return potName;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public String getProofPath() {
        return proofPath;
    }

    public String getNote() {
        return note;
    }

    public String getShortNote() {
        return getShortNote(30);
    }

    public String getShortNote(int maxLength) {
        if (note.length() <= maxLength) {
            return note;
        }

        return note.substring(0, maxLength) + "...";
    }
}
