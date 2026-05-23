package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oop.mony.Session;
import oop.mony.models.Club;
import oop.mony.models.TransactionRecord;
import oop.mony.models.User;
import oop.mony.services.ClubFinanceService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class TransactionPageController {

    @FXML private Label sidebarUsername;
    @FXML private Label logoutButton;
    @FXML private Label usernameLabel;
    @FXML private TextField searchField;
    @FXML private Label projectNameLabel;
    @FXML private Button recordTransactionButton;
    @FXML private ComboBox<String> projectFilterComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField minAmountField;
    @FXML private TextField maxAmountField;
    @FXML private Button clearFiltersButton;
    @FXML private Button allTabButton;
    @FXML private Button incomeTabButton;
    @FXML private Button expenseTabButton;
    @FXML private VBox transactionsTableBody;

    private User currentUser;
    private Club club;

    public void loadFromSession() {
        if (!Session.hasCurrentUser()) {
            navigateToLogin();
            return;
        }

        currentUser = Session.getCurrentUser();
        try {
            club = ClubFinanceService.loadFullClubForUser(currentUser.getUserId(), currentUser.getUsername());
            usernameLabel.setText(currentUser.getUsername());
            sidebarUsername.setText(currentUser.getUsername());
            refreshProjectFilter();
            refreshPage();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshProjectFilter() {
        projectFilterComboBox.getItems().clear();
        projectFilterComboBox.getItems().add("All Projects");
        club.getProjects().forEach(p -> projectFilterComboBox.getItems().add(p.getProjectId() + "|" + p.getProjectName()));
        projectFilterComboBox.getSelectionModel().selectFirst();
    }

    private void refreshPage() {
        ArrayList<TransactionRecord> results = ClubFinanceService.searchTransactions(
                club,
                searchField.getText(),
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                parseOptionalAmount(minAmountField),
                parseOptionalAmount(maxAmountField),
                selectedProjectFilterId()
        );

        renderTransactions(results);
    }

    private void renderTransactions(ArrayList<TransactionRecord> records) {
        transactionsTableBody.getChildren().clear();
        for (TransactionRecord r : records) {
            GridPane row = new GridPane();
            row.setHgap(12);
            row.add(new Label(r.getTransactionDate().toString()), 0, 0);
            row.add(new Label(r.getTransactionName()), 1, 0);
            row.add(new Label(r.getProjectName()), 2, 0);
            row.add(new Label(r.getPotName()), 3, 0);
            row.add(new Label(r.getPaidBy()), 4, 0);
            row.add(new Label(String.format("$%.2f", r.getAmount())), 5, 0);
            row.add(new Label(r.getShortNote(30)), 6, 0);
            transactionsTableBody.getChildren().add(row);
        }
    }

    private Double parseOptionalAmount(TextField field) {
        if (field == null) return null;
        String t = field.getText();
        if (t == null || t.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(t.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer selectedProjectFilterId() {
        String value = projectFilterComboBox.getValue();
        if (value == null || value.equals("All Projects")) return null;
        try {
            return Integer.parseInt(value.split("\\|")[0]);
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void handleGoToProjects() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/projects.fxml"));
            HBox root = loader.load();
            ProjectsController controller = loader.getController();
            controller.loadFromSession();
            Stage stage = (Stage) projectNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToProjectDetail() {
        // placeholder: navigate back to projects for now
        handleGoToProjects();
    }

    @FXML
    private void handleViewProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/profilePage.fxml"));
            HBox root = loader.load();
            ProfileController controller = loader.getController();
            controller.loadFromSession();
            Stage stage = (Stage) projectNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        navigateToLogin();
    }

    @FXML
    private void handleRecordTransaction() {
        // placeholder: navigate to projects
        handleGoToProjects();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        minAmountField.clear();
        maxAmountField.clear();
        projectFilterComboBox.getSelectionModel().selectFirst();
        refreshPage();
    }

    @FXML
    private void handleShowAllTransactions() {
        // placeholder
        refreshPage();
    }

    @FXML
    private void handleShowIncomeTransactions() {
        // not supported by backend
        transactionsTableBody.getChildren().clear();
        transactionsTableBody.getChildren().add(new Label("Transaction type not available yet."));
    }

    @FXML
    private void handleShowExpenseTransactions() {
        // not supported by backend
        transactionsTableBody.getChildren().clear();
        transactionsTableBody.getChildren().add(new Label("Transaction type not available yet."));
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/login.fxml"));
            HBox root = loader.load();
            Stage stage = (Stage) projectNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
 
