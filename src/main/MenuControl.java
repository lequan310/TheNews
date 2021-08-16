package main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MenuControl implements Initializable {
    @FXML private Button button1;
    @FXML private Button button2;
    @FXML private Button button3;
    @FXML private Button button4;
    @FXML private Button button5;
    @FXML private Button button6;
    @FXML private Button button7;
    @FXML private Button button8;
    @FXML private Button button9;
    @FXML private Button button10;

    private ArrayList<Button> buttons = new ArrayList<>();

    private Stage stage;
    private Scene scene;
    private Parent root;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addNodeToList();

        for (int i = 0; i < buttons.size(); i++){
            int categoryIndex = i;

            buttons.get(i).setOnAction(e ->{
                menuHome(e, categoryIndex);
            });
        }
    }

    public void menuHome1(ActionEvent event) throws IOException {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home1.fxml"));
            Controller controller = new Controller();
            controller.setCategoryIndex(0);
            loader.setController(controller);

            root = loader.load();
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void menuHome(ActionEvent event, int idx){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home1.fxml"));
            Controller controller = new Controller();
            controller.setCategoryIndex(idx);
            loader.setController(controller);

            root = loader.load();
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private void addNodeToList(){
        for (Button b : Arrays.asList(button1, button2, button3, button4, button5, button6, button7, button8, button9, button10)) {
            buttons.add(b);
        }
    }
}