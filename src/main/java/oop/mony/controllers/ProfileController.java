package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oop.mony.Session;
import oop.mony.models.User;
import oop.mony.dao.UserDAO;

import java.io.IOException;

public class ProfileController {

	@FXML private Label sidebarUsername;
	@FXML private Label logoutButton;
	@FXML private Label profileUsernameLabel;
	@FXML private VBox changePasswordForm;
	@FXML private PasswordField currentPasswordField;
	@FXML private PasswordField newPasswordField;
	@FXML private PasswordField confirmNewPasswordField;
	@FXML private Label changePasswordErrorLabel;

	private User currentUser;

	public void loadFromSession() {
		if (!Session.hasCurrentUser()) {
			navigateToLogin();
			return;
		}
		currentUser = Session.getCurrentUser();
		sidebarUsername.setText(currentUser.getUsername());
		profileUsernameLabel.setText(currentUser.getUsername());
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
	private void handleCancelChangePassword() {
		if (changePasswordForm != null) {
			changePasswordForm.setVisible(false);
			changePasswordForm.setManaged(false);
		}
		clearChangePasswordForm();
	}

	@FXML
	private void handleConfirmChangePassword() {
		// Password update not implemented in DAO; show placeholder validation
		if (currentPasswordField.getText() == null || currentPasswordField.getText().isEmpty()) {
			changePasswordErrorLabel.setText("Current password is required.");
			return;
		}
		if (newPasswordField.getText() == null || newPasswordField.getText().length() < 8) {
			changePasswordErrorLabel.setText("New password must be at least 8 characters.");
			return;
		}
		if (!newPasswordField.getText().equals(confirmNewPasswordField.getText())) {
			changePasswordErrorLabel.setText("New passwords do not match.");
			return;
		}

		if (currentPasswordField.getText().equals(currentUser.getPassword())) {
			// update the password
			try {
				boolean updated = UserDAO.updatePassword(currentUser.getUserId(), newPasswordField.getText());

				if (updated) {
					changePasswordErrorLabel.setText("Password updated successfully.");
					clearChangePasswordForm();
					changePasswordForm.setVisible(false);
					changePasswordForm.setManaged(false);
				}
				else {
					changePasswordErrorLabel.setText("Could not update password.");
				}
			} catch (Exception e) {
				e.printStackTrace();
        		changePasswordErrorLabel.setText("Database error while updating password.");
			}
			
			changePasswordErrorLabel.setText("Password is updated successfully.");
		}

	}

	private void clearChangePasswordForm() {
		if (currentPasswordField != null) currentPasswordField.clear();
		if (newPasswordField != null) newPasswordField.clear();
		if (confirmNewPasswordField != null) confirmNewPasswordField.clear();
		if (changePasswordErrorLabel != null) changePasswordErrorLabel.setText("");
	}

	private void navigateToLogin() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/login.fxml"));
			HBox root = loader.load();
			Stage stage = (Stage) sidebarUsername.getScene().getWindow();
			stage.getScene().setRoot(root);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
