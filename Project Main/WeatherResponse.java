package com.weatherapp;

import java.util.List;

public class WeatherResponse {
    public Main main;
    public Wind wind;
    public List<Weather> weather;
    public String name; // City name

    public static class Main {
        public double temp;
        public int humidity;
    }

    public static class Wind {
        public double speed;
    }

    public static class Weather {
        public String main;
        public String description;
        public String icon; // This is the icon code
    }
}