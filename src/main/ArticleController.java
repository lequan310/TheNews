package main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
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

public class ArticleController implements Initializable{
    @FXML private AnchorPane anchorPane;
    @FXML private FlowPane content;
    @FXML private ImageView thumbnail;
    @FXML private Label title;
    @FXML private Label timeLabel;
    @FXML private Label sourceLabel;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private ScrollPane scrollPane;

    private final int WORDSIZE = 18, ITEMSIZE = 49;
    private final ArrayList<Item> items;
    private Item item;
    private int index = 0;
    private final int categoryIndex;
    private double x, y;

    public ArticleController(ArrayList<Item> items, int index, int categoryIndex){
        this.items = items;
        this.index = index;
        this.categoryIndex = categoryIndex;
        item = items.get(index);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        readArticle();
        previousButton.setOnAction(e -> previousArticle());
        nextButton.setOnAction(e -> nextArticle());

        if (index == 0)
            previousButton.setDisable(true);
        else if (index == ITEMSIZE)
            nextButton.setDisable(true);
    }

    public void readArticle(){
        content.getChildren().clear();
        scrollPane.setVvalue(0);
        title.setText(item.getTitle());
        timeLabel.setText(item.getPubDate());
        sourceLabel.setText(item.getLink());

        if (item.getImgSrc().compareTo("") != 0)
            thumbnail.setImage(new Image(item.getImgSrc()));
        else
            thumbnail.setImage(null);

        switch (item.getSource()){
            case TT -> readArticleTT(item.getLink());
            case TN -> readArticleTN(item.getLink());
            case ZING -> readArticleZing(item.getLink());
            case ND -> readArticleND(item.getLink());
        }

        content.getChildren().addAll(createLabel("", WORDSIZE));
    }

    private void readArticleTT(String urlAddress){
        try {
            Document doc = Jsoup.connect(urlAddress).get();
            final int statusCode = doc.connection().response().statusCode();
            System.out.println("Status code: " + statusCode + " " + urlAddress);

            Elements body = doc.select("div#mainContentDetail");
            Elements article = body.select("div#main-detail-body > *");

            Label description = createDescription(body.select("h2").text());
            content.getChildren().add(description);

            for (Element e : article) {
                if (e.is("p")) {
                    content.getChildren().add(createLabel(e.text(), WORDSIZE));
                }
                else if (e.is("div")) {
                    if (e.attr("type").compareTo("Photo") == 0) {
                        Image image = new Image(e.select("img").attr("src"));
                        content.getChildren().add(createImageLabel(image, e.select("p").text()));
                    }
                    else if (e.attr("type").compareTo("VideoStream") == 0) {
                        String videoSrc = e.attr("data-src");
                        videoSrc = videoSrc.substring(videoSrc.indexOf("hls"));
                        videoSrc = videoSrc.substring(0, videoSrc.indexOf(".mp4") + 4);
                        videoSrc = videoSrc.replace("&vid=", "/");
                        videoSrc = "https://" + videoSrc;

                        content.getChildren().add(createVideoButton(videoSrc, e.select("p").text()));
                    }
                    else if (e.attr("type").compareTo("wrapnote") == 0) {
                        FlowPane pane = createWrapNote();

                        for (Element i : e.select("> *")){
                            if (i.is("p")) {
                                pane.getChildren().add(createLabel(i.text(), WORDSIZE));
                            }
                            else if (i.is("div") && i.attr("type").compareTo("Photo") == 0) {
                                Image image = new Image(i.select("img").attr("src"));
                                pane.getChildren().add(createImageLabel(image, i.select("p").text()));
                            }
                        }

                        content.getChildren().add(pane);
                    }
                }
            }

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

    private void readArticleTN(String urlAddress) {
        try {
            Document doc = Jsoup.connect(urlAddress).get();
            final int statusCode = doc.connection().response().statusCode();
            System.out.println("Status code: " + statusCode + " " + urlAddress);

            // Video article
            if (urlAddress.contains("https://thanhnien.vn/video")){
                Label label = createLabel(doc.select("div.sapo").text(), WORDSIZE);

                String videoSrc = doc.select("div.media-player script").toString();
                videoSrc = extract(videoSrc, "src=\"", "\"");
                Label videoButton = createVideoButton(videoSrc, "");
                content.getChildren().addAll(label, videoButton);
            }
            // Normal article
            else{
                Elements body = doc.select("div[class~=.*content]");
                Elements article = doc.select("div[id=abody] > *");

                // Description
                Label description = createDescription(body.select("div.sapo").text());

                // Thumbnail image
                try {
                    Image thumbnail = new Image(body.select("div[id=contentAvatar] img").attr("src"));
                    Label tnImage = createImageLabel(thumbnail, body.select("div[id=contentAvatar] div.imgcaption").text());
                    content.getChildren().addAll(description, tnImage);
                }
                // If no thumbnail image
                catch (IllegalArgumentException e) { }

                for (Element e : article) {
                    if (e.is("p")) {
                        content.getChildren().add(createLabel(e.text(), WORDSIZE));
                    }
                    else if (e.is("h2")) {
                        content.getChildren().add(createHeader(e.text(), WORDSIZE));
                    }
                    else if (e.is("div")) {
                        checkDivTN(e);
                    }
                }

                Label author = createDescription(doc.select("div.left h4").text());
                author.setAlignment(Pos.TOP_RIGHT);
                content.getChildren().add(author);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();

            if (e instanceof IOException)
                dealException(e, item);
        }
    }

    private void readArticleZing(String urlAddress){
        try{
            Document doc = Jsoup.connect(urlAddress).get();
            final int statusCode = doc.connection().response().statusCode();
            System.out.println("Status code: " + statusCode + " " + urlAddress);

            // Video article
            if (urlAddress.contains("https://zingnews.vn/video")){
                Elements body = doc.select("div[id=video-featured]");
                Elements videos = body.select("video");
                Elements articles = body.select("div.video-info");

                Label videoButton = createVideoButton(videos.first().attr("src"), "");
                Label label = createLabel(articles.select("p.video-summary").text(), WORDSIZE);
                Label author = createDescription(articles.select("span.video-author").text());
                author.setAlignment(Pos.CENTER_RIGHT);
                content.getChildren().addAll(videoButton, label, author);
            }
            // Normal article
            else{
                Elements body = doc.select("section.main");
                Elements article = doc.select("div.the-article-body > *");

                Label description =createDescription(doc.select("p.the-article-summary").text());
                content.getChildren().add(description);

                for (Element e : article) {
                    if (e.is("p")) {
                        content.getChildren().add(createLabel(e.text(), WORDSIZE));
                    }
                    else if (e.is("div") && e.attr("class").compareTo("notebox ncenter") == 0){
                        FlowPane pane = createWrapNote();

                        for (Element i : e.select("> *")){
                            if (i.is("p")) {
                                pane.getChildren().add(createLabel(i.text(), WORDSIZE));
                            }
                        }

                        content.getChildren().add(pane);
                    }
                    else if (e.is("h3")) {
                        content.getChildren().add(createHeader(e.text(), WORDSIZE));
                    }
                    else if (e.is("figure") && e.attr("class").contains("video")) {
                        content.getChildren().add(createVideoButton(e.attr("data-video-src"), e.select("figcaption").text()));
                    }
                    else if (e.is("table") && e.attr("class").compareTo("picture") == 0) {
                        String imageURL = e.select("img").attr("data-src");
                        if (imageURL.compareTo("") == 0)
                            imageURL = e.select("img").attr("src");

                        Image image = new Image(imageURL);
                        content.getChildren().add(createImageLabel(image, e.select("td[class=\"pCaption caption\"]").text()));
                    }
                }

                Label author = createDescription(doc.getElementsByClass("author").text());
                author.setAlignment(Pos.CENTER_RIGHT);
                content.getChildren().add(author);
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();

            if (e instanceof IOException)
                dealException(e, item);
        }
    }

    private void readArticleND(String urlAddress) {
        try {
            Document doc = Jsoup.connect(urlAddress).get();
            final int statusCode = doc.connection().response().statusCode();
            System.out.println("Status code: " + statusCode + " " + urlAddress);

            Elements body = doc.select("div.box-content-detail");
            Elements article = doc.select("div.detail-content-body > *");

            // Thumbnail image
            Image thumb = new Image(body.select("div.box-detail-thumb img").attr("src"));
            Label thumbnail = createImageLabel(thumb, body.select("div.box-detail-thumb span").text());

            // Description
            Label description = createDescription(body.select("div.box-des-detail p").text());
            content.getChildren().addAll(thumbnail, description);

            for (Element e : article) {
                if (e.is("p")) {
                    Label label = createLabel(e.text(), WORDSIZE);
                    if (e.select("strong").size() > 0)
                        label.setFont(Font.font("Times New Roman", FontWeight.BOLD, WORDSIZE));

                    content.getChildren().add(label);
                }
                else if (e.is("div") && e.attr("class").compareTo("light-img") == 0) {
                    Image image = new Image(e.select("figure").attr("data-src"));
                    content.getChildren().add(createImageLabel(image, e.select("figcaption").text()));
                }
                else if (e.is("ol")) {
                    for (Element li : e.select("> *")) {
                        content.getChildren().add(createLabel(li.text(), WORDSIZE));
                    }
                }
                else if (e.is("blockquote")) {
                    FlowPane pane = createWrapNote();

                    for (Element i : e.select("> *")){
                        if (i.is("p")) {
                            pane.getChildren().add(createLabel(i.text(), WORDSIZE));
                        }
                    }
                }
            }

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

    private void checkDivTN(Element div) {
        if (div.select("> *").size() == 0){
            content.getChildren().add(createLabel(div.text(), WORDSIZE));
            return;
        }

        for (Element i : div.select("> *")) {
            if (i.is("div")) {
                checkDivTN(i);
            }
            else if (i.is("table") && i.attr("class").compareTo("imagefull") == 0) {
                Image image = new Image(i.select("img").attr("data-src"));
                content.getChildren().add(createImageLabel(image, i.select("p").text()));
            }
            else if (i.is("table") && i.attr("class").compareTo("video") == 0) {
                Label videoButton = createVideoButton(i.select("div[class=\"clearfix cms-video\"]").attr("data-video-src"),
                        i.select("p").text());
                content.getChildren().add(videoButton);
            }
            else if (i.hasText()) {
                content.getChildren().add(createLabel(div.text(), WORDSIZE));
                break;
            }
        }
    }

    private Label createLabel(String text, int size){
        Label label = new Label(text);
        label.setFont(Font.font("Times New Roman", size));
        label.setTextFill(Color.valueOf("#ffffff"));
        label.setTextOverrun(OverrunStyle.CLIP);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER_LEFT);
        label.prefWidthProperty().bind(content.widthProperty().subtract(300));

        return label;
    }

    private Label createHeader(String text, int size) {
        Label label = createLabel(text, size);
        label.setFont(Font.font("Times New Roman", FontWeight.BOLD, size + 2));

        return label;
    }

    private Label createDescription(String text){
        Label description = new Label(text);
        description.setTextFill(Color.valueOf("#ffffff"));
        description.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        description.prefWidthProperty().bind(content.widthProperty().subtract(300));
        description.setWrapText(true);
        description.setTextOverrun(OverrunStyle.CLIP);
        description.setAlignment(Pos.CENTER_LEFT);

        return description;
    }

    private Label createImageLabel(Image image, String caption){
        // Create ImageView and Label, and set label graphic to image view
        final int MAX_WIDTH = 1000;
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(Math.min(image.getWidth(), MAX_WIDTH));

        // Adjust label position and size
        Label label = createGraphicLabel(caption);
        label.setGraphic(imageView);
        label.setPrefWidth(imageView.getFitWidth());
        label.setAlignment(Pos.CENTER);

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
            label.setBackground(new Background(new BackgroundFill(Color.valueOf("#dddddd"), new CornerRadii(0), new Insets(0))));
        label.setContentDisplay(ContentDisplay.TOP);
        label.setAlignment(Pos.TOP_CENTER);
        label.setFont(Font.font("Arial", FontPosture.ITALIC, 16));
        label.setTextOverrun(OverrunStyle.CLIP);
        label.setWrapText(true);

        return label;
    }

    private FlowPane createWrapNote() {
        FlowPane pane = new FlowPane();
        pane.setAlignment(Pos.CENTER);
        pane.prefWidthProperty().bind(content.widthProperty().subtract(300));
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(100, 100, 100), new CornerRadii(0), new Insets(0))));

        return pane;
    }

    private void dealException(Exception e, Item item){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Poor Internet Connection");
        alert.setHeaderText("Can't connect to\n" + item.getLink());
        alert.setContentText("Please check your internet connection. Press F5 to refresh article.");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        TextArea area = new TextArea(sw.toString());
        alert.getDialogPane().setExpandableContent(area);
        alert.setOnCloseRequest(dialogEvent -> anchorPane.setEffect(null));
        anchorPane.setEffect(new BoxBlur(anchorPane.getWidth(), anchorPane.getHeight(), 1));
        alert.show();
    }

    @FXML private void menuCategories() {
        new SceneSwitch(anchorPane).menuCategories();
    }

    @FXML private void menuHome(){
        new SceneSwitch(anchorPane).menuHome(categoryIndex);
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
        stage.setFullScreenExitHint("");
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML private void close(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void nextArticle(){
        if (index == ITEMSIZE) return;

        index++;
        item = items.get(index);
        readArticle();
        previousButton.setDisable(false);

        if (index == ITEMSIZE)
            nextButton.setDisable(true);
    }

    public void previousArticle(){
        if (index == 0) return;

        index--;
        item = items.get(index);
        readArticle();
        nextButton.setDisable(false);

        if (index == 0) previousButton.setDisable(true);
    }

    private static String extract(String line, String start, String end) {
        int firstPos = line.indexOf(start);
        String temp = line.substring(firstPos);
        temp = temp.replace(start, "");
        int lastPos = temp.indexOf(end);
        temp = temp.substring(0, lastPos);
        return temp;
    }
}
