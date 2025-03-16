package me.image.manager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        TabPane pane = loader.<TabPane>load();

        Scene scene = new Scene(pane, 500, 300);

        stage.setTitle("Image manager");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}