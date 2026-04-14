import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FirstPage extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Create a simple label
        Label label = new Label("Library Management System - Starting...\n\n\n\tBy code switchers");

        // Put the label in a layout container
        StackPane root = new StackPane();
        root.getChildren().add(label);

        // Create the scene (the content) and set it on the stage (the window)
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("Library System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}