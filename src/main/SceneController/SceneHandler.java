/*
  RMIT University Vietnam
  Course: INTE2512 Object-Oriented Programming
  Semester: 2021B
  Assessment: Final Project
  Created date: 01/08/2021
  Author: Thai Manh Phi, s3878070
  Last modified date: 17/09/2021
  Author: Thai Manh Phi, s3878070
  Acknowledgement:
  https://rmit.instructure.com/courses/88207/pages/w6-whats-happening-this-week?module_item_id=3237097
  https://rmit.instructure.com/courses/88207/pages/w8-whats-happening-this-week?module_item_id=3237104
  https://rmit.instructure.com/courses/88207/pages/w9-whats-happening-this-week?module_item_id=3237110
*/
package main.SceneController;

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
import main.Model.Item;
import main.Storage.Storage;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class SceneHandler {
    private Parent root;
    @FXML private AnchorPane anchorPane;

    protected boolean moving, resizeLeft, resizeRight, resizeUp, resizeDown, resizing = false;
    protected double x, y;

    public SceneHandler() {}

    //Changing page in main scene
    private void menuHandler(KeyEvent keyEvent, MenuController controller) {
        switch (keyEvent.getCode()) {
            case DIGIT1, NUMPAD1 -> controller.changePage(0);
            case DIGIT2, NUMPAD2 -> controller.changePage(1);
            case DIGIT3, NUMPAD3 -> controller.changePage(2);
            case DIGIT4, NUMPAD4 -> controller.changePage(3);
            case DIGIT5, NUMPAD5 -> controller.changePage(4);
        }
    }

    // Creating Main Menu scene and assigning controller
    public Scene loadMenuScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
        MenuController controller = new MenuController(0, true);
        loader.setController(controller);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        double height = ge.getMaximumWindowBounds().height, width = ge.getMaximumWindowBounds().width, ratio = width / height;
        height *= 0.85; width = height * ratio;

        Scene scene = new Scene(loader.load(), width, height);
        scene.setOnKeyPressed(keyEvent -> menuHandler(keyEvent, controller));
        scene.setFill(Color.valueOf("#1f1f1f"));
        System.gc();
        return scene;
    }

    // Loading Categories scene and assigning controller
    @FXML protected void menuCategories(int categoryIndex) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Categories.fxml"));
            CategoryController controller = new CategoryController(categoryIndex);
            loader.setController(controller);

            root = loader.load();
            root.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) controller.menuHome(categoryIndex, false);
            });
            anchorPane.getScene().setRoot(root);
            root.requestFocus();
            System.gc();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    // Loading Main Menu scene and assigning controller
    @FXML protected void menuHome(int categoryIndex, boolean reload) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            MenuController controller = new MenuController(categoryIndex, reload);
            loader.setController(controller);

            root = loader.load();
            root.setOnKeyPressed(keyEvent -> menuHandler(keyEvent, controller));
            anchorPane.getScene().setRoot(root);
            root.requestFocus();
            if (Storage.getInstance().getArticles().size() > 10)
                Storage.getInstance().getArticles().clear();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    // Loading article scene and assigning controller
    @FXML protected void article(ArrayList<Item> items, int index, int categoryIndex){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NewsTemplate.fxml"));
            ArticleController controller = new ArticleController(items, index, categoryIndex);
            KeyCombination reload = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
            loader.setController(controller);

            root = loader.load();
            root.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.LEFT)
                    controller.previousArticle(); // Left Arrow = previous article
                else if (event.getCode() == KeyCode.RIGHT)
                    controller.nextArticle(); // Right Arrow = next article
                else if (event.getCode() == KeyCode.F5 || reload.match(event))
                    controller.readArticle(); // F5 or Ctrl + R
                else if (event.getCode() == KeyCode.ESCAPE)  // Escape to return to Menu
                    controller.menuHome(categoryIndex, false);
            });
            anchorPane.getScene().setRoot(root);
            root.requestFocus();
            System.gc();
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
        Storage.getInstance().clearAll();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    //Function for checking Windows Bound
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

    // Function for resizing windows
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

    // Function to stop resizing windows
    @FXML protected void stopResize() {
        resizing = false; moving = false;
        anchorPane.setCursor(Cursor.DEFAULT);
    }
}