package main;

import javafx.concurrent.Task;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;

public class NewsController extends Task<Void> {
    private final ArrayList<Item> items = new ArrayList<>(); // List of items that is scraped and sorted to be displayed

    // List of URL to scrape from
    // New, Covid, Politics, Business, Technology, Health, Sports, Entertainment, World, Others
    private final String[] VNEXPRESS = {"https://vnexpress.net/rss/tin-moi-nhat.rss", "https://vnexpress.net/rss/tin-noi-bat.rss", "https://vnexpress.net/rss/phap-luat.rss",
            "https://vnexpress.net/kinh-doanh", "https://vnexpress.net/so-hoa", "https://vnexpress.net/suc-khoe",
            "https://vnexpress.net/the-thao", "https://vnexpress.net/giai-tri", "https://vnexpress.net/rss/the-gioi.rss",
            "https://vnexpress.net/rss/cuoi.rss", "https://vnexpress.net/rss/giao-duc.rss", "https://vnexpress.net/rss/khoa-hoc.rss"};
    private final String[] TUOITRE = {"https://tuoitre.vn/rss/tin-moi-nhat.rss", "https://tuoitre.vn/rss/suc-khoe.rss", "https://tuoitre.vn/rss/phap-luat.rss",
            "https://tuoitre.vn/rss/kinh-doanh.rss", "https://tuoitre.vn/rss/cong-nghe.rss", "https://tuoitre.vn/rss/suc-khoe.rss",
            "https://tuoitre.vn/rss/the-thao.rss", "https://tuoitre.vn/rss/giai-tri.rss", "https://tuoitre.vn/rss/the-gioi.rss",
            "https://tuoitre.vn/rss/xe.rss", "https://tuoitre.vn/rss/giao-duc.rss", "https://tuoitre.vn/rss/nhip-song-tre.rss"};
    private final String[] THANHNIEN = {"https://thanhnien.vn/rss/home.rss", "https://thanhnien.vn/rss/suc-khoe.rss", "https://thanhnien.vn/rss/thoi-su/chinh-tri.rss",
            "https://thanhnien.vn/rss/tai-chinh-kinh-doanh.rss", "https://thanhnien.vn/rss/cong-nghe.rss", "https://thanhnien.vn/rss/suc-khoe.rss",
            "https://thethao.thanhnien.vn/rss/home.rss", "https://thanhnien.vn/rss/giai-tri.rss", "https://thanhnien.vn/rss/the-gioi.rss",
            "https://game.thanhnien.vn/rss/home.rss", "https://thanhnien.vn/rss/giao-duc.rss", "https://thanhnien.vn/rss/ban-can-biet.rss"};
    private final String[] ZING = {"https://zingnews.vn/", "https://zingnews.vn/suc-khoe.html", "https://zingnews.vn/chinh-tri.html",
            "https://zingnews.vn/kinh-doanh-tai-chinh.html", "https://zingnews.vn/cong-nghe.html", "https://zingnews.vn/suc-khoe.html",
            "https://zingnews.vn/the-thao.html", "https://zingnews.vn/giai-tri.html", "https://zingnews.vn/the-gioi.html",
            "https://zingnews.vn/doi-song.html", "https://zingnews.vn/giao-duc.html", "https://zingnews.vn/du-lich.html"};
    private final String[] NHANDAN = {"https://nhandan.vn/", "https://nhandan.vn/y-te", "https://nhandan.vn/chinhtri",
            "https://nhandan.vn/kinhte", "https://nhandan.vn/khoahoc-congnghe", "https://nhandan.vn/y-te", "https://nhandan.vn/thethao",
            "https://nhandan.vn/vanhoa", "https://nhandan.vn/thegioi", "https://nhandan.vn/xahoi", "https://nhandan.vn/giaoduc", "https://nhandan.vn/bandoc"};

    private int categoryIndex = 0, progress = 0, maxProgress = 0; // Index to read from the arrays below
    private String error = ""; // Error message

    public NewsController(int categoryIndex) {
        this.categoryIndex = categoryIndex;
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
    protected Void call() throws Exception {
        long start = System.currentTimeMillis();
        updateProgress(0, 1);

        Thread t1 = new Thread(() -> readVE(VNEXPRESS[categoryIndex]));
        Thread t2 = new Thread(() -> readRSSTuoiTre(TUOITRE[categoryIndex]));
        Thread t3 = new Thread(() -> readRSSThanhNien(THANHNIEN[categoryIndex]));
        Thread t4 = new Thread(() -> readZing(ZING[categoryIndex]));
        Thread t5 = new Thread(() -> readNhanDan(NHANDAN[categoryIndex]));

        // If category is Others
        if (categoryIndex == 9) {
            maxProgress = 500;

            Thread t6 = new Thread(() -> readVE(VNEXPRESS[categoryIndex + 1]));
            Thread t7 = new Thread(() -> readRSSTuoiTre(TUOITRE[categoryIndex + 1]));
            Thread t8 = new Thread(() -> readRSSThanhNien(THANHNIEN[categoryIndex + 1]));
            Thread t9 = new Thread(() -> readZing(ZING[categoryIndex + 1]));
            Thread t10 = new Thread(() -> readNhanDan(NHANDAN[categoryIndex + 1]));
            Thread t11 = new Thread(() -> readVE(VNEXPRESS[categoryIndex + 2]));
            Thread t12 = new Thread(() -> readRSSTuoiTre(TUOITRE[categoryIndex + 2]));
            Thread t13 = new Thread(() -> readRSSThanhNien(THANHNIEN[categoryIndex + 2]));
            Thread t14 = new Thread(() -> readZing(ZING[categoryIndex + 2]));
            Thread t15 = new Thread(() -> readNhanDan(NHANDAN[categoryIndex + 2]));

            for (Thread t : Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)) {
                t.start();
            }
            for (Thread t : Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)) {
                t.join();
            }
        } else {
            maxProgress = 250;
            for (Thread t : Arrays.asList(t1, t2, t3, t4, t5)) {
                t.start();
            }
            for (Thread t : Arrays.asList(t1, t2, t3, t4, t5)) {
                t.join();
            }
        }

        // Sort items and update progress bar
        Collections.sort(items);
        updateProgress(1, 1);
        System.out.println("Achieve item list: " + (System.currentTimeMillis() - start) + " ms");
        return null;
    }

    private void readVE(String urlAddress) {
        long start = System.currentTimeMillis();

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
                        updateProgress(progress++, maxProgress);
                    }
                }

                in.close();
            }
            else {
                Document doc = Jsoup.connect(urlAddress).timeout(10000).get();
                Elements article = doc.select("article");
                String title = "", pubDate = "", link = "", imgSrc = "";

                for (Element e : article) {
                    LocalDateTime date = LocalDateTime.MIN;

                    // Get title
                    title = e.select("h3").text();
                    if (title.compareTo("") == 0) title = e.select("h2").text();
                    if (title.compareTo("") == 0) continue;
                    if (categoryIndex == 1 && !checkCovidKeyword(title)) continue;

                    // Get article link and thumbnail url
                    link = e.select("a").attr("href");
                    imgSrc = e.select("div.thumb-art").select("img").attr("data-src");

                    Document temp = Jsoup.connect(link).timeout(10000).get();
                    if (imgSrc.compareTo("") == 0) // Find first image in article if can't find thumbnail
                        imgSrc = temp.select("article.fck_detail").select("img").attr("data-src");

                    // Get published date
                    pubDate = temp.select("span.date").text();
                    if (pubDate.compareTo("") == 0) pubDate = temp.select("span.time").text();
                    if (pubDate.compareTo("") == 0) continue;

                    pubDate = extract(pubDate, ", ", " (GMT+7)");
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("d/M/yyyy, HH:mm");
                    date = LocalDateTime.parse(pubDate, df);

                    // Create and add news item to list
                    Item item = new Item(title, link, date, imgSrc, Item.Source.VE);
                    if (!inList(item)) items.add(item);
                    updateProgress(progress++, maxProgress);

                    if (item.getDuration().toHours() > 24) break;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Can't connect to " + urlAddress);
            error += urlAddress + ": " + e.getMessage() + "\n";
        }

        System.out.println("VN Express: " + items.size() + " " + (System.currentTimeMillis() - start) + " ms");
    }

    private void readRSSTuoiTre(String urlAddress) {
        long start = System.currentTimeMillis();

        try {
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
                    updateProgress(progress++, maxProgress);
                }
            }

            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Can't connect to " + urlAddress);
            error += urlAddress + ": " + e.getMessage() + "\n";
        }

        System.out.println("Tuoi Tre: " + items.size() + " " + (System.currentTimeMillis() - start) + " ms");
    }

    private void readRSSThanhNien(String urlAddress) {
        long start = System.currentTimeMillis();

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
                        updateProgress(progress++, maxProgress);
                    }
                    // Catch error lines which sometimes existed in ThanhNien RSS
                    catch (StringIndexOutOfBoundsException e) {
                    }
                }
            }

            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Can't connect to " + urlAddress);
            error += urlAddress + ": " + e.getMessage() + "\n";
        }

        System.out.println("Thanh Nien: " + items.size() + " " + (System.currentTimeMillis() - start) + " ms");
    }

    private void readZing(String urlAddress) {
        long start = System.currentTimeMillis();

        try {
            // Connect to URL and add all article element into list
            Document doc = Jsoup.connect(urlAddress).timeout(10000).get();
            Elements body = doc.select("section[id~=.*-latest]");
            Elements featured = doc.select("section[id~=.*-featured]");
            body.addAll(featured);

            String title = "", pubDate = "", link = "", imgSrc = "";
            LocalDateTime date = LocalDateTime.MIN;

            // Loop through each article-item
            for (Element e : body.select("article.article-item")) {
                // Get image source
                imgSrc = e.select("img").attr("src");
                if (!imgSrc.contains("https")) imgSrc = e.select("img").attr("data-src");

                // Get title
                title = e.getElementsByClass("article-title").text();
                if (categoryIndex == 1 && !checkCovidKeyword(title)) continue;

                // Get link
                link = e.select("a").attr("href");
                link = "https://zingnews.vn" + link;

                // Get published date
                pubDate = e.select("span.time").text();
                pubDate += " " + e.select("span.date").text();
                pubDate = pubDate.trim();

                if (pubDate.compareTo("") == 0) pubDate = e.select("span.friendly-time").text();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm d/M/yyyy");
                date = LocalDateTime.parse(pubDate, df);

                // Create and add news item to list
                Item item = new Item(title, link, date, imgSrc, Item.Source.ZING);
                if (!inList(item)) items.add(item);
                updateProgress(progress++, maxProgress);
            }
        } catch (IOException e) {
            System.out.println("Can't connect to " + urlAddress);
            error += urlAddress + ": " + e.getMessage() + "\n";
        }

        System.out.println("Zing: " + items.size() + " " + (System.currentTimeMillis() - start) + " ms");
    }

    private void readNhanDan(String urlAddress) {
        long start = System.currentTimeMillis();

        try {
            // Connect to URL and add all article element into list
            Document doc = Jsoup.connect(urlAddress).timeout(10000).get();
            Elements body = doc.select("div[class*=uk-width-3-4@m]");

            String title = "", pubDate = "", link = "", imgSrc = "";

            // Loop through article items in list
            for (Element e : body.select("article")) {
                LocalDateTime date = LocalDateTime.MIN;

                // Get title
                title = e.getElementsByClass("box-title").text();
                if (title.compareTo("") == 0) continue;
                if (categoryIndex == 1 && !checkCovidKeyword(title)) continue;

                // Get image source
                imgSrc = e.select("img").attr("data-src");

                // Get link
                link = e.select("a").attr("href");
                if (!link.contains("https://")) link = "https://nhandan.vn" + link;

                // Get pubDate
                pubDate = e.select("div[class*=box-meta]").text();
                if (pubDate.compareTo("") != 0) {
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm dd/M/yyyy");
                    date = LocalDateTime.parse(pubDate, df);
                } else {
                    try {
                        Document temp = Jsoup.connect(link).timeout(5000).get();
                        pubDate = temp.select("div.box-date").text();

                        if (pubDate.compareTo("") != 0) {
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
                                continue;
                            }
                        }
                    } catch (IOException exception) {
                        continue;
                    }
                }

                // Create and add news item to list
                Item item = new Item(title, link, date, imgSrc, Item.Source.ND);
                if (!inList(item)) items.add(item);
                updateProgress(progress++, maxProgress);
            }
        } catch (IOException e) {
            System.out.println("Can't connect to " + urlAddress);
            error += urlAddress + ": " + e.getMessage() + "\n";
        }

        System.out.println("Nhan Dan: " + items.size() + " " + (System.currentTimeMillis() - start) + " ms");
    }

    // Check if title of article is in covid category using keywords
    private boolean checkCovidKeyword(String title) {
        final String check = title.toLowerCase();
        final String[] keywords = {"cov", "ca", "f0", "f1", "vaccine", "vắc xin", "xét nghiệm", "phong tỏa", "mũi", "biến thể",
                "nhiễm", "dịch", "test", "pcr", "âm tính", "dương tính", "giãn cách", "chỉ thị", "mắc", "tiêm", "delta"};

        for (String s : keywords) {
            if (check.contains(s)) return true;
        }

        return false;
    }

    private boolean inList(Item item) {
        try {
            for (Item i : items) {
                if (item.equal(i)) return true;
            }

            return false;
        } catch (ConcurrentModificationException e) {
            System.out.println(e.getMessage());
            return true;
        }
    }
}