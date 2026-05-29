package hotel;

import hotel.service.HotelService;
import hotel.ui.MainUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HotelApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        HotelService service = new HotelService();
        MainUI mainUI = new MainUI(service);

        Scene scene = new Scene(mainUI.buildRoot(), 1100, 720);
        scene.getStylesheets().add(getClass().getResource("/hotel/style.css").toExternalForm());

        primaryStage.setTitle("🏨  Grand Vista Hotel — Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
