package app.controller;

import app.model.DataModel;
import app.model.Media;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;

public class ListController {
    @FXML
    public ListView<Media> listView;


    private DataModel dataModel;

    public void initModel(DataModel model) {
        if (this.dataModel != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.dataModel = model;
        listView.setItems(model.getMediaList());

        listView.getSelectionModel().selectedItemProperty().addListener(((observableValue, m1, m2) ->
                model.setCurrentMedia(m2)));

        model.currentMediaProperty().addListener(((observableValue, m1, m2) -> {
            if (m2 == null) {
                listView.getSelectionModel().clearSelection();
            } else {
                listView.getSelectionModel().select(m2);
            }
        }));

        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Media> call(ListView<Media> mediaListView) {
                ListCell<Media> cell = new ListCell<>() {

                    @Override
                    protected void updateItem(Media media, boolean empty) {
                        super.updateItem(media, empty);
                        Platform.runLater(() -> {
                            if (empty) {
                                setText(null);
                            } else {
                                setText(media.getFileName());
                            }
                        });
                    }
                };
                cell.setOnMousePressed(mouseEvent -> {
                    if (cell.isEmpty()) {
                        mouseEvent.consume();
                        return;
                    }
                    if (mouseEvent.isSecondaryButtonDown() && cell.getItem().getIsVideo()) {
                        final ContextMenu contextMenu = new ContextMenu();
                        MenuItem setAudio = new MenuItem("Set audio");
                        contextMenu.getItems().addAll(setAudio);
                        setAudio.setOnAction(actionEvent -> {

                            FileChooser fileChooser = new FileChooser();
                            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Wav Files (*.wav)", "*.wav"));
                            File audioFile = fileChooser.showOpenDialog(listView.getScene().getWindow());
                            if (audioFile == null) return;
                            MenuController.progressFormTask("Loading audio...", new Task<>() {
                                @Override
                                protected Void call() throws Exception {
                                    cell.getItem().addAudio(audioFile);
                                    return null;
                                }
                            });
                        });
                        contextMenu.show(cell, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                    }
                });
                return cell;
            }
        });
    }

}
