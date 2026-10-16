package edu.rentcar.app;

import edu.rentcar.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RentCarApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                RentCarApplication.class.getResource("/edu/rentcar/view/main-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 1100, 700);
        stage.setTitle("RentCar - Rental Management");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
