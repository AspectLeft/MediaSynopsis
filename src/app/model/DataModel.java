package app.model;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;

public class DataModel {
    private final ObservableList<Media> mediaList = FXCollections.observableArrayList(media ->
            new Observable[]{media.fileNameProperty()});

    private final ObjectProperty<Media> currentMedia = new SimpleObjectProperty<>(null);

    public ObjectProperty<Media> currentMediaProperty() {
        return currentMedia;
    }

    public final Media getCurrentMedia() {
        return currentMedia.get();
    }

    public final void setCurrentMedia(Media media) {
        currentMedia.set(media);
    }

    public ObservableList<Media> getMediaList() {
        return mediaList;
    }

    public void loadData(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file: files) {
            if (file.isDirectory()) continue;
            mediaList.add(new Media(file));
        }
    }
}
