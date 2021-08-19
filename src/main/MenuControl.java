package main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MenuControl implements Initializable {
    @FXML private Button mainMenu;
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

    @FXML private Label header1;
    @FXML private Label header2;
    @FXML private Label header3;
    @FXML private Label header4;
    @FXML private Label header5;
    @FXML private Label header6;
    @FXML private Label header7;
    @FXML private Label header8;
    @FXML private Label header9;
    @FXML private Label header10;

    private ArrayList<Button> buttons = new ArrayList<>();
    private ArrayList<Label> headers = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainMenu.setOnAction(e -> menuHome(e, 0));
        addNodeToList();

        for (int i = 0; i < buttons.size(); i++){
            int categoryIndex = i;

            buttons.get(i).setOnAction(e ->{
                menuHome(e, categoryIndex);
            });
        }
    }

    public void menuHome(ActionEvent event, int idx){
        SceneSwitch ss = new SceneSwitch();
        ss.menuHome(event, idx);
    }

    private void addNodeToList(){
        for (Button b : Arrays.asList(button1, button2, button3, button4, button5, button6, button7, button8, button9, button10)) {
            buttons.add(b);
        }

        for (Label l : Arrays.asList(header1, header2, header3, header4, header5, header6, header7, header8, header9, header10)) {
            headers.add(l);
        }
    }
}
