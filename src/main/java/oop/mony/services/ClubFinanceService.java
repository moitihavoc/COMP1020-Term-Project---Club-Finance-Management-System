package oop.mony.services;

import oop.mony.dao.ClubDAO;
import oop.mony.dao.PotDAO;
import oop.mony.dao.ProjectDAO;
import oop.mony.dao.TransactionDAO;
import oop.mony.models.Club;
import oop.mony.models.Pot;
import oop.mony.models.Project;
import oop.mony.models.Transaction;
import oop.mony.models.TransactionRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ClubFinanceService {
    private ClubFinanceService() {
    }

    public static Club loadFullClubForUser(int userId, String username) throws SQLException {
        Club club = ClubDAO.getOrCreateClubForUser(userId, username);
        List<Project> projects = ProjectDAO.findProjectsForUser(userId);

        for (Project project : projects) {
            List<Pot> pots = PotDAO.findPotsForProject(project.getProjectId());
            for (Pot pot : pots) {
                List<Transaction> transactions = TransactionDAO.findTransactionsForPot(pot.getPotId());
                for (Transaction transaction : transactions) {
                    pot.addTransaction(transaction);
                }
                project.addPot(pot);
            }
            club.addProject(project);
        }

        return club;
    }

    public static Club createProject(Club club, String name, double allocatedAmount)
            throws SQLException {
        if (club == null || !club.canAddProject(allocatedAmount)) {
            return club;
        }

        ProjectDAO.createProject(club.getUserId(), name, allocatedAmount);
        return loadFullClubForUser(club.getUserId(), club.getClubName());
    }

    public static Club createPot(Club club, int projectId, String name, double allocatedAmount)
            throws SQLException {
        if (club == null) {
            return null;
        }

        Project project = club.findProjectById(projectId);
        if (project == null || !project.canAddPot(allocatedAmount)) {
            return club;
        }

        PotDAO.createPot(projectId, name, allocatedAmount);
        return loadFullClubForUser(club.getUserId(), club.getClubName());
    }

    public static Club createTransaction(Club club, int potId, String name, double amount,
                                         String paidBy, LocalDate date, Path proofImage, String note)
            throws SQLException, IOException {
        if (club == null) {
            return null;
        }

        Pot pot = findPotById(club, potId);
        if (pot == null) {
            return club;
        }

        TransactionDAO.createTransaction(potId, name, amount, paidBy, date, proofImage, note);
        return loadFullClubForUser(club.getUserId(), club.getClubName());
    }

    public static Club deleteProject(Club club, int projectId) throws SQLException {
        if (club == null) {
            return null;
        }

        ProjectDAO.deleteProject(projectId, club.getUserId());
        return loadFullClubForUser(club.getUserId(), club.getClubName());
    }

    public static Club deletePot(Club club, int potId) throws SQLException {
        if (club == null) {
            return null;
        }

        Project project = findProjectForPot(club, potId);
        if (project == null) {
            return club;
        }

        PotDAO.deletePot(potId, project.getProjectId());
        return loadFullClubForUser(club.getUserId(), club.getClubName());
    }

    public static Club deleteTransaction(Club club, int transactionId, int potId) throws SQLException {
        if (club == null) {
            return null;
        }

        TransactionDAO.deleteTransaction(transactionId, potId);
        return loadFullClubForUser(club.getUserId(), club.getClubName());
    }

    public static ArrayList<TransactionRecord> searchTransactions(
            Club club,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Double minAmount,
            Double maxAmount,
            Integer projectId) {
        if (club == null) {
            return new ArrayList<>();
        }

        return club.searchTransactions(keyword, startDate, endDate, minAmount, maxAmount, projectId);
    }

    public static boolean updateTotalBalance(Club club, double totalBalance) throws SQLException {
        if (club == null || totalBalance < club.getTotalAllocated()) {
            return false;
        }

        return ClubDAO.updateTotalBalance(club.getUserId(), totalBalance);
    }

    private static Pot findPotById(Club club, int potId) {
        for (Project project : club.getProjects()) {
            Pot pot = project.findPotById(potId);
            if (pot != null) {
                return pot;
            }
        }
        return null;
    }

    private static Project findProjectForPot(Club club, int potId) {
        for (Project project : club.getProjects()) {
            if (project.findPotById(potId) != null) {
                return project;
            }
        }
        return null;
    }
}
