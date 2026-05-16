package oop.mony.mony;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Application.class.getResource("login.fxml"));
        Scene scene = new Scene(loader.load(), 1980, 1020);
        stage.setTitle("Mony");
        stage.setScene(scene);
        stage.show();
    }
}
