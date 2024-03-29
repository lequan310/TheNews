/*
  RMIT University Vietnam
  Course: INTE2512 Object-Oriented Programming
  Semester: 2021B
  Assessment: Final Project
  Created date: 01/08/2021
  Author: Pham Thanh Nam, s3878413
  Last modified date: 17/09/2021
  Author: Le Minh Quan, s3877969
  Acknowledgement:
  https://jsoup.org/
  https://www.baeldung.com/java-buffered-reader
  https://docs.oracle.com/javase/8/docs/api/javax/xml/ws/Service.html
  https://rmit.instructure.com/courses/88207/pages/w5-whats-happening-this-week?module_item_id=3237094
  https://rmit.instructure.com/courses/88207/pages/w10-whats-happening-this-week?module_item_id=3237113
*/
package main.Model;

import javafx.concurrent.Task;
import main.Storage.Storage;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class NewsController extends Task<Void> {
    private final int TIMEOUT = 10000;

    private static NewsController newsController = null;
    private final ArrayList<Item> items = Storage.getInstance().getItems(); // List of items that is scraped and sorted to be displayed
    private final Map<String, Item> itemStorage = Storage.getInstance().getItemStorage(); // Storage of item to reuse scraped items
    private final ForkJoinPool pool = ForkJoinPool.commonPool(); // Thread pool

    // List of URL to scrape from in order: New, Covid, Politics, Business, Technology, Health, Sports, Entertainment, World, Others
    private final ArrayList<String> VNEXPRESS = new ArrayList<>(
            List.of("https://vnexpress.net/rss/tin-moi-nhat.rss", "https://vnexpress.net/rss/covid-19.rss", "https://vnexpress.net/rss/chinh-tri.rss",
                    "https://vnexpress.net/rss/kinh-doanh.rss", "https://vnexpress.net/rss/so-hoa.rss", "https://vnexpress.net/rss/suc-khoe.rss",
                    "https://vnexpress.net/rss/the-thao.rss", "https://vnexpress.net/rss/giai-tri.rss", "https://vnexpress.net/rss/the-gioi.rss",
                    "https://vnexpress.net/rss/cuoi.rss", "https://vnexpress.net/rss/giao-duc.rss", "https://vnexpress.net/rss/khoa-hoc.rss",
                    "https://vnexpress.net/rss/y-kien.rss", "https://vnexpress.net/rss/phap-luat.rss", "https://vnexpress.net/rss/tam-su.rss",
                    "https://vnexpress.net/rss/du-lich.rss", "https://vnexpress.net/rss/gia-dinh.rss"));
    private final ArrayList<String> TUOITRE = new ArrayList<>(
            List.of("https://tuoitre.vn/rss/tin-moi-nhat.rss", "https://tuoitre.vn/rss/thoi-su.rss", "https://tuoitre.vn/rss/phap-luat.rss",
                    "https://tuoitre.vn/rss/kinh-doanh.rss", "https://tuoitre.vn/rss/cong-nghe.rss", "https://tuoitre.vn/rss/suc-khoe.rss",
                    "https://tuoitre.vn/rss/the-thao.rss", "https://tuoitre.vn/rss/giai-tri.rss", "https://tuoitre.vn/rss/the-gioi.rss", "https://tuoitre.vn/rss/xe.rss",
                    "https://tuoitre.vn/rss/giao-duc.rss", "https://tuoitre.vn/rss/nhip-song-tre.rss", "https://tuoitre.vn/rss/ban-doc-lam-bao.rss",
                    "https://tuoitre.vn/rss/van-hoa.rss", "https://tuoitre.vn/rss/du-lich.rss"));
    private final ArrayList<String> THANHNIEN = new ArrayList<>(
            List.of("https://thanhnien.vn/rss/home.rss", "https://thanhnien.vn/rss/thoi-su.rss", "https://thanhnien.vn/rss/thoi-su/chinh-tri.rss",
                    "https://thanhnien.vn/rss/tai-chinh-kinh-doanh.rss", "https://thanhnien.vn/rss/cong-nghe.rss", "https://thanhnien.vn/rss/suc-khoe.rss",
                    "https://thanhnien.vn/rss/the-thao.rss", "https://thanhnien.vn/rss/giai-tri.rss", "https://thanhnien.vn/rss/the-gioi.rss",
                    "https://thanhnien.vn/rss/game.rss", "https://thanhnien.vn/rss/giao-duc.rss", "https://thanhnien.vn/rss/ban-can-biet.rss",
                    "https://thanhnien.vn/rss/gioi-tre.rss", "https://thanhnien.vn/rss/van-hoa.rss", "https://thanhnien.vn/rss/doi-song.rss",
                    "https://thanhnien.vn/xe/xe.rss"));
    private final ArrayList<String> ZING = new ArrayList<>(
            List.of("https://zingnews.vn/", "https://zingnews.vn/suc-khoe.html", "https://zingnews.vn/chinh-tri.html",
                    "https://zingnews.vn/kinh-doanh-tai-chinh.html", "https://zingnews.vn/cong-nghe.html", "https://zingnews.vn/suc-khoe.html",
                    "https://zingnews.vn/the-thao.html", "https://zingnews.vn/giai-tri.html", "https://zingnews.vn/the-gioi.html",
                    "https://zingnews.vn/doi-song.html", "https://zingnews.vn/giao-duc.html", "https://zingnews.vn/du-lich.html",
                    "https://zingnews.vn/oto-xe-may.html", "https://zingnews.vn/phap-luat.html"));
    private final ArrayList<String> NHANDAN = new ArrayList<>(
            List.of("https://nhandan.vn/", "https://nhandan.vn/y-te", "https://nhandan.vn/chinhtri", "https://nhandan.vn/kinhte",
                    "https://nhandan.vn/khoahoc-congnghe", "https://nhandan.vn/y-te", "https://nhandan.vn/thethao", "https://nhandan.vn/vanhoa",
                    "https://nhandan.vn/thegioi", "https://nhandan.vn/xahoi", "https://nhandan.vn/giaoduc", "https://nhandan.vn/bandoc",
                    "https://nhandan.vn/phapluat", "https://nhandan.vn/moi-truong", "https://nhandan.vn/phapluat"));

    private int categoryIndex, progress = 0, maxProgress; // Index to read from the arrays below
    private String error = ""; // Error message

    private NewsController() {
    }

    public static NewsController getInstance() {
        if (newsController == null) {
            synchronized (NewsController.class) {
                if (newsController == null)
                    newsController = new NewsController();
            }
        }
        return newsController;
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

    // Getters
    public String getError() {
        return this.error;
    }

    public void setCategoryIndex(int categoryIndex) {
        this.categoryIndex = categoryIndex;
    }

    public void start() {
        try {
            // Initialize values and loading progress
            long start = System.currentTimeMillis();
            items.clear();
            progress = 0;
            error = "";
            updateProgress(0, 1);
            scrapeArticles(); // Scrape articles from all new outlets

            pool.awaitQuiescence(TIMEOUT * 2, TimeUnit.MILLISECONDS); // Wait until all tasks done or timeout
            System.out.println("Achieve " + items.size() + " items: " + (System.currentTimeMillis() - start) + " ms\n");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            synchronized (this) {
                if (items.size() > 1) {
                    // Remove duplicate and then sort
                    items.sort(Comparator.comparing(Item::getLink));
                    for (int i = 1; i < items.size(); i++) {
                        if (items.get(i).equalTo(items.get(i - 1))) {
                            items.remove(i);
                            i--;
                        }
                    }
                }
                Collections.sort(items); // Sort item by published date and time
                updateProgress(1, 1); // Set progress to full
            }

            System.gc(); // Call garbage collector
        }
    }

    @Override
    protected Void call() {
        return null;
    }

    private BufferedReader readRSS(String urlAddress) throws IOException {
        // Connect to rss URL
        URL rssURL = new URL(urlAddress);
        URLConnection connection = rssURL.openConnection();
        connection.setReadTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);

        // return new BufferedReader with input stream is the url
        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    // Scrape articles from category
    private void scrapeVE(List<String> links) {
        for (String urlAddress : links) {
            pool.execute(() -> {
                try {
                    // Creating buffered reader to read RSS file and extract items information
                    BufferedReader in = readRSS(urlAddress);

                    String title = "", pubDate = "", link = "", imgSrc = "", line;
                    LocalDateTime date = LocalDateTime.MIN;
                    boolean inItem = false;

                    // Loop through lines in RSS
                    while ((line = in.readLine()) != null) {
                        try {
                            // If line contains <item> then it's start of an item
                            if (line.contains("<item>")) {
                                inItem = true;
                            }
                            // Get item title
                            else if (line.contains("<title>") && inItem) {
                                title = extract(line, "<title>", "</title>");

                                if (categoryIndex == 1 && !checkCovidKeyword(title)) inItem = false;
                            }
                            // Get item published date
                            else if (line.contains("<pubDate>") && inItem) {
                                pubDate = extract(line, "<pubDate>", "</pubDate>");
                                DateTimeFormatter df = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss +0700");
                                date = LocalDateTime.parse(pubDate, df);
                            }
                            // Get item source link
                            else if (line.contains("<link>") && inItem) {
                                link = extract(line, "<link>", "</link>");

                                // Use existed item if exists
                                if (itemStorage.containsKey(link)) {
                                    addItem(itemStorage.get(link));
                                    inItem = false;
                                }
                            }
                            // Get item thumbnail link in description
                            else if (line.contains("<description>") && inItem) {
                                try {
                                    imgSrc = extract(line, "<description>", "</description>");
                                    imgSrc = extract(imgSrc, "<img src=\"", "\"");
                                } catch (StringIndexOutOfBoundsException e) {
                                    imgSrc = "";
                                }
                            }
                            // Add item to list at the end of item (when all information of an item object is gathered)
                            else if (line.contains("</item>") && inItem) {
                                Item item = new Item(title, link, date, imgSrc, Item.Source.VE);

                                addItem(item);
                                itemStorage.put(link, item);
                                inItem = false;
                            }
                        } catch (Exception exception) {
                            inItem = false;
                            System.out.println(exception.getMessage());
                        }
                    }

                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Read timeout: " + urlAddress);
                    error += urlAddress + ": " + e.getMessage() + "\n";
                }
            });
        }
        System.out.println("Done VN Express");
    }

    // Article scrapper for TuoiTre
    private void scrapeTuoiTre(List<String> links) {
        for (String urlAddress : links) {
            pool.execute(() -> {
                try {
                    // Creating buffered reader to read RSS file and extract items information
                    BufferedReader in = readRSS(urlAddress);

                    String title = "", pubDate = "", link = "", imgSrc = "", line;
                    LocalDateTime date = LocalDateTime.MIN;
                    boolean inItem = false;

                    // Loop through lines in RSS
                    while ((line = in.readLine()) != null) {
                        try {
                            if (line.contains("<item>")) {
                                inItem = true;
                            }
                            // Extract item title
                            else if (line.contains("<title>") && inItem) {
                                title = extract(line, "<title>", "</title>");
                                title = extract(title, "<![CDATA[", "]]>");

                                if (categoryIndex == 1 && !checkCovidKeyword(title)) inItem = false;
                            }
                            // Extract item published date
                            else if (line.contains("<pubDate>") && inItem) {
                                pubDate = extract(line, "<pubDate>", "</pubDate>");
                                pubDate = extract(pubDate, "<![CDATA[", " GMT+7");
                                DateTimeFormatter df = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss");
                                date = LocalDateTime.parse(pubDate, df);
                            }
                            // Extract item source link
                            else if (line.contains("<link>") && inItem) {
                                link = extract(line, "<link>", "</link>");
                                link = extract(link, "<![CDATA[", "]]>");

                                // Use existed item if exists
                                if (itemStorage.containsKey(link)) {
                                    addItem(itemStorage.get(link));
                                    inItem = false;
                                }
                            }
                            // Extract item thumbnail link in description
                            else if (line.contains("<description>") && inItem) {
                                try {
                                    imgSrc = extract(line, "<description>", "</description>");
                                    imgSrc = extract(imgSrc, "<img src=\"", "\"");
                                    imgSrc = imgSrc.replace("zoom/80_50/", "");
                                } catch (StringIndexOutOfBoundsException e) {
                                    imgSrc = "";
                                }
                            }
                            // Add item to list of items
                            else if (line.contains("</item>") && inItem) {
                                Item item = new Item(title, link, date, imgSrc, Item.Source.TT);

                                addItem(item);
                                itemStorage.put(link, item);
                                inItem = false;
                            }
                        } catch (Exception exception) {
                            inItem = false;
                            System.out.println(exception.getMessage());
                        }
                    }

                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Read timeout: " + urlAddress);
                    error += urlAddress + ": " + e.getMessage() + "\n";
                }
            });
        }
        System.out.println("Done Tuoi Tre");
    }

    // Article scrapper for Thanh Nien
    private void scrapeThanhNien(List<String> links) {
        for (String urlAddress : links) {
            pool.execute(() -> {
                try {
                    // Creating buffered reader to read RSS file and extract items information
                    BufferedReader in = readRSS(urlAddress);

                    String title = "", pubDate = "", link = "", imgSrc = "", line, previousLine = "";
                    LocalDateTime date = LocalDateTime.MIN;
                    boolean inItem = false, concat = false;

                    while ((line = in.readLine()) != null) {
                        try {
                            if (line.contains("<item>")) {
                                inItem = true;
                            }

                            if (inItem) {
                                if (!line.endsWith("</item>")) {
                                    previousLine += line;
                                    concat = true;
                                    continue;
                                } else {
                                    if (concat) {
                                        line = previousLine + line;
                                        previousLine = "";
                                        concat = false;
                                    }
                                }

                                // Extract title
                                title = extract(line, "<title>", "</title>");
                                if (title.contains("<![CDATA[ "))
                                    title = extract(title, "<![CDATA[ ", "]]>");
                                if (categoryIndex == 1 && !checkCovidKeyword(title)) {
                                    inItem = false;
                                    continue;
                                }

                                // Extract link
                                link = extract(line, "<link>", "</link>");
                                // Use existed item if exists
                                if (itemStorage.containsKey(link)) {
                                    addItem(itemStorage.get(link));
                                    inItem = false;
                                    continue;
                                }

                                //Extract pubDate
                                pubDate = extract(line, "<pubDate>", " GMT");
                                DateTimeFormatter df = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss");
                                date = LocalDateTime.parse(pubDate, df);
                                date = date.plusHours(7);

                                // Extract image
                                imgSrc = extract(line, "<image>", "/>");
                                if (imgSrc.contains(".gif")) {
                                    imgSrc = extract(imgSrc, "https://image.thanhnien.vn", ".gif");
                                    imgSrc = "https://image.thanhnien.vn" + imgSrc + ".gif";
                                } else if (imgSrc.contains(".png")) {
                                    imgSrc = extract(imgSrc, "https://image.thanhnien.vn", ".png");
                                    imgSrc = "https://image.thanhnien.vn" + imgSrc + ".png";
                                } else if (imgSrc.contains("jpg")) {
                                    imgSrc = extract(imgSrc, "https://image.thanhnien.vn", ".jpg");
                                    imgSrc = "https://image.thanhnien.vn" + imgSrc + ".jpg";
                                } else if (imgSrc.contains("jpeg")) {
                                    imgSrc = extract(imgSrc, "https://image.thanhnien.vn", ".jpeg");
                                    imgSrc = "https://image.thanhnien.vn" + imgSrc + ".jpeg";
                                }
                                try {
                                    imgSrc = imgSrc.replaceAll(".vn/.*/uploaded", ".vn/uploaded");
                                } catch (StringIndexOutOfBoundsException e) {
                                    continue;
                                }

                                Item item = new Item(title, link, date, imgSrc, Item.Source.TN);
                                addItem(item);
                                itemStorage.put(link, item);
                                inItem = false;
                            }
                        } catch (Exception exception) {
                            inItem = false;
                            System.out.println(exception.getMessage());
                        }
                    }

                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Read timeout: " + urlAddress);
                    error += urlAddress + ": " + e.getMessage() + "\n";
                }
            });
        }
        System.out.println("Done Thanh Nien");
    }

    // Article scrapper for ZingNews
    private void scrapeZing(List<String> links) {
        for (String urlAddress : links) {
            pool.execute(() -> {
                try {
                    Connection.Response response = Jsoup.connect(urlAddress).timeout(TIMEOUT).execute();
                    if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());

                    // Connect to URL and add all article element into list
                    Document doc = response.parse();
                    Elements body = doc.select("section[id~=.*-latest]");
                    Elements featured = doc.select("section[id~=.*-featured]");
                    body.addAll(featured);
                    Elements article = body.select("article.article-item");
                    int count = article.size();

                    // Loop through each article-item
                    for (int i = 0; i < count; i++) {
                        int current = i;
                        pool.execute(() -> {
                            try {
                                Element e = article.get(current);
                                String title = "", pubDate = "", link = "", imgSrc = "";
                                boolean add = true;
                                LocalDateTime date = LocalDateTime.MIN;

                                // Get title
                                title = e.getElementsByClass("article-title").text();
                                if (categoryIndex == 1 && !checkCovidKeyword(title)) add = false;

                                // Get link
                                link = e.select("a").attr("href");
                                link = "https://zingnews.vn" + link;

                                // Use existed item if exists
                                if (itemStorage.containsKey(link)) {
                                    if (add) {
                                        addItem(itemStorage.get(link));
                                    }
                                } else {
                                    // Get image source
                                    imgSrc = e.select("img").attr("src");
                                    if (!imgSrc.contains("https")) imgSrc = e.select("img").attr("data-src");
                                    try {
                                        imgSrc = imgSrc.replaceAll("w\\d\\d\\d/", "");
                                    } catch (StringIndexOutOfBoundsException exception) {
                                    }

                                    // Get published date
                                    pubDate = e.select("span.time").text();
                                    pubDate += " " + e.select("span.date").text();
                                    pubDate = pubDate.trim();

                                    if (pubDate.equals("")) pubDate = e.select("span.friendly-time").text();
                                    DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm d/M/yyyy");
                                    date = LocalDateTime.parse(pubDate, df);

                                    // Create and add news item to list
                                    Item item = new Item(title, link, date, imgSrc, Item.Source.ZING);
                                    if (add) {
                                        addItem(item);
                                        itemStorage.put(link, item);
                                    }
                                }
                            } catch (Exception exception) {
                                System.out.println(exception.getMessage());
                            }
                        });
                    }
                } catch (IOException e) {
                    System.out.println("Read timeout: " + urlAddress);
                    error += urlAddress + ": " + e.getMessage() + "\n";
                }
            });
        }
        System.out.println("Done Zing");
    }

    // Article scrapper for NhanDan
    private void scrapeNhanDan(List<String> links) {
        for (String urlAddress : links) {
            pool.execute(() -> {
                try {
                    Connection.Response response = Jsoup.connect(urlAddress).timeout(TIMEOUT).execute();
                    if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());

                    // Connect to URL and add all article element into list
                    Document doc = response.parse();
                    Elements body = doc.select("div[class*=uk-width-3-4@m]");
                    Elements article = body.select("article");
                    int count = article.size();

                    // Loop through article items in list
                    for (int i = 0; i < count; i++) {
                        int current = i;
                        pool.execute(() -> {
                            try {
                                Element e = article.get(current);
                                String title = "", pubDate = "", link = "", imgSrc = "";
                                boolean add = true;
                                LocalDateTime date = LocalDateTime.MIN;

                                // Get title
                                title = e.getElementsByClass("box-title").text();
                                if (title.equals("")) add = false;
                                if (categoryIndex == 1 && !checkCovidKeyword(title)) add = false;

                                // Get link
                                link = e.select("a").attr("href");
                                if (!link.contains("https://")) link = "https://nhandan.vn" + link;

                                // Use existed item if exists
                                if (itemStorage.containsKey(link)) {
                                    if (add) {
                                        addItem(itemStorage.get(link));
                                    }
                                } else {
                                    // Get image source
                                    imgSrc = e.select("img").attr("data-src");
                                    try {
                                        imgSrc = imgSrc.replace("resize/320x-/", "");
                                    } catch (StringIndexOutOfBoundsException exception) {
                                    }

                                    // Get pubDate
                                    pubDate = e.select("div[class*=box-meta]").text();
                                    if (!pubDate.equals("")) {
                                        DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm dd/M/yyyy");
                                        date = LocalDateTime.parse(pubDate, df);
                                    } else {
                                        try {
                                            Connection.Response tempResponse = Jsoup.connect(link).timeout(5000).execute();
                                            if (tempResponse.statusCode() >= 400) throw new IOException();

                                            Document temp = tempResponse.parse();
                                            pubDate = temp.select("div.box-date").text();

                                            if (!pubDate.equals("")) {
                                                pubDate = pubDate.substring(pubDate.indexOf(", ") + 2);
                                            }

                                            try {
                                                DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm");
                                                date = LocalDateTime.parse(pubDate, df);
                                            } catch (DateTimeParseException exception) {
                                                try {
                                                    DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                                                    date = LocalDateTime.parse(pubDate, df);
                                                } catch (DateTimeParseException ex) {
                                                    add = false;
                                                }
                                            }
                                        } catch (IOException exception) {
                                            add = false;
                                        }
                                    }

                                    // Create and add news item to list
                                    Item item = new Item(title, link, date, imgSrc, Item.Source.ND);
                                    if (add) {
                                        addItem(item);
                                        itemStorage.put(link, item);
                                    }
                                }
                            } catch (Exception exception) {
                                System.out.println(exception.getMessage());
                            }
                        });
                    }
                } catch (IOException e) {
                    System.out.println("Read timeout: " + urlAddress);
                    error += urlAddress + ": " + e.getMessage() + "\n";
                }
            });
        }
        System.out.println("Done Nhan Dan");
    }

    // Obtain article list
    private void scrapeArticles() {
        System.gc(); // Call Garbage Collector

        // Scrape which URL depends on category
        if (categoryIndex == 0) {
            maxProgress = 1000;
            pool.execute(() -> scrapeVE(Collections.singletonList(VNEXPRESS.get(0))));
            pool.execute(() -> scrapeTuoiTre(Collections.singletonList(TUOITRE.get(0))));
            pool.execute(() -> scrapeThanhNien(Collections.singletonList(THANHNIEN.get(0))));
            pool.execute(() -> scrapeZing(ZING));
            pool.execute(() -> scrapeNhanDan(NHANDAN.subList(categoryIndex + 1, NHANDAN.size())));
        } else if (categoryIndex == 1) {
            maxProgress = 500;
            pool.execute(() -> scrapeVE(Arrays.asList(VNEXPRESS.get(1), VNEXPRESS.get(5), VNEXPRESS.get(8))));
            pool.execute(() -> scrapeTuoiTre(Arrays.asList(TUOITRE.get(1), TUOITRE.get(5), TUOITRE.get(8))));
            pool.execute(() -> scrapeThanhNien(Arrays.asList(THANHNIEN.get(1), THANHNIEN.get(5), THANHNIEN.get(8))));
            pool.execute(() -> scrapeZing(Arrays.asList(ZING.get(1), ZING.get(5), ZING.get(8))));
            pool.execute(() -> scrapeNhanDan(Arrays.asList(NHANDAN.get(1), NHANDAN.get(5), NHANDAN.get(8))));
        } else if (categoryIndex == 9) {
            maxProgress = 1500;
            pool.execute(() -> scrapeVE(VNEXPRESS.subList(categoryIndex, VNEXPRESS.size())));
            pool.execute(() -> scrapeTuoiTre(TUOITRE.subList(categoryIndex, TUOITRE.size())));
            pool.execute(() -> scrapeThanhNien(THANHNIEN.subList(categoryIndex, THANHNIEN.size())));
            pool.execute(() -> scrapeZing(ZING.subList(categoryIndex, ZING.size())));
            pool.execute(() -> scrapeNhanDan(NHANDAN.subList(categoryIndex, NHANDAN.size())));
        } else {
            maxProgress = 250;
            pool.execute(() -> scrapeVE(Collections.singletonList(VNEXPRESS.get(categoryIndex))));
            pool.execute(() -> scrapeTuoiTre(Collections.singletonList(TUOITRE.get(categoryIndex))));
            pool.execute(() -> scrapeThanhNien(Collections.singletonList(THANHNIEN.get(categoryIndex))));
            pool.execute(() -> scrapeZing(Collections.singletonList(ZING.get(categoryIndex))));
            pool.execute(() -> scrapeNhanDan(Collections.singletonList(NHANDAN.get(categoryIndex))));
        }
    }

    // Check if title of article is in covid category using keywords
    private static boolean checkCovidKeyword(String title) {
        final String check = title.toLowerCase();
        final String[] keywords = {"cov", "f0", "f1", "vaccine", "vắc xin", "xét nghiệm", "phong tỏa", "mũi", "biến thể", "kháng thể",
                "nhiễm", "dịch", "test", "pcr", "âm tính", "dương tính", "giãn cách", "chỉ thị", "delta", "vùng đỏ", "vùng cam", "thẻ xanh"};

        for (String s : keywords) {
            if (check.contains(s)) {
                return true;
            }
        }

        return false;
    }

    private void addItem(Item item) {
        synchronized (this) {
            if (!items.contains(item)) {
                items.add(item);
                updateProgress(progress++, maxProgress);
            }
        }
    }
}