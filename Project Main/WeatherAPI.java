package com.weatherapp;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherAPI {

    private static final String API_KEY = "1d716718ea8851fee109b7fe01e6f01b";  // Store API key securely
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    public WeatherResponse fetchWeather(String city) {
        String urlString = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            Gson gson = new Gson();
            return gson.fromJson(reader, WeatherResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
