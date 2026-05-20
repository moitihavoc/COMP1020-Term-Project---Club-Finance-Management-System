package oop.mony.models;

public class Club {
    private final int clubId;
    private final int userId;
    private final String clubName;
    private final double totalBalance;
    private final double totalSpent;

    public Club(String clubName) {
        this(0, 0, clubName, 0.0, 0.0);
    }

    public Club(int userId, String clubName, double totalBalance) {
        this(0, userId, clubName, totalBalance, 0.0);
    }

    public Club(int clubId, int userId, String clubName, double totalBalance, double totalSpent) {
        this.clubId = clubId;
        this.userId = userId;
        this.clubName = clubName == null ? "" : clubName.trim();
        this.totalBalance = Math.max(0.0, totalBalance);
        this.totalSpent = Math.max(0.0, totalSpent);
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

    public double getTotalSpent() {
        return totalSpent;
    }

    public double getTotalRemaining() {
        return totalBalance - totalSpent;
    }

    public boolean hasName() {
        return !clubName.isEmpty();
    }
}
