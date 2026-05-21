package oop.mony.models;

import java.util.ArrayList;
import java.util.List;

public class Pot {
    private final int potId;
    private final int projectId;
    private final String potName;
    private final double allocatedAmount;
    private final ArrayList<Transaction> transactions;

    public Pot(String potName) {
        this(0, 0, potName, 0.0, 0.0);
    }

    public Pot(int projectId, String potName, double allocatedAmount) {
        this(0, projectId, potName, allocatedAmount, 0.0);
    }

    public Pot(int potId, int projectId, String potName,
               double allocatedAmount, double spentAmount) {
        this.potId = potId;
        this.projectId = projectId;
        this.potName = potName == null ? "" : potName.trim();
        this.allocatedAmount = Math.max(0.0, allocatedAmount);
        this.transactions = new ArrayList<>();
    }

    public int getPotId() {
        return potId;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getPotName() {
        return potName;
    }

    public double getAllocatedAmount() {
        return allocatedAmount;
    }

    public double getSpentAmount() {
        return getTotalSpent();
    }

    public double getRemainingAmount() {
        return allocatedAmount - getTotalSpent();
    }

    public boolean hasName() {
        return !potName.isEmpty();
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
        if (transaction != null) {
            transactions.add(transaction);
        }
    }

    public double getTotalSpent() {
        double total = 0.0;
        for (Transaction transaction : transactions) {
            total += transaction.getAmount();
        }
        return total;
    }

    public boolean canAddTransaction(double amount) {
        return getTotalSpent() + Math.max(0.0, amount) <= allocatedAmount;
    }
}
