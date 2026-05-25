package oop.mony.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import oop.mony.ClubFinanceService;
import oop.mony.Session;
import oop.mony.models.Club;
import oop.mony.models.Project;
import oop.mony.models.User;
import oop.mony.utils.DialogUtils;
import oop.mony.utils.MoneyUtils;
import oop.mony.utils.NavigationUtils;

import java.sql.SQLException;

public class ProjectController {

    @FXML private VBox sidebar;
    @FXML private Label sidebarProjectNameLabel;
    @FXML private Label sidebarUsername;
    @FXML private Label allocatedAmountLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label totalRemainingLabel;
    @FXML private Label projectNameLabel;
    @FXML private ProjectPotSectionController potsSectionController;
    @FXML private ProjectTransactionSectionController transactionsSectionController;

    private User currentUser;
    private Club club;
    private Project selectedProject;

    @FXML
    private void initialize() {
        NavigationUtils.sizeSidebar(sidebar);
    }

    public void loadProjectFromSession(int projectId) {
        if (!Session.hasCurrentUser()) {
            NavigationUtils.goToLogin(projectNameLabel);
            return;
        }
        currentUser = Session.getCurrentUser();
        try {
            club = ClubFinanceService.loadFullClubForUser(currentUser.getUserId(), currentUser.getUsername());
            selectedProject = club.findProjectById(projectId);
            if (selectedProject == null) {
                NavigationUtils.goToDashboard(projectNameLabel);
                return;
            }
            refreshPage();
        } catch (SQLException e) {
            showError("Failed to load project.");
            e.printStackTrace();
        }
    }

    private void refreshPage() {
        if (currentUser != null) {
            sidebarUsername.setText(currentUser.getUsername());
        }
        if (selectedProject == null) {
            return;
        }
        sidebarProjectNameLabel.setText(selectedProject.getProjectName());
        projectNameLabel.setText(selectedProject.getProjectName());
        allocatedAmountLabel.setText(formatMoney(selectedProject.getAllocatedAmount()));
        totalSpentLabel.setText(formatMoney(selectedProject.getTotalSpent()));
        totalRemainingLabel.setText(formatMoney(selectedProject.getRemainingAmount()));
        if (potsSectionController != null) {
            potsSectionController.setContext(club, selectedProject, this::handleClubUpdated);
        }
        if (transactionsSectionController != null) {
            transactionsSectionController.setContext(club, selectedProject, this::handleClubUpdated);
        }
    }

    private void handleClubUpdated(Club updatedClub) {
        if (updatedClub == null || selectedProject == null) {
            return;
        }
        int projectId = selectedProject.getProjectId();
        club = updatedClub;
        selectedProject = club.findProjectById(projectId);
        if (selectedProject == null) {
            NavigationUtils.goToDashboard(projectNameLabel);
            return;
        }
        refreshPage();
    }

    @FXML
    private void handleGoToDashboard() {
        NavigationUtils.goToDashboard(projectNameLabel);
    }

    @FXML
    private void handleGoToTransactions() {
        NavigationUtils.goToTransactions(projectNameLabel);
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
    private void handleEditMenu() {
        if (selectedProject == null) return;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Project");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField nameField = new TextField(selectedProject.getProjectName());
        TextField allocatedField = new TextField(MoneyUtils.format(selectedProject.getAllocatedAmount()));
        MoneyUtils.attach(allocatedField);
        grid.add(new Label("Project name"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Allocated amount"), 0, 1);
        grid.add(allocatedField, 1, 1);
        Label errorLabel = new Label("");
        errorLabel.getStyleClass().add("inline-error");
        grid.add(errorLabel, 0, 2, 2, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> dialogButton);
        DialogUtils.style(dialog);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String newName = nameField.getText();
            if (newName == null || newName.trim().isEmpty()) {
                errorLabel.setText("Project name is required.");
                event.consume();
                return;
            }
            double newAllocated;
            try {
                newAllocated = MoneyUtils.parse(allocatedField);
            } catch (NumberFormatException e) {
                errorLabel.setText("Allocated amount must be a number.");
                event.consume();
                return;
            }
            if (newAllocated < 0) {
                errorLabel.setText("Allocated amount cannot be negative.");
                event.consume();
                return;
            }
            if (newAllocated < selectedProject.getTotalSpent()) {
                errorLabel.setText("Allocated cannot be less than already spent.");
                event.consume();
                return;
            }
            double allocatedToOtherProjects = club.getTotalAllocated() - selectedProject.getAllocatedAmount();
            if (allocatedToOtherProjects + Math.max(0.0, newAllocated) > club.getTotalBalance()) {
                errorLabel.setText("Not enough club balance available for this project.");
                event.consume();
                return;
            }
            try {
                Club updatedClub = ClubFinanceService.updateProject(
                        club,
                        selectedProject.getProjectId(),
                        newName.trim(),
                        newAllocated
                );
                handleClubUpdated(updatedClub);
            } catch (SQLException e) {
                errorLabel.setText("Failed to update project.");
                e.printStackTrace();
                event.consume();
            }
        });
        dialog.showAndWait();
    }

    @FXML
    private void handleDeleteProject() {
        if (club == null || selectedProject == null) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this project? All pots and transactions in this project will also be deleted.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete Project");
        DialogUtils.style(confirm);
        confirm.showAndWait().ifPresent(button -> {
            if (button != ButtonType.YES) {
                return;
            }
            try {
                ClubFinanceService.deleteProject(club, selectedProject.getProjectId());
                NavigationUtils.goToDashboard(projectNameLabel);
            } catch (SQLException e) {
                showError("Failed to delete project.");
                e.printStackTrace();
            }
        });
    }

    private String formatMoney(double amount) {
        return MoneyUtils.formatVnd(amount);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        DialogUtils.style(alert);
        alert.showAndWait();
    }
}
