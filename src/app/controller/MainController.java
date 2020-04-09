package app.controller;

import app.model.DataModel;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class MainController {
    @FXML
    public StackPane menu;
    public MenuController menuController;
    public StackPane list;
    public ListController listController;


    public void initModel(DataModel model) {
        menuController.initModel(model);
        listController.initModel(model);
    }
}
