package oop.mony.utils;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import oop.mony.Application;

import java.io.File;
import java.net.URL;

public final class DialogUtils {
    private DialogUtils() {
    }

    public static void style(Dialog<?> dialog) {
        if (dialog == null) {
            return;
        }

        DialogPane pane = dialog.getDialogPane();
        pane.setGraphic(null);
        URL stylesheet = Application.class.getResource("styles.css");
        if (stylesheet != null && !pane.getStylesheets().contains(stylesheet.toExternalForm())) {
            pane.getStylesheets().add(stylesheet.toExternalForm());
        }
        if (!pane.getStyleClass().contains("mony-dialog")) {
            pane.getStyleClass().add("mony-dialog");
        }

        boolean dangerDialog = dialog.getTitle() != null
                && dialog.getTitle().toLowerCase().contains("delete");

        for (ButtonType buttonType : pane.getButtonTypes()) {
            Button button = (Button) pane.lookupButton(buttonType);
            if (button == null) {
                continue;
            }

            ButtonBar.ButtonData buttonData = buttonType.getButtonData();
            button.getStyleClass().removeAll("dialog-primary-button", "dialog-danger-button", "dialog-secondary-button");
            if (buttonData == ButtonBar.ButtonData.OK_DONE || buttonData == ButtonBar.ButtonData.YES) {
                button.getStyleClass().add(dangerDialog && buttonData == ButtonBar.ButtonData.YES
                        ? "dialog-danger-button"
                        : "dialog-primary-button");
            } else {
                button.getStyleClass().add("dialog-secondary-button");
            }
        }
    }

    public static void showProofImage(String proofPath) {
        if (proofPath == null || proofPath.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No proof image available.");
            style(alert);
            alert.showAndWait();
            return;
        }
        try {
            File file = new File(proofPath);
            if (!file.exists()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Proof image file not found.");
                style(alert);
                alert.showAndWait();
                return;
            }
            Stage proofStage = new Stage();
            proofStage.setTitle("Proof Image");
            Image image = new Image(file.toURI().toString());
            double maxWidth = Screen.getPrimary().getVisualBounds().getWidth() * 0.85;
            double maxHeight = Screen.getPrimary().getVisualBounds().getHeight() * 0.85;
            double sceneWidth = Math.min(maxWidth, Math.max(420, image.getWidth() + 24));
            double sceneHeight = Math.min(maxHeight, Math.max(320, image.getHeight() + 24));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(sceneWidth - 24);
            imageView.setFitHeight(sceneHeight - 24);
            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            proofStage.setMaxWidth(maxWidth);
            proofStage.setMaxHeight(maxHeight);
            proofStage.setScene(new Scene(scrollPane, sceneWidth, sceneHeight));
            proofStage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open proof image.");
            style(alert);
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}
