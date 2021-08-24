package main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {
    @FXML private AnchorPane anchorPane;
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

    private final ArrayList<Button> buttons = new ArrayList<>();
    private double x, y;
    private final int previousCategoryIndex;

    public CategoryController(int categoryIndex) {
        this.previousCategoryIndex = categoryIndex;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainMenu.setOnAction(e -> menuHome(previousCategoryIndex));
        addNodeToList();

        for (int i = 0; i < buttons.size(); i++){
            int categoryIndex = i;

            buttons.get(i).setOnAction(e -> menuHome(categoryIndex));
        }
    }

    public void menuHome(int idx){
        new SceneSwitch(anchorPane).menuHome(idx);
    }

    private void addNodeToList(){
        buttons.addAll(Arrays.asList(button1, button2, button3, button4, button5, button6, button7, button8, button9, button10));
    }

    @FXML private void dragged(MouseEvent event) {
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - x);
        stage.setY(event.getScreenY() - y);
    }

    @FXML private void pressed(MouseEvent event) {
        x = event.getSceneX();
        y = event.getSceneY();
    }

    @FXML private void min(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML private void max(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML private void close(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
