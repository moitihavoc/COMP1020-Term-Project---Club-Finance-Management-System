package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import oop.mony.Session;
import oop.mony.models.Club;
import oop.mony.models.Pot;
import oop.mony.models.Project;
import oop.mony.models.Transaction;
import oop.mony.models.User;
import oop.mony.services.ClubFinanceService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;

public class ProjectPageController {

    @FXML private Label sidebarProjectNameLabel;
    @FXML private Label sidebarUsername;
    @FXML private Label logoutButton;
    @FXML private TextField projectPageSearchField;
    @FXML private Label allocatedAmountLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label totalRemainingLabel;
    @FXML private Label projectNameLabel;
    @FXML private Button editMenuBtn;

    @FXML private VBox createPotForm;
    @FXML private TextField newPotNameField;
    @FXML private TextField newPotAllocatedField;
    @FXML private Label createPotErrorLabel;
    @FXML private FlowPane potsGrid;

    @FXML private VBox createTransactionForm;
    @FXML private TextField transactionNameField;
    @FXML private ComboBox<String> potComboBox;
    @FXML private TextField transactionAmountField;
    @FXML private TextField paidByField;
    @FXML private DatePicker transactionDatePicker;
    @FXML private Button proofUploadButton;
    @FXML private TextField noteField;
    @FXML private Label createTransactionErrorLabel;
    @FXML private VBox transactionsContainer;

    private User currentUser;
    private Club club;
    private Project selectedProject;
    private Path selectedProofImage;

    public void loadProjectFromSession(int projectId) {
        if (!Session.hasCurrentUser()) {
            navigateToLogin();
            return;
        }

        currentUser = Session.getCurrentUser();
        try {
            club = ClubFinanceService.loadFullClubForUser(currentUser.getUserId(), currentUser.getUsername());
            selectedProject = club.findProjectById(projectId);
            if (selectedProject == null) {
                navigateToProjects();
                return;
            }
            refreshPage();
        } catch (SQLException e) {
            if (createPotErrorLabel != null) createPotErrorLabel.setText("Failed to load project.");
            e.printStackTrace();
        }
    }

    private void refreshPage() {
        if (currentUser != null) {
            sidebarUsername.setText(currentUser.getUsername());
        }
        if (selectedProject != null) {
            sidebarProjectNameLabel.setText(selectedProject.getProjectName());
            projectNameLabel.setText(selectedProject.getProjectName());
            allocatedAmountLabel.setText(formatMoney(selectedProject.getAllocatedAmount()));
            totalSpentLabel.setText(formatMoney(selectedProject.getTotalSpent()));
            totalRemainingLabel.setText(formatMoney(selectedProject.getRemainingAmount()));
            renderPots();
            renderProjectTransactions();
            refreshPotComboBox();
        }
    }

    private void renderPots() {
        potsGrid.getChildren().clear();
        for (Pot pot : selectedProject.getPots()) {
            VBox card = new VBox();
            card.setSpacing(6);
            card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 12; -fx-min-width: 260;");
            Label name = new Label(pot.getPotName());
            Label allocated = new Label("Allocated: " + formatMoney(pot.getAllocatedAmount()));
            Label spent = new Label("Spent: " + formatMoney(pot.getTotalSpent()));
            Label remaining = new Label("Remaining: " + formatMoney(pot.getRemainingAmount()));
            card.getChildren().addAll(name, allocated, spent, remaining);
            potsGrid.getChildren().add(card);
        }
    }

    private void renderProjectTransactions() {
        transactionsContainer.getChildren().clear();
        for (Pot pot : selectedProject.getPots()) {
            for (Transaction tx : pot.getTransactions()) {
                HBox row = new HBox();
                row.setSpacing(12);
                Label date = new Label(tx.getTransactionDate().toString());
                Label name = new Label(tx.getTransactionName());
                Label potName = new Label(pot.getPotName());
                Label paidBy = new Label(tx.getPaidBy());
                Label note = new Label(tx.getNote());
                Label amount = new Label(formatMoney(tx.getAmount()));
                row.getChildren().addAll(date, name, potName, paidBy, note, amount);
                transactionsContainer.getChildren().add(row);
            }
        }
    }

    private void refreshPotComboBox() {
        potComboBox.getItems().clear();
        for (Pot pot : selectedProject.getPots()) {
            potComboBox.getItems().add(pot.getPotId() + "|" + pot.getPotName());
        }
    }

    @FXML
    private void handleGoToProjects() {
        navigateToProjects();
    }

    @FXML
    private void handleGoToTransactions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/transactionPage.fxml"));
            HBox root = loader.load();
            TransactionPageController controller = loader.getController();
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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to log out?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Logout");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                Session.clear();
                navigateToLogin();
            }
        });
    }

    @FXML
    private void handleEditMenu() {
        // Placeholder for editing project menu actions
    }

    @FXML
    private void handleCreatePot() {
        if (createPotForm != null) {
            createPotForm.setVisible(true);
            createPotForm.setManaged(true);
        }
    }

    @FXML
    private void handleConfirmCreatePot() {
        if (selectedProject == null) return;
        String name = newPotNameField.getText();
        double amount;
        try {
            amount = Double.parseDouble(newPotAllocatedField.getText().trim());
        } catch (Exception e) {
            createPotErrorLabel.setText("Allocated amount must be a number.");
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            createPotErrorLabel.setText("Pot name is required.");
            return;
        }
        if (!selectedProject.canAddPot(amount)) {
            createPotErrorLabel.setText("Not enough allocation available in project.");
            return;
        }

        try {
            club = ClubFinanceService.createPot(club, selectedProject.getProjectId(), name.trim(), amount);
            selectedProject = club.findProjectById(selectedProject.getProjectId());
            if (createPotForm != null) {
                createPotForm.setVisible(false);
                createPotForm.setManaged(false);
            }
            refreshPage();
        } catch (SQLException e) {
            createPotErrorLabel.setText("Failed to create pot. Try again.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelCreatePot() {
        if (createPotForm != null) {
            createPotForm.setVisible(false);
            createPotForm.setManaged(false);
        }
    }

    @FXML
    private void handleCreateTransaction() {
        if (createTransactionForm != null) {
            createTransactionForm.setVisible(true);
            createTransactionForm.setManaged(true);
        }
    }

    @FXML
    private void handleProofUpload() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(projectNameLabel.getScene().getWindow());
        if (file != null) {
            selectedProofImage = file.toPath();
            proofUploadButton.setText(file.getName());
        }
    }

    @FXML
    private void handleConfirmCreateTransaction() {
        if (selectedProject == null) return;
        String name = transactionNameField.getText();
        String potValue = potComboBox.getValue();
        if (potValue == null || potValue.isEmpty()) {
            createTransactionErrorLabel.setText("Select a pot.");
            return;
        }
        int potId = Integer.parseInt(potValue.split("\\|")[0]);
        double amount;
        try {
            amount = Double.parseDouble(transactionAmountField.getText().trim());
        } catch (Exception e) {
            createTransactionErrorLabel.setText("Amount must be a number.");
            return;
        }
        if (amount <= 0) {
            createTransactionErrorLabel.setText("Amount must be greater than 0.");
            return;
        }
        String paidBy = paidByField.getText();
        LocalDate date = transactionDatePicker.getValue();
        String note = noteField.getText();

        try {
            club = ClubFinanceService.createTransaction(club, potId, name, amount, paidBy, date, selectedProofImage, note);
            selectedProject = club.findProjectById(selectedProject.getProjectId());
            if (createTransactionForm != null) {
                createTransactionForm.setVisible(false);
                createTransactionForm.setManaged(false);
            }
            refreshPage();
        } catch (Exception e) {
            createTransactionErrorLabel.setText("Failed to record transaction.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelCreateTransaction() {
        if (createTransactionForm != null) {
            createTransactionForm.setVisible(false);
            createTransactionForm.setManaged(false);
        }
    }

    private String formatMoney(double amount) {
        return String.format("$%.2f", amount);
    }

    private void navigateToProjects() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/projects.fxml"));
            Parent root = loader.load();
            ProjectsController controller = loader.getController();
            controller.loadFromSession();
            Stage stage = (Stage) projectNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
