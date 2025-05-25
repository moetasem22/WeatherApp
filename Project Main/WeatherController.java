package com.weatherapp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class WeatherController {
    private final ComboBox<String> cityComboBox = new ComboBox<>();
    private final Label cityLabel = new Label();
    private final Label tempLabel = new Label();
    private final Label humidityLabel = new Label();
    private final Label windSpeedLabel = new Label();
    private final Label weatherDescLabel = new Label();
    private final ImageView weatherIcon = new ImageView();
    private final ProgressIndicator loadingSpinner = new ProgressIndicator();
    private final WeatherAPI weatherAPI = new WeatherAPI();

    public VBox createUI() {
        configureComponents();
        return createMainLayout();
    }

    private void configureComponents() {
        cityComboBox.setPromptText("Select a city");
        cityComboBox.setPrefWidth(300);
        cityComboBox.getStyleClass().add("combo-box");
        loadCitiesFromJSON();
        cityComboBox.setOnAction(e -> fetchWeatherData());

        weatherIcon.setFitWidth(120);
        weatherIcon.setFitHeight(120);
        weatherIcon.setPreserveRatio(true);
        weatherIcon.getStyleClass().add("weather-icon");

        loadingSpinner.setVisible(false);
        loadingSpinner.setMaxSize(40, 40);

        setupLabels();
    }

    private void setupLabels() {
        Font titleFont = Font.font("Arial", FontWeight.BOLD, 24);
        Font dataFont  = Font.font("Arial", FontWeight.NORMAL, 20);
        Font descFont  = Font.font("Arial", FontWeight.NORMAL, 16);

        cityLabel.setFont(titleFont);
        tempLabel.setFont(dataFont);
        humidityLabel.setFont(dataFont);
        windSpeedLabel.setFont(dataFont);
        weatherDescLabel.setFont(descFont);

        cityLabel.getStyleClass().add("city-label");
        tempLabel.getStyleClass().add("temperature-label");
        weatherDescLabel.getStyleClass().add("description-label");
    }

    private VBox createMainLayout() {
        HBox header         = createHeader();
        HBox inputSection   = createInputSection();
        HBox currentWeather = createWeatherDisplay();
        HBox footer         = createFooter();

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.getChildren().addAll(
                header,
                inputSection,
                currentWeather,
                footer
        );
        mainLayout.getStyleClass().add("main-layout");
        return mainLayout;
    }

    private HBox createInputSection() {
        HBox inputBox = new HBox(15);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.getChildren().add(cityComboBox);
        return inputBox;
    }

    private HBox createWeatherDisplay() {
        // Icon and description stacked
        StackPane iconHolder = new StackPane(weatherIcon, loadingSpinner);
        iconHolder.getStyleClass().add("icon-container");
        VBox iconSection = new VBox(8, iconHolder, weatherDescLabel);
        iconSection.setAlignment(Pos.CENTER);

        VBox weatherData = new VBox(12);
        weatherData.getChildren().addAll(
                cityLabel,
                tempLabel,
                humidityLabel,
                windSpeedLabel
        );
        weatherData.getStyleClass().add("weather-data");

        HBox currentWeather = new HBox(30);
        currentWeather.setAlignment(Pos.CENTER_LEFT);
        currentWeather.getChildren().addAll(iconSection, weatherData);
        return currentWeather;
    }

    private HBox createHeader() {
        Label title = new Label("WeatherWise");
        title.getStyleClass().add("app-title");
        return createCenteredBox(title);
    }

    private HBox createFooter() {
        Hyperlink credit = new Hyperlink("Data provided by OpenWeatherMap");
        credit.getStyleClass().add("footer-text");

        // Add click handler to open website
        credit.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(
                        new java.net.URI("https://openweathermap.org")
                );
            } catch (Exception ex) {
                System.err.println("Error opening website: " + ex.getMessage());
            }
        });

        return createCenteredBox(credit);
    }

    private HBox createCenteredBox(Label content) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(content);
        return box;
    }

    private void loadCitiesFromJSON() {
        try (InputStream stream = getClass().getResourceAsStream("/capitals.json");
             InputStreamReader reader = new InputStreamReader(stream)) {

            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            List<Map<String, String>> capitals = new Gson().fromJson(reader, listType);

            capitals.stream()
                    .map(entry -> entry.get("capital"))
                    .filter(capital -> capital != null && !capital.isEmpty())
                    .sorted()
                    .forEach(cityComboBox.getItems()::add);

        } catch (Exception e) {
            showError("Failed to load cities: " + e.getMessage());
        }
    }

    private void fetchWeatherData() {
        String city = cityComboBox.getValue();
        if (city == null || city.trim().isEmpty()) return;

        toggleLoading(true);
        new Thread(() -> {
            WeatherResponse response = weatherAPI.fetchWeather(city);
            Platform.runLater(() -> {
                toggleLoading(false);
                if (response != null) {
                    updateWeatherUI(response);
                } else {
                    showError("Could not retrieve weather data");
                }
            });
        }).start();
    }

    private void toggleLoading(boolean isLoading) {
        loadingSpinner.setVisible(isLoading);
        weatherIcon.setVisible(!isLoading);
        cityComboBox.setDisable(isLoading);
    }

    private void updateWeatherUI(WeatherResponse response) {
        cityLabel.setText(response.name);
        weatherDescLabel.setText(response.weather.get(0).description);

        tempLabel.setText(String.format("Temperature: %.1f°C", response.main.temp));
        humidityLabel.setText(String.format("Humidity: %d%%", response.main.humidity));
        windSpeedLabel.setText(String.format("Wind: %.1f km/h", response.wind.speed));

        loadWeatherIcon(response.weather.get(0).icon);
    }

    private void loadWeatherIcon(String iconCode) {
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        try {
            Image image = new Image(iconUrl, true);
            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0) {
                    weatherIcon.setImage(image);
                }
            });
        } catch (Exception e) {
            weatherIcon.setImage(new Image("/default-weather.png"));
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}