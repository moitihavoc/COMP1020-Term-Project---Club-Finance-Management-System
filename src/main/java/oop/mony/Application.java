package oop.mony;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    private static final int MIN_WINDOW_WIDTH = 900;
    private static final int MIN_WINDOW_HEIGHT = 600;
    private static final int INITIAL_WINDOW_WIDTH = 1200;
    private static final int INITIAL_WINDOW_HEIGHT = 800;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Application.class.getResource("login.fxml"));
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double initialWidth = Math.min(INITIAL_WINDOW_WIDTH, screenBounds.getWidth() * 0.9);
        double initialHeight = Math.min(INITIAL_WINDOW_HEIGHT, screenBounds.getHeight() * 0.9);
        Scene scene = new Scene(loader.load(), initialWidth, initialHeight);

        stage.setTitle("Mony");
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        stage.setResizable(true);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
