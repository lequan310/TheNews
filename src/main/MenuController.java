package main;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.net.URL;
import java.util.*;

public class MenuController implements Initializable {
    // UI components in Main Menu scene
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
    @FXML private Button categoryButton;

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
    private double x, y;

    public void setCategoryIndex(int index) {
        categoryIndex = index;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Create news controller to scrape news in specific category
        NewsController newsController = new NewsController(categoryIndex);
        pb.progressProperty().bind(newsController.progressProperty()); // Bind loading bar to scraping progress

        addNodeToList();
        categoryButton.setDisable(true);
        categoryLabel.setText(categories[categoryIndex]);

        for (Button b : pages){
            b.setDisable(true);
        }

        try {
            // Create a news controller for menu scene to use
            new Service() {
                @Override
                protected Task createTask() {
                    return new Task() {
                        @Override
                        protected Object call() throws Exception {
                            // Start scraping articles from 5 sources
                            Thread thread = new Thread(newsController);
                            thread.start();
                            thread.join();

                            // Get articles after sorted by published date and initialize buttons
                            items = newsController.getItems();
                            Platform.runLater(() -> loadAfterBar());

                            // Display error if there is error message
                            if (newsController.getError().compareTo("") != 0) {
                                throwAlert("Read Error", "Please check your Internet connection", newsController.getError());
                            }
                            return null;
                        }
                    };
                }
            }.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to display alert
    public void throwAlert(String title, String content, String error){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Read Error");
            alert.setContentText("Please check your internet connection.");

            TextArea area = new TextArea(error);
            alert.getDialogPane().setExpandableContent(area);
            anchorPane.setEffect(new BoxBlur(anchorPane.getWidth(), anchorPane.getHeight(), 1));
            alert.setOnCloseRequest(dialogEvent -> anchorPane.setEffect(null));
            alert.show();
        });
    }

    private void loadAfterBar() {
        // Setting category label and set current page to first page
        categoryButton.setDisable(false);
        changePage(0);

        // Assigning function to page buttons
        for (int i = 0; i < pages.size(); i++){
            int idx = i;
            pages.get(i).setOnAction(e -> changePage(idx));
        }

        for (Button b : pages){
            b.setDisable(false);
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
        try {
            Thread thread = new Thread(() -> {
                scrollPane.setVvalue(0); // Reset scroll bar

                // Change page if selected page is not the current active page
                if (currentPage != page){
                    final int ITEMCOUNT = 10;
                    long start = System.currentTimeMillis();
                    currentPage = page;

                    System.out.println("\nInitializing new items:");

                    // Initializing article buttons
                    for (int i = 0; i < ITEMCOUNT; i++){
                        int idx = i + (page * ITEMCOUNT);
                        int currentButton = i;

                        new Thread(() -> Platform.runLater(() -> {
                            // If item exists
                            try{
                                labels.get(currentButton).setText(items.get(idx).getTitle());
                                timeLabels.get(currentButton).setText(items.get(idx).durationToString());
                                buttons.get(currentButton).setOnAction(e -> article(idx, categoryIndex));

                                switch (items.get(idx).getSource()) {
                                    case VE -> icons.get(currentButton).setImage(new Image("/image/iconVE.png"));
                                    case TT -> icons.get(currentButton).setImage(new Image("/image/iconTT.png"));
                                    case TN -> icons.get(currentButton).setImage(new Image("/image/iconTN.jpeg"));
                                    case ZING -> icons.get(currentButton).setImage(new Image("/image/iconZING.png"));
                                    case ND -> icons.get(currentButton).setImage(new Image("/image/iconND.png"));
                                }

                                try {
                                    images.get(currentButton).setImage(new Image(items.get(idx).getImgSrc()));
                                }
                                catch (IllegalArgumentException e) {
                                    images.get(currentButton).setImage(null);
                                }

                                buttons.get(currentButton).setDisable(false);
                            }
                            // If no more item left
                            catch (IndexOutOfBoundsException e){
                                buttons.get(currentButton).setDisable(true);
                                labels.get(currentButton).setText("Empty");
                                images.get(currentButton).setImage(null);
                                timeLabels.get(currentButton).setText("Not available");
                                icons.get(currentButton).setImage(null);
                            }
                            finally {
                                System.out.println("Item " + currentButton + " " + (System.currentTimeMillis() - start) + " ms");
                            }
                        })).start();
                    }
                }
            });
            thread.start();
            thread.join();
        }
        catch (InterruptedException e) {
            throwAlert("Interrupted Exception", "", e.getMessage());
        }
    }

    // Function to switch to article scene
    public void article(int index, int categoryIndex) {
        new SceneSwitch(anchorPane).article(items, index, categoryIndex);
    }

    // Function to switch to category scene
    @FXML private void menuCategories() {
        new SceneSwitch(anchorPane).menuCategories(categoryIndex);
    }

    // Title bar functions
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
        stage.setFullScreenExitHint("");
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML private void close(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}