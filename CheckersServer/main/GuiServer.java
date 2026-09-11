import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiServer extends Application {

    HashMap<String, Scene> sceneMap;
    Server serverConnection;
    ListView<String> listItems;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        listItems = new ListView<String>();
        serverConnection = new Server(data -> {
            Platform.runLater(() -> {
                listItems.getItems().add(data.toString());
            });
        });
        sceneMap = new HashMap<String, Scene>();
        sceneMap.put("server", createServerGui());
        primaryStage.setOnCloseRequest((WindowEvent t) -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.setScene(sceneMap.get("server"));
        primaryStage.setTitle("Checkers Server");
        primaryStage.show();
    }

    public Scene createServerGui() {
        Label title = new Label("Checkers Server Log");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f6ead7;");
        BorderPane pane = new BorderPane();
        VBox top = new VBox(title);
        top.setPadding(new Insets(18, 18, 8, 18));
        pane.setTop(top);
        pane.setCenter(listItems);
        pane.setPadding(new Insets(18));
        pane.setStyle("-fx-background-color: linear-gradient(to bottom, #2d180f, #4b2d1b, #24130c); -fx-font-family: 'System';");
        return new Scene(pane, 760, 560);
    }
}
