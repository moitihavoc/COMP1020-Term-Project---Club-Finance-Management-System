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
import javafx.stage.Stage;
import oop.mony.Application;

import java.io.File;
import java.net.URL;

public final class DialogUtils {
    private static final String PRIMARY_BUTTON_STYLE = "-fx-background-color: #299D91; -fx-text-fill: white; "
            + "-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 8 18 8 18; -fx-background-radius: 8;";
    private static final String DANGER_BUTTON_STYLE = "-fx-background-color: #ffebee; -fx-text-fill: #e53935; "
            + "-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 8 18 8 18; -fx-background-radius: 8;";
    private static final String SECONDARY_BUTTON_STYLE = "-fx-background-color: white; -fx-text-fill: #191919; "
            + "-fx-border-color: #d8d8d8; -fx-font-size: 13px; -fx-padding: 8 18 8 18; "
            + "-fx-border-radius: 8; -fx-background-radius: 8;";

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
            if (buttonData == ButtonBar.ButtonData.OK_DONE || buttonData == ButtonBar.ButtonData.YES) {
                button.setStyle(dangerDialog && buttonData == ButtonBar.ButtonData.YES
                        ? DANGER_BUTTON_STYLE
                        : PRIMARY_BUTTON_STYLE);
            } else {
                button.setStyle(SECONDARY_BUTTON_STYLE);
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
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(600);
            imageView.setFitHeight(600);
            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            proofStage.setScene(new Scene(scrollPane, 700, 700));
            proofStage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open proof image.");
            style(alert);
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}
