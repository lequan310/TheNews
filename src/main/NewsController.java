package main;

import javafx.concurrent.Task;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NewsController extends Task<Void> {
    private final ArrayList<Item> items = new ArrayList<>(); // List of items that is scraped and sorted to be displayed
    private final ExecutorService es = Executors.newCachedThreadPool();

    // List of URL to scrape from
    // New, Covid, Politics, Business, Technology, Health, Sports, Entertainment, World, Others
    private final ArrayList<String> VNEXPRESS = new ArrayList<>(
    List.of("https://vnexpress.net/rss/tin-moi-nhat.rss", "https://vnexpress.net/rss/tin-noi-bat.rss", "https://vnexpress.net/thoi-su/chinh-tri",
    "https://vnexpress.net/kinh-doanh", "https://vnexpress.net/so-hoa", "https://vnexpress.net/rss/suc-khoe.rss", "https://vnexpress.net/the-thao",
    "https://vnexpress.net/giai-tri", "https://vnexpress.net/rss/the-gioi.rss", "https://vnexpress.net/rss/cuoi.rss",
    "https://vnexpress.net/rss/giao-duc.rss", "https://vnexpress.net/rss/khoa-hoc.rss", "https://vnexpress.net/rss/y-kien.rss"));
    private final ArrayList<String> TUOITRE = new ArrayList<>(
    List.of ("https://tuoitre.vn/rss/tin-moi-nhat.rss", "https://tuoitre.vn/rss/suc-khoe.rss", "https://tuoitre.vn/tim-kiem.htm?keywords=ch%C3%ADnh%20tr%E1%BB%8B",
    "https://tuoitre.vn/rss/kinh-doanh.rss", "https://tuoitre.vn/rss/cong-nghe.rss", "https://tuoitre.vn/rss/suc-khoe.rss",
    "https://tuoitre.vn/rss/the-thao.rss", "https://tuoitre.vn/rss/giai-tri.rss", "https://tuoitre.vn/rss/the-gioi.rss", "https://tuoitre.vn/rss/xe.rss",
    "https://tuoitre.vn/rss/giao-duc.rss", "https://tuoitre.vn/rss/nhip-song-tre.rss", "https://tuoitre.vn/rss/ban-doc-lam-bao.rss"));
    private final ArrayList<String> THANHNIEN = new ArrayList<>(
    List.of ("https://thanhnien.vn/rss/home.rss", "https://thanhnien.vn/rss/suc-khoe.rss", "https://thanhnien.vn/rss/thoi-su/chinh-tri.rss",
    "https://thanhnien.vn/rss/tai-chinh-kinh-doanh.rss", "https://thanhnien.vn/rss/cong-nghe.rss", "https://thanhnien.vn/rss/suc-khoe.rss",
    "https://thethao.thanhnien.vn/rss/home.rss", "https://thanhnien.vn/rss/giai-tri.rss", "https://thanhnien.vn/rss/the-gioi.rss",
    "https://game.thanhnien.vn/rss/home.rss", "https://thanhnien.vn/rss/giao-duc.rss", "https://thanhnien.vn/rss/ban-can-biet.rss", "https://thanhnien.vn/rss/gioi-tre.rss"));
    private final ArrayList<String> ZING = new ArrayList<>(
    List.of ("https://zingnews.vn/", "https://zingnews.vn/suc-khoe.html", "https://zingnews.vn/chinh-tri.html",
    "https://zingnews.vn/kinh-doanh-tai-chinh.html", "https://zingnews.vn/cong-nghe.html", "https://zingnews.vn/suc-khoe.html",
    "https://zingnews.vn/the-thao.html", "https://zingnews.vn/giai-tri.html", "https://zingnews.vn/the-gioi.html",
    "https://zingnews.vn/doi-song.html", "https://zingnews.vn/giao-duc.html", "https://zingnews.vn/du-lich.html", "https://zingnews.vn/oto-xe-may.html"));
    private final ArrayList<String> NHANDAN = new ArrayList<>(
    List.of ("https://nhandan.vn/", "https://nhandan.vn/y-te", "https://nhandan.vn/chinhtri", "https://nhandan.vn/kinhte",
    "https://nhandan.vn/khoahoc-congnghe", "https://nhandan.vn/y-te", "https://nhandan.vn/thethao", "https://nhandan.vn/vanhoa",
    "https://nhandan.vn/thegioi", "https://nhandan.vn/xahoi", "https://nhandan.vn/giaoduc", "https://nhandan.vn/bandoc", "https://nhandan.vn/phapluat"));

    private volatile boolean t1, t2, t3, t4, t5;
    private volatile int progress = 0, maxProgress = 0; // Index to read from the arrays below
    private int categoryIndex;
    private String error = ""; // Error message

    public NewsController(int categoryIndex) {
        this.categoryIndex = categoryIndex;

        // If category is Others
        if (categoryIndex == 9) {
            maxProgress = 500;
            scrapeAll(categoryIndex, 4);
        }
        // If category is not Others
        else {
            maxProgress = 250;
            scrapeAll(categoryIndex, 1);
        }
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
    public ArrayList<Item> getItems() {
        return this.items;
    }

    public String getError() {
        return this.error;
    }

    @Override
    protected Void call() {
        try {
            // Sort items and update progress bar
            long start = System.currentTimeMillis();
            while (!t1 || !t2 || !t3 || !t4 || !t5) {}

            es.shutdown();
            es.awaitTermination(10, TimeUnit.SECONDS);
            Collections.sort(items);
            System.out.println("\nAchieve item list: " + (System.currentTimeMillis() - start) + " ms");

            // Remove duplicate
            for (int i = 1; i < items.size(); i++) {
                if (items.get(i).equalTo(items.get(i - 1))){
                    items.remove(i);
                    i--;
                }
            }

            updateProgress(1, 1);
            return null;
        }
        catch (Exception e) {
            return null;
        }
    }

    private void scrapeVE(List<String> links) {
        for (String urlAddress : links) {
            es.submit(() -> {
                try {
                    if (urlAddress.contains(".rss")) {
                        // Creating buffered reader to read RSS file and extract items information
                        URL rssURL = new URL(urlAddress);
                        BufferedReader in = new BufferedReader(new InputStreamReader(rssURL.openStream()));
                        String title = "", pubDate = "", link = "", imgSrc = "", line;
                        LocalDateTime date = LocalDateTime.MIN;
                        boolean inItem = false;

                        // Loop through lines in RSS
                        while ((line = in.readLine()) != null) {
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
                                inItem = false;
                                Item item = new Item(title, link, date, imgSrc, Item.Source.VE);
                                items.add(item);
                                loadProgress(); // updateProgress(progress++, maxProgress);
                            }
                        }

                        in.close();
                    }
                    else {
                        Connection.Response response = Jsoup.connect(urlAddress).timeout(10000).execute();
                        if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());

                        Document doc = response.parse();
                        Elements article = doc.select("article");
                        int count = article.size();

                        for (int i = 0; i < count; i++) {
                            int current = i;
                            if (!es.isShutdown())
                                es.execute(() -> {
                                    Element e = article.get(current);
                                    String title = "", pubDate = "", link = "", imgSrc = "";
                                    boolean add = true;
                                    LocalDateTime date = LocalDateTime.MIN;

                                    // Get title
                                    title = e.select("h3").text();
                                    if (title.equals("")) {
                                        title = e.select("h2").text();
                                    }
                                    if (title.equals("")) add = false;
                                    if (categoryIndex == 1 && !checkCovidKeyword(title)) add = false;

                                    // Get article link and thumbnail url
                                    link = e.select(" a").attr("href");
                                    imgSrc = e.select("div.thumb-art").select("img").attr("data-src");

                                    try {
                                        Connection.Response tempResponse = Jsoup.connect(urlAddress).timeout(10000).execute();
                                        if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());

                                        Document temp = tempResponse.parse();
                                        if (imgSrc.equals("")) // Find first image in article if can't find thumbnail
                                            imgSrc = temp.select("article.fck_detail").select("img").attr("data-src");

                                        // Get published date
                                        pubDate = temp.select("span.date").text();
                                        if (pubDate.equals("")) pubDate = temp.select("span.time").text();
                                        if (pubDate.equals("")) add = false;

                                        pubDate = extract(pubDate, ", ", " (GMT+7)");
                                        DateTimeFormatter df = DateTimeFormatter.ofPattern("d/M/yyyy, HH:mm");
                                        date = LocalDateTime.parse(pubDate, df);
                                    } catch (Exception exception) {
                                        add = false;
                                    }

                                    // Create and add news item to list
                                    Item item = new Item(title, link, date, imgSrc, Item.Source.VE);
                                    if (add) items.add(item);
                                    loadProgress(); // updateProgress(progress++, maxProgress);
                                });
                        }
                    }
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    System.out.println("Can't connect to " + urlAddress);
                    error += urlAddress + ": " + e.getMessage() + "\n";
                }
            });
        }
        t1 = true;
    }

    private void scrapeTuoiTre(List<String> links) {
        for (String urlAddress : links) {
            es.submit(() -> {
                try {
                    if (urlAddress.contains(".rss")) {
                        t2 = true;
                        // Creating buffered reader to read RSS file and extract items information
                        URL rssURL = new URL(urlAddress);
                        BufferedReader in = new BufferedReader(new InputStreamReader(rssURL.openStream()));
                        String title = "", pubDate = "", link = "", imgSrc = "", line;
                        LocalDateTime date = LocalDateTime.MIN;
                        boolean inItem = false;

                        // Loop through lines in RSS
                        while ((line = in.readLine()) != null) {
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
                                inItem = false;
                                Item item = new Item(title, link, date, imgSrc, Item.Source.TT);
                                items.add(item);
                                loadProgress(); // updateProgress(progress++, maxProgress);
                            }
                        }

                        in.close();
                    }
                    else {
                        Connection.Response response = Jsoup.connect(urlAddress).timeout(10000).execute();
                        if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());

                        Document doc = response.parse();
                        Elements article = doc.select("li.news-item");
                        int count = article.size();

                        for (int i = 0; i < count; i++) {
                            int current = i;
                            if (!es.isShutdown())
                                es.execute(() -> {
                                    Element e = article.get(current);
                                    String title = "", pubDate = "", link = "", imgSrc = "";
                                    boolean add = true;
                                    LocalDateTime date = LocalDateTime.MIN;

                                    // Get title
                                    title = e.select("h3").text();
                                    if (title.equals("")) add = false;
                                    if (categoryIndex == 1 && !checkCovidKeyword(title)) add = false;

                                    // Get article link and thumbnail url
                                    link = e.select("a").attr("href");
                                    link = "https://tuoitre.vn" + link;
                                    imgSrc = e.select("img").attr("src");
                                    try {
                                        imgSrc = imgSrc.replace("zoom/212_132/", "");
                                    }
                                    catch (StringIndexOutOfBoundsException exception) {}

                                    try {
                                        Document temp = Jsoup.connect(link).timeout(10000).get();

                                        // Get published date
                                        pubDate = temp.select("div.date-time").text();
                                        pubDate = pubDate.replace(" GMT+7", "");
                                        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                                        date = LocalDateTime.parse(pubDate, df);
                                    } catch (Exception exception) { add = false; }

                                    // Create and add news item to list
                                    Item item = new Item(title, link, date, imgSrc, Item.Source.TT);
                                    if (add) items.add(item);
                                    loadProgress(); // updateProgress(progress++, maxProgress);
                                });
                        }
                    }
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    System.out.println("Can't connect to " + urlAddress);
                    error += urlAddress + ": " + e.getMessage() + "\n";
                }
            });
        }
        t2 = true;
    }

    private void scrapeThanhNien(List<String> links) {
        for (String urlAddress : links) {
            es.submit(() -> {
                try {
                    // Creating buffered reader to read RSS file and extract items information
                    URL rssURL = new URL(urlAddress);
                    BufferedReader in = new BufferedReader(new InputStreamReader(rssURL.openStream()));
                    String title = "", pubDate = "", link = "", imgSrc = "", line;
                    LocalDateTime date = LocalDateTime.MIN;
                    boolean inItem = false;

                    // Loop through each line in RSS file
                    while ((line = in.readLine()) != null) {
                        if (line.contains("<item>"))
                            inItem = true;

                        if (inItem) {
                            try {
                                // Extract title
                                title = extract(line, "<title>", "</title>");
                                if (title.contains("<![CDATA[ "))
                                    title = extract(title, "<![CDATA[ ", "]]>");
                                if (categoryIndex == 1 && !checkCovidKeyword(title)) continue;
                                title = title.replaceAll("<label>", "");
                                title = title.replaceAll("</label>", " -");

                                // Extract link
                                link = extract(line, "<link>", "</link>");

                                // Extract published date
                                pubDate = extract(line, "<pubDate>", " GMT");
                                DateTimeFormatter df = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss");
                                date = LocalDateTime.parse(pubDate, df);
                                date = date.plusHours(7);

                                // Extract thumbnail link
                                imgSrc = extract(line, "<image>", "</image>");

                                // Add item into list of items
                                Item item = new Item(title, link, date, imgSrc, Item.Source.TN);
                                items.add(item);
                                inItem = false;
                                loadProgress(); // updateProgress(progress++, maxProgress);
                            }
                            // Catch error lines which sometimes existed in ThanhNien RSS
                            catch (StringIndexOutOfBoundsException e) {
                            }
                        }
                    }

                    in.close();
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    System.out.println("Can't connect to " + urlAddress);
                    error += urlAddress + ": " + e.getMessage() + "\n";
                }
            });
        }
        t3 = true;
    }

    private void scrapeZing(List<String> links) {
        for (String urlAddress : links) {
            es.submit(() -> {
                try {
                    // Connect to URL and add all article element into list
                    Connection.Response response = Jsoup.connect(urlAddress).timeout(10000).execute();
                    if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());

                    Document doc = response.parse();
                    Elements body = doc.select("section[id~=.*-latest]");
                    Elements featured = doc.select("section[id~=.*-featured]");
                    body.addAll(featured);
                    Elements article = body.select("article.article-item");
                    int count = article.size();

                    // Loop through each article-item
                    for (int i = 0; i < count; i++) {
                        int current = i;
                        if (!es.isShutdown())
                            es.execute(() -> {
                                Element e = article.get(current);
                                String title = "", pubDate = "", link = "", imgSrc = "";
                                boolean add = true;
                                LocalDateTime date = LocalDateTime.MIN;

                                // Get image source
                                imgSrc = e.select("img").attr("src");
                                if (!imgSrc.contains("https")) imgSrc = e.select("img").attr("data-src");

                                // Get title
                                title = e.getElementsByClass("article-title").text();
                                if (categoryIndex == 1 && !checkCovidKeyword(title)) add = false;

                                // Get link
                                link = e.select("a").attr("href");
                                link = "https://zingnews.vn" + link;

                                // Get published date
                                pubDate = e.select("span.time").text();
                                pubDate += " " + e.select("span.date").text();
                                pubDate = pubDate.trim();

                                if (pubDate.equals("")) pubDate = e.select("span.friendly-time").text();
                                DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm d/M/yyyy");
                                date = LocalDateTime.parse(pubDate, df);

                                // Create and add news item to list
                                Item item = new Item(title, link, date, imgSrc, Item.Source.ZING);
                                if (add) items.add(item);
                                loadProgress(); // updateProgress(progress++, maxProgress);
                            });
                    }
                }
                catch (IOException e) {
                    System.out.println("Can't connect to " + urlAddress);
                    error += urlAddress + ": " + e.getMessage() + "\n";
                }
            });
        }
        t4 = true;
    }

    private void scrapeNhanDan(List<String> links) {
        for (String urlAddress : links) {
            es.submit(() -> {
                try {
                    // Connect to URL and add all article element into list
                    Connection.Response response = Jsoup.connect(urlAddress).timeout(10000).execute();
                    if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());

                    Document doc = response.parse();
                    Elements body = doc.select("div[class*=uk-width-3-4@m]");
                    Elements article = body.select("article");
                    int count = article.size();

                    // Loop through article items in list
                    for (int i = 0; i < count; i++) {
                        int current = i;
                        if (!es.isShutdown())
                            es.execute(() -> {
                                Element e = article.get(current);
                                String title = "", pubDate = "", link = "", imgSrc = "";
                                boolean add = true;
                                LocalDateTime date = LocalDateTime.MIN;

                                // Get title
                                title = e.getElementsByClass("box-title").text();
                                if (title.equals("")) add = false;
                                if (categoryIndex == 1 && !checkCovidKeyword(title)) add = false;

                                // Get image source
                                imgSrc = e.select("img").attr("data-src");

                                // Get link
                                link = e.select("a").attr("href");
                                if (!link.contains("https://")) link = "https://nhandan.vn" + link;

                                // Get pubDate
                                pubDate = e.select("div[class*=box-meta]").text();
                                if (!pubDate.equals("")) {
                                    DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm dd/M/yyyy");
                                    date = LocalDateTime.parse(pubDate, df);
                                }
                                else {
                                    try {
                                        Connection.Response tempResponse = Jsoup.connect(urlAddress).timeout(5000).execute();
                                        if (response.statusCode() >= 400) throw new IOException("Status code: " + response.statusCode());

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
                                if (add) items.add(item);
                                loadProgress(); // updateProgress(progress++, maxProgress);
                            });
                    }
                }
                catch (IOException  e) {
                    System.out.println("Can't connect to " + urlAddress);
                    error += urlAddress + ": " + e.getMessage() + "\n";
                }
            });
        }
        t5 = true;
    }

    private void scrapeAll(Integer categoryIndex, int count) {
        es.execute(() -> scrapeVE(VNEXPRESS.subList(categoryIndex, categoryIndex + count)));
        es.execute(() -> scrapeTuoiTre(TUOITRE.subList(categoryIndex, categoryIndex + count)));
        es.execute(() -> scrapeThanhNien(THANHNIEN.subList(categoryIndex, categoryIndex + count)));
        es.execute(() -> scrapeZing(ZING.subList(categoryIndex, categoryIndex + count)));
        es.execute(() -> scrapeNhanDan(NHANDAN.subList(categoryIndex, categoryIndex + count)));
    }

    // Check if title of article is in covid category using keywords
    private static boolean checkCovidKeyword(String title) {
        final String check = title.toLowerCase();
        final String[] keywords = {"cov", "ca", "f0", "f1", "vaccine", "vắc xin", "xét nghiệm", "phong tỏa", "mũi", "biến thể", "kháng thể",
                "nhiễm", "dịch", "test", "pcr", "âm tính", "dương tính", "giãn cách", "chỉ thị", "mắc", "tiêm", "delta", "âm tính"};

        for (String s : keywords) {
            if (check.contains(s)) return true;
        }

        return false;
    }

    private synchronized void loadProgress() {
        updateProgress(progress++, maxProgress);
    }
}