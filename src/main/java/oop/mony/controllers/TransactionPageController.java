package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
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
    @FXML private TextField searchField;
    @FXML private Label projectNameLabel;
    @FXML private Button recordTransactionButton;
    @FXML private ComboBox<String> projectFilterComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField minAmountField;
    @FXML private TextField maxAmountField;
    @FXML private Button clearFiltersButton;
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
        projectFilterComboBox.getItems().add("All Projects");
        club.getProjects().forEach(p -> projectFilterComboBox.getItems().add(p.getProjectId() + "|" + p.getProjectName()));
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
            row.setStyle("-fx-padding: 14 20 14 20; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");
            
            ColumnConstraints col0 = new ColumnConstraints();
            col0.setPercentWidth(12);
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(18);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(16);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPercentWidth(14);
            ColumnConstraints col4 = new ColumnConstraints();
            col4.setPercentWidth(14);
            ColumnConstraints col5 = new ColumnConstraints();
            col5.setPercentWidth(12);
            ColumnConstraints col6 = new ColumnConstraints();
            col6.setPercentWidth(14);
            
            row.getColumnConstraints().addAll(col0, col1, col2, col3, col4, col5, col6);
            
            Label dateLabel = new Label(formatDate(r.getTransactionDate()));
            dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            dateLabel.setWrapText(true);
            
            Label nameLabel = new Label(r.getTransactionName());
            nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            nameLabel.setWrapText(true);
            
            Label projectLabel = new Label(r.getProjectName());
            projectLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            projectLabel.setWrapText(true);
            
            Label potLabel = new Label(r.getPotName());
            potLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            potLabel.setWrapText(true);
            
            Label paidByLabel = new Label(r.getPaidBy());
            paidByLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            paidByLabel.setWrapText(true);
            
            Label amountLabel = new Label(String.format("$%.2f", r.getAmount()));
            amountLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #191919;");
            
            Label noteLabel = new Label(r.getShortNote(30));
            noteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            noteLabel.setWrapText(true);
            
            row.add(dateLabel, 0, 0);
            row.add(nameLabel, 1, 0);
            row.add(projectLabel, 2, 0);
            row.add(potLabel, 3, 0);
            row.add(paidByLabel, 4, 0);
            row.add(amountLabel, 5, 0);
            row.add(noteLabel, 6, 0);
 
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

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) projectNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "-";
        return date.toString();
    }
}
 
