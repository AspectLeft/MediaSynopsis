package app.model;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    private File tmpDir;

    public DataModel() {
        tmpDir = new File("tmp");
        tmpDir.mkdirs();
    }

    public void cleanup() {
        if (currentMedia.get().isPlaying()) {
            currentMedia.get().stop();
        }
        String[] entries = tmpDir.list();
        for(String s: entries){
            File currentFile = new File(tmpDir.getPath(),s);
            System.out.println(currentFile.getName());
            currentFile.delete();
        }
        tmpDir.delete();
    }

    public void loadData(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        List<Media> bufferlist = new ArrayList<>();
        for (File file: files) {
            if (file.isDirectory()) continue;
            bufferlist.add(new Media(file));
        }
        mediaList.addAll(bufferlist);
    }
}
