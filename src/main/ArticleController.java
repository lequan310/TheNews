package main;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
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
    @FXML private Button home;
    @FXML private Button category;

    private final int WORDSIZE = 18;
    private final int SPACE = 5;
    private final ArrayList<Item> items;
    private Item item = null;
    private int index = 0;

    public ArticleController(ArrayList<Item> items, int index){
        this.items = items;
        this.index = index;
        item = items.get(index);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (item.getImgSrc().compareTo("") != 0)
            thumbnail.setImage(new Image(item.getImgSrc()));
        else
            thumbnail.setImage(null);
        title.setText(item.getTitle());

        if (item.getSource() == Source.TT)
            readArticleTT(item.getLink());
        else if (item.getSource() == Source.ZING)
            readArticleZing(item.getLink());
        else if (item.getSource() == Source.ND)
            readArticleND(item.getLink());

        content.getChildren().addAll(createLabel("", WORDSIZE), createLabel("", WORDSIZE));
    }

    public void readArticleTT(String urlAddress){
        try{
            Document doc = Jsoup.connect(urlAddress).get();
            Elements body = doc.select("div.main-content-body"); // Select elements in the div with class main-content-body
            Elements images = body.select("div[type=Photo]"); // Select image elements from the main-content-body
            Elements article = body.select("div[id=main-detail-body] > p"); // Select text elements from the main-content-body
            Elements wrapNote = body.select("div[type=wrapnote] > p");
            Elements video = body.select("div[type=VideoStream]");

            String bodyHTML = body.toString();
            String[] components = bodyHTML.trim().split("\n");

            Label description = createDescription(body.select("h2").text());

            content.getChildren().clear();
            content.getChildren().add(createLabel("", SPACE));
            content.getChildren().add(description);

            Boolean inDiv = false, inWrapNote = false, inArticle = false;
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

                        Button btnPlayPause = createVideoButton(videoSrc);
                        Label label = createLabel(video.get(y).select("p").text(), 16);
                        label.setBackground(new Background(new BackgroundFill(Color.valueOf("#dddddd"), new CornerRadii(0), new Insets(0))));

                        content.getChildren().add(btnPlayPause);
                        content.getChildren().add(label);
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
            dealException(e, item);
        }
    }

    public void readArticleZing(String urlAddress){
        try{
            Document doc = Jsoup.connect(urlAddress).get();

            // Video article
            if (urlAddress.contains("https://zingnews.vn/video")){
                Elements body = doc.select("div[id=video-featured]");
                Elements videos = doc.select("video");
                Elements articles = doc.select("div.video-info");

                Button btnPlayPause = createVideoButton(videos.first().attr("src"));
                Label summary = createLabel(articles.select("p.video-summary").text(), WORDSIZE);
                Label author = createDescription(articles.select("span.video-author").text());
                author.setAlignment(Pos.CENTER_RIGHT);
                content.getChildren().addAll(btnPlayPause, summary, author);
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
                content.getChildren().addAll(createLabel("", SPACE), description);

                Boolean inArticle = false;
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
                            Button btnPlayPause = createVideoButton(video.get(y).attr("data-video-src"));
                            Label label = createLabel(video.get(y).select("figcaption").text(), 16);
                            label.setBackground(new Background(new BackgroundFill(Color.valueOf("#dddddd"), new CornerRadii(0), new Insets(0))));

                            content.getChildren().add(btnPlayPause);
                            content.getChildren().add(label);
                            y++;
                        }
                    }
                }

                Label author = createDescription(doc.getElementsByClass("author").text());
                author.setAlignment(Pos.CENTER_RIGHT);
                content.getChildren().addAll(createLabel("", SPACE), author);
            }
        }
        catch (Exception e){
            dealException(e, item);
        }
    }

    public void readArticleND(String urlAddress){
        try {
            Document doc = Jsoup.connect(urlAddress).get();
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

            content.getChildren().clear();
            content.getChildren().addAll(thumbnail, description);

            // Article and Images
            Boolean inArticle = false, inBlockquote = false, inDiv = false;
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
        } catch (Exception e){
            dealException(e, item);
        }
    }

    public Label createLabel(String text, int size){
        Label label = new Label(text);
        label.setFont(Font.font("Times New Roman", size));
        label.setTextFill(Color.valueOf("#ffffff"));
        label.setTextAlignment(TextAlignment.LEFT);
        label.setTextOverrun(OverrunStyle.CLIP);
        label.setWrapText(true);
        label.setPrefWidth(800);

        return label;
    }

    public Label createDescription(String text){
        Label description = new Label(text);
        description.setTextFill(Color.valueOf("#ffffff"));
        description.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        description.setPrefWidth(800);
        description.setWrapText(true);
        description.setTextOverrun(OverrunStyle.CLIP);

        return description;
    }

    public Label createImageLabel(Image image, String caption){
        // Create ImageView and Label, and set label graphic to image view
        final int MAX_WIDTH = 1000;
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        Label label = new Label(caption);
        imageView.setFitWidth(Math.min(image.getWidth(), MAX_WIDTH));
        label.setGraphic(imageView);

        // Adjust label position and size
        if (caption.compareTo("") != 0)
            label.setBackground(new Background(new BackgroundFill(Color.valueOf("#dddddd"), new CornerRadii(0), new Insets(0))));
        label.setContentDisplay(ContentDisplay.TOP);
        label.setAlignment(Pos.TOP_CENTER);
        label.setFont(Font.font("Arial", FontPosture.ITALIC, 16));
        label.setTextOverrun(OverrunStyle.CLIP);
        label.setWrapText(true);
        label.setPrefWidth(imageView.getFitWidth());

        return label;
    }

    public Button createVideoButton(String videoSrc){
        // Create media player
        Media media = new Media(videoSrc);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setFitWidth(800);
        mediaView.setPreserveRatio(true);

        // Assign media player to button
        Button btnPlayPause = new Button();
        btnPlayPause.setGraphic(mediaView);
        btnPlayPause.setDefaultButton(false);
        btnPlayPause.setOnAction(e -> {
            if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING))
                mediaPlayer.pause();
            else
                mediaPlayer.play();
        });

        return btnPlayPause;
    }

    public void dealException(Exception e, Item item){
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

    public void menuCategories(ActionEvent event)  {
        new SceneSwitch(anchorPane).menuCategories();
    }

    public void menuHome(ActionEvent event){
        new SceneSwitch(anchorPane).menuHome(0);
    }
}
