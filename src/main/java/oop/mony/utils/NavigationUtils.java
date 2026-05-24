package oop.mony.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import oop.mony.Session;
import oop.mony.controllers.ProfileController;
import oop.mony.controllers.ProjectPageController;
import oop.mony.controllers.ProjectsController;
import oop.mony.controllers.TransactionPageController;

import java.io.IOException;

public final class NavigationUtils {
    private NavigationUtils() {
    }

    public static boolean goToLogin(Node source) {
        return setRoot(source, "/oop/mony/login.fxml");
    }

    public static boolean goToRegister(Node source) {
        return setRoot(source, "/oop/mony/register.fxml");
    }

    public static boolean logout(Node source) {
        Session.clear();
        return goToLogin(source);
    }

    public static boolean goToProjects(Node source) {
        try {
            FXMLLoader loader = load("/oop/mony/projects.fxml");
            ProjectsController controller = loader.getController();
            controller.loadFromSession();
            applyRoot(source, loader.getRoot());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean goToProjectPage(Node source, int projectId) {
        try {
            FXMLLoader loader = load("/oop/mony/projectPage.fxml");
            ProjectPageController controller = loader.getController();
            controller.loadProjectFromSession(projectId);
            applyRoot(source, loader.getRoot());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean goToTransactions(Node source) {
        try {
            FXMLLoader loader = load("/oop/mony/transactionPage.fxml");
            TransactionPageController controller = loader.getController();
            controller.loadFromSession();
            applyRoot(source, loader.getRoot());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean goToProfile(Node source) {
        try {
            FXMLLoader loader = load("/oop/mony/profilePage.fxml");
            ProfileController controller = loader.getController();
            controller.loadFromSession();
            applyRoot(source, loader.getRoot());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean setRoot(Node source, String fxmlPath) {
        try {
            FXMLLoader loader = load(fxmlPath);
            applyRoot(source, loader.getRoot());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static FXMLLoader load(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource(fxmlPath));
        loader.load();
        return loader;
    }

    private static void applyRoot(Node source, Parent root) {
        Stage stage = (Stage) source.getScene().getWindow();
        stage.getScene().setRoot(root);
    }
}
