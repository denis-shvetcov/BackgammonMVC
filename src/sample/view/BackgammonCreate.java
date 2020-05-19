package sample.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.model.BackgammonModel;
import sample.model.Player;

import java.io.FileNotFoundException;

public class BackgammonCreate extends Application {
    private static Stage window;
    private static Player player;
    private static BackgammonModel backgammonModel;
    private static BackgammonView backgammonView;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        window = primaryStage;

        window.setResizable(true);

        Player player = new Player();
        BackgammonModel backgammonModel = new BackgammonModel(player);
        BackgammonView backgammonView = new BackgammonView(backgammonModel, player);

        Scene scene = new Scene(backgammonView, BackgammonView.FIELD_WIDTH + backgammonView.getStuffWidth(), BackgammonView.FIELD_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("css\\Backgammon.css").toExternalForm());

        window.setScene(scene);
        window.setTitle("Long backgammon");

        window.show();
    }

    public static void refresh() throws FileNotFoundException {
         player = new Player();
         backgammonModel = new BackgammonModel(player);
         backgammonView = new BackgammonView(backgammonModel, player);
         window.getScene().setRoot(backgammonView);
    }

    public static void close() {
        window.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


