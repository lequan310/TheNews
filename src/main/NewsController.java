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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class NewsController extends Task {
    private final ArrayList<Item> items = new ArrayList<>();
    private int categoryIndex = 0;

    private final String[] VNEXPRESS = {"https://vnexpress.net/rss/tin-moi-nhat.rss", "https://vnexpress.net/rss/suc-khoe.rss", "https://vnexpress.net/rss/phap-luat.rss",
            "https://vnexpress.net/rss/kinh-doanh.rss", "https://vnexpress.net/rss/so-hoa.rss", "https://vnexpress.net/rss/suc-khoe.rss",
            "https://vnexpress.net/rss/the-thao.rss", "https://vnexpress.net/rss/giai-tri.rss", "https://vnexpress.net/rss/the-gioi.rss",
            "https://vnexpress.net/rss/du-lich.rss", "https://vnexpress.net/rss/giao-duc.rss", "https://vnexpress.net/rss/khoa-hoc.rss"};

    private final String[] TUOITRE = {"https://tuoitre.vn/rss/tin-moi-nhat.rss", "https://tuoitre.vn/rss/suc-khoe.rss", "https://tuoitre.vn/rss/phap-luat.rss",
            "https://tuoitre.vn/rss/kinh-doanh.rss", "https://tuoitre.vn/rss/cong-nghe.rss", "https://tuoitre.vn/rss/suc-khoe.rss",
            "https://tuoitre.vn/rss/the-thao.rss", "https://tuoitre.vn/rss/giai-tri.rss", "https://tuoitre.vn/rss/the-gioi.rss",
            "https://tuoitre.vn/rss/xe.rss", "https://tuoitre.vn/rss/giao-duc.rss", "https://tuoitre.vn/rss/khoa-hoc.rss"};

    private final String[] THANHNIEN = {"https://thanhnien.vn/rss/home.rss", "https://thanhnien.vn/rss/suc-khoe.rss", "https://thanhnien.vn/rss/thoi-su/chinh-tri.rss",
            "https://thanhnien.vn/rss/tai-chinh-kinh-doanh.rss", "https://thanhnien.vn/rss/cong-nghe.rss", "https://thanhnien.vn/rss/suc-khoe.rss",
            "https://thethao.thanhnien.vn/rss/home.rss", "https://thanhnien.vn/rss/giai-tri.rss", "https://thanhnien.vn/rss/the-gioi.rss",
            "https://game.thanhnien.vn/rss/home.rss", "https://thanhnien.vn/rss/giao-duc.rss", "https://thanhnien.vn/rss/gioi-tre.rss"};

    private final String[] ZING = {"https://zingnews.vn/", "https://zingnews.vn/suc-khoe.html", "https://zingnews.vn/chinh-tri.html",
            "https://zingnews.vn/kinh-doanh-tai-chinh.html", "https://zingnews.vn/cong-nghe.html", "https://zingnews.vn/suc-khoe.html",
            "https://zingnews.vn/the-thao.html", "https://zingnews.vn/giai-tri.html", "https://zingnews.vn/the-gioi.html",
            "https://zingnews.vn/doi-song.html", "https://zingnews.vn/giao-duc.html", "https://zingnews.vn/du-lich.html"};

    private final String[] NHANDAN = {"https://nhandan.vn/", "https://nhandan.vn/y-te", "https://nhandan.vn/chinhtri",
            "https://nhandan.vn/kinhte", "https://nhandan.vn/khoahoc-congnghe", "https://nhandan.vn/y-te", "https://nhandan.vn/thethao",
            "https://nhandan.vn/vanhoa", "https://nhandan.vn/thegioi", "https://nhandan.vn/xahoi", "https://nhandan.vn/giaoduc", "https://nhandan.vn/bandoc"};

    public ArrayList<Item> getItems() {
        return this.items;
    }

    @Override
    protected Object call() throws Exception {
        long start = System.currentTimeMillis();

        Thread t1 = new Thread(() -> {
            readRSSVe(VNEXPRESS[categoryIndex]);
            updateProgress(0.1, 1);
            System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
        });
        Thread t2 = new Thread(() -> {
            readRSSTuoiTre(TUOITRE[categoryIndex]);
            updateProgress(0.2, 1);
            System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
        });
        Thread t3 = new Thread(() -> {
            readRSSThanhNien(THANHNIEN[categoryIndex]);
            updateProgress(0.3, 1);
            System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
        });
        Thread t4 = new Thread(() -> {
            readZing(ZING[categoryIndex]);
            updateProgress(0.4, 1);
            System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
        });
        Thread t5 = new Thread(() -> {
            readNhanDan(NHANDAN[categoryIndex]);
            updateProgress(0.5, 1);
            System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
        });

        if (categoryIndex == 9){
            System.out.println("Others");
            Thread t6 = new Thread(() -> {
                readRSSVe(VNEXPRESS[categoryIndex + 1]);
                System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
            });
            Thread t7 = new Thread(() -> {
                readRSSTuoiTre(TUOITRE[categoryIndex + 1]);
                System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
            });
            Thread t8 = new Thread(() -> {
                readRSSThanhNien(THANHNIEN[categoryIndex + 1]);
                System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
            });
            Thread t9 = new Thread(() -> {
                readZing(ZING[categoryIndex + 1]);
                System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
            });
            Thread t10 = new Thread(() -> {
                readNhanDan(NHANDAN[categoryIndex + 1]);
                System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
            });
            Thread t11 = new Thread(() -> {
                readRSSVe(VNEXPRESS[categoryIndex + 2]);
                System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
            });
            Thread t12 = new Thread(() -> {
                readRSSTuoiTre(TUOITRE[categoryIndex + 2]);
                System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
            });
            Thread t13 = new Thread(() -> {
                readRSSThanhNien(THANHNIEN[categoryIndex + 2]);
                System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
            });
            Thread t14 = new Thread(() -> {
                readZing(ZING[categoryIndex + 2]);
                System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
            });
            Thread t15 = new Thread(() -> {
                readNhanDan(NHANDAN[categoryIndex + 2]);
                System.out.println(items.size() + " " + (System.currentTimeMillis() - start) + " ms");
            });

            for (Thread t : Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)) {
                t.start();
            }

            for (Thread t : Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)) {
                t.join();
            }
        }
        else{
            for (Thread t : Arrays.asList(t1, t2, t3, t4, t5)) {
                t.start();
            }

            for (Thread t : Arrays.asList(t1, t2, t3, t4, t5)) {
                t.join();
            }
        }

        updateProgress(0.5, 1);
        Collections.sort(items);
        updateProgress(1, 1);
        System.out.println("Achieve item list: " + (System.currentTimeMillis() - start) + " ms");
        return null;
    }

    public NewsController(int categoryIndex) {
        this.categoryIndex = categoryIndex;
    }

    public void readRSSVe(String urlAddress) {
        try {
            URL rssURL = new URL(urlAddress);
            BufferedReader in = new BufferedReader(new InputStreamReader(rssURL.openStream()));
            String title = "", pubDate = "", link = "", imgSrc = "", line;
            LocalDateTime date = LocalDateTime.MIN;
            boolean inItem = false;

            while ((line = in.readLine()) != null) {
                if (line.contains("<item>")) {
                    inItem = true;
                } else if (line.contains("<title>") && inItem) {
                    title = extract(line, "<title>", "</title>");
                } else if (line.contains("<pubDate>") && inItem) {
                    pubDate = extract(line, "<pubDate>", "</pubDate>");
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss +0700");
                    date = LocalDateTime.parse(pubDate, df);
                } else if (line.contains("<link>") && inItem) {
                    link = extract(line, "<link>", "</link>");
                } else if (line.contains("<description>") && inItem) {
                    try {
                        imgSrc = extract(line, "<description>", "</description>");
                        imgSrc = extract(imgSrc, "<img src=\"", "\"");
                    } catch (StringIndexOutOfBoundsException e) {
                        imgSrc = "";
                    }
                } else if (line.contains("</item>") && inItem) {
                    inItem = false;
                    Item item = new Item(title, link, date, imgSrc, Source.VE);
                    items.add(item);
                }
            }

            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Can't connect to " + urlAddress);
        }
    }

    public void readRSSTuoiTre(String urlAddress) {
        try {
            URL rssURL = new URL(urlAddress);
            BufferedReader in = new BufferedReader(new InputStreamReader(rssURL.openStream()));
            String title = "", pubDate = "", link = "", imgSrc = "", line;
            LocalDateTime date = LocalDateTime.MIN;
            boolean inItem = false;

            while ((line = in.readLine()) != null) {
                if (line.contains("<item>")) {
                    inItem = true;
                } else if (line.contains("<title>") && inItem) {
                    title = extract(line, "<title>", "</title>");
                    title = extract(title, "<![CDATA[", "]]>");
                } else if (line.contains("<pubDate>") && inItem) {
                    pubDate = extract(line, "<pubDate>", "</pubDate>");
                    pubDate = extract(pubDate, "<![CDATA[", " GMT+7");
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss");
                    date = LocalDateTime.parse(pubDate, df);
                }
                if (line.contains("<link>") && inItem) {
                    link = extract(line, "<link>", "</link>");
                    link = extract(link, "<![CDATA[", "]]>");
                } else if (line.contains("<description>") && inItem) {
                    try {
                        imgSrc = extract(line, "<description>", "</description>");
                        imgSrc = extract(imgSrc, "<img src=\"", "\"");
                        imgSrc = imgSrc.replace("zoom/80_50/", "");
                    } catch (StringIndexOutOfBoundsException e) {
                        imgSrc = "";
                    }
                } else if (line.contains("</item>") && inItem) {
                    inItem = false;
                    Item item = new Item(title, link, date, imgSrc, Source.TT);
                    items.add(item);
                }
            }

            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Can't connect to " + urlAddress);
        }
    }

    public void readRSSThanhNien(String urlAddress) {
        try {
            URL rssURL = new URL(urlAddress);
            BufferedReader in = new BufferedReader(new InputStreamReader(rssURL.openStream()));
            String title = "", pubDate = "", link = "", imgSrc = "", line;
            LocalDateTime date = LocalDateTime.MIN;
            boolean inItem = false;

            while ((line = in.readLine()) != null) {
                if (line.contains("<item>"))
                    inItem = true;

                if (inItem) {
                    try {
                        // Extract title
                        title = extract(line, "<title>", "</title>");
                        if (title.contains("<![CDATA[ "))
                            title = extract(title, "<![CDATA[ ", "]]>");

                        // Extract link
                        link = extract(line, "<link>", "</link>");

                        // Extract pubDate
                        pubDate = extract(line, "<pubDate>", " GMT");
                        DateTimeFormatter df = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss");
                        date = LocalDateTime.parse(pubDate, df);

                        // Extract image
                        imgSrc = extract(line, "<image>", "</image>");

                        Item item = new Item(title, link, date, imgSrc, Source.TN);
                        items.add(item);
                        inItem = false;
                    } catch (StringIndexOutOfBoundsException e) {

                    }
                }
            }

            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Can't connect to " + urlAddress);
        }
    }

    public void readZing(String urlAddress) {
        try {
            Document doc = Jsoup.connect(urlAddress).get();
            Elements body = doc.select("section[id~=.*-latest]");
            Elements featured = doc.select("section[id~=.*-featured]");
            body.addAll(featured);

            String title = "", pubDate = "", link = "", imgSrc = "";
            LocalDateTime date = LocalDateTime.MIN;

            for (Element e : body.select("article.article-item")) {
                // Get image source
                imgSrc = e.select("img").attr("src");
                if (!imgSrc.contains("https")) imgSrc = e.select("img").attr("data-src");

                // Get title and link
                title = e.getElementsByClass("article-title").text();
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
                Item item = new Item(title, link, date, imgSrc, Source.ZING);
                items.add(item);
            }
        } catch (IOException e) {
            System.out.println("Can't connect to " + urlAddress);
        }
    }

    public void readNhanDan(String urlAddress) {
        try {
            int count = 0;
            Document doc = Jsoup.connect(urlAddress).get();
            Elements body = doc.getElementsByClass("swrapper");

            String title = "", pubDate = "", link = "", imgSrc = "";

            for (Element e : body.select("article")) {
                LocalDateTime date = LocalDateTime.MIN;

                // Get title
                title = e.getElementsByClass("box-title").text();
                if (title.compareTo("") == 0) continue;

                // Get image source
                imgSrc = e.select("img").attr("data-src");

                // Get link
                link = e.select("a").attr("href");
                if (!link.contains("https://")) link = "https://nhandan.vn" + link;

                // Get pubDate
                pubDate = e.getElementsByClass("box-meta-small").text();
                if (pubDate.compareTo("") != 0) {
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm dd/M/yyyy");
                    date = LocalDateTime.parse(pubDate, df);
                }

                // Create and add news item to list
                Item item = new Item(title, link, date, imgSrc, Source.ND);
                items.add(item);
            }
        }
        catch (IOException e) {
            System.out.println("Can't connect to " + urlAddress);
        }
    }

    public String extract(String line, String start, String end) {
        int firstPos = line.indexOf(start);
        String temp = line.substring(firstPos);
        temp = temp.replace(start, "");
        int lastPos = temp.indexOf(end);
        temp = temp.substring(0, lastPos);
        return temp;
    }
}