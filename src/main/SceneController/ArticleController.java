package main.SceneController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;
import main.Model.Item;
import main.Storage.Storage;
import org.jsoup.Connection;
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
    private final int WORDSIZE = 18, categoryIndex; // default word size and category index
    private int index; // current item index
    private final ArrayList<Item> items; // list of items
    private Item item; // current item
    private Storage storage = Storage.getInstance();

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

    // Constructor with parameters to receive data from main menu scene
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
        blackPane.prefHeightProperty().bind(anchorPane.heightProperty().divide(3));
        thumbnail.fitHeightProperty().bind(blackPane.heightProperty().subtract(10));
        title.prefHeightProperty().bind(blackPane.heightProperty());
        content.setOnScroll(e -> {
            double delta = e.getDeltaY() * -4;
            scrollPane.setVvalue(scrollPane.getVvalue() + delta / scrollPane.getContent().getBoundsInLocal().getHeight());
        });
        if (index == 0) previousButton.setDisable(true);
    }

    public void readArticle(){
        System.gc();
        Platform.runLater(() -> {
            // Clear previous article and read new article
            content.getChildren().clear();

            if (storage.getArticles().containsKey(item.getLink())) {
                content.getChildren().addAll(storage.getArticles().get(item.getLink()));
            }
            else {
                // Call read article function depends on which source
                try {
                    switch (item.getSource()){
                        case VE -> readArticleVE(item.getLink());
                        case TT -> readArticleTT(item.getLink());
                        case TN -> readArticleTN(item.getLink());
                        case ZING -> readArticleZing(item.getLink());
                        case ND -> readArticleND(item.getLink());
                    }

                    ObservableList<Node> nodes = FXCollections.observableArrayList(content.getChildren());
                    storage.getArticles().put(item.getLink(), nodes);
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());

                    if (e instanceof IOException)
                        dealException(e, item);
                }
            }

            content.getChildren().addAll(createLabel(""));
        });

        // Initialize UI components
        anchorPane.focusedProperty().addListener(observable -> anchorPane.requestFocus()); // Help anchor pane detects key events
        scrollPane.setVvalue(0); // Set scroll bar to the top
        title.setText(item.getTitle());
        timeLabel.setText(item.getPubDate());
        sourceLabel.setText(item.getLink());

        // Set thumbnail Image
        try {
            if (storage.getImage().containsKey(item.getImgSrc())) {
                thumbnail.setImage(storage.getImage().get(item.getImgSrc()));
            }
            else {
                Image background = new Image(item.getImgSrc(), thumbnail.getFitWidth(), thumbnail.getFitHeight(), false, true, true);
                thumbnail.setImage(background);
                storage.getImage().put(item.getImgSrc(), background);
            }
        }
        catch (IllegalArgumentException e) {
            thumbnail.setImage(null);
        }
    }

    // Function to read article from VN Express
    private void readArticleVE(String urlAddress) throws Exception {
        // Try to connect to item url
        Connection.Response response = Jsoup.connect(urlAddress).timeout(10000).execute();
        System.out.println("Status code: " + response.statusCode() + " " + urlAddress);
        if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());
        Document doc = response.parse();

        // Video article
        if (urlAddress.contains("video.vnexpress.net")) {
            // Extract video URL
            String videoURL = doc.select("script").toString();
            videoURL = extract(videoURL, "\"contentUrl\": \"", "\",");

            // Add video label
            try {
                content.getChildren().add(createVideoLabel(videoVE(videoURL), ""));
            }
            catch (IllegalArgumentException e) {}

            // Create description, video, and author label and add after video
            Label description = createDescription(doc.select("div.lead_detail").text());
            Label author = createDescription(doc.select("p.author").text());
            content.getChildren().addAll(description, author);
        }
        // Normal article
        else {
            // Get main article element
            Element article;
            if (doc.select("article.fck_detail").size() > 0)
                article = doc.select("article.fck_detail").first();
            else
                article = doc.select("div[class*=fck_detail]").first();

            // Add description to article view
            Label description = createDescription(doc.select("p.description").text());
            content.getChildren().add(description);

            // Loop through elements in main article
            checkDivVE(article, content);

            // Add author label to article view
            Label author = createDescription(article.select("p[style*=text-align:right]").text());
            if (author.getText().equals(""))
                author.setText(article.select("p[class*=author]").text());

            author.setAlignment(Pos.TOP_RIGHT);
            content.getChildren().add(author);
        }
    }

    // Function to read article from TuoiTre
    private void readArticleTT(String urlAddress) throws Exception {
        // Connect to article URL
        Connection.Response response = Jsoup.connect(urlAddress).timeout(10000).execute();
        System.out.println("Status code: " + response.statusCode() + " " + urlAddress);
        if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());
        Document doc = response.parse();

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

    // Function to read article from ThanhNien
    private void readArticleTN(String urlAddress) throws Exception {
        // Connect to article URL
        Connection.Response response = Jsoup.connect(urlAddress).timeout(10000).execute();
        System.out.println("Status code: " + response.statusCode() + " " + urlAddress);
        if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());
        Document doc = response.parse();

        // Video article
        if (urlAddress.contains("https://thanhnien.vn/video")){
            // Extract main article, description and author
            Elements article = doc.select("div[id=abody]");
            Label label = createDescription(doc.select("div.sapo").text()); // Create description label
            Label author = createDescription(doc.select("div.details__author h4").text()); // Create author label
            author.setAlignment(Pos.TOP_RIGHT);
            content.getChildren().add(label);

            // Extract video url and add video label
            try {
                String videoSrc = doc.select("div.media-player script").toString();
                videoSrc = extract(videoSrc, "src=\"", "\"");
                Label videoLabel = createVideoLabel(videoSrc, ""); // Create video
                content.getChildren().add(videoLabel); // Add all created components to article view
            }
            catch (Exception e) {} // Catch exception mostly due to video being a live stream

            // Loop through main article and add content, finally add author
            checkDivTN(article.first(), content);
            content.getChildren().add(author);
        }
        // Normal article
        else{
            Elements body = doc.select("div[class~=.*content]");
            Elements article = doc.select("div[id=abody]");

            // Create Description label
            if (body.select("div.sapo").size() > 0) {
                content.getChildren().add(createDescription(body.select("div.sapo").text()));
            }
            else {
                content.getChildren().add(createDescription(doc.select("div.summary").text()));
            }

            // Thumbnail image
            try {
                Image thumbnail = new Image(body.select("div[id=contentAvatar] img").attr("src"), true);
                Label tnImage = createImageLabel(thumbnail, body.select("div[id=contentAvatar] div.imgcaption").text());
                content.getChildren().add(tnImage);
            }
            // If no thumbnail image
            catch (IllegalArgumentException e) {}

            // Loop through elements in main article
            checkDivTN(article.first(), content);

            // Create author label
            Label author = createDescription(doc.select("div.left h4").text());
            author.setAlignment(Pos.TOP_RIGHT);
            content.getChildren().add(author);
        }
    }

    // Function to read article from ZingNews
    private void readArticleZing(String urlAddress) throws Exception {
        // Connect to article URL
        Connection.Response response = Jsoup.connect(urlAddress).timeout(10000).execute();
        System.out.println("Status code: " + response.statusCode() + " " + urlAddress);
        if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());
        Document doc = response.parse();

        // Video article
        if (urlAddress.contains("https://zingnews.vn/video")){
            // Extract video URL and information
            Elements body = doc.select("div[id=video-featured]");
            Elements videos = body.select("video");
            Elements articles = body.select("div.video-info");

            // Creating and adding video, summary and author to article view
            try {
                Label videoLabel = createVideoLabel(videos.first().attr("src"), "");
                Label label = createLabel(articles.select("p.video-summary").text());
                Label author = createDescription(articles.select("span.video-author").text());
                author.setAlignment(Pos.TOP_RIGHT);
                content.getChildren().addAll(videoLabel, label, author);
            }
            catch (Exception e) {}
        }
        // Normal article
        else{
            // Extract article components
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

    // Function to read article from Nhan Dan
    private void readArticleND(String urlAddress) throws Exception {
        // Connect to article URL
        Connection.Response response = Jsoup.connect(urlAddress).timeout(10000).execute();
        System.out.println("Status code: " + response.statusCode() + " " + urlAddress);
        if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());
        Document doc = response.parse();

        // Special Nhan Dan articles
        if (urlAddress.contains("special.nhandan.vn")) {
            Elements article = doc.select("article > *");
            String newURL = urlAddress.replace("index.html", "");

            for (Element e : article) {
                // Check div for each div element with id containing "section"
                if (e.is("div") && e.attr("id").contains("section")) {
                    addSpecialND(e.select("> *"), content, newURL);
                }
            }
        }
        // Normal Nhan Dan articles
        else {
            // Extract elements from main article
            Elements body = doc.select("div.box-content-detail");
            Elements article = doc.select("div.detail-content-body > *");

            // Create and add Thumbnail image
            try {
                Image thumb = new Image(body.select("div.box-detail-thumb img").attr("src"), true);
                Label thumbnail = createImageLabel(thumb, body.select("div.box-detail-thumb span").text());
                content.getChildren().add(thumbnail);
            }
            catch (IllegalArgumentException ex) {}

            // Create and add Description label
            Label description = createDescription(body.select("div.box-des-detail p").text());
            content.getChildren().add(description);

            // Loop through elements in main article
            addND(article, content);

            // Create and add author label
            Label author = createDescription(body.select("div.box-author strong").text());
            author.setAlignment(Pos.TOP_RIGHT);
            content.getChildren().add(author);
        }
    }

    private void checkDivVE(Element div, FlowPane content) {
        // If element is <p>, add text label
        if (div.is("p")) {
            content.getChildren().add(createLabel(div.text()));
            return;
        }

        for (Element e : div.select("> *")) {
            // If element is text not author, add text label
            if (e.is("p") && !e.attr("style").contains("text-align:right;") && !e.attr("class").contains("author")) {
                Label label = createLabel(e.text());

                if (e.select("strong").size() > 0)
                    label.setFont(Font.font("Roboto", FontWeight.BOLD, WORDSIZE));

                content.getChildren().add(label);
            }
            // If element is header, add header label
            else if (e.is("h2")) {
                content.getChildren().add(createDescription(e.text()));
            }
            // If element is image
            else if (e.is("figure") && e.select("img").size() > 0) {
                // Add single image label if the number of <img> element is 1
                if (e.select("img").size() == 1) {
                    // Extract image link, create image and add to content
                    String imageURL = e.select("img").attr("data-src");
                    if (imageURL.equals("")) imageURL = e.select("img").attr("src");

                    Image image = new Image(imageURL, true);
                    content.getChildren().add(createImageLabel(image, e.select("figcaption").text()));
                }
                // Add multiple image label if the number of <img> element is more than 1
                else {
                    for (Element el : e.select("img")) {
                        // Extract image link, create image and add to content
                        String imageURL = el.select("img").attr("data-desktop-src");
                        if (imageURL.equals("")) imageURL = el.select("img").attr("data-src");

                        Image image = new Image(imageURL, true);
                        content.getChildren().add(createImageLabel(image, e.select("figcaption").text()));
                    }
                }
            }
            // If element is either video or image
            else if (e.attr("class").contains("clearfix")) {
                // If element has <video>
                if (e.select("video").size() > 0) {
                    // Add video label
                    content.getChildren().add(createVideoLabel(videoVE(e.select("video").attr("src")), e.select("figcaption").text()));
                }
                // If element doesn't have <video> and has <image>
                else if (e.select("img").size() > 0) {
                    // Extract and add image
                    String imageURL = e.select("img").attr("data-src");
                    if (imageURL.equals("")) imageURL = e.select("img").attr("src");

                    Image image = new Image(imageURL, true);
                    content.getChildren().add(createImageLabel(image, ""));
                }

                // Check if there is anything below video or image
                for (int i = 1; i < e.select("> *").size(); i++) {
                    checkDivVE(e.select("> *").get(i), content);
                }
            }
            // If element is video
            else if (e.is("div") && e.select("video").size() > 0 &&
                    (e.attr("class").contains("text-align:center") || e.attr("style").contains("center"))) {
                // Add video label
                content.getChildren().add(createVideoLabel(videoVE(e.select("video").attr("src")), e.select("p").text()));
            }
            // If element is wrapnote
            else if (e.is("div") && e.attr("class").equals("box_brief_info")) {
                // Create a wrapnote
                FlowPane pane = createWrapNote();

                // Loop through elements in wrapnote
                for (Element i : e.select("> *")) {
                    if (i.is("p")) {
                        pane.getChildren().add(createLabel(i.text()));
                    }
                }

                content.getChildren().add(pane); // Add wrapnote into content
            }
            else if (e.is("div")) {
                checkDivVE(e, content); // Check inside div element if element is <div>
            }
        }
    }

    // Utilities function to read Thanh Nien article
    private void checkDivTN(Element div, FlowPane content) {
        // If element has 0 children and is not an ad div
        if (div.select("> *").size() == 0 && !div.className().contains("ads") && div.hasText()){
            content.getChildren().add(createLabel(div.text()));
            return;
        }

        // Loop through div elements
        for (Element i : div.select("> *")) {
            try {
                // Recursion call if child is div
                if (i.is("div") && !i.attr("class").contains("image") && !i.className().equals("details__morenews")) {
                    checkDivTN(i, content);
                }
                // Add text label if child is text
                else if (i.is("p")) {
                    content.getChildren().add(createLabel(i.text()));
                }
                // Add image if child is image
                else if (i.attr("class").contains("image")) {
                    Image image = new Image(i.select("img").attr("data-src"), true);
                    content.getChildren().add(createImageLabel(image, i.select("p").text()));
                }
                // Add video if child is video
                else if (i.is("table") && i.attr("class").equals("video")) {
                    Label videoLabel = createVideoLabel(i.select("div[class=\"clearfix cms-video\"]").attr("data-video-src"), i.select("p").text());
                    content.getChildren().add(videoLabel);
                }
                // Add image if child is image
                else if (i.is("figure") && i.attr("class").equals("picture")) {
                    if (i.select("img").size() > 0) {
                        Image image = new Image(i.select("img").attr("data-src"), true);
                        content.getChildren().add(createImageLabel(image, i.select("figcaption").text()));
                    }
                    else if (i.hasText()) {
                        content.getChildren().add(createLabel(i.text()));
                    }
                }
                // Add quote table
                else if (i.is("table") && i.attr("class").equals("quotetable")) {
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
                else if ((i.hasText() && i.select("div").size() == 0)) {
                    content.getChildren().add(createLabel(div.text()));
                    if (i.nextElementSiblings().select("table,figure").size() == 0) break;
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
                    Label label = createLabel(e.text());
                    if (e.select("b").size() > 0)
                        label.setFont(Font.font("Roboto", FontWeight.BOLD, WORDSIZE));

                    content.getChildren().add(label);
                }
                // Else if element is div
                else if (e.is("div")) {
                    // Add image if element is image
                    if (e.attr("type").equals("Photo")) {
                        String imageSrc = e.select("img").attr("src");
                        try {
                            imageSrc = imageSrc.replace("thumb_w/586/", "");
                        }
                        catch (StringIndexOutOfBoundsException exception) {}

                        Image image = new Image(imageSrc, true);
                        content.getChildren().add(createImageLabel(image, e.select("p").text()));
                    }
                    // Add video if element is video
                    else if (e.attr("type").equals("VideoStream")) {
                        // Extract video url from data-src attribute
                        String videoSrc = e.attr("data-src");
                        videoSrc = videoSrc.substring(videoSrc.indexOf("hls"));

                        if (videoSrc.contains(".mp4"))
                            videoSrc = videoSrc.substring(0, videoSrc.indexOf(".mp4") + 4);
                        else
                            videoSrc = videoSrc.substring(0, videoSrc.indexOf(".webm") + 5);

                        videoSrc = videoSrc.replace("&vid=", "/");
                        videoSrc = "https://" + videoSrc;

                        content.getChildren().add(createVideoLabel(videoSrc, e.select("p").text()));
                    }
                    // Add wrapnote if element is wrapnote
                    else if (e.attr("type").equals("wrapnote")) {
                        FlowPane pane = createWrapNote();

                        // Loop through elements in wrap note and add into wrapnote
                        addTT(e.select("> *"), pane);

                        content.getChildren().add(pane);
                    }
                }
            }
            catch (IllegalArgumentException ex) {
            }
        }
    }

    // Utilities function to read ZingNews main article
    private void addZing(Elements elements, FlowPane content) {
        for (Element e : elements) {
            try {
                // Create and add label if element is text
                if (e.is("p")) {
                    content.getChildren().add(createLabel(e.text()));
                }
                // Create and add wrapnote if element is wrapnote
                else if (e.is("div") && e.attr("class").equals("notebox ncenter")){
                    FlowPane pane = createWrapNote();
                    addZing(e.select("> *"), pane);
                    content.getChildren().add(pane);
                }
                // Create and add header label if element is header
                else if (e.is("h3")) {
                    content.getChildren().add(createHeader(e.text()));
                }
                // Create and add video if element is video
                else if (e.is("figure") && e.attr("class").contains("video")) {
                    content.getChildren().add(createVideoLabel(e.attr("data-video-src"), e.select("figcaption").text()));
                }
                // Create and add images if element is image/gallery
                else if (e.is("table") && e.attr("class").contains("picture")) {
                    // Loop through every image in table
                    for (Element i : e.select("td.pic > *")) {
                        // Get image link and add image to content
                        String imageURL = i.select("img").attr("data-src");
                        if (imageURL.equals("")) imageURL = i.select("img").attr("src");

                        Image image = new Image(imageURL, true);
                        content.getChildren().add(createImageLabel(image, e.select("td[class*=caption]").text()));
                    }
                }
                // Create and add image
                else if (e.is("h1") && e.select("img").size() > 0) {
                    Image image = new Image(e.select("img").attr("data-src"), true);
                    content.getChildren().add(createImageLabel(image, ""));
                }
                // For covid graph
                else if (e.is("div") && e.attr("class").contains("widget")) {
                    Image image = new Image(e.attr("data-src"), true);
                    content.getChildren().add(createImageLabel(image, " "));
                }
                // Create and add group of text
                else if (e.is("ul")  || e.is("div")) {
                    addZing(e.select("> *"), content);
                }
                // Create and add label into group of text above
                else if (e.hasText() && e.is("li")) {
                    content.getChildren().add(createLabel(e.text()));
                }
                // Create and add blockquote
                else if (e.is("blockquote")) {
                    // Create wrapnote and add content inside blockquote into wrapnote
                    FlowPane pane = createWrapNote();
                    addZing(e.select("> *"), pane);
                    content.getChildren().add(pane);
                }
            }
            catch (IllegalArgumentException ex) {
            }
        }
    }

    // Utilities function to read normal Nhan Dan article
    private void addND(Elements elements, FlowPane content) {
        for (Element e : elements) {
            try {
                // Create and add label if element is text
                if (e.is("p")) {
                    if (e.select("video").size() == 0) {
                        content.getChildren().add(createLabel(e.text()));
                    }
                    else {
                        content.getChildren().add(createVideoLabel(e.select("source").attr("src"), ""));
                    }
                }
                // Create and add image if element is image
                else if (e.is("div") && e.attr("class").equals("light-img")) {
                    Image image = new Image(e.select("figure").attr("data-src"), true);
                    content.getChildren().add(createImageLabel(image, e.select("figcaption").text()));
                }
                // Create and add label
                else if (e.is("ol") || e.is("ul")) {
                    for (Element li : e.select("> *")) {
                        content.getChildren().add(createLabel(li.text()));
                    }
                }
                // Create and add wrapnote if element is wrapnote
                else if (e.is("blockquote")) {
                    FlowPane pane = createWrapNote();
                    addND(e.select("> *"), pane);
                    content.getChildren().add(pane);
                }
            }
            catch (IllegalArgumentException ex) {
            }
        }
    }

    // Utilities function to read special Nhan Dan article
    private void addSpecialND(Elements elements, FlowPane content, String urlAddress) {
        for (Element e : elements) {
            // Create and add label if element is text
            if (e.is("p")) {
                Label label = createLabel(e.text());
                if (e.select("strong").size() > 0) {
                    label.setFont(Font.font("Roboto", FontWeight.BOLD, WORDSIZE));
                }

                content.getChildren().add(label);
            }
            // Create and add header if element is header
            else if (e.is("h2") || e.is("h3")) {
                content.getChildren().add(createHeader(e.text()));
            }
            // Create and add wrapnote if element is wrapnote
            else if (e.is("blockquote")) {
                FlowPane pane = createWrapNote();

                if (!e.select("> *").first().is("footer")){
                    addSpecialND(e.select("> *"), pane, urlAddress);
                    pane.getChildren().add(createLabel(e.select("footer").text()));
                }
                else {
                    pane.getChildren().add(createLabel(e.text()));
                }
                content.getChildren().add(pane);
            }
            // Create and add image if element is image
            else if ((e.is("figure") && e.select("img").size() > 0) || e.is("picture")){
                try {
                    String imgSrc = e.select("img").attr("data-src");
                    imgSrc = imgSrc.replace("./", "");
                    imgSrc = urlAddress + imgSrc;

                    Image image = new Image(imgSrc, true);
                    Element figcaption = e.select("figcaption p").first();
                    String caption = (figcaption == null) ? "" : figcaption.text();

                    if (imgSrc.endsWith("g") || imgSrc.endsWith(".gif"))
                        content.getChildren().add(createImageLabel(image, caption));
                }
                catch (StringIndexOutOfBoundsException | IllegalArgumentException exception) {}
            }
            // Create and add label if element is text
            else if (e.is("span") && e.hasText()) {
                content.getChildren().add(createLabel(e.text()));
            }
            // Create and add label if element is text
            else if (e.is("div") && e.attr("class").contains("Caption") && e.select("p").size() > 0){
                Label caption = createLabel(e.selectFirst("p").text());
                caption.setAlignment(Pos.CENTER);
                content.getChildren().add(caption);
            }
            // Recursion to check inside the div element
            else if (e.is("div")) {
                addSpecialND(e.select("> *"), content, urlAddress);
            }
        }
    }

    // Create UI components to add to article view
    // Default article Label design
    protected Label createLabel(String text){
        Label label = new Label(text);

        label.setFont(Font.font("Roboto", WORDSIZE));
        label.setTextFill(Color.valueOf("#ffffff"));
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER_LEFT);
        label.prefWidthProperty().bind(content.widthProperty().subtract(400)); // label prefwidth = content prefwidth - 400
        label.setCursor(Cursor.TEXT);

        return label;
    }

    // Header label design
    protected Label createHeader(String text) {
        Label label = createLabel(text);
        label.setFont(Font.font("Roboto", FontWeight.BOLD, WORDSIZE + 2));

        return label;
    }

    // Description label design
    protected Label createDescription(String text){
        Label description = createLabel(text);
        description.setFont(Font.font("Corbel", FontWeight.BOLD, WORDSIZE + 4));

        return description;
    }

    // Image label with caption
    protected Label createImageLabel(Image image, String caption) {
        // Create ImageView and Label, and set label graphic to image view
        final double MAX_WIDTH = content.getWidth();
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(600);

        // Adjust label position and size
        Label label = createGraphicLabel(caption);
        label.setGraphic(imageView);
        label.setPrefWidth(imageView.getFitWidth());

        // Make label more interactive with mouse event (zoom in and out on click)
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

    // Video label with caption
    protected Label createVideoLabel(String videoSrc, String caption){
        // Create media player
        Media media = new Media(videoSrc);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(0.5);
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setFitWidth(800);
        mediaView.setPreserveRatio(true);
        mediaView.setOnMouseEntered(e -> mediaPlayer.play());
        mediaView.setOnMouseExited(e -> mediaPlayer.pause());
        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.play();
        });

        // Add video and help image into stack pane and then set stack pane as label graphic
        Label label = createGraphicLabel(caption);
        StackPane container = new StackPane();
        ImageView imageView = new ImageView(new Image("/image/dragPlay.png", mediaView.getFitWidth(), mediaView.getFitHeight(), true, true));
        imageView.setOpacity(0.7);
        container.getChildren().add(mediaView);
        container.getChildren().add(imageView);
        label.setGraphic(container);

        // Temporary handler to change graphic to only media viewer after first mouse in event
        EventHandler<MouseEvent> tempHandler = new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                label.setGraphic(mediaView);
                label.removeEventHandler(MouseEvent.MOUSE_ENTERED, this);
            }
        };

        label.addEventHandler(MouseEvent.MOUSE_ENTERED, tempHandler);
        label.setPrefWidth(mediaView.getFitWidth());
        return label;
    }

    // Default label set up for image label and video label
    protected Label createGraphicLabel(String caption) {
        Label label = new Label(caption);

        label.setContentDisplay(ContentDisplay.TOP);
        label.setAlignment(Pos.TOP_CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFont(Font.font("Arial", FontPosture.ITALIC, 16));
        label.setWrapText(true);
        if (!caption.equals(""))
            label.setStyle("-fx-border-color: #404040; -fx-background-color: #bcbcbc");
        else
            label.setStyle("-fx-background-color: transparent");

        return label;
    }

    // A pane to distinguish between normal and text inside wrapnote
    protected FlowPane createWrapNote() {
        FlowPane pane = new FlowPane();

        pane.setStyle("-fx-border-color: #2c91c7; -fx-background-color: #404040; -fx-alignment: center");
        pane.prefWidthProperty().bind(content.widthProperty().subtract(380));
        pane.setVgap(15);

        return pane;
    }

    // Function to display alert
    protected void dealException(Exception e, Item item) {
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
        System.out.println(item.getLink());
    }

    // Function to navigate next and previous articles
    public void nextArticle(){
        if (index == items.size() - 1) return; // If last item of the list reached, return

        // If not last item, increase current item index and load that article
        index++;
        item = items.get(index);
        Platform.runLater(() -> readArticle());
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
        Platform.runLater(() -> readArticle());
        nextButton.setDisable(false);

        // Disable previous article button if first item of the list reached
        if (index == 0)
            previousButton.setDisable(true);
    }

    @FXML private void menuHome(){
        content.getChildren().clear();
        menuHome(categoryIndex, false);
    }
}