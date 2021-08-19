package main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        SceneSwitch ss = new SceneSwitch();
        Scene scene = ss.loadMenuScene(0);

        scene.getStylesheets().add("css/style.css");
        stage.setTitle("News Application");
        stage.setResizable(true);
        stage.getIcons().add(new Image("image/book.jpg"));
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setMinHeight(700);
        stage.setMinWidth(870);
        //remove the app bar
        //stage.initStyle(StageStyle.UNDECORATED);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
