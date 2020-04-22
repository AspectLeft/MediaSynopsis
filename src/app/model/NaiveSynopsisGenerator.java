package app.model;

import app.controller.SynopsisController;
import app.util.ImageTrasform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.ArrayList;
import java.util.List;

public class NaiveSynopsisGenerator extends SynopsisGeneratorBase {

    int x0 = 0;
    int frameStride = 3000;


    @Override
    public Synopsis generate(final List<Media> mediaList) {
        WritableImage writableImage = new WritableImage(SynopsisController.W, SynopsisController.H);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        List<Synopsis.MediaCoordinate> coordinateList = new ArrayList<>();
        x0 = 0;
        frameStride = getFrameStride(mediaList);
        for (final Media media: mediaList) {
            if (writableImage.getWidth() - x0 < 120) break;
            if (media.getIsImage()) {
                coordinateList.add(addImage(media, pixelWriter));
            }
            else if (media.getIsVideo()) {
                addVideo(media, pixelWriter, writableImage, coordinateList);
            }
            x0 += 2;
        }
        return new Synopsis(writableImage, coordinateList);
    }

    private Synopsis.MediaCoordinate addComponent(Media media, Image image, PixelWriter pixelWriter, double time) {
        ImageTrasform trasform = new ImageTrasform(image);
        Image thumbnail = trasform.scale(100.0 / image.getHeight(), true);
        PixelReader pixelReader = thumbnail.getPixelReader();
        for (int x = 0; x < thumbnail.getWidth(); ++x) {
            for (int y = 0; y < thumbnail.getHeight(); ++y) {
                pixelWriter.setArgb(x + x0, y, pixelReader.getArgb(x, y));
            }
        }
        x0 += thumbnail.getWidth();
        return new Synopsis.MediaCoordinate((int)(x0 - thumbnail.getWidth() / 2), 50, media, time);
    }

    private Synopsis.MediaCoordinate addImage(Media imageMedia, PixelWriter pixelWriter) {
        return addComponent(imageMedia, imageMedia.getImage(), pixelWriter, 0);
    }

    private void addVideo(Media videoMedia, PixelWriter pixelWriter, WritableImage writableImage,
                                              List<Synopsis.MediaCoordinate> coordinateList) {
        List<Image> frameList = videoMedia.getFrames();
        for (int i = 0; i < frameList.size(); i += frameStride) {
            if (writableImage.getWidth() - x0 < 120) break;
            coordinateList.add(addComponent(videoMedia, frameList.get(i), pixelWriter, 100.0 * i / frameList.size()));
            x0 += 2;
        }
    }

    private int getFrameStride(final List<Media> mediaList) {
        int totalcount = SynopsisController.W / 120;
        totalcount -= imageCount(mediaList);
        return videoFrames(mediaList) / totalcount;
    }

    private int imageCount(final List<Media> mediaList) {
        int count = 0;
        for (final Media media: mediaList) {
            if (media.getIsImage()) {
                count++;
            }
        }
        return count;
    }

    private int videoFrames(final List<Media> mediaList) {
        int count = 0;
        for (final Media media: mediaList) {
            if (media.getIsVideo()) {
                count += media.getFrames().size();
            }
        }
        return count;
    }
}
