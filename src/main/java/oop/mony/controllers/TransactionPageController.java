package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import oop.mony.ClubFinanceService;
import oop.mony.Session;
import oop.mony.models.Club;
import oop.mony.models.Transaction;
import oop.mony.models.User;
import oop.mony.utils.DialogUtils;
import oop.mony.utils.MoneyUtils;
import oop.mony.utils.NavigationUtils;
import oop.mony.utils.TransactionTableRenderer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class TransactionPageController {

    @FXML private VBox sidebar;
    @FXML private Label sidebarUsername;
    @FXML private TextField searchField;
    @FXML private Label projectNameLabel;
    @FXML private ComboBox<ProjectFilterOption> projectFilterComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField minAmountField;
    @FXML private TextField maxAmountField;
    @FXML private VBox transactionsTableBody;

    private User currentUser;
    private Club club;

    @FXML
    private void initialize() {
        NavigationUtils.sizeSidebar(sidebar);
        MoneyUtils.attach(minAmountField);
        MoneyUtils.attach(maxAmountField);
    }

    public void loadFromSession() {
        if (!Session.hasCurrentUser()) {
            NavigationUtils.goToLogin(projectNameLabel);
            return;
        }
        currentUser = Session.getCurrentUser();
        try {
            club = ClubFinanceService.loadFullClubForUser(currentUser.getUserId(), currentUser.getUsername());
            sidebarUsername.setText(currentUser.getUsername());
            refreshProjectFilter();
            setupFilterListeners();
            refreshPage();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshProjectFilter() {
        projectFilterComboBox.getItems().clear();
        projectFilterComboBox.getItems().add(new ProjectFilterOption(null, "All Projects"));
        club.getProjects().forEach(p -> projectFilterComboBox.getItems().add(
                new ProjectFilterOption(p.getProjectId(), p.getProjectName())
        ));
        projectFilterComboBox.getSelectionModel().selectFirst();
    }

    private void setupFilterListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshPage());
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> refreshPage());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> refreshPage());
        minAmountField.textProperty().addListener((obs, oldVal, newVal) -> refreshPage());
        maxAmountField.textProperty().addListener((obs, oldVal, newVal) -> refreshPage());
        projectFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> refreshPage());
    }

    private void refreshPage() {
        ArrayList<Transaction> results = ClubFinanceService.searchTransactions(
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

    private void renderTransactions(ArrayList<Transaction> records) {
        TransactionTableRenderer.renderTransactions(
                transactionsTableBody,
                records,
                this::formatDate,
                this::handleViewProof
        );
    }

    private Double parseOptionalAmount(TextField field) {
        if (field == null) return null;
        String t = field.getText();
        if (t == null || t.trim().isEmpty()) return null;
        try {
            return MoneyUtils.parse(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer selectedProjectFilterId() {
        ProjectFilterOption selected = projectFilterComboBox.getValue();
        return selected == null ? null : selected.projectId();
    }

    @FXML
    private void handleGoToDashboard() {
        NavigationUtils.goToDashboard(projectNameLabel);
    }

    @FXML
    private void handleChangePassword() {
        NavigationUtils.goToChangePassword(projectNameLabel);
    }

    @FXML
    private void handleLogout() {
        NavigationUtils.logout(projectNameLabel);
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

    private String formatDate(LocalDate date) {
        if (date == null) return "-";
        return date.toString();
    }

    private void handleViewProof(String proofPath) {
        DialogUtils.showProofImage(proofPath);
    }

    private static final class ProjectFilterOption {
        private final Integer projectId;
        private final String label;

        private ProjectFilterOption(Integer projectId, String label) {
            this.projectId = projectId;
            this.label = label;
        }

        private Integer projectId() {
            return projectId;
        }
        @Override
        public String toString() {
            return label;
        }
    }
}
