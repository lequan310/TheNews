package main.Storage;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.Image;
import main.Model.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    private static Storage storage = null;
    private final int ICONSIZE = 50;

    private final Map<String, Item> itemStorage = new ConcurrentHashMap<>();
    private final ArrayList<Item> items = new ArrayList<>();
    private final HashMap<String, Image> images = new HashMap<>();
    private final HashMap<String, ObservableList<Node>> articles = new HashMap<>();
    private final ArrayList<Image> icons = new ArrayList<>(
        List.of(new Image("/image/iconVE.png", ICONSIZE, ICONSIZE, true, true),
                new Image("/image/iconTT.png", ICONSIZE, ICONSIZE, true, true),
                new Image("/image/iconTN.jpeg", ICONSIZE, ICONSIZE, true, true),
                new Image("/image/iconZING.png", ICONSIZE, ICONSIZE, true, true),
                new Image("/image/iconND.png", ICONSIZE, ICONSIZE, true, true)));

    private Storage() {}

    public static Storage getInstance() {
        if (storage == null) {
            synchronized (Storage.class) {
                if (storage == null)
                    storage = new Storage();
            }
        }
        return storage;
    }

    public Map<String, Item> getItemStorage() {
        return itemStorage;
    }

    public ArrayList<Item> getItems() {
        return this.items;
    }

    public HashMap<String, Image> getImage() {
        return this.images;
    }

    public HashMap<String, ObservableList<Node>> getArticles() {
        return articles;
    }

    public ArrayList<Image> getIcons() {
        return this.icons;
    }

    public void clearAll() {
        itemStorage.clear();
        items.clear();
        images.clear();
        articles.clear();
        icons.clear();
    }
}
