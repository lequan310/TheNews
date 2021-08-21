package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Scene scene = new SceneSwitch().loadMenuScene(0);

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
