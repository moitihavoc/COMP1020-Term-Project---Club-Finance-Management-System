package oop.mony.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import oop.mony.models.User;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    @FXML
    private void handleRegister() {
        if (usernameField.getText().isEmpty() || 
            passwordField.getText().isEmpty() || 
            confirmPasswordField.getText().isEmpty()) {
            // check if all fields have been filled.
            errorLabel.setText("Please fill in all fields.");
            return;
        }
        else if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        else if (passwordField.getText().length() < 8){
            errorLabel.setText("Password should have at least 8 characters.");
            return;
        }

        // attempts to create new user and add to database
        String username = usernameField.getText();
        String password = passwordField.getText();
        User user = new User(username, password);
        boolean created = user.createAccount();

        if (created) {
            if (errorLabel != null) errorLabel.setText("");
            if (successLabel != null) {
                successLabel.setText("Account created successfully.");
                successLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #299D91;");
            }
        } else {
            if (successLabel != null) successLabel.setText("");
            if (errorLabel != null) errorLabel.setText("Username already exists.");
        }
    }

    @FXML
    private void handleBackToLogin() throws IOException{
        try {
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/oop/mony/login.fxml"));
            StackPane loginRoot = loginLoader.load();

            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.getScene().setRoot(loginRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}