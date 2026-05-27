package oop.mony.controllers;

import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
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

public class DashboardController {

    @FXML private VBox sidebar;
    @FXML private Label sidebarUsername;
    @FXML private Label totalBalanceLabel;
    @FXML private Label totalAllocatedLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label remainingBalanceLabel;
    @FXML private VBox createProjectForm;
    @FXML private TextField newProjectNameField;
    @FXML private TextField newProjectAllocatedField;
    @FXML private Label createProjectErrorLabel;
    @FXML private FlowPane projectsGrid;

    private User currentUser;
    private Club club;

    @FXML
    private void initialize() {
        NavigationUtils.sizeSidebar(sidebar);
        MoneyUtils.attach(newProjectAllocatedField);
    }

    public void loadFromSession() {
        if (!Session.hasCurrentUser()) {
            NavigationUtils.goToLogin(sidebarUsername);
            return;
        }
        currentUser = Session.getCurrentUser();
        try {
            club = ClubFinanceService.loadFullClubForUser(currentUser.getUserId(), currentUser.getUsername());
            refreshPage();
        } catch (SQLException e) {
            if (createProjectErrorLabel != null) {
                createProjectErrorLabel.setText("Unable to load club data. Please try again.");
            }
            e.printStackTrace();
        }
    }

    private void refreshPage() {
        if (currentUser != null) {
            sidebarUsername.setText(currentUser.getUsername());
        }
        if (club != null) {
            totalBalanceLabel.setText(formatMoney(club.getTotalBalance()));
            totalAllocatedLabel.setText(formatMoney(club.getTotalAllocated()));
            totalSpentLabel.setText(formatMoney(club.getTotalSpent()));
            remainingBalanceLabel.setText(formatMoney(club.getTotalRemaining()));
            renderProjects();
        }
    }

    private void renderProjects() {
        projectsGrid.getChildren().clear();
        if (club == null) {
            return;
        }
        for (Project project : club.getProjects()) {
            projectsGrid.getChildren().add(createProjectCard(project));
        }
    }

    private VBox createProjectCard(Project project) {
        VBox card = new VBox();
        card.setSpacing(12);
        card.getStyleClass().add("finance-card");
        Label nameLabel = new Label(project.getProjectName());
        nameLabel.getStyleClass().add("finance-card-title");
        Label spentSummaryLabel = new Label("Spent " + formatMoney(project.getTotalSpent())
                + " of " + formatMoney(project.getAllocatedAmount()));
        spentSummaryLabel.getStyleClass().add("finance-card-summary");
        ProgressBar progressBar = new ProgressBar(calculateSpentProgress(project));
        progressBar.getStyleClass().add("budget-progress");
        progressBar.setMaxWidth(Double.MAX_VALUE);
        Button openButton = new Button("View");
        openButton.getStyleClass().add("finance-primary-button");
        openButton.setOnAction(event -> {
            if (!NavigationUtils.goToProjectPage(projectsGrid, project.getProjectId())) {
                if (createProjectErrorLabel != null) {
                    createProjectErrorLabel.setText("Unable to open project page.");
                }
            }
        });
        card.getChildren().addAll(nameLabel, spentSummaryLabel, progressBar, openButton);
        return card;
    }

    private double calculateSpentProgress(Project project) {
        if (project == null || project.getAllocatedAmount() <= 0) {
            return 0.0;
        }
        return Math.min(1.0, project.getTotalSpent() / project.getAllocatedAmount());
    }

    private String formatMoney(double amount) {
        return MoneyUtils.formatVnd(amount);
    }

    private void showCreateProjectForm(boolean visible) {
        if (createProjectForm != null) {
            createProjectForm.setVisible(visible);
            createProjectForm.setManaged(visible);
        }
        if (!visible) {
            clearCreateProjectForm();
        }
    }

    private void clearCreateProjectForm() {
        if (newProjectNameField != null) {
            newProjectNameField.clear();
        }
        if (newProjectAllocatedField != null) {
            newProjectAllocatedField.clear();
        }
        if (createProjectErrorLabel != null) {
            createProjectErrorLabel.setText("");
        }
    }

    @FXML
    private void handleGoToTransactions() {
        if (!NavigationUtils.goToTransactions(sidebarUsername)) {
            showError("Unable to open transactions page.");
        }
    }

    @FXML
    private void handleChangePassword() {
        if (!NavigationUtils.goToChangePassword(sidebarUsername)) {
            showError("Unable to open change password page.");
        }
    }

    @FXML
    private void handleEditBalance() {
        if (club == null) {
            showError("Club data is not loaded.");
            return;
        }
        javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Edit Total Balance");
        dialog.setHeaderText("Update club total balance");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField balanceField = new TextField(MoneyUtils.format(club.getTotalBalance()));
        MoneyUtils.attach(balanceField);
        Label errorLabel = new Label("");
        errorLabel.getStyleClass().add("inline-error");
        grid.add(new Label("New total balance"), 0, 0);
        grid.add(balanceField, 1, 0);
        grid.add(errorLabel, 0, 1, 2, 1);
        dialog.getDialogPane().setContent(grid);
        DialogUtils.style(dialog);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            double newBalance;
            try {
                newBalance = MoneyUtils.parse(balanceField);
            } catch (NumberFormatException e) {
                errorLabel.setText("Please enter a valid number.");
                event.consume();
                return;
            }
            try {
                boolean updated = ClubFinanceService.updateTotalBalance(club, newBalance);
                if (!updated) {
                    errorLabel.setText("New balance must be at least allocated amount.");
                    event.consume();
                    return;
                }
                club = ClubFinanceService.loadFullClubForUser(club.getUserId(), club.getClubName());
                refreshPage();
            } catch (SQLException e) {
                errorLabel.setText("Unable to update balance. Please try again.");
                e.printStackTrace();
                event.consume();
            }
        });
        dialog.showAndWait();
    }

    @FXML
    private void handleCreateProject() {
        showCreateProjectForm(true);
    }

    @FXML
    private void handleLogout() {
        if (!NavigationUtils.logout(sidebarUsername) && createProjectErrorLabel != null) {
            createProjectErrorLabel.setText("Unable to navigate to login.");
        }
    }

    @FXML
    private void handleConfirmCreateProject() {
        if (club == null) {
            createProjectErrorLabel.setText("Unable to create project. Reload the page.");
            return;
        }
        String name = newProjectNameField.getText();
        if (name == null || name.trim().isEmpty()) {
            createProjectErrorLabel.setText("Project name is required.");
            return;
        }
        double allocatedAmount;
        try {
            allocatedAmount = MoneyUtils.parse(newProjectAllocatedField);
        } catch (NumberFormatException e) {
            createProjectErrorLabel.setText("Allocated amount must be a number.");
            return;
        }
        if (allocatedAmount < 0) {
            createProjectErrorLabel.setText("Allocated amount cannot be negative.");
            return;
        }
        if (!club.canAddProject(allocatedAmount)) {
            createProjectErrorLabel.setText("Not enough available balance to allocate this project.");
            return;
        }
        try {
            club = ClubFinanceService.createProject(club, name.trim(), allocatedAmount);
            showCreateProjectForm(false);
            refreshPage();
        } catch (SQLException e) {
            createProjectErrorLabel.setText("Failed to create project. Please try again.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelCreateProject() {
        showCreateProjectForm(false);
    }

    private void showError(String message) {
        if (createProjectErrorLabel != null) {
            createProjectErrorLabel.setText(message);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            DialogUtils.style(alert);
            alert.showAndWait();
        }
    }
}
