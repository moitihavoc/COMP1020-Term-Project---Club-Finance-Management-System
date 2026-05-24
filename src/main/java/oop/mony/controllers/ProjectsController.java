package oop.mony.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oop.mony.ClubFinanceService;
import oop.mony.Session;
import oop.mony.models.Club;
import oop.mony.models.Project;
import oop.mony.models.User;
import oop.mony.utils.DialogUtils;
import oop.mony.utils.MoneyFormatter;
import oop.mony.utils.MoneyInputFormatter;
import oop.mony.utils.SidebarSizer;

public class ProjectsController {

    @FXML private VBox sidebar;
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

    @FXML
    private void initialize() {
        SidebarSizer.bindToWindow(sidebar);
        MoneyInputFormatter.attach(newProjectAllocatedField);
        if (projectSearchField != null) {
            projectSearchField.textProperty().addListener((obs, oldText, newText) -> renderProjects());
        }
    }

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
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 20; -fx-min-width: 320; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 4);");

        Label nameLabel = new Label(project.getProjectName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #191919;");

        Label spentSummaryLabel = new Label("Spent " + formatMoney(project.getTotalSpent())
                + " of " + formatMoney(project.getAllocatedAmount()));
        spentSummaryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        ProgressBar progressBar = new ProgressBar(calculateSpentProgress(project));
        progressBar.getStyleClass().add("budget-progress");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Button openButton = new Button("View");
        openButton.setStyle("-fx-background-color: #299D91; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 8 14 8 14; -fx-background-radius: 8;");
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
        return MoneyFormatter.formatVnd(amount);
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/transactionPage.fxml"));
            HBox root = loader.load();
            TransactionPageController controller = loader.getController();
            controller.loadFromSession();
            Stage stage = (Stage) sidebarUsername.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            showError("Unable to open transactions page.");
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
            Stage stage = (Stage) sidebarUsername.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            showError("Unable to open profile page.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditBalance() {
        if (club == null) {
            showError("Club data is not loaded.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(MoneyInputFormatter.format(club.getTotalBalance()));
        dialog.setTitle("Edit Total Balance");
        dialog.setHeaderText("Update club total balance");
        dialog.setContentText("New total balance:");
        DialogUtils.style(dialog);
        MoneyInputFormatter.attach(dialog.getEditor());

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        double newBalance;
        try {
            newBalance = MoneyInputFormatter.parse(result.get());
        } catch (NumberFormatException e) {
            showError("Please enter a valid number.");
            return;
        }

        try {
            boolean updated = ClubFinanceService.updateTotalBalance(club, newBalance);
            if (!updated) {
                showError("New balance must be at least equal to allocated amount.");
                return;
            }
            club = ClubFinanceService.loadFullClubForUser(club.getUserId(), club.getClubName());
            refreshPage();
            showInfo("Total balance updated.");
        } catch (SQLException e) {
            showError("Unable to update balance. Please try again.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateProject() {
        showCreateProjectForm(true);
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        navigateToLogin();
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
            allocatedAmount = MoneyInputFormatter.parse(newProjectAllocatedField);
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
            Parent root = loader.load();
            Stage stage = (Stage) sidebarUsername.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            if (createProjectErrorLabel != null) {
                createProjectErrorLabel.setText("Unable to navigate to login.");
            }
            e.printStackTrace();
        }
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        DialogUtils.style(alert);
        alert.showAndWait();
    }
}
