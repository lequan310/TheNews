package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Scene scene = new SceneHandler().loadMenuScene(0);

        // Adding css styles, setting app icons and titles
        scene.getStylesheets().add("css/style.css");
        stage.setTitle("News Application");
        stage.getIcons().add(new Image("image/book.jpg"));
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
