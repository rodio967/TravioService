package model;

public record Weather(
        double tempC,
        double feelsLike,
        String description,
        double windMs
) {}
