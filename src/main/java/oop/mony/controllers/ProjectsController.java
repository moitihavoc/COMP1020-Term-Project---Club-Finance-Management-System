package oop.mony.controllers;

import java.io.IOException;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oop.mony.Session;
import oop.mony.models.Club;
import oop.mony.models.Project;
import oop.mony.models.User;
import oop.mony.services.ClubFinanceService;

public class ProjectsController {

    @FXML private Label usernameLabel;
    @FXML private Label sidebarUsername;
    @FXML private Label totalBalanceLabel;
    @FXML private Label totalAllocatedLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label remainingBalanceLabel;
    @FXML private TextField projectSearchField;
    @FXML private Button createProjectBtn;
    @FXML private VBox createProjectForm;
    @FXML private TextField newProjectNameField;
    @FXML private TextField newProjectAllocatedField;
    @FXML private Label createProjectErrorLabel;
    @FXML private FlowPane projectsGrid;

    private User currentUser;
    private Club club;

    public void loadFromSession() {
        if (!Session.hasCurrentUser()) {
            navigateToLogin();
            return;
        }

        currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }

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
            usernameLabel.setText(currentUser.getUsername());
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
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 20; -fx-min-width: 320; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 4);");

        Label nameLabel = new Label(project.getProjectName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #191919;");

        Label allocatedLabel = new Label("Allocated: " + formatMoney(project.getAllocatedAmount()));
        Label spentLabel = new Label("Spent: " + formatMoney(project.getTotalSpent()));
        Label remainingLabel = new Label("Remaining: " + formatMoney(project.getRemainingAmount()));

        Button openButton = new Button("View project");
        openButton.setStyle("-fx-background-color: #299D91; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600; -fx-background-radius: 8;");
        openButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/projectPage.fxml"));
                HBox root = loader.load();
                ProjectPageController controller = loader.getController();
                controller.loadProjectFromSession(project.getProjectId());
                Stage stage = (Stage) projectsGrid.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException e) {
                if (createProjectErrorLabel != null) {
                    createProjectErrorLabel.setText("Unable to open project page.");
                }
                e.printStackTrace();
            }
        });

        card.getChildren().addAll(nameLabel, allocatedLabel, spentLabel, remainingLabel, openButton);
        return card;
    }

    private String formatMoney(double amount) {
        return String.format("$%.2f", amount);
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
        // TODO: implement navigation to transactions page
    }

    @FXML
    private void handleViewProfile() {
        // TODO: implement navigation to profile page
    }

    @FXML
    private void handleEditBalance() {
        // stub: show edit balance UI (not implemented yet)
    }

    @FXML
    private void handleCreateProject() {
        showCreateProjectForm(true);
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
            allocatedAmount = Double.parseDouble(newProjectAllocatedField.getText().trim());
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

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/login.fxml"));
            HBox root = loader.load();
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            if (createProjectErrorLabel != null) {
                createProjectErrorLabel.setText("Unable to navigate to login.");
            }
            e.printStackTrace();
        }
    }
}
