package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.SceneController.SceneHandler;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Scene scene = new SceneHandler().loadMenuScene();

        // Adding css styles
        scene.getStylesheets().add("css/style.css");
        
        // Setting stage title
        stage.setTitle("News Application");
        
        // Setting stage icon
        stage.getIcons().add(new Image("image/icon.png"));
        stage.setScene(scene);

        // Edit stage
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
