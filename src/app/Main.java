package app;

import app.controller.MainController;
import app.model.DataModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ResourceBundle bundle = ResourceBundle.getBundle("string");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/main.fxml"));
        loader.setResources(bundle);
        Pane mainPane = loader.load();
        MainController mainController = loader.getController();

        DataModel model = new DataModel();
        mainController.initModel(model);

        primaryStage.setTitle("Media Synopsis");
        primaryStage.setScene(new Scene(mainPane, 800, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
