package oop.mony.models;

public class Project {
    private final int projectId;
    private final int userId;
    private final String projectName;
    private final double allocatedAmount;
    private final double spentAmount;

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
        this.spentAmount = Math.max(0.0, spentAmount);
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
        return spentAmount;
    }

    public double getRemainingAmount() {
        return allocatedAmount - spentAmount;
    }

    public boolean hasName() {
        return !projectName.isEmpty();
    }
}
