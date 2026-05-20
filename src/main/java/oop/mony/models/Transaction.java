package oop.mony.models;

import java.time.LocalDate;

public class Transaction {
    private final int transactionId;
    private final int potId;
    private final String transactionName;
    private final double amount;
    private final String paidBy;
    private final LocalDate transactionDate;
    private final String proofPath;
    private final String note;

    public Transaction(int potId, String transactionName, double amount,
                       String paidBy, LocalDate transactionDate,
                       String proofPath, String note) {
        this(0, potId, transactionName, amount, paidBy, transactionDate, proofPath, note);
    }

    public Transaction(int transactionId, int potId, String transactionName, double amount,
                       String paidBy, LocalDate transactionDate,
                       String proofPath, String note) {
        this.transactionId = transactionId;
        this.potId = potId;
        this.transactionName = transactionName == null ? "" : transactionName.trim();
        this.amount = Math.max(0.0, amount);
        this.paidBy = paidBy == null ? "" : paidBy.trim();
        this.transactionDate = transactionDate == null ? LocalDate.now() : transactionDate;
        this.proofPath = proofPath == null ? "" : proofPath.trim();
        this.note = note == null ? "" : note.trim();
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getPotId() {
        return potId;
    }

    public String getTransactionName() {
        return transactionName;
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

    public boolean hasName() {
        return !transactionName.isEmpty();
    }
}
