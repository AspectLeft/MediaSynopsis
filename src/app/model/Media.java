package app.model;

import app.util.FFmpegCli;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
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

public class Media {
    public Media(File file) {
        setFileName(file.getName());
        String path = file.toURI().toString();
        System.out.println(path);

        if (getFileName().endsWith(".avi")) {
            try {
                file = new File(aviToMp4(file));
                parseMp4(file);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (getFileName().endsWith(".jpg")) {
            setIsImage(true);
            setImage(new Image(path));
        }

    }

    public static String aviToMp4(File aviFile) throws IOException {
        String tmpFileName = buildTmpFileName();

        FFmpeg ffmpeg = FFmpegCli.getFFmpeg();
        FFprobe ffprobe = FFmpegCli.getFFprobe();
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(aviFile.getPath())
                .overrideOutputFiles(true)
                .addOutput(tmpFileName)
                .setFormat("mp4").done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
        return tmpFileName;
    }

    private void parseMp4(File file) throws Exception {
        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        Picture picture;
        this.frames = new ArrayList<>();
        while (null != (picture = grab.getNativeFrame())) {
            this.frames.add(SwingFXUtils.toFXImage(AWTUtil.toBufferedImage(picture), null));
        }
    }

    private static String buildTmpFileName() {
        return String.format("tmp/%s.mp4", UUID.randomUUID().toString());
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

}
