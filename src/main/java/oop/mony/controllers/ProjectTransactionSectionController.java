package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import oop.mony.ClubFinanceService;
import oop.mony.models.Club;
import oop.mony.models.Pot;
import oop.mony.models.Project;
import oop.mony.utils.DialogUtils;
import oop.mony.utils.MoneyUtils;
import oop.mony.utils.TransactionTableRenderer;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.Consumer;

public class ProjectTransactionSectionController {

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

    private Club club;
    private Project selectedProject;
    private Path selectedProofImage;
    private Consumer<Club> onClubChanged;

    @FXML
    private void initialize() {
        MoneyUtils.attach(transactionAmountField);
    }

    public void setContext(Club club, Project selectedProject, Consumer<Club> onClubChanged) {
        this.club = club;
        this.selectedProject = selectedProject;
        this.onClubChanged = onClubChanged;
        refreshPotComboBox();
        renderProjectTransactions();
    }

    private void renderProjectTransactions() {
        TransactionTableRenderer.renderProjectTransactions(
                transactionsContainer,
                selectedProject,
                MoneyUtils::formatVnd,
                this::formatDate,
                this::handleViewProof
        );
    }

    private void refreshPotComboBox() {
        potComboBox.getItems().clear();
        if (selectedProject == null) {
            return;
        }

        for (Pot pot : selectedProject.getPots()) {
            potComboBox.getItems().add(new PotOption(pot.getPotId(), pot.getPotName()));
        }
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
        File file = chooser.showOpenDialog(transactionNameField.getScene().getWindow());
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

        double amount;
        try {
            amount = MoneyUtils.parse(transactionAmountField);
        } catch (NumberFormatException e) {
            createTransactionErrorLabel.setText("Amount must be a number.");
            return;
        }

        if (amount <= 0) {
            createTransactionErrorLabel.setText("Amount must be greater than 0.");
            return;
        }

        try {
            Club updatedClub = ClubFinanceService.createTransaction(
                    club,
                    selectedPot.potId(),
                    name,
                    amount,
                    paidByField.getText(),
                    transactionDatePicker.getValue(),
                    selectedProofImage,
                    noteField.getText()
            );
            hideCreateForm();
            clearTransactionForm();
            notifyClubChanged(updatedClub);
        } catch (Exception e) {
            createTransactionErrorLabel.setText("Failed to record transaction.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelCreateTransaction() {
        hideCreateForm();
        clearTransactionForm();
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

            proofStage.setScene(new Scene(scrollPane, 700, 700));
            proofStage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open proof image.");
            DialogUtils.style(alert);
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "-";
        return date.toString();
    }

    private void hideCreateForm() {
        if (createTransactionForm != null) {
            createTransactionForm.setVisible(false);
            createTransactionForm.setManaged(false);
        }
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

    private void notifyClubChanged(Club updatedClub) {
        if (onClubChanged != null) {
            onClubChanged.accept(updatedClub);
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
