package app.model;

import app.util.FFmpegCli;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.media.MediaPlayer;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Media {
    public Media(File file) {
        setFileName(file.getName());
        String path = file.toURI().toString();
        System.out.println(path);

        if (getFileName().endsWith(".avi")) {
            setIsVideo(true);
            try {
                file = new File(aviToMp4(file));
                parseMp4(file);
                this.executorService = Executors.newScheduledThreadPool(8);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (getFileName().endsWith(".mp4")) {
            setIsVideo(true);
            try {
                parseMp4(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.executorService = Executors.newScheduledThreadPool(8);
        }
        else if (getFileName().endsWith(".jpg")) {
            setIsImage(true);
            setImage(new Image(path));
        }

    }

    public static String aviToMp4(File aviFile) throws IOException {
        return transform(aviFile, "mp4");
    }

    private void parseMp4(File file) throws Exception {
        // video
        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        Picture picture;
        this.frames = new ArrayList<>();
        while (null != (picture = grab.getNativeFrame())) {
            this.frames.add(SwingFXUtils.toFXImage(AWTUtil.toBufferedImage(picture), null));
        }
        this.framesList.addAll(this.frames);

        // audio
        audioFile = new File(transform(file, "mp3"));

        audioMedia = new javafx.scene.media.Media(audioFile.toURI().toString());
        audioPlayer = new MediaPlayer(audioMedia);
    }

    private static String transform(File inputFile, String outputFormat) throws IOException {
        String tmpFileName = buildTmpFileName(outputFormat);

        FFmpeg ffmpeg = FFmpegCli.getFFmpeg();
        FFprobe ffprobe = FFmpegCli.getFFprobe();
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputFile.getPath())
                .overrideOutputFiles(true)
                .addOutput(tmpFileName)
                .setFormat(outputFormat).done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
        return tmpFileName;
    }

    private static String buildTmpFileName(String format) {
        return String.format("tmp/%s.%s", UUID.randomUUID().toString(), format);
    }

    protected final StringProperty fileName = new SimpleStringProperty();

    public final StringProperty fileNameProperty() { return this.fileName; }

    public final String getFileName() {
        return this.fileName.get();
    }

    public final void setFileName(final String name) {
        this.fileNameProperty().setValue(name);
    }

    private final BooleanProperty isImage = new SimpleBooleanProperty();

    public final BooleanProperty isImageProperty() {
        return this.isImage;
    }

    public final boolean getIsImage() {
        return this.isImage.get();
    }

    public final void setIsImage(final boolean b) {
        this.isImage.setValue(b);
    }

    private final BooleanProperty isVideo = new SimpleBooleanProperty();

    public final BooleanProperty isVideoProperty() {
        return this.isVideo;
    }

    public final boolean getIsVideo() {
        return this.isVideo.get();
    }

    public final void setIsVideo(final boolean b) {
        this.isVideo.setValue(b);
    }

    private Image image;
    public Image getImage() {
        return image;
    }
    public void setImage(Image image) {
        this.image = image;
    }

    private List<Image> frames;
    public List<Image> getFrames() {
        return frames;
    }

    private final ObservableList<Image> framesList = FXCollections.observableArrayList(image1 ->
            new Observable[]{image1.widthProperty()});
    private final ObjectProperty<Image> currentFrame = new SimpleObjectProperty<>(null);

    public ObjectProperty<Image> currentFrameProperty() {
        return currentFrame;
    }

    public final Image getCurrentImage() {
        return currentFrame.get();
    }

    private final int[] index = new int[]{0};
    private ScheduledExecutorService executorService = null;
    private Future<?> future;

    private BooleanProperty stoppedProperty = new SimpleBooleanProperty();
    public BooleanProperty getStoppedProperty() {
        return this.stoppedProperty;
    }
    private void setStoppedProperty(boolean b) {
        this.stoppedProperty.setValue(b);
    }

    private BooleanProperty isPlayingProperty = new SimpleBooleanProperty();
    public BooleanProperty getIsPlayingProperty() {
        return this.isPlayingProperty;
    }
    private void setIsPlayingProperty(boolean b) {
        this.isPlayingProperty.setValue(b);
    }

    public void play() {
        setIsPlayingProperty(true);
        setStoppedProperty(false);

        audioPlayer.play();
        future = executorService.scheduleAtFixedRate(() -> {
            if (index[0] >= this.frames.size()) {
                stop();
            }
            currentFrameProperty().setValue(this.frames.get(index[0]));
            index[0]++;
        }, 0, 33, TimeUnit.MILLISECONDS);
    }

    public void pause() {
        if (!getIsPlayingProperty().get()) return;
        setIsPlayingProperty(false);
        audioPlayer.pause();
        future.cancel(false);
    }

    public void stop() {
        stoppedProperty.setValue(true);
        pause();
        audioPlayer.stop();
        index[0] = 0;
        currentFrameProperty().setValue(this.frames.get(index[0]));
    }

    public boolean isPlaying() {
        return getIsPlayingProperty().get();
    }

    private File audioFile;

    private javafx.scene.media.Media audioMedia;
    public MediaPlayer audioPlayer;

    public void videoSeek(double percent) {
        if (!getIsVideo()) return;

        boolean wasPlaying = false;
        if (isPlaying()) {
            pause();
            wasPlaying = true;
        }
        index[0] = (int) (framesList.size() * percent / 100);
        currentFrameProperty().setValue(this.frames.get(index[0]));
        audioPlayer.seek(audioPlayer.getMedia().getDuration().multiply(percent / 100));

        if (wasPlaying) {
            play();
        }
    }
}
