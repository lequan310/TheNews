package main;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.util.ArrayList;

public class SceneSwitch {
    private Parent root;
    private AnchorPane anchorPane;

    public SceneSwitch(AnchorPane anchorPane){
        this.anchorPane = anchorPane;
    }

    public SceneSwitch(){}

    static class MenuHandler implements EventHandler<KeyEvent> {
        private final Controller controller;

        public MenuHandler(Controller controller){
            this.controller = controller;
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            switch (keyEvent.getCode()) {
                case DIGIT1, NUMPAD1 -> controller.changePage(0);
                case DIGIT2, NUMPAD2 -> controller.changePage(1);
                case DIGIT3, NUMPAD3 -> controller.changePage(2);
                case DIGIT4, NUMPAD4 -> controller.changePage(3);
                case DIGIT5, NUMPAD5 -> controller.changePage(4);
            }
        }
    }

    static class ArticleHandler implements EventHandler<KeyEvent> {
        private final ArticleController articleController;

        public ArticleHandler(ArticleController articleController){
            this.articleController = articleController;
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            switch (keyEvent.getCode()){
                case RIGHT, CLOSE_BRACKET -> articleController.nextArticle();
                case LEFT, OPEN_BRACKET -> articleController.previousArticle();
                case F5 -> articleController.readArticle();
            }
        }
    }

    public Scene loadMenuScene(int index) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/Home.fxml"));
        Controller controller = new Controller();
        controller.setCategoryIndex(index);
        loader.setController(controller);

        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(new MenuHandler(controller));
        return scene;
    }

    public void menuCategories() {
        try{
            root = FXMLLoader.load(getClass().getResource("../fxml/Categories.fxml"));
            anchorPane.getScene().setRoot(root);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void menuHome(int idx) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/Home.fxml"));
            Controller controller = new Controller();
            controller.setCategoryIndex(idx);
            loader.setController(controller);

            root = loader.load();
            root.setOnKeyPressed(new MenuHandler(controller));
            anchorPane.getScene().setRoot(root);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void article(ArrayList<Item> items, int index, int categoryIndex){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/NewsTemplate.fxml"));
            ArticleController articleController = new ArticleController(items, index, categoryIndex);
            loader.setController(articleController);

            root = loader.load();
            root.setOnKeyPressed(new ArticleHandler(articleController));
            anchorPane.getScene().setRoot(root);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}