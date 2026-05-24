package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
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
import oop.mony.ClubFinanceService;
import oop.mony.Session;
import oop.mony.models.Club;
import oop.mony.models.Pot;
import oop.mony.models.Project;
import oop.mony.models.Transaction;
import oop.mony.models.User;
import oop.mony.utils.DialogUtils;
import oop.mony.utils.MoneyFormatter;
import oop.mony.utils.MoneyInputFormatter;
import oop.mony.utils.NavigationUtils;
import oop.mony.utils.SidebarSizer;

import java.io.File;
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
        MoneyInputFormatter.attach(newPotAllocatedField);
        MoneyInputFormatter.attach(transactionAmountField);
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
                NavigationUtils.goToProjects(projectNameLabel);
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
            card.setSpacing(12);
            card.setPrefWidth(320);
            card.getStyleClass().add("finance-card");

            Label name = new Label(pot.getPotName());
            name.getStyleClass().add("finance-card-title");

            Label spentSummary = new Label("Spent " + formatMoney(pot.getTotalSpent())
                    + " of " + formatMoney(pot.getAllocatedAmount()));
            spentSummary.getStyleClass().add("finance-card-summary");

            ProgressBar progressBar = new ProgressBar(calculateSpentProgress(pot));
            progressBar.getStyleClass().add("budget-progress");
            progressBar.setMaxWidth(Double.MAX_VALUE);

            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().add("finance-secondary-button");
            editBtn.setOnAction(e -> handleEditPot(pot));

            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("finance-danger-button");
            deleteBtn.setOnAction(e -> handleDeletePot(pot.getPotId()));

            HBox actionBox = new HBox(8, editBtn, deleteBtn);

            card.getChildren().addAll(name, spentSummary, progressBar, actionBox);
            potsGrid.getChildren().add(card);
        }
    }

    private double calculateSpentProgress(Pot pot) {
        if (pot == null || pot.getAllocatedAmount() <= 0) {
            return 0.0;
        }
        return Math.min(1.0, pot.getTotalSpent() / pot.getAllocatedAmount());
    }

    private void renderProjectTransactions() {
        transactionsContainer.getChildren().clear();
        int rowNumber = 0;
        for (Pot pot : selectedProject.getPots()) {
            for (Transaction tx : pot.getTransactions()) {
                GridPane row = new GridPane();
                row.setHgap(12);
                row.setAlignment(Pos.TOP_LEFT);
                row.getStyleClass().add("transaction-row");
                
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
                dateLabel.getStyleClass().add("transaction-cell");
                dateLabel.setWrapText(true);
                
                Label nameLabel = new Label(tx.getTransactionName());
                nameLabel.getStyleClass().add("transaction-cell");
                nameLabel.setWrapText(true);

                Label projectLabel = new Label(selectedProject.getProjectName());
                projectLabel.getStyleClass().add("transaction-cell");
                projectLabel.setWrapText(true);
                
                Label potNameLabel = new Label(pot.getPotName());
                potNameLabel.getStyleClass().add("transaction-cell");
                potNameLabel.setWrapText(true);
                
                Label paidByLabel = new Label(tx.getPaidBy());
                paidByLabel.getStyleClass().add("transaction-cell");
                paidByLabel.setWrapText(true);
                
                Label noteLabel = new Label(tx.getNote() != null && !tx.getNote().isEmpty() ? tx.getNote() : "-");
                noteLabel.getStyleClass().add("transaction-cell");
                noteLabel.setWrapText(true);
                
                Label amountLabel = new Label(formatMoney(tx.getAmount()));
                amountLabel.getStyleClass().add("transaction-amount");
                
                VBox proofBox = new VBox();
                proofBox.setAlignment(Pos.TOP_LEFT);
                if (tx.getProofPath() != null && !tx.getProofPath().isEmpty()) {
                    Button viewProofBtn = new Button("View");
                    viewProofBtn.getStyleClass().add("proof-button");
                    viewProofBtn.setOnAction(e -> handleViewProof(tx.getProofPath()));
                    proofBox.getChildren().add(viewProofBtn);
                } else {
                    Label noProofLabel = new Label("-");
                    noProofLabel.getStyleClass().add("muted-table-cell");
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
        NavigationUtils.goToProjects(projectNameLabel);
    }

    @FXML
    private void handleGoToTransactions() {
        NavigationUtils.goToTransactions(projectNameLabel);
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
    private void handleEditMenu() {
        if (selectedProject == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Project");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(selectedProject.getProjectName());
        TextField allocatedField = new TextField(MoneyInputFormatter.format(selectedProject.getAllocatedAmount()));
        MoneyInputFormatter.attach(allocatedField);

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
        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String newName = nameField.getText();
                double newAllocated;
                try {
                    newAllocated = MoneyInputFormatter.parse(allocatedField);
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
                club = ClubFinanceService.deleteProject(club, selectedProject.getProjectId());
                NavigationUtils.goToProjects(projectNameLabel);
            } catch (SQLException e) {
                if (createPotErrorLabel != null) {
                    createPotErrorLabel.setText("Failed to delete project.");
                }
                e.printStackTrace();
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
            amount = MoneyInputFormatter.parse(newPotAllocatedField);
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
            amount = MoneyInputFormatter.parse(transactionAmountField);
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
        return MoneyFormatter.formatVnd(amount);
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
        DialogUtils.style(confirm);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    club = ClubFinanceService.deletePot(club, potId);
                    selectedProject = club.findProjectById(selectedProject.getProjectId());
                    refreshPage();
                } catch (SQLException e) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "Failed to delete pot.");
                    DialogUtils.style(error);
                    error.showAndWait();
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleEditPot(Pot pot) {
        if (pot == null || selectedProject == null) {
            showPotError("Pot data is not loaded.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Pot");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(pot.getPotName());
        TextField allocatedField = new TextField(MoneyInputFormatter.format(pot.getAllocatedAmount()));
        MoneyInputFormatter.attach(allocatedField);
        Label errorLabel = new Label("");
        errorLabel.getStyleClass().add("inline-error");

        grid.add(new Label("Pot name"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Allocated amount"), 0, 1);
        grid.add(allocatedField, 1, 1);
        grid.add(errorLabel, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> dialogButton);
        DialogUtils.style(dialog);

        dialog.showAndWait().ifPresent(button -> {
            if (button != ButtonType.OK) {
                return;
            }

            String newName = nameField.getText();
            if (newName == null || newName.trim().isEmpty()) {
                showPotError("Pot name is required.");
                return;
            }

            double newAllocated;
            try {
                newAllocated = MoneyInputFormatter.parse(allocatedField);
            } catch (NumberFormatException e) {
                showPotError("Allocated amount must be a number.");
                return;
            }

            if (newAllocated < pot.getTotalSpent()) {
                showPotError("Allocated cannot be less than already spent: " + formatMoney(pot.getTotalSpent()));
                return;
            }

            double allocatedToOtherPots = selectedProject.getTotalAllocatedToPots() - pot.getAllocatedAmount();
            if (allocatedToOtherPots + Math.max(0.0, newAllocated) > selectedProject.getAllocatedAmount()) {
                showPotError("Not enough project allocation available for this pot.");
                return;
            }

            try {
                club = ClubFinanceService.updatePot(club, pot.getPotId(), newName.trim(), newAllocated);
                selectedProject = club.findProjectById(selectedProject.getProjectId());
                refreshPage();
            } catch (SQLException e) {
                showPotError("Failed to update pot.");
                e.printStackTrace();
            }
        });
    }

    private void showPotError(String message) {
        if (createPotErrorLabel != null) {
            createPotErrorLabel.setText(message);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            DialogUtils.style(alert);
            alert.showAndWait();
        }
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
