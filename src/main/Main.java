package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("home1.fxml"));
        Controller controller = new Controller();
        controller.setCategoryIndex(0);
        loader.setController(controller);

        Parent root = loader.load();
        Scene scene = new Scene(root);

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
