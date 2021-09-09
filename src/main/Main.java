package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.SceneController.SceneHandler;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Scene scene = new SceneHandler().loadMenuScene();

        // Adding css styles, setting app icons and titles
        scene.getStylesheets().add("css/style.css");
        
        //Setting stage title
        stage.setTitle("News Application");
        
        //Setting stage icon
        stage.getIcons().add(new Image("image/icon.png"));
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
