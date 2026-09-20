package service;

import api.geocoding.GeoCodingClient;
import api.places.PlacesClient;
import api.weather.WeatherClient;
import model.Location;
import model.Place;
import model.Weather;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TravioService {

    private static final int PLACES_RADIUS_METERS = 5000;

    private final GeoCodingClient geoCodingClient;
    private final WeatherClient weatherClient;
    private final PlacesClient placesClient;

    public TravioService(GeoCodingClient geoCodingClient, WeatherClient weatherClient, PlacesClient placesClient) {
        this.geoCodingClient = geoCodingClient;
        this.weatherClient = weatherClient;
        this.placesClient = placesClient;
    }

    public CompletableFuture<List<Location>> searchLocations(String query) {
        return geoCodingClient.searchLocations(query);
    }

    public CompletableFuture<Weather> fetchWeather(Location location) {
        return weatherClient.fetchWeather(location.lat(), location.lon());
    }

    public CompletableFuture<List<Place>> fetchPlaces(Location location) {
        return placesClient.fetchPlaces(location.lat(), location.lon(), PLACES_RADIUS_METERS);
    }


}
