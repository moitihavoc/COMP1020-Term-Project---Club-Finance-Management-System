package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.SVGPath;

import oop.mony.Session;
import oop.mony.dao.UserDAO;
import oop.mony.models.User;
import oop.mony.utils.NavigationUtils;

public class LoginController {
    private static final String OPEN_EYE_ICON = "M12 4.5 C7 4.5 2.73 7.61 1 12 C2.73 16.39 7 19.5 12 19.5 C17 19.5 21.27 16.39 23 12 C21.27 7.61 17 4.5 12 4.5 Z M12 16 C9.79 16 8 14.21 8 12 C8 9.79 9.79 8 12 8 C14.21 8 16 9.79 16 12 C16 14.21 14.21 16 12 16 Z M12 14 C13.1 14 14 13.1 14 12 C14 10.9 13.1 10 12 10 C10.9 10 10 10.9 10 12 C10 13.1 10.9 14 12 14 Z";
    private static final String HIDDEN_EYE_ICON = "M2 4.27 L4.28 6.55 L4.74 7.01 C3.08 8.26 1.79 9.98 1 12 C2.73 16.39 7 19.5 12 19.5 C13.55 19.5 15.03 19.2 16.38 18.66 L19.73 22 L21 20.73 L3.27 3 L2 4.27 Z M7.53 9.8 L9.08 11.35 C9.03 11.56 9 11.78 9 12 C9 13.66 10.34 15 12 15 C12.22 15 12.44 14.97 12.65 14.92 L14.2 16.47 C13.53 16.81 12.78 17 12 17 C9.24 17 7 14.76 7 12 C7 11.22 7.19 10.47 7.53 9.8 Z M12 4.5 C17 4.5 21.27 7.61 23 12 C22.46 13.38 21.6 14.61 20.52 15.64 L17.82 12.94 C17.94 12.63 18 12.32 18 12 C18 8.69 15.31 6 12 6 C11.68 6 11.37 6.06 11.06 6.18 L8.99 4.11 C9.95 3.72 10.96 4.5 12 4.5 Z";

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private SVGPath passwordVisibilityIcon;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    private boolean passwordVisible;

    @FXML
    private void initialize() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        updatePasswordVisibility();
    }

    @FXML
    private void handleTogglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        updatePasswordVisibility();
    }

    private void updatePasswordVisibility() {
        visiblePasswordField.setVisible(passwordVisible);
        visiblePasswordField.setManaged(passwordVisible);
        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
        passwordVisibilityIcon.setContent(passwordVisible ? HIDDEN_EYE_ICON : OPEN_EYE_ICON);
    }

    @FXML
    private void handleLogin() {
        /*
        log in and change to project screen if user exists; else, throw error
        */
        String username =  usernameField.getText();
        String password =  passwordField.getText();

        if (username.isEmpty() && password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        if (username.isEmpty()) {
            errorLabel.setText("Please enter your username.");
            return;
        }

        if (password.isEmpty()) {
            errorLabel.setText("Please enter your password.");
            return;
        }

            try {
                User savedUser = UserDAO.findByUsernameAndPassword(username, password);
                if (savedUser != null) {
                    Session.setCurrentUser(savedUser);
                    NavigationUtils.goToDashboard(loginButton);
                } else {
                    errorLabel.setText("Incorrect username or password.");
                }
            } catch (Exception e) {
                errorLabel.setText("Login failed. Please try again.");
                e.printStackTrace();
            }

    }

    @FXML
    private void handleRegister() {
        // move scene to Register 
        if (!NavigationUtils.goToRegister(loginButton)) {
            errorLabel.setText("Failed to load register.fxml");
        }
    }
}
