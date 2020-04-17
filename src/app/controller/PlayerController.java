package app.controller;

import app.model.DataModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

//TODO add a control panel
public class PlayerController {
    @FXML
    public ImageView imageView;
    public Button playButton;
    public Button pauseButton;
    public Button stopButton;

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
            playButton.disableProperty().unbind();
            pauseButton.disableProperty().unbind();
            stopButton.disableProperty().unbind();
            playButton.setDisable(true);
            pauseButton.setDisable(true);
            stopButton.setDisable(true);
            if (m2 == null) return;
            if (m2.getIsImage()) {
                imageView.setImage(m2.getImage());

                return;
            }
            m2.currentFrameProperty().addListener(((observableValue1, image1, image2) -> {
                imageView.setImage(image2);
            }));


            playButton.disableProperty().bind(m2.getIsPlayingProperty());
            pauseButton.disableProperty().bind(m2.getStoppedProperty());
            stopButton.disableProperty().bind(m2.getStoppedProperty());

            m2.play();
        }));
    }

    @FXML
    public void play() {
        dataModel.getCurrentMedia().play();
    }

    @FXML
    public void pause() {
        dataModel.getCurrentMedia().pause();
    }

    @FXML
    public void stop() {
        dataModel.getCurrentMedia().stop();
    }


}