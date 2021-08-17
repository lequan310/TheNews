package main;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.News;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    @FXML private Label categoryLabel;
    @FXML private Label header;
    @FXML private Label header1;
    @FXML private Label header2;
    @FXML private Label header3;
    @FXML private Label header4;
    @FXML private Label header5;
    @FXML private Label header6;
    @FXML private Label header7;
    @FXML private Label header8;
    @FXML private Label header9;

    @FXML private ImageView image;
    @FXML private ImageView image1;
    @FXML private ImageView image2;
    @FXML private ImageView image3;
    @FXML private ImageView image4;
    @FXML private ImageView image5;
    @FXML private ImageView image6;
    @FXML private ImageView image7;
    @FXML private ImageView image8;
    @FXML private ImageView image9;

    @FXML private Button button;
    @FXML private Button button1;
    @FXML private Button button2;
    @FXML private Button button3;
    @FXML private Button button4;
    @FXML private Button button5;
    @FXML private Button button6;
    @FXML private Button button7;
    @FXML private Button button8;
    @FXML private Button button9;

    @FXML private Button page1;
    @FXML private Button page2;
    @FXML private Button page3;
    @FXML private Button page4;
    @FXML private Button page5;

    private ArrayList<ImageView> images = new ArrayList<>();
    private ArrayList<Label> labels = new ArrayList<>();
    private ArrayList<Button> buttons = new ArrayList<>();
    private ArrayList<Button> pages = new ArrayList<>();
    private ArrayList<Item> items;
    private NewsController newsController;

    private String category = "";
    private int categoryIndex = 0;
    private final int itemsPerPage = 10;

    public void setCategoryIndex(int index) {
        categoryIndex = index;
    }

    public void setCategoryLabel(String s){
        category = s;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        categoryLabel.setText(category);
        newsController = new NewsController();
        items = newsController.getItems();

        addNodeToList();
        changePage(0);

        for (int i = 0; i < pages.size(); i++){
            int idx = i;

            pages.get(i).setOnAction(e -> {
                changePage(idx);
            });
        }
    }

    private void addNodeToList(){
        for (Label l : Arrays.asList(header, header1, header2, header3, header4, header5, header6, header7, header8, header9)) {
            labels.add(l);
        }

        for (Button b : Arrays.asList(button, button1, button2, button3, button4, button5, button6, button7, button8, button9)) {
            buttons.add(b);
        }

        for (Button b : Arrays.asList(page1, page2, page3, page4, page5)) {
            pages.add(b);
        }

        for (ImageView i : Arrays.asList(image, image1, image2, image3, image4, image5, image6, image7, image8, image9)){
            images.add(i);
        }
    }

    private void changePage(int page){
        for (int i = 0; i < itemsPerPage; i++){
            int idx = i + (page * itemsPerPage);

            try{
                labels.get(i).setText(items.get(idx).getTitle());
                buttons.get(i).setDisable(false);
                buttons.get(i).setOnAction(e -> {
                    article(e, items.get(idx));
                });

                if (!items.get(idx).getImgSrc().equals("")){
                    images.get(i).setImage(new Image(items.get(idx).getImgSrc()));
                }
            }
            catch (IndexOutOfBoundsException e){
                labels.get(i).setText("Empty");
                buttons.get(i).setDisable(true);
                images.get(i).setImage(null);
            }
        }
    }

    private Stage stage;
    private Scene scene;
    private Parent root;

    public void menuCategories(ActionEvent event) {
        try{
            root = FXMLLoader.load(getClass().getResource("Categories.fxml"));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void article(ActionEvent event, Item item) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("NewsTemplate.fxml"));

            ArticleController articleController = new ArticleController();
            articleController.setItem(item);

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
