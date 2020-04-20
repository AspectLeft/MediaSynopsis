package app.controller;

import app.model.DataModel;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class MainController {
    @FXML
    public StackPane menu;
    public MenuController menuController;
    public StackPane list;
    public ListController listController;
    public BorderPane player;
    public PlayerController playerController;

    public void initModel(DataModel model) {
        menuController.initModel(model);
        listController.initModel(model);
        playerController.initModel(model);
    }

    public void cleanup() {
        playerController.cleanup();
    }
}
