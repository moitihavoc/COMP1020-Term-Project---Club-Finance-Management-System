package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() throws IOException {
        String username =  usernameField.getText();
        String password =  passwordField.getText();
    }

    @FXML
    private void handleRegister() throws IOException {
        String username =  usernameField.getText();
        String password =  passwordField.getText();
    }
}
