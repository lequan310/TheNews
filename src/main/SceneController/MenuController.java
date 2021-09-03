package main.SceneController;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import main.Model.Item;
import main.Model.NewsController;
import java.net.URL;
import java.util.*;

public class MenuController extends SceneHandler implements Initializable {
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
    private NewsController newsController;

    private final String[] categories = {"NEW", "COVID", "POLITICS", "BUSINESS", "TECHNOLOGY", "HEALTH", "SPORTS", "ENTERTAINMENT", "WORLD", "OTHERS"};
    private int categoryIndex, currentPage = 1;
    private final boolean reload;

    public MenuController(int categoryIndex, boolean reload) {
        this.categoryIndex = categoryIndex;
        this.reload = reload;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Create news controller to scrape news in specific category
        newsController = NewsController.getInstance();
        newsController.setCategoryIndex(categoryIndex);
        pb.progressProperty().bind(newsController.progressProperty()); // Bind loading bar to scraping progress

        addNodeToList();
        categoryButton.setDisable(true);
        categoryLabel.setText(categories[categoryIndex]);

        for (Button b : pages) {
            b.setDisable(true);
        }
        for (Button b : buttons) {
            b.setDisable(true);
        }

        if (reload) {
            try {
                // Create a news controller for menu scene to use
                Service<Void> service = new Service<>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<>() {
                            @Override
                            protected Void call() throws Exception {
                                // Start scraping articles from 5 sources
                                Thread thread = new Thread(() -> newsController.start());
                                thread.start();
                                thread.join();

                                // Get articles after sorted by published date and initialize buttons
                                items = newsController.getItems();
                                changePage(0);
                                Platform.runLater(() -> loadAfterBar());

                                // Display error if there is error message
                                if (newsController.getError().compareTo("") != 0) {
                                    throwAlert("Read Error", "Please check your Internet connection", newsController.getError());
                                }

                                return null;
                            }
                        };
                    }
                };
                service.start();
            }
            catch (Exception e) {
                throwAlert(e.getClass().getCanonicalName(), e.getMessage(), e.toString());
            }
        }
        else {
            items = newsController.getItems();
            changePage(0);
            Platform.runLater(() -> loadAfterBar());
        }
    }

    // Function to display alert
    public void throwAlert(String title, String content, String error) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(content);

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

        // Assigning function to page buttons
        for (int i = 0; i < pages.size(); i++) {
            int idx = i;
            pages.get(i).setOnAction(e -> changePage(idx));
        }

        for (Button b : pages) {
            b.setDisable(false);
        }
    }

    private void addNodeToList() {
        labels.addAll(Arrays.asList(header, header1, header2, header3, header4, header5, header6, header7, header8, header9));
        buttons.addAll(Arrays.asList(button, button1, button2, button3, button4, button5, button6, button7, button8, button9));
        pages.addAll(Arrays.asList(page1, page2, page3, page4, page5));
        images.addAll(Arrays.asList(image, image1, image2, image3, image4, image5, image6, image7, image8, image9));
        icons.addAll(Arrays.asList(icon, icon1, icon2, icon3, icon4, icon5, icon6, icon7, icon8, icon9));
        timeLabels.addAll(Arrays.asList(time, time1, time2, time3, time4, time5, time6, time7, time8, time9));
    }

    public void changePage(int page) {
        try {
            scrollPane.setVvalue(0); // Reset scroll bar

            // Change page if selected page is not the current active page
            if (currentPage != page) {
                final int ITEMCOUNT = 10;
                currentPage = page;

                // Initializing article buttons
                for (int i = 0; i < ITEMCOUNT; i++) {
                    int idx = i + (page * ITEMCOUNT);
                    int currentButton = i;

                    // If item exists
                    try {
                        buttons.get(currentButton).setOnAction(e -> article(idx, categoryIndex));
                        Platform.runLater(() -> {
                            labels.get(currentButton).setText(items.get(idx).getTitle());
                            timeLabels.get(currentButton).setText(items.get(idx).durationToString());
                            buttons.get(currentButton).setDisable(false);

                            switch (items.get(idx).getSource()) {
                                case VE -> icons.get(currentButton).setImage(new Image("/image/iconVE.png", icon.getFitWidth(), icon.getFitHeight(), true, true));
                                case TT -> icons.get(currentButton).setImage(new Image("/image/iconTT.png", icon.getFitWidth(), icon.getFitHeight(), true, true));
                                case TN -> icons.get(currentButton).setImage(new Image("/image/iconTN.jpeg", icon.getFitWidth(), icon.getFitHeight(), true, true));
                                case ZING -> icons.get(currentButton).setImage(new Image("/image/iconZING.png", icon.getFitWidth(), icon.getFitHeight(), true, true));
                                case ND -> icons.get(currentButton).setImage(new Image("/image/iconND.png", icon.getFitWidth(), icon.getFitHeight(), true, true));
                            }

                            try {
                                Image background = new Image(items.get(idx).getImgSrc(), image.getFitWidth(), image.getFitHeight(), true, true, true);
                                images.get(currentButton).setImage(background);
                            } catch (IllegalArgumentException e) {
                                images.get(currentButton).setImage(null);
                            }
                        });
                    }
                    // If no more item left
                    catch (IndexOutOfBoundsException e) {
                        Platform.runLater(() -> {
                            buttons.get(currentButton).setDisable(true);
                            labels.get(currentButton).setText("Empty");
                            images.get(currentButton).setImage(null);
                            timeLabels.get(currentButton).setText("Not available");
                            icons.get(currentButton).setImage(null);
                        });
                    }
                }
            }
        }
        catch (Exception e) {
            throwAlert(e.getClass().getCanonicalName(), e.getMessage(), e.toString());
        }
    }

    // Function to switch to article scene
    public void article(int index, int categoryIndex) {
        article(items, index, categoryIndex);
    }

    // Function to switch to category scene
    @FXML private void menuCategories() {
        menuCategories(categoryIndex);
    }
}