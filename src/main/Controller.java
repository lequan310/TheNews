package main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
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

    @FXML private Label time;
    @FXML private Label time1;
    @FXML private Label time2;
    @FXML private Label time3;
    @FXML private Label time4;
    @FXML private Label time5;
    @FXML private Label time6;
    @FXML private Label time7;
    @FXML private Label time8;
    @FXML private Label time9;

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

    @FXML private ImageView icon;
    @FXML private ImageView icon1;
    @FXML private ImageView icon2;
    @FXML private ImageView icon3;
    @FXML private ImageView icon4;
    @FXML private ImageView icon5;
    @FXML private ImageView icon6;
    @FXML private ImageView icon7;
    @FXML private ImageView icon8;
    @FXML private ImageView icon9;

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
    @FXML private AnchorPane anchorPane;
    @FXML private ProgressBar pb;

    private ArrayList<ImageView> images = new ArrayList<>();
    private ArrayList<ImageView> icons = new ArrayList<>();
    private ArrayList<Label> timeLabels = new ArrayList<>();
    private ArrayList<Label> labels = new ArrayList<>();
    private ArrayList<Button> buttons = new ArrayList<>();
    private ArrayList<Button> pages = new ArrayList<>();
    private ArrayList<Item> items;

    private String[] categories = {"NEW", "COVID", "POLITICS", "BUSINESS", "TECHNOLOGY", "HEALTH", "SPORTS", "ENTERTAINMENT", "WORLD", "OTHERS"};
    private int categoryIndex = 0, currentPage = 1;

    public void setCategoryIndex(int index) {
        categoryIndex = index;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Multi-threading
        try {
            NewsController newsController = new NewsController(categoryIndex);
            Thread load = new Thread(newsController);

            load.start();
            load.join();
            loadAfterBar(newsController);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // No multi-threading
        /*NewsController newsController = new NewsController(categoryIndex);
        items = newsController.getItems();
        loadAfterBar(newsController);*/
    }

    private void loadAfterBar(NewsController newsController){
        items = newsController.getItems();

        categoryLabel.setText(categories[categoryIndex]);
        addNodeToList();
        changePage(0);

        for (int i = 0; i < pages.size(); i++){
            int idx = i;
            pages.get(i).setOnAction(e -> changePage(idx));
        }
    }

    private void addNodeToList(){
        labels.addAll(Arrays.asList(header, header1, header2, header3, header4, header5, header6, header7, header8, header9));
        buttons.addAll(Arrays.asList(button, button1, button2, button3, button4, button5, button6, button7, button8, button9));
        pages.addAll(Arrays.asList(page1, page2, page3, page4, page5));
        images.addAll(Arrays.asList(image, image1, image2, image3, image4, image5, image6, image7, image8, image9));
        icons.addAll(Arrays.asList(icon, icon1, icon2, icon3, icon4, icon5, icon6, icon7, icon8, icon9));
        timeLabels.addAll(Arrays.asList(time, time1, time2, time3, time4, time5, time6, time7, time8, time9));
    }

    public void changePage(int page){
        long start = System.currentTimeMillis();
        if (currentPage != page){
            currentPage = page;

            final int ITEMCOUNT = 10;
            for (int i = 0; i < ITEMCOUNT; i++){
                int idx = i + (page * ITEMCOUNT);
                int current = i;

                try{
                    labels.get(i).setText(items.get(idx).getTitle());
                    timeLabels.get(i).setText(items.get(idx).durationToString());
                    buttons.get(i).setDisable(false);
                    buttons.get(i).setOnAction(e -> article(e, idx));

                    switch (items.get(idx).getSource()){
                        case VE:
                            icons.get(i).setImage(new Image("/image/iconVE.png")); break;
                        case TT:
                            icons.get(i).setImage(new Image("/image/iconTT.png")); break;
                        case TN:
                            icons.get(i).setImage(new Image("/image/iconTN.png")); break;
                        case ZING:
                            icons.get(i).setImage(new Image("/image/iconZING.png")); break;
                        case ND:
                            icons.get(i).setImage(new Image("/image/iconND.png")); break;
                    }

                    if (!items.get(idx).getImgSrc().equals("")){
                        images.get(i).setImage(new Image(items.get(idx).getImgSrc()));
                    }else
                        images.get(i).setImage(null);
                }
                catch (IndexOutOfBoundsException e){
                    labels.get(i).setText("Empty");
                    buttons.get(i).setDisable(true);
                    images.get(i).setImage(null);
                    timeLabels.get(i).setText("Not available");
                    icons.get(i).setImage(null);
                }

                /*new Thread(() -> {
                    try{
                        labels.get(current).setText(items.get(idx).getTitle());
                        timeLabels.get(current).setText(items.get(idx).durationToString());
                        buttons.get(current).setDisable(false);
                        buttons.get(current).setOnAction(e -> article(e, idx));

                        switch (items.get(idx).getSource()){
                            case VE:
                                icons.get(current).setImage(new Image("/image/iconVE.png")); break;
                            case TT:
                                icons.get(current).setImage(new Image("/image/iconTT.png")); break;
                            case TN:
                                icons.get(current).setImage(new Image("/image/iconTN.png")); break;
                            case ZING:
                                icons.get(current).setImage(new Image("/image/iconZING.png")); break;
                            case ND:
                                icons.get(current).setImage(new Image("/image/iconND.png")); break;
                        }

                        if (!items.get(idx).getImgSrc().equals("")){
                            images.get(current).setImage(new Image(items.get(idx).getImgSrc()));
                        }else
                            images.get(current).setImage(null);
                    }
                    catch (IndexOutOfBoundsException e){
                        labels.get(current).setText("Empty");
                        buttons.get(current).setDisable(true);
                        images.get(current).setImage(null);
                        timeLabels.get(current).setText("Not available");
                        icons.get(current).setImage(null);
                    }
                }).start();*/
            }
        }
        System.out.println("Change page: " + (System.currentTimeMillis() - start) + " ms");
    }

    public void menuCategories(ActionEvent event) {
        new SceneSwitch().menuCategories(event);
    }

    public void article(ActionEvent event, int index) {
        new SceneSwitch().article(event, items, index);
        System.out.println(items.get(index).getLink());
    }
}
