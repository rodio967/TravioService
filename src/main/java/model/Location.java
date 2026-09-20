package model;

public record Location(
        String name,
        String country,
        String city,
        double lat,
        double lon
) {}
