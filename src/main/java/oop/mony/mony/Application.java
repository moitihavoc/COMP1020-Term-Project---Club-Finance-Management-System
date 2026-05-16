package oop.mony.mony;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    private static final int WINDOW_WIDTH = 1440;
    private static final int WINDOW_HEIGHT = 1024;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Application.class.getResource("login.fxml"));
        Scene scene = new Scene(loader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle("Mony");
        stage.setScene(scene);
        stage.show();
    }
}
