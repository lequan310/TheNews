package main;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

enum Source { VE, TT, TN, ZING, ND}

public class Item implements Comparable<Item> {
    private String title;
    private String link;
    private LocalDateTime pubDate;
    private Duration duration;
    private String imgSrc;
    private Source source;

    public Item(String title, String link, LocalDateTime pubDate, String imgSrc, Source source) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.duration = Duration.between(pubDate, LocalDateTime.now());
        this.imgSrc = imgSrc;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getPubDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy\nh:mm a");
        return dtf.format(pubDate);
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public Source getSource() {
        return source;
    }

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

        String result = day + hour + min + sec + "ago.";
        return result;
    }

    public String toString(){
        String res = "";
        res += title + "\t" + link + "\t" + durationToString() + "\t" + imgSrc;
        return res;
    }

    @Override
    public int compareTo(Item item) {
        return duration.compareTo(item.duration);
    }
}
