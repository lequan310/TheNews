package main;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SceneHandler {
    private Parent root;
    @FXML private AnchorPane anchorPane;

    protected final ExecutorService es = Executors.newCachedThreadPool();
    protected boolean moving, resizeLeft, resizeRight, resizeUp, resizeDown, resizing = false;
    protected double x, y;

    public SceneHandler() {}

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
        height *= 0.85; width = height * ratio;

        Scene scene = new Scene(loader.load(), width, height);
        scene.setOnKeyPressed(new MenuHandler(controller));
        scene.setFill(Color.valueOf("#1f1f1f"));
        return scene;
    }

    // Loading Categories scene and assigning controller
    public void menuCategories(int categoryIndex) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/Categories.fxml"));
            CategoryController controller = new CategoryController(categoryIndex);
            loader.setController(controller);

            es.shutdown();
            root = loader.load();
            root.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) controller.menuHome(categoryIndex);
            });
            anchorPane.getScene().setRoot(root);
            root.requestFocus();
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

            es.shutdown();
            root = loader.load();
            root.setOnKeyPressed(new MenuHandler(controller));
            anchorPane.getScene().setRoot(root);
            root.requestFocus();
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
            KeyCombination reload = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
            loader.setController(controller);

            es.shutdown();
            root = loader.load();
            root.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.LEFT)
                    controller.previousArticle(); // Left Arrow = previous article
                else if (event.getCode() == KeyCode.RIGHT)
                    controller.nextArticle(); // Right Arrow = next article
                else if (event.getCode() == KeyCode.F5 || reload.match(event))
                    controller.readArticle(); // F5 or Ctrl + R
                else if (event.getCode() == KeyCode.ESCAPE)  // Escape to return to Menu
                    controller.menuHome(categoryIndex);
            });
            anchorPane.getScene().setRoot(root);
            root.requestFocus();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    // Title bar functions
    @FXML protected void dragged(MouseEvent event) {
        if (!resizing) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setFullScreen(false);
            stage.setX(event.getScreenX() - x);
            stage.setY(event.getScreenY() - y);
            moving = true;
        }
    }

    @FXML protected void update(MouseEvent event) {
        x = event.getSceneX();
        y = event.getSceneY();
    }

    @FXML protected void min(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML protected void max(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setFullScreenExitHint("");
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML protected void close(MouseEvent event) {
        es.shutdown();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    // Function for resizing windows
    @FXML protected void checkBound(MouseEvent event) {
        if (!resizing && !moving) {
            final int LIMIT = 3;
            x = event.getSceneX();
            y = event.getSceneY();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (!stage.isFullScreen()) {
                // Resize left
                if (x <= LIMIT) {
                    resizeLeft = true; resizeRight = false; resizeUp = false; resizeDown = false;
                    anchorPane.setCursor(javafx.scene.Cursor.H_RESIZE);
                }
                // Resize up
                else if (y <= LIMIT && x < anchorPane.getWidth() - 50){
                    resizeLeft = false; resizeRight = false; resizeUp = true; resizeDown = false;
                    anchorPane.setCursor(javafx.scene.Cursor.V_RESIZE);
                }
                // Resize right
                else if (x >= anchorPane.getWidth() - LIMIT && y > 30) {
                    resizeLeft = false; resizeRight = true; resizeUp = false; resizeDown = false;
                    anchorPane.setCursor(javafx.scene.Cursor.H_RESIZE);
                }
                // Resize down
                else if (y >= anchorPane.getHeight() - LIMIT) {
                    resizeLeft = false; resizeRight = false; resizeUp = false; resizeDown = true;
                    anchorPane.setCursor(javafx.scene.Cursor.V_RESIZE);
                }
                else {
                    resizeLeft = false; resizeRight = false; resizeUp = false; resizeDown = false;
                    anchorPane.setCursor(Cursor.DEFAULT);
                }
            }
        }
    }

    @FXML protected void resize(MouseEvent event) {
        double deltaX = 0, deltaY = 0;
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        if (resizeLeft || resizeRight) {
            resizing = true;
            deltaX = resizeLeft ? event.getSceneX() - x : x - event.getSceneX();
            x = event.getSceneX();

            anchorPane.resize(anchorPane.getWidth() - deltaX, anchorPane.getHeight() - deltaY);
            stage.setWidth(anchorPane.getWidth());

            if (resizeLeft) {
                anchorPane.relocate(x, 0);
                stage.setX(event.getScreenX());
            }
        }
        else if (resizeUp || resizeDown) {
            resizing = true;
            deltaY = resizeUp ? event.getSceneY() - y : y - event.getSceneY();
            y = event.getSceneY();

            anchorPane.resize(anchorPane.getWidth() - deltaX, anchorPane.getHeight() - deltaY);
            stage.setHeight(anchorPane.getHeight());

            if (resizeUp) {
                anchorPane.relocate(0, y);
                stage.setY(event.getScreenY());
            }
        }
    }

    @FXML protected void stopResize() {
        resizing = false; moving = false;
        anchorPane.setCursor(Cursor.DEFAULT);
    }
}