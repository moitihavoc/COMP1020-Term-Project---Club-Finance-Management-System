package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import oop.mony.Session;
import oop.mony.dao.UserDAO;
import oop.mony.models.User;

import java.io.IOException;
import java.sql.SQLException;

public class ProfileController {
	private static final String OPEN_EYE_ICON = "M12 4.5 C7 4.5 2.73 7.61 1 12 C2.73 16.39 7 19.5 12 19.5 C17 19.5 21.27 16.39 23 12 C21.27 7.61 17 4.5 12 4.5 Z M12 16 C9.79 16 8 14.21 8 12 C8 9.79 9.79 8 12 8 C14.21 8 16 9.79 16 12 C16 14.21 14.21 16 12 16 Z M12 14 C13.1 14 14 13.1 14 12 C14 10.9 13.1 10 12 10 C10.9 10 10 10.9 10 12 C10 13.1 10.9 14 12 14 Z";
	private static final String HIDDEN_EYE_ICON = "M2 4.27 L4.28 6.55 L4.74 7.01 C3.08 8.26 1.79 9.98 1 12 C2.73 16.39 7 19.5 12 19.5 C13.55 19.5 15.03 19.2 16.38 18.66 L19.73 22 L21 20.73 L3.27 3 L2 4.27 Z M7.53 9.8 L9.08 11.35 C9.03 11.56 9 11.78 9 12 C9 13.66 10.34 15 12 15 C12.22 15 12.44 14.97 12.65 14.92 L14.2 16.47 C13.53 16.81 12.78 17 12 17 C9.24 17 7 14.76 7 12 C7 11.22 7.19 10.47 7.53 9.8 Z M12 4.5 C17 4.5 21.27 7.61 23 12 C22.46 13.38 21.6 14.61 20.52 15.64 L17.82 12.94 C17.94 12.63 18 12.32 18 12 C18 8.69 15.31 6 12 6 C11.68 6 11.37 6.06 11.06 6.18 L8.99 4.11 C9.95 3.72 10.96 4.5 12 4.5 Z";

	@FXML private Label sidebarUsername;
	@FXML private Label logoutButton;
	@FXML private VBox changePasswordForm;
	@FXML private PasswordField currentPasswordField;
	@FXML private TextField visibleCurrentPasswordField;
	@FXML private SVGPath currentPasswordVisibilityIcon;
	@FXML private PasswordField newPasswordField;
	@FXML private TextField visibleNewPasswordField;
	@FXML private SVGPath newPasswordVisibilityIcon;
	@FXML private PasswordField confirmNewPasswordField;
	@FXML private TextField visibleConfirmNewPasswordField;
	@FXML private SVGPath confirmNewPasswordVisibilityIcon;
	@FXML private Label changePasswordErrorLabel;

	private User currentUser;
	private boolean currentPasswordVisible;
	private boolean newPasswordVisible;
	private boolean confirmNewPasswordVisible;

	@FXML
	private void initialize() {
		visibleCurrentPasswordField.textProperty().bindBidirectional(currentPasswordField.textProperty());
		visibleNewPasswordField.textProperty().bindBidirectional(newPasswordField.textProperty());
		visibleConfirmNewPasswordField.textProperty().bindBidirectional(confirmNewPasswordField.textProperty());
		updateCurrentPasswordVisibility();
		updateNewPasswordVisibility();
		updateConfirmNewPasswordVisibility();
	}

	public void loadFromSession() {
		if (!Session.hasCurrentUser()) {
			navigateToLogin();
			return;
		}
		currentUser = Session.getCurrentUser();
		sidebarUsername.setText(currentUser.getUsername());
		showChangePasswordForm(true);
	}

	@FXML
	private void handleGoToProjects() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/projects.fxml"));
			HBox root = loader.load();
			ProjectsController controller = loader.getController();
			controller.loadFromSession();
			Stage stage = (Stage) sidebarUsername.getScene().getWindow();
			stage.getScene().setRoot(root);
		} catch (IOException e) {
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}

	@FXML
	private void handleViewProfile() {
		// already on profile
	}

	@FXML
	private void handleLogout() {
		Session.clear();
		navigateToLogin();
	}

	@FXML
	private void handleChangePassword() {
		if (changePasswordForm != null) {
			changePasswordForm.setVisible(true);
			changePasswordForm.setManaged(true);
		}
	}

	@FXML
	private void handleToggleCurrentPasswordVisibility() {
		currentPasswordVisible = !currentPasswordVisible;
		updateCurrentPasswordVisibility();
	}

	@FXML
	private void handleToggleNewPasswordVisibility() {
		newPasswordVisible = !newPasswordVisible;
		updateNewPasswordVisibility();
	}

	@FXML
	private void handleToggleConfirmNewPasswordVisibility() {
		confirmNewPasswordVisible = !confirmNewPasswordVisible;
		updateConfirmNewPasswordVisibility();
	}

	@FXML
	private void handleCancelChangePassword() {
		clearChangePasswordForm();
	}

	@FXML
	private void handleConfirmChangePassword() {
		String currentPassword = currentPasswordField.getText();
		String newPassword = newPasswordField.getText();
		String confirmPassword = confirmNewPasswordField.getText();

		if (currentPassword == null || currentPassword.isEmpty()) {
			showPasswordError("Current password is required.");
			return;
		}

		if (!currentPassword.equals(currentUser.getPassword())) {
			showPasswordError("Current password is incorrect.");
			return;
		}

		if (newPassword == null || newPassword.length() < 8) {
			showPasswordError("New password must be at least 8 characters.");
			return;
		}

		if (!newPassword.equals(confirmPassword)) {
			showPasswordError("New passwords do not match.");
			return;
		}

		try {
			boolean updated = UserDAO.updatePassword(currentUser.getUserId(), newPassword);
			if (!updated) {
				showPasswordError("Could not update password.");
				return;
			}

			currentUser = new User(currentUser.getUserId(), currentUser.getUsername(), newPassword);
			Session.setCurrentUser(currentUser);
			clearPasswordFields();
			showPasswordSuccess("Password updated successfully.");
		} catch (SQLException e) {
			e.printStackTrace();
			showPasswordError("Database error while updating password.");
		}

	}

	private void clearChangePasswordForm() {
		clearPasswordFields();
		if (changePasswordErrorLabel != null) changePasswordErrorLabel.setText("");
	}

	private void clearPasswordFields() {
		if (currentPasswordField != null) currentPasswordField.clear();
		if (newPasswordField != null) newPasswordField.clear();
		if (confirmNewPasswordField != null) confirmNewPasswordField.clear();
	}

	private void showChangePasswordForm(boolean visible) {
		if (changePasswordForm != null) {
			changePasswordForm.setVisible(visible);
			changePasswordForm.setManaged(visible);
		}
	}

	private void updateCurrentPasswordVisibility() {
		updatePasswordVisibility(currentPasswordField, visibleCurrentPasswordField,
				currentPasswordVisibilityIcon, currentPasswordVisible);
	}

	private void updateNewPasswordVisibility() {
		updatePasswordVisibility(newPasswordField, visibleNewPasswordField,
				newPasswordVisibilityIcon, newPasswordVisible);
	}

	private void updateConfirmNewPasswordVisibility() {
		updatePasswordVisibility(confirmNewPasswordField, visibleConfirmNewPasswordField,
				confirmNewPasswordVisibilityIcon, confirmNewPasswordVisible);
	}

	private void updatePasswordVisibility(PasswordField hiddenField, TextField visibleField,
										  SVGPath icon, boolean visible) {
		visibleField.setVisible(visible);
		visibleField.setManaged(visible);
		hiddenField.setVisible(!visible);
		hiddenField.setManaged(!visible);
		icon.setContent(visible ? HIDDEN_EYE_ICON : OPEN_EYE_ICON);
	}

	private void showPasswordError(String message) {
		if (changePasswordErrorLabel != null) {
			changePasswordErrorLabel.setStyle("-fx-text-fill: #e53935; -fx-font-size: 13px;");
			changePasswordErrorLabel.setText(message);
		}
	}

	private void showPasswordSuccess(String message) {
		if (changePasswordErrorLabel != null) {
			changePasswordErrorLabel.setStyle("-fx-text-fill: #299D91; -fx-font-size: 13px;");
			changePasswordErrorLabel.setText(message);
		}
	}

	private void navigateToLogin() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/login.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) sidebarUsername.getScene().getWindow();
			stage.getScene().setRoot(root);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
