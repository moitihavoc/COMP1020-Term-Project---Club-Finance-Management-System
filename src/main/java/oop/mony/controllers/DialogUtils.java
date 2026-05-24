package oop.mony.controllers;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import oop.mony.Application;

import java.net.URL;

final class DialogUtils {
    private static final String PRIMARY_BUTTON_STYLE = "-fx-background-color: #299D91; -fx-text-fill: white; "
            + "-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 8 18 8 18; -fx-background-radius: 8;";
    private static final String DANGER_BUTTON_STYLE = "-fx-background-color: #ffebee; -fx-text-fill: #e53935; "
            + "-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 8 18 8 18; -fx-background-radius: 8;";
    private static final String SECONDARY_BUTTON_STYLE = "-fx-background-color: white; -fx-text-fill: #191919; "
            + "-fx-border-color: #d8d8d8; -fx-font-size: 13px; -fx-padding: 8 18 8 18; "
            + "-fx-border-radius: 8; -fx-background-radius: 8;";

    private DialogUtils() {
    }

    static void style(Dialog<?> dialog) {
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
}
