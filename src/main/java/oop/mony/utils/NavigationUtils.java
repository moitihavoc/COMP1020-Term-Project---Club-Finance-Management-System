package oop.mony.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import oop.mony.Session;
import oop.mony.controllers.ChangePasswordController;
import oop.mony.controllers.DashboardController;
import oop.mony.controllers.ProjectController;
import oop.mony.controllers.TransactionPageController;

import java.io.IOException;

public final class NavigationUtils {
    private static final double SIDEBAR_WIDTH_RATIO = 0.18;
    private static final double SIDEBAR_MIN_WIDTH = 200;
    private static final double SIDEBAR_MAX_WIDTH = 260;

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

    public static boolean goToDashboard(Node source) {
        try {
            FXMLLoader loader = load("/oop/mony/dashboard.fxml");
            DashboardController controller = loader.getController();
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
            FXMLLoader loader = load("/oop/mony/project.fxml");
            ProjectController controller = loader.getController();
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

    public static boolean goToChangePassword(Node source) {
        try {
            FXMLLoader loader = load("/oop/mony/changePassword.fxml");
            ChangePasswordController controller = loader.getController();
            controller.loadFromSession();
            applyRoot(source, loader.getRoot());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void sizeSidebar(Region sidebar) {
        if (sidebar == null) {
            return;
        }

        double width = calculateSidebarWidth();
        sidebar.prefWidthProperty().unbind();
        sidebar.setMinWidth(width);
        sidebar.setPrefWidth(width);
        sidebar.setMaxWidth(width);
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

    private static double calculateSidebarWidth() {
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        return clamp(screenWidth * SIDEBAR_WIDTH_RATIO, SIDEBAR_MIN_WIDTH, SIDEBAR_MAX_WIDTH);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
