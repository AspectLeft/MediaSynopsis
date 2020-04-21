package app.controller;

import app.model.DataModel;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;

public class PlayerController {
    @FXML
    public ImageView imageView;
    public Button playButton;
    public Button pauseButton;
    public Button stopButton;
    public Slider timeSlider;

    private DataModel dataModel;

    private InvalidationListener timeSliderUpdater;

    public void initModel(DataModel model) {
        if (this.dataModel != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.dataModel = model;

        model.currentMediaProperty().addListener(((observableValue, m1, m2) -> {
            if (m1 != null && m1.isPlaying()) {
                m1.stop();
            }
            if (m1 != null && m1.getIsVideo()) {
                m1.audioPlayer.currentTimeProperty().removeListener(timeSliderUpdater);
            }
            playButton.disableProperty().unbind();
            pauseButton.disableProperty().unbind();
            stopButton.disableProperty().unbind();
            playButton.setDisable(true);
            pauseButton.setDisable(true);
            stopButton.setDisable(true);
            timeSlider.setValue(0);
            timeSlider.setDisable(true);
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
            timeSlider.setDisable(false);

            timeSliderUpdater = ov -> Platform.runLater(() -> timeSlider.setValue(
                    m2.audioPlayer.getCurrentTime().toMillis() / m2.audioPlayer.getTotalDuration().toMillis() * 100));
            m2.audioPlayer.currentTimeProperty().addListener(timeSliderUpdater);
            m2.play();
        }));

        timeSlider.valueProperty().addListener(observable -> {
            if (timeSlider.isPressed()) {
                model.videoSeek(timeSlider.getValue());
            }
        });
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


    public void cleanup() {
        playButton.disableProperty().unbind();
        pauseButton.disableProperty().unbind();
        stopButton.disableProperty().unbind();
    }
}