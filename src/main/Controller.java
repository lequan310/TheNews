package main;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
    @FXML private ScrollPane scrollPane;
    @FXML private ProgressBar pb;

    private final ArrayList<ImageView> images = new ArrayList<>();
    private final ArrayList<ImageView> icons = new ArrayList<>();
    private final ArrayList<Label> timeLabels = new ArrayList<>();
    private final ArrayList<Label> labels = new ArrayList<>();
    private final ArrayList<Button> buttons = new ArrayList<>();
    private final ArrayList<Button> pages = new ArrayList<>();
    private ArrayList<Item> items;

    private final String[] categories = {"NEW", "COVID", "POLITICS", "BUSINESS", "TECHNOLOGY", "HEALTH", "SPORTS", "ENTERTAINMENT", "WORLD", "OTHERS"};
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
            pb.progressProperty().bind(newsController.progressProperty());
            load.start();
            load.join();

            items = newsController.getItems();
            addNodeToList();
            loadAfterBar();

            if (items.size() == 0) {
                throwAlert();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void throwAlert(){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Poor Internet Connection");
            alert.setContentText("Please check your internet connection.");

            anchorPane.setEffect(new BoxBlur(anchorPane.getWidth(), anchorPane.getHeight(), 1));
            alert.setOnCloseRequest(dialogEvent -> anchorPane.setEffect(null));
            alert.show();
        });
    }

    private void loadAfterBar(){
        categoryLabel.setText(categories[categoryIndex]);
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

    public void changePage(int page) {
        scrollPane.setVvalue(0);

        if (currentPage != page){
            Platform.runLater(() -> {
                System.out.println("\nInitializing new items:");
                currentPage = page;

                final int ITEMCOUNT = 10;
                long start = System.currentTimeMillis();

                for (int i = 0; i < ITEMCOUNT; i++){
                    int idx = i + (page * ITEMCOUNT);

                    try{
                        labels.get(i).setText(items.get(idx).getTitle());
                        timeLabels.get(i).setText(items.get(idx).durationToString());
                        buttons.get(i).setOnAction(e -> article(idx));

                        switch (items.get(idx).getSource()) {
                            case VE -> icons.get(i).setImage(new Image("/image/iconVE.png"));
                            case TT -> icons.get(i).setImage(new Image("/image/iconTT.png"));
                            case TN -> icons.get(i).setImage(new Image("/image/iconTN.jpeg"));
                            case ZING -> icons.get(i).setImage(new Image("/image/iconZING.png"));
                            case ND -> icons.get(i).setImage(new Image("/image/iconND.png"));
                        }

                        if (!items.get(idx).getImgSrc().equals("")){
                            images.get(i).setImage(new Image(items.get(idx).getImgSrc()));
                        }else
                            images.get(i).setImage(null);

                        buttons.get(i).setDisable(false);
                    }
                    catch (IndexOutOfBoundsException e){
                        buttons.get(i).setDisable(true);
                        labels.get(i).setText("Empty");
                        images.get(i).setImage(null);
                        timeLabels.get(i).setText("Not available");
                        icons.get(i).setImage(null);
                    }
                    finally {
                        System.out.println("Item " + i + " " + (System.currentTimeMillis() - start) + " ms");
                    }
                }
            });
        }
    }

    public void menuCategories() {
        new SceneSwitch(anchorPane).menuCategories();
    }

    public void article(int index) {
        new SceneSwitch(anchorPane).article(items, index);
    }
}