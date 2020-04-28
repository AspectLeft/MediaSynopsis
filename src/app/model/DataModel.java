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

    private final ObjectProperty<Synopsis> synopsis = new SimpleObjectProperty<>(null);

    public ObjectProperty<Synopsis> synopsisProperty() {
        return synopsis;
    }

    public final Synopsis getSynopsis() {
        return synopsis.get();
    }

    public final void setSynopsis(Synopsis synopsis) {
        this.synopsis.set(synopsis);
    }

    SynopsisGeneratorBase generator;

    public DataModel() {
        tmpDir = new File("tmp");
        tmpDir.mkdirs();

        generator = new NaiveSynopsisGenerator();
    }

    public void cleanup() {
        for (Media media: mediaList) {
            media.cleanup();
        }
        String[] entries = tmpDir.list();
        for(String s: entries){
            File currentFile = new File(tmpDir.getPath(),s);
            System.out.println(currentFile.getName());
            currentFile.delete();
        }
        tmpDir.delete();
        System.out.println("Quiting...");
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

        setSynopsis(generator.generate(mediaList));
    }

    public void generateSynposis() {
        setSynopsis(generator.generate(mediaList));
    }

    public void videoSeek(double percent) {
        if (getCurrentMedia() != null) {
            getCurrentMedia().videoSeek(percent);
        }
    }
}
