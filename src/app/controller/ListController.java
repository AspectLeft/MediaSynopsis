package app.controller;

import app.model.DataModel;
import app.model.Media;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

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

        listView.setCellFactory(mediaListView -> new ListCell<>() {
            @Override
            protected void updateItem(Media media, boolean empty) {
                super.updateItem(media, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    setText(media.getFileName());
                }
            }
        });
    }
}
