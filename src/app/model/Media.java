package app.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.File;
import java.io.IOException;

public class Media {
    public Media(File file) {
        setFileName(file.getName());

        if (getFileName().endsWith(".avi")) {
            try {
                file = new File(aviToMp4(file));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        String path = file.toURI().toString();
        System.out.println(path);
        this.media = new javafx.scene.media.Media(path);
    }

    public static String aviToMp4(File aviFile) throws IOException {
        String tmpFileName = "output.mp4"; // TODO build ./tmp and store it with a random name, and sweep up on quit


        // TODO give an exclusive progress bar when transforming
        FFmpeg ffmpeg = new FFmpeg("F:/ffmpeg-20200403-52523b6-win64-static/bin/ffmpeg.exe");
        FFprobe ffprobe = new FFprobe("F:/ffmpeg-20200403-52523b6-win64-static/bin/ffprobe.exe");
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(aviFile.getPath())
                .overrideOutputFiles(true)
                .addOutput(tmpFileName)
                .setFormat("mp4").done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
        return tmpFileName;
    }


    protected final StringProperty fileName = new SimpleStringProperty();
    protected final javafx.scene.media.Media media;

    public final StringProperty fileNameProperty() { return this.fileName; }

    public final String getFileName() {
        return this.fileName.get();
    }

    public final void setFileName(final String name) {
        this.fileNameProperty().setValue(name);
    }

    public final javafx.scene.media.Media getMedia() {
        return this.media;
    }
}
