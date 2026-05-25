package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import oop.mony.ClubFinanceService;
import oop.mony.models.Club;
import oop.mony.models.Pot;
import oop.mony.models.Project;
import oop.mony.utils.DialogUtils;
import oop.mony.utils.MoneyUtils;

import java.sql.SQLException;
import java.util.function.Consumer;

public class ProjectPotSectionController {

    @FXML private VBox createPotForm;
    @FXML private TextField newPotNameField;
    @FXML private TextField newPotAllocatedField;
    @FXML private Label createPotErrorLabel;
    @FXML private FlowPane potsGrid;

    private Club club;
    private Project selectedProject;
    private Consumer<Club> onClubChanged;

    @FXML
    private void initialize() {
        MoneyUtils.attach(newPotAllocatedField);
    }

    public void setContext(Club club, Project selectedProject, Consumer<Club> onClubChanged) {
        this.club = club;
        this.selectedProject = selectedProject;
        this.onClubChanged = onClubChanged;
        renderPots();
    }

    private void renderPots() {
        potsGrid.getChildren().clear();
        if (selectedProject == null) {
            return;
        }

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
            amount = MoneyUtils.parse(newPotAllocatedField);
        } catch (NumberFormatException e) {
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
            Club updatedClub = ClubFinanceService.createPot(
                    club,
                    selectedProject.getProjectId(),
                    name.trim(),
                    amount
            );
            hideCreateForm();
            clearPotForm();
            notifyClubChanged(updatedClub);
        } catch (SQLException e) {
            createPotErrorLabel.setText("Failed to create pot. Try again.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelCreatePot() {
        hideCreateForm();
        clearPotForm();
    }

    private void handleDeletePot(int potId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this pot? All transactions in this pot will also be deleted.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete Pot");
        DialogUtils.style(confirm);

        confirm.showAndWait().ifPresent(button -> {
            if (button != ButtonType.YES) {
                return;
            }

            try {
                notifyClubChanged(ClubFinanceService.deletePot(club, potId));
            } catch (SQLException e) {
                showPotError("Failed to delete pot.");
                e.printStackTrace();
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
        TextField allocatedField = new TextField(MoneyUtils.format(pot.getAllocatedAmount()));
        MoneyUtils.attach(allocatedField);
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
                newAllocated = MoneyUtils.parse(allocatedField);
            } catch (NumberFormatException e) {
                showPotError("Allocated amount must be a number.");
                return;
            }

            if (newAllocated < pot.getTotalSpent()) {
                showPotError("Allocated cannot be less than already spent: "
                        + formatMoney(pot.getTotalSpent()));
                return;
            }

            double allocatedToOtherPots = selectedProject.getTotalAllocatedToPots() - pot.getAllocatedAmount();
            if (allocatedToOtherPots + Math.max(0.0, newAllocated) > selectedProject.getAllocatedAmount()) {
                showPotError("Not enough project allocation available for this pot.");
                return;
            }

            try {
                notifyClubChanged(ClubFinanceService.updatePot(club, pot.getPotId(), newName.trim(), newAllocated));
            } catch (SQLException e) {
                showPotError("Failed to update pot.");
                e.printStackTrace();
            }
        });
    }

    private double calculateSpentProgress(Pot pot) {
        if (pot == null || pot.getAllocatedAmount() <= 0) {
            return 0.0;
        }
        return Math.min(1.0, pot.getTotalSpent() / pot.getAllocatedAmount());
    }

    private String formatMoney(double amount) {
        return MoneyUtils.formatVnd(amount);
    }

    private void hideCreateForm() {
        if (createPotForm != null) {
            createPotForm.setVisible(false);
            createPotForm.setManaged(false);
        }
    }

    private void clearPotForm() {
        newPotNameField.setText("");
        newPotAllocatedField.setText("");
        createPotErrorLabel.setText("");
    }

    private void showPotError(String message) {
        if (createPotErrorLabel != null) {
            createPotErrorLabel.setText(message);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        DialogUtils.style(alert);
        alert.showAndWait();
    }

    private void notifyClubChanged(Club updatedClub) {
        if (onClubChanged != null) {
            onClubChanged.accept(updatedClub);
        }
    }
}
