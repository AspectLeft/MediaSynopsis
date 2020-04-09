package app.controller;

import app.model.DataModel;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

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
        model.loadData(selectedDir);
    }

    @FXML
    public void exit() {
        menuBar.getScene().getWindow().hide();
    }
}
