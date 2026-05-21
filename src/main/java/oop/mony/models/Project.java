package oop.mony.models;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private final int projectId;
    private final int userId;
    private final String projectName;
    private final double allocatedAmount;
    private final ArrayList<Pot> pots;

    public Project(String projectName) {
        this(0, 0, projectName, 0.0, 0.0);
    }

    public Project(int userId, String projectName) {
        this(0, userId, projectName, 0.0, 0.0);
    }

    public Project(int userId, String projectName, double allocatedAmount) {
        this(0, userId, projectName, allocatedAmount, 0.0);
    }

    public Project(int projectId, int userId, String projectName,
                   double allocatedAmount, double spentAmount) {
        this.projectId = projectId;
        this.userId = userId;
        this.projectName = projectName == null ? "" : projectName.trim();
        this.allocatedAmount = Math.max(0.0, allocatedAmount);
        this.pots = new ArrayList<>();
    }

    public int getProjectId() {
        return projectId;
    }

    public int getUserId() {
        return userId;
    }

    public String getProjectName() {
        return projectName;
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
        return !projectName.isEmpty();
    }

    public List<Pot> getPots() {
        return pots;
    }

    public void addPot(Pot pot) {
        if (pot != null) {
            pots.add(pot);
        }
    }

    public double getTotalAllocatedToPots() {
        double total = 0.0;
        for (Pot pot : pots) {
            total += pot.getAllocatedAmount();
        }
        return total;
    }

    public double getTotalSpent() {
        double total = 0.0;
        for (Pot pot : pots) {
            total += pot.getTotalSpent();
        }
        return total;
    }

    public boolean canAddPot(double amount) {
        return getTotalAllocatedToPots() + Math.max(0.0, amount) <= allocatedAmount;
    }

    public Pot findPotById(int potId) {
        for (Pot pot : pots) {
            if (pot.getPotId() == potId) {
                return pot;
            }
        }
        return null;
    }
}
