package oop.mony.models;

public class Pot {
    private final int potId;
    private final int projectId;
    private final String potName;
    private final double allocatedAmount;
    private final double spentAmount;

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
        this.spentAmount = Math.max(0.0, spentAmount);
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
        return spentAmount;
    }

    public double getRemainingAmount() {
        return allocatedAmount - spentAmount;
    }

    public boolean hasName() {
        return !potName.isEmpty();
    }
}
