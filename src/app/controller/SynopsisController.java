package app.controller;

import app.model.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class SynopsisController {
    public static final int W = 1200;
    public static final int H = 100;

    @FXML
    public ImageView imageView;

    public void initModel(DataModel model) {

        model.synopsisProperty().addListener(((observableValue, s1, s2) -> {
            if (s2 == null) return;
            imageView.setImage(s2.synopsisImage);
        }));
        imageView.setOnMouseClicked(mouseEvent -> {
            if (model.getSynopsis() == null) return;
            double x = mouseEvent.getX();
            double y = mouseEvent.getY();
            System.out.println(String.format("x:%f,y:%f", x, y));
            Synopsis.MediaCoordinate closetMedia = model.getSynopsis().closestMedia((int)x, (int)y);
            model.setCurrentMedia(closetMedia.media);
            if (closetMedia.media.getIsVideo()) {
                System.out.println("Seeking to " + closetMedia.time + "%");
                closetMedia.media.videoSeek(closetMedia.time);
            }
        });
    }

    private WritableImage initPlaceholder() {

        WritableImage placeholder = new WritableImage(W, H);
        PixelWriter writer = placeholder.getPixelWriter();
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; ++y) {
                writer.setColor(x, y, Color.BLACK);
            }
        }
        return placeholder;
    }


}
