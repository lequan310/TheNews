package main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ArticleController extends SceneHandler implements Initializable {
    private final int WORDSIZE = 18;
    private final ArrayList<Item> items;
    private final int categoryIndex;

    @FXML private AnchorPane anchorPane;
    @FXML private FlowPane content;
    @FXML private ImageView thumbnail;
    @FXML private Label title;
    @FXML private Label timeLabel;
    @FXML private Label sourceLabel;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private ScrollPane scrollPane;
    @FXML private Pane blackPane;

    private Item item;
    private int index;

    public ArticleController(ArrayList<Item> items, int index, int categoryIndex){
        this.items = items;
        this.index = index;
        this.categoryIndex = categoryIndex;
        item = items.get(index);
    }

    // Utilities function to trim the string from start to end
    private static String extract(String line, String start, String end) {
        // Trim from left side
        int firstPos = line.indexOf(start);
        String temp = line.substring(firstPos + start.length());

        // Trim from right side
        int lastPos = temp.indexOf(end);
        temp = temp.substring(0, lastPos);
        return temp;
    }

    // Utilities Function to extract video URL from VN Express
    private static String videoVE(String urlAddress) {
        String vidURL = urlAddress.replaceFirst("d1.", "v.");
        vidURL = vidURL.replaceFirst("video/video", "video");

        if (vidURL.contains("/vne/master.m3u8")){
            String temp1 = vidURL.substring(0, vidURL.indexOf("/mp4") + 4);
            String temp2 = vidURL.substring(vidURL.indexOf("/mp4/") + 5);
            temp2 = temp2.substring(temp2.indexOf("/"));
            temp2 = temp2.replace("/vne/master.m3u8", ".mp4");
            vidURL = temp1 + temp2;
        }
        else if (vidURL.contains("index-v1-a1.m3u8")){
            vidURL = vidURL.replace("/index-v1-a1.m3u8", ".mp4");
        }

        return vidURL;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Read current article and assigning buttons functions
        readArticle();
        previousButton.setOnAction(e -> previousArticle());
        nextButton.setOnAction(e -> nextArticle());

        if (index == 0)
            previousButton.setDisable(true);

        blackPane.prefHeightProperty().bind(anchorPane.heightProperty().divide(3));
        thumbnail.fitHeightProperty().bind(blackPane.heightProperty().subtract(10));
        title.prefHeightProperty().bind(blackPane.heightProperty());
    }

    public void readArticle(){
        new Thread(() -> Platform.runLater(() -> {
            content.getChildren().clear();

            // Call read article function depends on which source
            switch (item.getSource()){
                case VE -> readArticleVE(item.getLink());
                case TT -> readArticleTT(item.getLink());
                case TN -> readArticleTN(item.getLink());
                case ZING -> readArticleZing(item.getLink());
                case ND -> readArticleND(item.getLink());
            }

            content.getChildren().addAll(createLabel("", WORDSIZE));
        })).start();

        // Initialize UI components
        scrollPane.setVvalue(0);
        title.setText(item.getTitle());
        timeLabel.setText(item.getPubDate());
        sourceLabel.setText(item.getLink());

        try {
            thumbnail.setImage(new Image(item.getImgSrc()));
        }
        catch (IllegalArgumentException e) {
            thumbnail.setImage(null);
        }
    }

    // Function to read article from VN Express
    private void readArticleVE(String urlAddress) {
        try {
            // Try to connect to item url
            Document doc = Jsoup.connect(urlAddress).timeout(10000).get();
            final int statusCode = doc.connection().response().statusCode();
            System.out.println("Status code: " + statusCode + " " + urlAddress);

            // Video article
            if (urlAddress.contains("video.vnexpress.net")) {
                // Extract video URL
                String videoURL = doc.select("script").toString();
                videoURL = extract(videoURL, "\"contentUrl\": \"", "\",");

                try {
                    content.getChildren().add(createVideoButton(videoVE(videoURL), ""));
                }
                catch (IllegalArgumentException e) {}

                // Create description, video, and author label
                Label description = createDescription(doc.select("div.lead_detail").text());
                Label author = createDescription(doc.select("p.author").text());

                content.getChildren().addAll(description, author);
            }
            // Normal article
            else {
                Element article = doc.select("article.fck_detail").first();

                // Add description to article view
                Label description = createDescription(doc.select("p.description").text());
                content.getChildren().add(description);

                // Loop through elements in main article
                checkDivVE(article, content);

                // Add author label to article view
                Label author = createDescription(article.select("p[style*=text-align:right]").text());
                if (author.getText().compareTo("") == 0)
                    author.setText(article.select("p[class*=author]").text());

                author.setAlignment(Pos.TOP_RIGHT);
                content.getChildren().add(author);
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());

            if (e instanceof IOException)
                dealException(e, item);
        }
    }

    // Function to read article from TuoiTre
    private void readArticleTT(String urlAddress){
        try {
            // Connect to article URL
            Document doc = Jsoup.connect(urlAddress).timeout(10000).get();
            final int statusCode = doc.connection().response().statusCode();
            System.out.println("Status code: " + statusCode + " " + urlAddress);

            // Extracting article contents
            Elements body = doc.select("div#mainContentDetail");
            Elements article = body.select("div#main-detail-body > *");

            // Adding description label
            Label description = createDescription(body.select("h2").text());
            content.getChildren().add(description);

            // Loop through main article
            addTT(article, content);

            // Adding author label
            if (body.select("div.author").size() > 0){
                Label author = createDescription(body.select("div.author").text());
                author.setAlignment(Pos.TOP_RIGHT);
                content.getChildren().add(author);
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());

            if (e instanceof IOException)
                dealException(e, item);
        }
    }

    // Function to read article from ThanhNien
    private void readArticleTN(String urlAddress) {
        try {
            // Connect to article URL
            Document doc = Jsoup.connect(urlAddress).timeout(10000).get();
            final int statusCode = doc.connection().response().statusCode();
            System.out.println("Status code: " + statusCode + " " + urlAddress);

            // Video article
            if (urlAddress.contains("https://thanhnien.vn/video")){
                Label label = createDescription(doc.select("div.sapo").text()); // Create description label
                Label author = createDescription(doc.select("div.details__author h4").text()); // Create author label
                author.setAlignment(Pos.TOP_RIGHT);

                // Extract video url
                String videoSrc = doc.select("div.media-player script").toString();
                videoSrc = extract(videoSrc, "src=\"", "\"");
                Label videoButton = createVideoButton(videoSrc, ""); // Create video
                content.getChildren().addAll(label, videoButton, author); // Add all created components to article view
            }
            // Normal article
            else{
                Elements body = doc.select("div[class~=.*content]");
                Elements article = doc.select("div[id=abody] > *");

                // Create Description label
                Label description = createDescription(body.select("div.sapo").text());
                content.getChildren().add(description);

                // Thumbnail image
                try {
                    Image thumbnail = new Image(body.select("div[id=contentAvatar] img").attr("src"));
                    Label tnImage = createImageLabel(thumbnail, body.select("div[id=contentAvatar] div.imgcaption").text());
                    content.getChildren().add(tnImage);
                }
                // If no thumbnail image
                catch (IllegalArgumentException e) {}

                // Loop through elements in main article
                for (Element e : article) {
                    // Add label if element is text
                    if (e.is("p")) {
                        content.getChildren().add(createLabel(e.text(), WORDSIZE));
                    }
                    // Add header label if element is header text
                    else if (e.is("h2") || e.is("h3")) {
                        content.getChildren().add(createHeader(e.text()));
                    }
                    // Call TN utilities function for div elements
                    else if (e.is("div") && e.className().compareTo("details__morenews") != 0) {
                        checkDivTN(e, content);
                    }
                }

                // Create author label
                Label author = createDescription(doc.select("div.left h4").text());
                author.setAlignment(Pos.TOP_RIGHT);
                content.getChildren().add(author);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());

            if (e instanceof IOException)
                dealException(e, item);
        }
    }

    // Function to read article from ZingNews
    private void readArticleZing(String urlAddress){
        try{
            // Connect to article URL
            Document doc = Jsoup.connect(urlAddress).timeout(10000).get();
            final int statusCode = doc.connection().response().statusCode();
            System.out.println("Status code: " + statusCode + " " + urlAddress);

            // Video article
            if (urlAddress.contains("https://zingnews.vn/video")){
                // Extract video URL and information
                Elements body = doc.select("div[id=video-featured]");
                Elements videos = body.select("video");
                Elements articles = body.select("div.video-info");

                // Creating and adding video, summary and author to article view
                Label videoButton = createVideoButton(videos.first().attr("src"), "");
                Label label = createLabel(articles.select("p.video-summary").text(), WORDSIZE);
                Label author = createDescription(articles.select("span.video-author").text());
                author.setAlignment(Pos.TOP_RIGHT);
                content.getChildren().addAll(videoButton, label, author);
            }
            // Normal article
            else{
                // Extract article components
                Elements body = doc.select("section.main");
                Elements article = doc.select("div.the-article-body > *");

                // Creating and adding description label
                Label description =createDescription(doc.select("p.the-article-summary").text());
                content.getChildren().add(description);

                // Loop through article elements
                addZing(article, content);

                // Create and add author label
                Label author = createDescription(doc.getElementsByClass("author").text());
                author.setAlignment(Pos.TOP_RIGHT);
                content.getChildren().add(author);
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());

            if (e instanceof IOException)
                dealException(e, item);
        }
    }

    // Function to read article from NhanDan
    private void readArticleND(String urlAddress) {
        try {
            // Connect to article URL
            Document doc = Jsoup.connect(urlAddress).timeout(10000).get();
            final int statusCode = doc.connection().response().statusCode();
            System.out.println("Status code: " + statusCode + " " + urlAddress);

            // Extract elements from main article
            Elements body = doc.select("div.box-content-detail");
            Elements article = doc.select("div.detail-content-body > *");

            // Create and add Thumbnail image
            try {
                Image thumb = new Image(body.select("div.box-detail-thumb img").attr("src"));
                Label thumbnail = createImageLabel(thumb, body.select("div.box-detail-thumb span").text());
                content.getChildren().add(thumbnail);
            }
            catch (IllegalArgumentException ex) {}

            // Create and add Description label
            Label description = createDescription(body.select("div.box-des-detail p").text());
            content.getChildren().add(description);

            // Loop through elements in main article
            for (Element e : article) {
                try {
                    // Create and add label if element is text
                    if (e.is("p")) {
                        if (e.select("video").size() == 0) {
                            content.getChildren().add(createLabel(e.text(), WORDSIZE));
                        }
                        else {
                            content.getChildren().add(createVideoButton(e.select("source").attr("src"), ""));
                        }
                    }
                    // Create and add image if element is image
                    else if (e.is("div") && e.attr("class").compareTo("light-img") == 0) {
                        Image image = new Image(e.select("figure").attr("data-src"));
                        content.getChildren().add(createImageLabel(image, e.select("figcaption").text()));
                    }
                    // Create and add label
                    else if (e.is("ol")) {
                        for (Element li : e.select("> *")) {
                            content.getChildren().add(createLabel(li.text(), WORDSIZE));
                        }
                    }
                    // Create and add wrapnote if element is wrapnote
                    else if (e.is("blockquote")) {
                        FlowPane pane = createWrapNote();

                        for (Element i : e.select("> *")){
                            if (i.is("p")) {
                                pane.getChildren().add(createLabel(i.text(), WORDSIZE));
                            }
                        }

                        content.getChildren().add(pane);
                    }
                }
                catch (IllegalArgumentException ex) { continue; }
            }

            // Create and add author label
            Label author = createDescription(body.select("div.box-author strong").text());
            author.setAlignment(Pos.TOP_RIGHT);
            content.getChildren().add(author);
        }
        catch (Exception e){
            System.out.println(e.getMessage());

            if (e instanceof IOException)
                dealException(e, item);
        }
    }

    private void checkDivVE(Element div, FlowPane content) {
        for (Element e : div.select("> *")) {
            // If element is text not author
            if (e.is("p") && !e.attr("style").contains("text-align:right;") && !e.attr("class").contains("author")) {
                Label label = createLabel(e.text(), WORDSIZE);

                if (e.select("strong").size() > 0)
                    label.setFont(Font.font("Roboto", FontWeight.BOLD, WORDSIZE));

                content.getChildren().add(label);
            }
            // If element is image
            else if (e.is("figure") && e.select("img").size() > 0) {
                String imageURL = e.select("img").attr("data-src");
                if (imageURL.compareTo("") == 0) imageURL = e.select("img").attr("src");

                Image image = new Image(imageURL);
                content.getChildren().add(createImageLabel(image, e.select("figcaption").text()));
            }
            // If element is either video or image
            else if (e.attr("class").contains("clearfix")) {
                if (e.select("video").size() > 0) {
                    content.getChildren().add(createVideoButton(videoVE(e.select("video").attr("src")), e.select("p").text()));
                }
                else if (e.select("img").size() > 0) {
                    String imageURL = e.select("img").attr("data-src");
                    if (imageURL.compareTo("") == 0) imageURL = e.select("img").attr("src");

                    Image image = new Image(imageURL);
                    content.getChildren().add(createImageLabel(image, e.select("p").text()));
                }
            }
            // If element is video
            else if (e.is("div") && e.select("video").size() > 0 &&
                    (e.attr("class").contains("text-align:center") || e.attr("style").contains("center"))) {
                content.getChildren().add(createVideoButton(videoVE(e.select("video").attr("src")), e.select("p").text()));
            }
            // If element is wrapnote
            else if (e.is("div") && e.attr("class").compareTo("box_brief_info") == 0) {
                FlowPane pane = createWrapNote();

                for (Element i : e.select("> *")) {
                    if (i.is("p")) {
                        pane.getChildren().add(createLabel(i.text(), WORDSIZE));
                    }
                }

                content.getChildren().add(pane);
            }
            else if (e.is("div")) {
                checkDivVE(e, content);
            }
        }
    }

    // Utilities function to read ThanhNien article
    private void checkDivTN(Element div, FlowPane content) {
        // If element has 0 children and is not an ad div
        if (div.select("> *").size() == 0 && !div.className().contains("ads") && div.hasText()){
            content.getChildren().add(createLabel(div.text(), WORDSIZE));
            return;
        }

        // Loop through div elements
        for (Element i : div.select("> *")) {
            try {
                // Recursion call if child is div
                if (i.is("div") && !i.attr("class").contains("image")) {
                    checkDivTN(i, content);
                }
                // Add text label if child is text
                else if (i.is("p")) {
                    content.getChildren().add(createLabel(i.text(), WORDSIZE));
                }
                // Add image if child is image
                else if (i.attr("class").contains("image")) {
                    Image image = new Image(i.select("img").attr("data-src"));
                    content.getChildren().add(createImageLabel(image, i.select("p").text()));
                }
                // Add video if child is video
                else if (i.is("table") && i.attr("class").compareTo("video") == 0) {
                    Label videoButton = createVideoButton(i.select("div[class=\"clearfix cms-video\"]").attr("data-video-src"), i.select("p").text());
                    content.getChildren().add(videoButton);
                }
                // Add image if child is image
                else if (i.is("figure") && i.attr("class").compareTo("picture") == 0) {
                    Image image = new Image(i.select("img").attr("data-src"));
                    content.getChildren().add(createImageLabel(image, i.select("figcaption").text()));
                }
                // Add quote table
                else if (i.is("table") && i.attr("class").compareTo("quotetable") == 0) {
                    FlowPane pane = createWrapNote();

                    Element inQuote = i.select("div.quote").first();
                    checkDivTN(inQuote, pane);

                    content.getChildren().add(pane);
                }
                // Add header if child is header
                else if (i.is("h2") || i.is("h3")) {
                    content.getChildren().add(createHeader(i.text()));
                }
                // Add text label if child is neither image nor video and has text
                else if (i.hasText()) {
                    content.getChildren().add(createLabel(div.text(), WORDSIZE));
                    break;
                }
            }
            catch (IllegalArgumentException ex) { continue; }
        }
    }

    // Utilities function to read TuoiTre main article
    private void addTT(Elements elements, FlowPane content) {
        for (Element e : elements) {
            try {
                // Add label if element is text
                if (e.is("p") && e.hasText()) {
                    Label label = createLabel(e.text(), WORDSIZE);
                    if (e.select("b").size() > 0)
                        label.setFont(Font.font("Roboto", FontWeight.BOLD, WORDSIZE));

                    content.getChildren().add(label);
                }
                // Else if element is div
                else if (e.is("div")) {
                    // Add image if element is image
                    if (e.attr("type").compareTo("Photo") == 0) {
                        Image image = new Image(e.select("img").attr("src"));
                        content.getChildren().add(createImageLabel(image, e.select("p").text()));
                    }
                    // Add video if element is video
                    else if (e.attr("type").compareTo("VideoStream") == 0) {
                        // Extract video url from data-src attribute
                        String videoSrc = e.attr("data-src");
                        videoSrc = videoSrc.substring(videoSrc.indexOf("hls"));
                        videoSrc = videoSrc.substring(0, videoSrc.indexOf(".mp4") + 4);
                        videoSrc = videoSrc.replace("&vid=", "/");
                        videoSrc = "https://" + videoSrc;

                        content.getChildren().add(createVideoButton(videoSrc, e.select("p").text()));
                    }
                    // Add wrapnote if element is wrapnote
                    else if (e.attr("type").compareTo("wrapnote") == 0) {
                        FlowPane pane = createWrapNote();

                        // Loop through elements in wrap note and add into wrapnote
                        addTT(e.select("> *"), pane);

                        content.getChildren().add(pane);
                    }
                }
            }
            catch (IllegalArgumentException ex) { continue; }
        }
    }

    // Utilities function to read ZingNews main article
    private void addZing(Elements elements, FlowPane content) {
        for (Element e : elements) {
            try {
                // Create and add label if element is text
                if (e.is("p")) {
                    content.getChildren().add(createLabel(e.text(), WORDSIZE));
                }
                // Create and add wrapnote if element is wrapnote
                else if (e.is("div") && e.attr("class").compareTo("notebox ncenter") == 0){
                    FlowPane pane = createWrapNote();
                    addZing(e.select("> *"), pane);
                    content.getChildren().add(pane);
                }
                // Create and add header label if element is header
                else if (e.is("h3") || e.is("h2")) {
                    content.getChildren().add(createHeader(e.text()));
                }
                // Create and add video if element is video
                else if (e.is("figure") && e.attr("class").contains("video")) {
                    content.getChildren().add(createVideoButton(e.attr("data-video-src"), e.select("figcaption").text()));
                }
                // Create and add images if element is image/gallery
                else if (e.is("table") && e.attr("class").contains("picture")) {
                    for (Element i : e.select("td.pic > *")) {
                        String imageURL = i.select("img").attr("data-src");
                        if (imageURL.compareTo("") == 0) imageURL = i.select("img").attr("src");

                        Image image = new Image(imageURL);
                        content.getChildren().add(createImageLabel(image, e.select("td[class=\"pCaption caption\"]").text()));
                    }
                }
                // For covid graph
                else if (e.is("div") && e.attr("class").contains("widget")) {
                    Image image = new Image(e.attr("data-src"));
                    content.getChildren().add(createImageLabel(image, ""));
                }
                // Create and add group of text
                else if (e.is("ul")  || e.is("div")) {
                    addZing(e.select("> *"), content);
                }
                else if (e.hasText() && e.is("li")) {
                    content.getChildren().add(createLabel(e.text(), WORDSIZE));
                }
            }
            catch (IllegalArgumentException ex) { continue; }
        }
    }

    // Create UI components to add to article view
    private Label createLabel(String text, int size){
        Label label = new Label(text);

        label.setFont(Font.font("Roboto", size));
        label.setTextFill(Color.valueOf("#ffffff"));
        label.setTextOverrun(OverrunStyle.CLIP);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER_LEFT);
        label.prefWidthProperty().bind(content.widthProperty().subtract(400));
        label.setCursor(Cursor.TEXT);

        return label;
    }

    private Label createHeader(String text) {
        Label label = createLabel(text, WORDSIZE);
        label.setFont(Font.font("Roboto", FontWeight.BOLD, WORDSIZE + 2));

        return label;
    }

    private Label createDescription(String text){
        Label description = createLabel(text, WORDSIZE);
        description.setFont(Font.font("Arial", FontWeight.BOLD,
                text.length() >= 250 ? WORDSIZE + 2 : WORDSIZE + 4));

        return description;
    }

    private Label createImageLabel(Image image, String caption){
        // Create ImageView and Label, and set label graphic to image view
        final double MAX_WIDTH = content.getWidth();
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(Math.min(600, image.getWidth()));

        // Adjust label position and size
        Label label = createGraphicLabel(caption);
        label.setGraphic(imageView);
        label.setPrefWidth(imageView.getFitWidth());

        // Make label more interactive with mouse event
        label.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (imageView.getFitWidth() == 600) {
                imageView.setFitWidth(Math.min(image.getWidth(), MAX_WIDTH));
            }
            else {
                imageView.setFitWidth(600);
            }
        });
        label.setCursor(Cursor.HAND);

        return label;
    }

    private Label createVideoButton(String videoSrc, String caption){
        // Create media player
        Media media = new Media(videoSrc);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setFitWidth(800);
        mediaView.setPreserveRatio(true);
        mediaView.setOnMouseEntered(e -> mediaPlayer.play());
        mediaView.setOnMouseExited(e -> mediaPlayer.pause());

        // Adjust label position and size
        Label label = createGraphicLabel("DRAG MOUSE IN TO PLAY: " + caption);
        label.setGraphic(mediaView);
        label.setPrefWidth(mediaView.getFitWidth());
        return label;
    }

    private Label createGraphicLabel(String caption) {
        Label label = new Label(caption);
        if (caption.compareTo("") != 0)
            label.setBackground(new Background(new BackgroundFill(Color.valueOf("#b4b4b4"), new CornerRadii(0), new Insets(0))));
        label.setContentDisplay(ContentDisplay.TOP);
        label.setAlignment(Pos.TOP_CENTER);
        label.setStyle("-fx-border-color: #404040; -fx-background-color: #bcbcbc");
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFont(Font.font("Arial", FontPosture.ITALIC, 16));
        label.setTextOverrun(OverrunStyle.CLIP);
        label.setWrapText(true);

        return label;
    }

    private FlowPane createWrapNote() {
        FlowPane pane = new FlowPane();

        pane.setStyle("-fx-border-color: #2c91c7; -fx-background-color: #404040; -fx-alignment: center");
        pane.prefWidthProperty().bind(content.widthProperty().subtract(380));
        pane.setVgap(20);

        return pane;
    }

    // Function to display alert
    private void dealException(Exception e, Item item){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Poor Internet Connection");
            alert.setHeaderText("Can't connect to\n" + item.getLink());
            alert.setContentText("Please check your internet connection. Press F5 or Ctrl + R to refresh article.");

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            TextArea area = new TextArea(sw.toString());
            alert.getDialogPane().setExpandableContent(area);
            alert.setOnCloseRequest(dialogEvent -> anchorPane.setEffect(null));
            anchorPane.setEffect(new BoxBlur(anchorPane.getWidth(), anchorPane.getHeight(), 1));
            alert.show();
        });
    }

    @FXML private void menuHome(){
        super.menuHome(categoryIndex);
    }

    // Function to navigate next and previous articles
    public void nextArticle(){
        if (index == items.size() - 1) return; // If last item of the list reached, return

        // If not last item, increase current item index and load that article
        index++;
        item = items.get(index);
        readArticle();
        previousButton.setDisable(false);

        // Disable next article button if last item of the list reached
        if (index == items.size() - 1)
            nextButton.setDisable(true);
    }

    public void previousArticle(){
        if (index == 0) return; // If first item of the list reached, return

        // If not first item, decrease current item index and load that article
        index--;
        item = items.get(index);
        readArticle();
        nextButton.setDisable(false);

        // Disable previous article button if first item of the list reached
        if (index == 0)
            previousButton.setDisable(true);
    }
}