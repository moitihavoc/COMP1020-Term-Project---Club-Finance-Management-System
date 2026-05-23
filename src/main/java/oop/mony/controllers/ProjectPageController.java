package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
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

    @FXML private VBox sidebar;
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
    @FXML private ComboBox<PotOption> potComboBox;
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

    @FXML
    private void initialize() {
        SidebarSizer.bindToWindow(sidebar);
    }

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
            card.setSpacing(8);
            card.setPrefWidth(320);
            card.setMinHeight(160);
            card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 12; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
            
            HBox titleBox = new HBox();
            titleBox.setAlignment(Pos.CENTER_LEFT);
            titleBox.setSpacing(8);
            Label name = new Label(pot.getPotName());
            name.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #191919;");
            titleBox.getChildren().add(name);
            
            HBox dataBox = new HBox();
            dataBox.setSpacing(12);
            dataBox.setPrefWidth(300);
            VBox col1 = new VBox();
            col1.setSpacing(4);
            Label allocLabel = new Label("Allocated");
            allocLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
            Label allocValue = new Label(formatMoney(pot.getAllocatedAmount()));
            allocValue.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #191919;");
            col1.getChildren().addAll(allocLabel, allocValue);
            
            VBox col2 = new VBox();
            col2.setSpacing(4);
            Label spentLabel = new Label("Spent");
            spentLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
            Label spentValue = new Label(formatMoney(pot.getTotalSpent()));
            spentValue.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #191919;");
            col2.getChildren().addAll(spentLabel, spentValue);
            
            VBox col3 = new VBox();
            col3.setSpacing(4);
            Label remainLabel = new Label("Remaining");
            remainLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
            Label remainValue = new Label(formatMoney(pot.getRemainingAmount()));
            remainValue.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #299D91;");
            col3.getChildren().addAll(remainLabel, remainValue);
            
            dataBox.getChildren().addAll(col1, col2, col3);
            
            HBox actionBox = new HBox();
            actionBox.setSpacing(8);
            actionBox.setAlignment(Pos.BOTTOM_RIGHT);
            actionBox.setPrefHeight(30);
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #e53935; -fx-font-size: 12px; -fx-padding: 6 12 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> handleDeletePot(pot.getPotId()));
            actionBox.getChildren().add(deleteBtn);
            
            card.getChildren().addAll(titleBox, dataBox, actionBox);
            potsGrid.getChildren().add(card);
        }
    }

    private void renderProjectTransactions() {
        transactionsContainer.getChildren().clear();
        int rowNumber = 0;
        for (Pot pot : selectedProject.getPots()) {
            for (Transaction tx : pot.getTransactions()) {
                GridPane row = new GridPane();
                row.setHgap(12);
                row.setAlignment(Pos.TOP_LEFT);
                row.setStyle("-fx-padding: 14 20 14 20; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");
                
                ColumnConstraints col0 = new ColumnConstraints();
                col0.setPercentWidth(11);
                ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(18);
                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPercentWidth(11);
                ColumnConstraints col3 = new ColumnConstraints();
                col3.setPercentWidth(10);
                ColumnConstraints col4 = new ColumnConstraints();
                col4.setPercentWidth(12);
                ColumnConstraints col5 = new ColumnConstraints();
                col5.setPercentWidth(14);
                ColumnConstraints col6 = new ColumnConstraints();
                col6.setPercentWidth(14);
                ColumnConstraints col7 = new ColumnConstraints();
                col7.setPercentWidth(10);
                
                row.getColumnConstraints().addAll(col0, col1, col2, col3, col4, col5, col6, col7);
                
                Label dateLabel = new Label(formatDate(tx.getTransactionDate()));
                dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
                dateLabel.setWrapText(true);
                
                Label nameLabel = new Label(tx.getTransactionName());
                nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
                nameLabel.setWrapText(true);

                Label projectLabel = new Label(selectedProject.getProjectName());
                projectLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
                projectLabel.setWrapText(true);
                
                Label potNameLabel = new Label(pot.getPotName());
                potNameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
                potNameLabel.setWrapText(true);
                
                Label paidByLabel = new Label(tx.getPaidBy());
                paidByLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
                paidByLabel.setWrapText(true);
                
                Label noteLabel = new Label(tx.getNote() != null && !tx.getNote().isEmpty() ? tx.getNote() : "-");
                noteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
                noteLabel.setWrapText(true);
                
                Label amountLabel = new Label(formatMoney(tx.getAmount()));
                amountLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #191919;");
                
                VBox proofBox = new VBox();
                proofBox.setAlignment(Pos.TOP_LEFT);
                if (tx.getProofPath() != null && !tx.getProofPath().isEmpty()) {
                    Button viewProofBtn = new Button("View");
                    viewProofBtn.setStyle("-fx-background-color: #299D91; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
                    viewProofBtn.setOnAction(e -> handleViewProof(tx.getProofPath()));
                    proofBox.getChildren().add(viewProofBtn);
                } else {
                    Label noProofLabel = new Label("-");
                    noProofLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa;");
                    proofBox.getChildren().add(noProofLabel);
                }
                
                row.add(dateLabel, 0, 0);
                row.add(nameLabel, 1, 0);
                row.add(projectLabel, 2, 0);
                row.add(potNameLabel, 3, 0);
                row.add(paidByLabel, 4, 0);
                row.add(amountLabel, 5, 0);
                row.add(noteLabel, 6, 0);
                row.add(proofBox, 7, 0);
                
                transactionsContainer.getChildren().add(row);
                rowNumber++;
            }
        }
    }

    private void refreshPotComboBox() {
        potComboBox.getItems().clear();
        for (Pot pot : selectedProject.getPots()) {
            potComboBox.getItems().add(new PotOption(pot.getPotId(), pot.getPotName()));
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
        System.out.println("profile clicked");
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
    private void handleEditMenu() {
        if (selectedProject == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Project");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(selectedProject.getProjectName());
        TextField allocatedField = new TextField(String.valueOf(selectedProject.getAllocatedAmount()));

        grid.add(new Label("Project name"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Allocated amount"), 0, 1);
        grid.add(allocatedField, 1, 1);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #e53935;");
        grid.add(errorLabel, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> dialogButton);
        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String newName = nameField.getText();
                double newAllocated;
                try {
                    newAllocated = Double.parseDouble(allocatedField.getText().trim());
                } catch (Exception e) {
                    errorLabel.setText("Allocated amount must be a number.");
                    return;
                }
                if (newName == null || newName.trim().isEmpty()) {
                    errorLabel.setText("Project name is required.");
                    return;
                }
                if (newAllocated < selectedProject.getTotalSpent()) {
                    errorLabel.setText("Allocated cannot be less than already spent: " + formatMoney(selectedProject.getTotalSpent()));
                    return;
                }

                try {
                    club = ClubFinanceService.updateProject(club, selectedProject.getProjectId(), newName.trim(), newAllocated);
                    selectedProject = club.findProjectById(selectedProject.getProjectId());
                    refreshPage();
                } catch (SQLException e) {
                    errorLabel.setText("Failed to update project.");
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleCreatePot() {
        clearPotForm();
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
            clearPotForm();
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
        clearPotForm();
    }

    @FXML
    private void handleCreateTransaction() {
        clearTransactionForm();
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
        PotOption selectedPot = potComboBox.getValue();
        if (selectedPot == null) {
            createTransactionErrorLabel.setText("Select a pot.");
            return;
        }
        int potId = selectedPot.potId();
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
            clearTransactionForm();
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
        clearTransactionForm();
    }

    private String formatMoney(double amount) {
        return String.format("$%.2f", amount);
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "-";
        return date.toString();
    }

    private void clearPotForm() {
        newPotNameField.setText("");
        newPotAllocatedField.setText("");
        createPotErrorLabel.setText("");
    }

    private void clearTransactionForm() {
        transactionNameField.setText("");
        transactionAmountField.setText("");
        paidByField.setText("");
        noteField.setText("");
        transactionDatePicker.setValue(null);
        potComboBox.setValue(null);
        selectedProofImage = null;
        proofUploadButton.setText("Upload File");
        createTransactionErrorLabel.setText("");
    }

    private void handleDeletePot(int potId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this pot? All transactions in this pot will also be deleted.", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete Pot");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    club = ClubFinanceService.deletePot(club, potId);
                    selectedProject = club.findProjectById(selectedProject.getProjectId());
                    refreshPage();
                } catch (SQLException e) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "Failed to delete pot.");
                    error.showAndWait();
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleViewProof(String proofPath) {
        if (proofPath == null || proofPath.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No proof image available.");
            alert.showAndWait();
            return;
        }

        try {
            File file = new File(proofPath);
            if (!file.exists()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Proof image file not found.");
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
            alert.showAndWait();
            e.printStackTrace();
        }
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

    private static final class PotOption {
        private final int potId;
        private final String label;

        private PotOption(int potId, String label) {
            this.potId = potId;
            this.label = label;
        }

        private int potId() {
            return potId;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
