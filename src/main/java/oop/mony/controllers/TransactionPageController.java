package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oop.mony.Session;
import oop.mony.models.Club;
import oop.mony.models.TransactionRecord;
import oop.mony.models.User;
import oop.mony.services.ClubFinanceService;

import java.io.File;
import java.io.IOException;
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
    }

    public void loadFromSession() {
        if (!Session.hasCurrentUser()) {
            navigateToLogin();
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
        transactionsTableBody.getChildren().clear();
        for (TransactionRecord r : records) {
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
            
            Label dateLabel = new Label(formatDate(r.getTransactionDate()));
            dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            dateLabel.setWrapText(true);
            
            Label nameLabel = new Label(r.getTransactionName());
            nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            nameLabel.setWrapText(true);
            
            Label projectLabel = new Label(r.getProjectName());
            projectLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            projectLabel.setWrapText(true);
            
            Label potLabel = new Label(r.getPotName());
            potLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            potLabel.setWrapText(true);
            
            Label paidByLabel = new Label(r.getPaidBy());
            paidByLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            paidByLabel.setWrapText(true);
            
            Label amountLabel = new Label(MoneyFormatter.formatVnd(r.getAmount()));
            amountLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #191919;");
            
            Label noteLabel = new Label(r.getShortNote(30));
            noteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #191919;");
            noteLabel.setWrapText(true);

            VBox proofBox = new VBox();
            proofBox.setAlignment(Pos.TOP_LEFT);
            if (r.getProofPath() != null && !r.getProofPath().isEmpty()) {
                Button viewProofBtn = new Button("View");
                viewProofBtn.setStyle("-fx-background-color: #299D91; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
                viewProofBtn.setOnAction(e -> handleViewProof(r.getProofPath()));
                proofBox.getChildren().add(viewProofBtn);
            } else {
                Label noProofLabel = new Label("-");
                noProofLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa;");
                proofBox.getChildren().add(noProofLabel);
            }
            
            row.add(dateLabel, 0, 0);
            row.add(nameLabel, 1, 0);
            row.add(projectLabel, 2, 0);
            row.add(potLabel, 3, 0);
            row.add(paidByLabel, 4, 0);
            row.add(amountLabel, 5, 0);
            row.add(noteLabel, 6, 0);
            row.add(proofBox, 7, 0);
 
            transactionsTableBody.getChildren().add(row);
        }
    }

    private Double parseOptionalAmount(TextField field) {
        if (field == null) return null;
        String t = field.getText();
        if (t == null || t.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(t.trim());
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/projects.fxml"));
            HBox root = loader.load();
            ProjectsController controller = loader.getController();
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
        Session.clear();
        navigateToLogin();
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

    private String formatDate(LocalDate date) {
        if (date == null) return "-";
        return date.toString();
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
 
