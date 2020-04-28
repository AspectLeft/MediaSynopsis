package app.controller;

import app.model.DataModel;
import app.model.Media;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.stage.*;

import java.io.File;

public class MenuController {

    private DataModel model ;

    @FXML
    private MenuBar menuBar ;

    public void initModel(DataModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.model = model ;
    }

    @FXML
    public void load() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDir = directoryChooser.showDialog(menuBar.getScene().getWindow());
        if (selectedDir == null) return;

        ProgressForm progressForm = new ProgressForm("Loading files...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                model.loadData(selectedDir);
                return null;
            }
        };
        progressForm.activateProgressBar(task);
        task.setOnSucceeded(event -> {
            progressForm.getDialogStage().close();
        });

        progressForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();
    }

    @FXML
    public void exit() {
        menuBar.getScene().getWindow().hide();
    }

    @FXML
    public void addImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("RGB Files (*.rgb)", "*.rgb"));
        File imageFile = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        if (imageFile == null) return;
        progressFormTask("Loading image", new Task<>() {
            @Override
            protected Void call() throws Exception {
                model.getMediaList().add(new Media(imageFile));
                return null;
            }
        });
    }

    @FXML
    public void addVideo() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDir = directoryChooser.showDialog(menuBar.getScene().getWindow());
        if (selectedDir == null) return;

        progressFormTask("Loading video", new Task<>() {
            @Override
            protected Void call() throws Exception {
                model.getMediaList().add(new Media(selectedDir));
                return null;
            }
        });
    }

    public void generateSynopsis() {
        progressFormTask("Generating synopsis...", new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                model.generateSynposis();
                return null;
            }
        });
    }

    public static void progressFormTask(String title, Task<Void> task) {
        ProgressForm progressForm = new ProgressForm(title);
        progressForm.activateProgressBar(task);
        task.setOnSucceeded(event -> progressForm.getDialogStage().close());

        progressForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();
    }

    public static class ProgressForm {
        private final Stage dialogStage;
        private final ProgressBar pb = new ProgressBar();
        private final ProgressIndicator pin = new ProgressIndicator();

        public ProgressForm(String title) {
            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setOnCloseRequest(Event::consume);
            dialogStage.setTitle(title);

            // PROGRESS BAR
            final Label label = new Label();
            label.setText("alerto");

            pb.setProgress(-1F);
            pin.setProgress(-1F);

            final HBox hb = new HBox();
            hb.setSpacing(5);
            hb.setAlignment(Pos.CENTER);
            hb.getChildren().addAll(pb, pin);

            Scene scene = new Scene(hb);
            dialogStage.setScene(scene);
        }

        public void activateProgressBar(final Task<?> task)  {
            pb.progressProperty().bind(task.progressProperty());
            pin.progressProperty().bind(task.progressProperty());
            dialogStage.show();
        }

        public Stage getDialogStage() {
            return dialogStage;
        }
    }
}
