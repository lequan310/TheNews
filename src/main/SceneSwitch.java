package main;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class SceneSwitch {
    private Parent root;
    private final AnchorPane anchorPane;

    public SceneSwitch(AnchorPane anchorPane){
        this.anchorPane = anchorPane;
    }

    // Keyboard Handler for Main Menu scene
    static class MenuHandler implements EventHandler<KeyEvent> {
        private final MenuController controller;

        public MenuHandler(MenuController controller){
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

    // Creating Main Menu scene and assigning controller
    public Scene loadMenuScene(int index) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/Home.fxml"));
        MenuController controller = new MenuController();
        controller.setCategoryIndex(index);
        loader.setController(controller);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        double height = ge.getMaximumWindowBounds().height, width = ge.getMaximumWindowBounds().width, ratio = width / height;
        height -= 200; width = height * ratio;

        Scene scene = new Scene(loader.load(), width, height);
        scene.setOnKeyPressed(new MenuHandler(controller));
        return scene;
    }

    // Loading Categories scene and assigning controller
    public void menuCategories(int categoryIndex) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/Categories.fxml"));
            CategoryController controller = new CategoryController(categoryIndex);
            loader.setController(controller);

            root = loader.load();
            anchorPane.getScene().setRoot(root);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    // Loading Main Menu scene and assigning controller
    public void menuHome(int categoryIndex) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/Home.fxml"));
            MenuController controller = new MenuController();

            controller.setCategoryIndex(categoryIndex);
            loader.setController(controller);

            root = loader.load();
            root.setOnKeyPressed(new MenuHandler(controller));
            anchorPane.getScene().setRoot(root);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    // Loading article scene and assigning controller
    public void article(ArrayList<Item> items, int index, int categoryIndex){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/NewsTemplate.fxml"));
            ArticleController controller = new ArticleController(items, index, categoryIndex);
            KeyCombination left = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN);
            KeyCombination right = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);

            loader.setController(controller);
            root = loader.load();
            root.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (left.match(event)) controller.previousArticle(); // Control + Left Arrow = previous article
                else if (right.match(event)) controller.nextArticle(); // Control + Right Arrow = next article
                else if (event.getCode() == KeyCode.F5) controller.readArticle(); // F5 = refresh
            });
            anchorPane.getScene().setRoot(root);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}