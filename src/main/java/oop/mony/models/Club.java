package oop.mony.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Club {
    private final int clubId;
    private final int userId;
    private final String clubName;
    private final double totalBalance;
    private final ArrayList<Project> projects;

    public Club(String clubName) {
        this(0, 0, clubName, 0.0);
    }

    public Club(int userId, String clubName, double totalBalance) {
        this(0, userId, clubName, totalBalance);
    }

    public Club(int clubId, int userId, String clubName, double totalBalance) {
        this.clubId = clubId;
        this.userId = userId;
        this.clubName = clubName == null ? "" : clubName.trim();
        this.totalBalance = Math.max(0.0, totalBalance);
        this.projects = new ArrayList<>();
    }

    public int getClubId() {
        return clubId;
    }

    public int getUserId() {
        return userId;
    }

    public String getClubName() {
        return clubName;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public double getTotalAllocated() {
        double total = 0.0;
        for (Project project : projects) {
            total += project.getAllocatedAmount();
        }
        return total;
    }

    public double getTotalSpent() {
        double total = 0.0;
        for (Project project : projects) {
            total += project.getTotalSpent();
        }
        return total;
    }

    public double getTotalRemaining() {
        return totalBalance - getTotalSpent();
    }

    public boolean hasName() {
        return !clubName.isEmpty();
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void addProject(Project project) {
        if (project != null) {
            projects.add(project);
        }
    }

    public boolean canAddProject(double amount) {
        return getTotalAllocated() + Math.max(0.0, amount) <= totalBalance;
    }

    public Project findProjectById(int projectId) {
        for (Project project : projects) {
            if (project.getProjectId() == projectId) {
                return project;
            }
        }
        return null;
    }

    public ArrayList<Transaction> searchTransactions(
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Double minAmount,
            Double maxAmount,
            Integer projectId) {

        ArrayList<Transaction> results = new ArrayList<>();
        for (Project project : projects) {
            for (Pot pot : project.getPots()) {
                for (Transaction transaction : pot.getTransactions()) {
                    Transaction record = new Transaction(
                            transaction.getTransactionId(),
                            project.getProjectId(),
                            pot.getPotId(),
                            transaction.getTransactionName(),
                            project.getProjectName(),
                            pot.getPotName(),
                            transaction.getAmount(),
                            transaction.getPaidBy(),
                            transaction.getTransactionDate(),
                            transaction.getProofPath(),
                            transaction.getNote()
                    );

                    if (record.matchesKeyword(keyword)
                            && record.isAfterOrOn(startDate)
                            && record.isBeforeOrOn(endDate)
                            && record.isAtLeast(minAmount)
                            && record.isAtMost(maxAmount)
                            && record.belongsToProject(projectId)) {
                        results.add(record);
                    }
                }
            }
        }
        return results;
    }
}
