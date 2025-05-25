package com.weatherapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Locale;

public class WeatherApp extends Application {
    private WeatherController controller;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        controller = new WeatherController();

        VBox root = controller.createUI();
        Scene scene = new Scene(root, 700, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setTitle("Weather App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
