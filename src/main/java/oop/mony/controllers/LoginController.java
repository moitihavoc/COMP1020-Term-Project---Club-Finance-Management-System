package oop.mony.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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

        User user = new User(username, password);

        try {
            if (user.login()) {
                // change to project scene
                FXMLLoader projectLoader = new FXMLLoader(getClass().getResource("/oop/mony/projects.fxml"));
                HBox projectRoot = projectLoader.load();
                ProjectsController controller = projectLoader.getController();
                controller.setUsername(username);
                
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.getScene().setRoot(projectRoot);
                
            } 
            else {
                errorLabel.setText("Incorrect username or password.");
                return;
            }        
        } catch (IOException e) {
            // handle exception when projects.fxml file cant be loaded
            errorLabel.setText("Failed to load scene, check console.");
            e.printStackTrace();
        }

    }

    @FXML
    private void handleRegister() throws IOException {
        // move scene to Register 
        try {
            FXMLLoader registrationLoader = new FXMLLoader(getClass().getResource("/oop/mony/register.fxml"));
            StackPane registerRoot = registrationLoader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.getScene().setRoot(registerRoot);
        }catch (IOException e){
            errorLabel.setText("Failed to load register.fxml");
            e.printStackTrace();
        }
    }
}
