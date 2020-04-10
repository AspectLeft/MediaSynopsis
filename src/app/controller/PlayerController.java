package app.controller;

import app.model.DataModel;
import javafx.fxml.FXML;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

//TODO add a control panel
public class PlayerController {
    @FXML
    public MediaView mediaView;

    private DataModel dataModel;

    private MediaPlayer mediaPlayer;

    public void initModel(DataModel model) {
        if (this.dataModel != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.dataModel = model;

        model.currentMediaProperty().addListener(((observableValue, m1, m2) -> {
            if (m2 == null) return;
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            mediaPlayer = new MediaPlayer(m2.getMedia());
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.play();
        }));
    }
}