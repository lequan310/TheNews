/*
  RMIT University Vietnam
  Course: INTE2512 Object-Oriented Programming
  Semester: 2021B
  Assessment: Final Project
  Created date: 01/08/2021
  Author: Le Minh Quan, s3877969
  Last modified date: 17/09/2021
  Author: Thai Manh Phi, s3878070
  Acknowledgement:
  https://rmit.instructure.com/courses/88207/pages/w6-whats-happening-this-week?module_item_id=3237097
  https://rmit.instructure.com/courses/88207/pages/w8-whats-happening-this-week?module_item_id=3237104
  https://rmit.instructure.com/courses/88207/pages/w9-whats-happening-this-week?module_item_id=3237110
*/
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
