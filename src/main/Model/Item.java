package main.Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Item implements Comparable<Item> {
    // News Source (VNExpress, TuoiTre, ThanhNien, ZingNews, NhanDan)
    public enum Source { VE, TT, TN, ZING, ND }

    // Item title, source link, published date, duration since posted, thumbnail link, source
    private final String title;
    private final String link;
    private final LocalDateTime pubDate;
    private final Duration duration;
    private final String imgSrc;
    private final Source source;

    // Constructor and Getters
    public Item(String title, String link, LocalDateTime pubDate, String imgSrc, Source source) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.duration = Duration.between(pubDate, LocalDateTime.now());
        this.imgSrc = imgSrc;
        this.source = source;
    }
    // Obtaining Title
    public String getTitle() {
        return title;
    }
    // Obtaining link
    public String getLink() {
        return link;
    }
    // Obtaining published date
    public String getPubDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy\nh:mm a");
        return dtf.format(pubDate);
    }

    // Obtaining Image Source
    public String getImgSrc() {
        return imgSrc;
    }

    // Obtaining article source
    public Source getSource() {
        return source;
    }

    // String functions
    public String durationToString(){
        String day = "", hour = "", min = "", sec = "";
        Duration current = Duration.between(pubDate, LocalDateTime.now());

        if (current.toDaysPart() != 0){
            day += current.toDaysPart() + " days ";
        }
        if (current.toHoursPart() != 0){
            hour += current.toHoursPart() + " hours ";
        }
        if (current.toMinutesPart() != 0){
            min += current.toMinutesPart() + " minutes ";
        }
        if (current.toSecondsPart() != 0){
            sec += current.toSecondsPart() + " seconds ";
        }

        return day + hour + min + sec + "ago.";
    }

    // Turns output to string
    public String toString(){
        String res = "";
        res += title + "\t" + link + "\t" + durationToString() + "\t" + imgSrc;
        return res;
    }

    @Override
    public int compareTo(Item item) {
        // Function to compare item published date
        return duration.compareTo(item.duration);
    }

    public boolean equalTo(Item item) {
        return this.link.equals(item.getLink());
    }
}