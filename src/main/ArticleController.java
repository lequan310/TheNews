package main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

    private final int WORDSIZE = 18;
    private final ArrayList<Item> items;
    private Item item;
    private int index = 0, categoryIndex;

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
        else if (index == items.size() - 1)
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
        try{
            Document doc = Jsoup.connect(urlAddress).get();
            final int statusCode = doc.connection().response().statusCode();
            System.out.println("Status code: " + statusCode + " " + urlAddress);

            Elements body = doc.select("div.main-content-body"); // Select elements in the div with class main-content-body
            Elements images = body.select("div[type=Photo]"); // Select image elements from the main-content-body
            Elements article = body.select("div[id=main-detail-body] > p"); // Select text elements from the main-content-body
            Elements wrapNote = body.select("div[type=wrapnote] > p");
            Elements video = body.select("div[type=VideoStream]");

            String bodyHTML = body.toString();
            String[] components = bodyHTML.trim().split("\n");

            Label description = createDescription(body.select("h2").text());

            content.getChildren().add(description);

            boolean inDiv = false, inWrapNote = false, inArticle = false;
            int divCounter = 0;

            for (int i = 0, j = 0, y = 0, z = 0, k = 0; k < components.length; k++){
                if (components[k].contains("main-detail-body")){
                    inArticle = true;
                    continue;
                }

                if (inArticle){
                    if (components[k].contains("<p")){
                        if (i < article.size() && !inDiv){
                            Label label = createLabel(article.get(i).text(), WORDSIZE);
                            if (article.get(i).select("b").size() > 0)
                                label.setFont(Font.font("Times New Roman", FontWeight.BOLD, WORDSIZE));
                            if (article.get(i).attr("style").compareTo("text-align:right") == 0)
                                label.setAlignment(Pos.CENTER_RIGHT);

                            content.getChildren().add(label);
                            i++;
                        }
                        else if (z < wrapNote.size() && inWrapNote){
                            Label label = createLabel(wrapNote.get(z).text(), WORDSIZE);
                            label.setBackground(new Background(new BackgroundFill(Color.valueOf("#222222"), new CornerRadii(0), new Insets(0))));
                            if (wrapNote.get(z).select("b").size() > 0)
                                label.setFont(Font.font("Times New Roman", FontWeight.BOLD, WORDSIZE));
                            if (wrapNote.get(z).attr("style").compareTo("text-align: right;") == 0)
                                label.setAlignment(Pos.CENTER_RIGHT);

                            content.getChildren().add(label);
                            z++;
                        }
                    }
                    else if (components[k].contains("type=\"VideoStream\"") && y < video.size()){
                        String videoSrc = video.get(y).attr("data-src");
                        videoSrc = videoSrc.substring(videoSrc.indexOf("hls"));
                        videoSrc = videoSrc.substring(0, videoSrc.indexOf(".mp4") + 4);
                        videoSrc = videoSrc.replace("&vid=", "/");
                        videoSrc = "https://" + videoSrc;

                        Label videoButton = createVideoButton(videoSrc, video.get(y).select("p").text());

                        content.getChildren().add(videoButton);
                        y++;
                    }
                    else if (components[k].contains("<img") && j < images.size()){
                        Image image = new Image(images.get(j).select("img").attr("src"));
                        Label label = createImageLabel(image, images.get(j).select("p").text());

                        content.getChildren().add(label);
                        j++;
                    }
                    else if (components[k].contains("<div")){
                        inDiv = true;
                        divCounter++;

                        if (components[k].contains("type=\"wrapnote\""))
                            inWrapNote = true;
                    }
                    else if (components[k].contains("</div>")){
                        divCounter--;

                        if (divCounter == 0){
                            inDiv = false;

                            if (inWrapNote) {
                                inWrapNote = false;
                                content.getChildren().add(createLabel("", 10));
                            }
                        }
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
            System.out.println(urlAddress);
            Document doc = Jsoup.connect(urlAddress).get();

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
                Elements body = doc.select("div.pswp-content");
                Elements article = body.select("div[id=abody] > div");
                Elements images = body.select("div[id=abody] table.imagefull");
                Elements videos = body.select("div[id=abody] table.video");
                Elements headers = body.select("div[id=abody] h2");

                String bodyHTML = body.toString();
                String[] components = bodyHTML.trim().split("\n");

                // Description
                Label description = createDescription(body.select("div.sapo").text());

                // Thumbnail image
                Image thumbnail = new Image(body.select("div[id=contentAvatar] img").attr("src"));
                Label tnImage = createImageLabel(thumbnail, body.select("div[id=contentAvatar] div.imgcaption").text());
                content.getChildren().addAll(description, tnImage);

                int divCounter = 0;
                boolean inArticle = false;
                String text = "";

                for (int i = 0, j = 0, y = 0, z = 0, k = 0; k < components.length; k++) {
                    if (components[k].contains("id=\"abody\"")){
                        inArticle = true;
                        continue;
                    }

                    if (inArticle) {
                        if (components[k].contains("<div")) {
                            divCounter++;
                            text = article.get(i).text();

                            if (divCounter >= 2) text = "";
                        }
                        if (components[k].contains("</div>")) {
                            divCounter--;

                            if (divCounter == 0) {
                                if (text.compareTo("") != 0)
                                    content.getChildren().add(createLabel(text, WORDSIZE));
                                i++;
                            }
                            else if (divCounter == -1) break;
                        }

                        if (components[k].contains("class=\"imagefull\"")) {
                            Image image = new Image(images.get(j).select("img").attr("data-src"));
                            content.getChildren().add(createImageLabel(image, images.get(j).select("div.imgcaption").text()));
                            j++;
                        }
                        else if (components[k].contains("<h2")) {
                            Label label = createLabel(headers.get(z).text(), WORDSIZE);
                            label.setFont(Font.font("Times New Roman", FontWeight.BOLD, WORDSIZE + 2));
                            content.getChildren().add(label);
                            z++;
                        }
                        else if (components[k].contains("class=\"video\"")) {
                            Label videoButton = createVideoButton(videos.get(y).select("div[class=\"clearfix cms-video\"]").attr("data-video-src"),
                                    videos.get(y).select("p").text());
                            content.getChildren().add(videoButton);
                            y++;
                        }
                    }
                }

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
                Elements images = body.select("table.picture");
                Elements articles = body.select("div.the-article-body > p");
                Elements headers = body.select("div.the-article-body > h3");
                Elements video = body.select("figure.video");
                String summary = doc.select("p.the-article-summary").text();

                String bodyHTML = body.toString();
                String[] components = bodyHTML.trim().split("\n");

                Label description = createDescription(summary);
                content.getChildren().add(description);

                boolean inArticle = false;
                for (int i = 0, j = 0, y = 0, z = 0, k = 0; k < components.length; k++) {
                    if (components[k].contains("the-article-body")){
                        inArticle = true;
                        continue;
                    }

                    if (inArticle){
                        if (components[k].contains("<p") && i < articles.size()){
                            Label label = createLabel(articles.get(i).text(), WORDSIZE);

                            content.getChildren().add(label);
                            i++;
                        }
                        else if (components[k].contains("<img") && j < images.size()){
                            Image image = new Image(images.get(j).select("img").attr("data-src"));
                            Label label = createImageLabel(image, images.get(j).select("td[class=\"pCaption caption\"]").text());

                            content.getChildren().add(label);
                            j++;
                        }
                        else if (components[k].contains("<h3") && z < headers.size()){
                            Label label = createLabel(headers.get(z).text(), WORDSIZE);
                            label.setFont(Font.font("Times New Roman", FontWeight.BOLD, WORDSIZE));

                            content.getChildren().add(label);
                            z++;
                        }
                        else if (components[k].contains("data-video-src")){
                            Label videoButton = createVideoButton(video.get(y).attr("data-video-src"), video.get(y).select("figcaption").text());

                            content.getChildren().add(videoButton);
                            y++;
                        }
                    }
                }

                Label author = createDescription(doc.getElementsByClass("author").text());
                author.setAlignment(Pos.CENTER_RIGHT);
                content.getChildren().add(author);
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());

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
            Elements images = body.select("div.detail-content-body figure");
            Elements article = body.select("div.detail-content-body > p");
            Elements blockquote = body.select("blockquote > p");
            Elements li = body.select("div.detail-content-body ol > li");

            String bodyHTML = body.toString();
            String[] components = bodyHTML.trim().split("\n");

            // Thumbnail image
            Image thumb = new Image(body.select("div.box-detail-thumb img").attr("src"));
            Label thumbnail = createImageLabel(thumb, body.select("div.box-detail-thumb span").text());

            // Description
            Label description = createDescription(body.select("div.box-des-detail p").text());

            content.getChildren().addAll(thumbnail, description);

            // Article and Images
            boolean inArticle = false, inBlockquote = false, inDiv = false;
            for (int i = 0, j = 0, y = 0, z = 0, k = 0; k < components.length; k++){
                if (components[k].contains("detail-content-body")){
                    inArticle = true;
                    continue;
                }

                if (inArticle){
                    if (components[k].contains("<p")){
                        if (i < article.size() && !inBlockquote && !inDiv){
                            Label label = createLabel(article.get(i).text(), WORDSIZE);
                            if (article.get(i).select("strong").size() > 0)
                                label.setFont(Font.font("Times New Roman", FontWeight.BOLD, WORDSIZE));
                            if (article.get(i).attr("style").compareTo("text-align:right") == 0)
                                label.setAlignment(Pos.CENTER_RIGHT);

                            content.getChildren().add(label);
                            i++;
                        }else if (z < blockquote.size() && inBlockquote){
                            Label label = createLabel(blockquote.get(z).text(), WORDSIZE);
                            label.setBackground(new Background(new BackgroundFill(Color.valueOf("#dddddd"), new CornerRadii(0), new Insets(0))));

                            if (blockquote.get(z).select("strong").size() > 0)
                                label.setFont(Font.font("Times New Roman", FontWeight.BOLD, WORDSIZE));
                            if (blockquote.get(z).attr("style").compareTo("text-align:right") == 0)
                                label.setAlignment(Pos.CENTER_RIGHT);

                            content.getChildren().add(label);
                            z++;
                        }
                    }
                    else if (components[k].contains("<figure") && j < images.size()){
                        Image image = new Image(images.get(j).attr("data-src"));
                        Label label = createImageLabel(image, images.get(j).select("em").text());

                        content.getChildren().add(label);
                        j++;
                    }
                    else if (components[k].contains("<li") && y < li.size()){
                        content.getChildren().add(createLabel(li.get(y).text(), WORDSIZE));
                        y++;
                    }
                    else if (components[k].contains("<blockquote"))
                        inBlockquote = true;
                    else if (components[k].contains("</blockquote>"))
                        inBlockquote = false;
                    else if (components[k].contains("<div"))
                        inDiv = true;
                    else if (components[k].contains("</div>"))
                        inDiv = false;
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

    private void dealException(Exception e, Item item){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Poor Internet Connection");
        alert.setHeaderText("Can't connect to\n" + item.getLink());
        alert.setContentText("Please check your internet connection.");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        TextArea area = new TextArea(sw.toString());
        alert.getDialogPane().setExpandableContent(area);
        alert.setOnCloseRequest(dialogEvent -> anchorPane.setEffect(null));
        anchorPane.setEffect(new BoxBlur(anchorPane.getWidth(), anchorPane.getHeight(), 1));
        alert.show();
    }

    public void menuCategories()  {
        new SceneSwitch(anchorPane).menuCategories();
    }

    public void menuHome(){
        new SceneSwitch(anchorPane).menuHome(categoryIndex);
    }

    public void nextArticle(){
        if (index == items.size() - 1) return;

        index++;
        item = items.get(index);
        readArticle();
        previousButton.setDisable(false);

        if (index == items.size())
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
