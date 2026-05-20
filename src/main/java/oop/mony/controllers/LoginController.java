package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import oop.mony.models.User;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() throws IOException {
        /*
        log in and change to project screen if user exists; else, throw error
        */
        String username =  usernameField.getText();
        String password =  passwordField.getText();
        User user = new User(username, password);

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        try {
            if (user.login()) {
                // change to project scene
                FXMLLoader projectLoader = new FXMLLoader(getClass().getResource("projects.fxml"));
                HBox projectRoot = projectLoader.load();
                ProjectsController controller = projectLoader.getController();
                controller.setUsername(username);
                
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.getScene().setRoot(projectRoot);
                
            }            
        } catch (IOException e) {
            // handle exception when projects.fxml file cant be loaded
            errorLabel.setText("Failed to load scene, check console.");
            e.printStackTrace();
        }

    }

    @FXML
    private void handleRegister() throws IOException {
        String username =  usernameField.getText();
        String password =  passwordField.getText();
    }
}
