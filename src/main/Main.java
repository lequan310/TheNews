package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Scene scene = new SceneSwitch().loadMenuScene(0);

        scene.getStylesheets().add("css/style.css");
        stage.setTitle("News Application");
        stage.getIcons().add(new Image("image/book.jpg"));
        stage.setScene(scene);
        //stage.setMaximized(true);
        stage.setMinHeight(800);
        stage.setMinWidth(600);
        stage.initStyle(StageStyle.UNDECORATED);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
