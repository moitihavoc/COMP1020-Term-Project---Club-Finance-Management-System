package oop.mony.controllers;

import javafx.scene.layout.Region;
import javafx.stage.Screen;

final class SidebarSizer {
    private static final double WIDTH_RATIO = 0.18;
    private static final double MIN_WIDTH = 200;
    private static final double MAX_WIDTH = 260;

    private SidebarSizer() {
    }

    static void bindToWindow(Region sidebar) {
        if (sidebar == null) {
            return;
        }

        sidebar.prefWidthProperty().unbind();
        double width = calculateScreenBasedWidth();
        sidebar.setMinWidth(width);
        sidebar.setPrefWidth(width);
        sidebar.setMaxWidth(width);
    }

    private static double calculateScreenBasedWidth() {
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        return clamp(screenWidth * WIDTH_RATIO, MIN_WIDTH, MAX_WIDTH);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
