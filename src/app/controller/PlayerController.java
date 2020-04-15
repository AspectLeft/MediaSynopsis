package app.controller;

import app.model.DataModel;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

//TODO add a control panel
public class PlayerController {
    @FXML
    public ImageView imageView;

    private DataModel dataModel;

    public void initModel(DataModel model) {
        if (this.dataModel != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.dataModel = model;

        model.currentMediaProperty().addListener(((observableValue, m1, m2) -> {
            if (m1 != null && m1.isPlaying()) {
                m1.stop();
            }
            if (m2 == null) return;
            if (m2.getIsImage()) {
                imageView.setImage(m2.getImage());
                return;
            }
            m2.currentFrameProperty().addListener(((observableValue1, image1, image2) -> {
                imageView.setImage(image2);
            }));
            m2.play();
        }));
    }
}