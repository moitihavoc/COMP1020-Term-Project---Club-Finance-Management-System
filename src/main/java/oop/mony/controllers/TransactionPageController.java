package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oop.mony.ClubFinanceService;
import oop.mony.Session;
import oop.mony.models.Club;
import oop.mony.models.TransactionRecord;
import oop.mony.models.User;
import oop.mony.utils.DialogUtils;
import oop.mony.utils.MoneyInputFormatter;
import oop.mony.utils.NavigationUtils;
import oop.mony.utils.SidebarSizer;
import oop.mony.utils.TransactionTableRenderer;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class TransactionPageController {

    @FXML private VBox sidebar;
    @FXML private Label sidebarUsername;
    @FXML private Label logoutButton;
    @FXML private TextField searchField;
    @FXML private Label projectNameLabel;
    @FXML private ComboBox<ProjectFilterOption> projectFilterComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField minAmountField;
    @FXML private TextField maxAmountField;
    @FXML private Button clearFiltersButton;
    @FXML private VBox transactionsTableBody;

    private User currentUser;
    private Club club;

    @FXML
    private void initialize() {
        SidebarSizer.bindToWindow(sidebar);
        MoneyInputFormatter.attach(minAmountField);
        MoneyInputFormatter.attach(maxAmountField);
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
        TransactionTableRenderer.renderTransactionRecords(
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
            return MoneyInputFormatter.parse(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer selectedProjectFilterId() {
        ProjectFilterOption selected = projectFilterComboBox.getValue();
        return selected == null ? null : selected.projectId();
    }

    @FXML
    private void handleGoToProjects() {
        NavigationUtils.goToProjects(projectNameLabel);
    }

    @FXML
    private void handleViewProfile() {
        NavigationUtils.goToProfile(projectNameLabel);
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
        if (proofPath == null || proofPath.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No proof image available.");
            DialogUtils.style(alert);
            alert.showAndWait();
            return;
        }

        try {
            File file = new File(proofPath);
            if (!file.exists()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Proof image file not found.");
                DialogUtils.style(alert);
                alert.showAndWait();
                return;
            }

            Stage proofStage = new Stage();
            proofStage.setTitle("Proof Image");
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(600);
            imageView.setFitHeight(600);

            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            proofStage.setScene(new javafx.scene.Scene(scrollPane, 700, 700));
            proofStage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open proof image.");
            DialogUtils.style(alert);
            alert.showAndWait();
            e.printStackTrace();
        }
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
 
