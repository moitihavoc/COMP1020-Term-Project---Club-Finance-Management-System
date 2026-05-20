package oop.mony.controllers;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import oop.mony.models.Project;

public class ProjectsController {

    @FXML private Label usernameLabel;
    @FXML private Label sidebarUsername;
    @FXML private Label totalBalanceLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label remainingBalanceLabel;
    @FXML private Button createProjectBtn;
    @FXML private FlowPane projectsGrid;

    private String username;
    private double totalBalance = 0;
    private final List<Project> projects = new ArrayList<>();

    public void setUsername(String username) {
        this.username = username;
        if (usernameLabel != null) usernameLabel.setText(username);
        if (sidebarUsername != null) sidebarUsername.setText(username);
    }

    @FXML
    private void handleGoToTransactions(){

    }

    @FXML
    private void handleViewProfile(){
    }
}
