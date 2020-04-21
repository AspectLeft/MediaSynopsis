package app.controller;

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

    public void initModel() {
        WritableImage placeholder = initPlaceholder();
        imageView.setImage(placeholder);

        imageView.setOnMouseClicked(mouseEvent -> {
            double x = mouseEvent.getX();
            double y = mouseEvent.getY();
            System.out.println(String.format("x:%f,y:%f", x, y));
            placeholder.getPixelWriter().setColor((int) x, (int) y, Color.WHITE);
            //imageView.setImage(placeholder);
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
