package main;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;

public class SceneSwitch {
    private Stage stage;
    private Scene scene;
    private Parent root;

    private class Handler implements EventHandler<KeyEvent>{
        private Controller controller = null;

        public Handler(Controller controller){
            this.controller = controller;
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            switch (keyEvent.getCode()){
                case DIGIT1:
                    controller.changePage(0); break;
                case DIGIT2:
                    controller.changePage(1); break;
                case DIGIT3:
                    controller.changePage(2); break;
                case DIGIT4:
                    controller.changePage(3); break;
                case DIGIT5:
                    controller.changePage(4); break;
            }
        }
    }

    public Scene loadMenuScene(int index) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/home1.fxml"));
        Controller controller = new Controller();
        controller.setCategoryIndex(index);
        loader.setController(controller);

        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(new Handler(controller));
        return scene;
    }

    public void menuCategories(ActionEvent event) {
        try{
            root = FXMLLoader.load(getClass().getResource("../fxml/Categories.fxml"));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void menuHome(ActionEvent event, int idx){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/home1.fxml"));
            Controller controller = new Controller();
            controller.setCategoryIndex(idx);
            loader.setController(controller);

            root = loader.load();
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            scene.setOnKeyPressed(new Handler(controller));
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void article(ActionEvent event, ArrayList<Item> items, int index){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/NewsTemplate.fxml"));
            ArticleController articleController = new ArticleController(items, index);
            loader.setController(articleController);
            root = loader.load();

            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
