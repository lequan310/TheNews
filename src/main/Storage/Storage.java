package main.Storage;

import javafx.scene.Node;
import javafx.scene.image.Image;
import main.Model.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Storage {
    private static Storage storage = null;
    private final int ICONSIZE = 50;

    private ArrayList<Item> items = new ArrayList<>();
    private HashMap<String, Image> images = new HashMap<>();
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

    public ArrayList<Item> getItems() {
        return this.items;
    }

    public HashMap<String, Image> getImage() {
        return this.images;
    }

    public ArrayList<Image> getIcons() {
        return this.icons;
    }
}
