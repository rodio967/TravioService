package api.weather;

import model.Weather;

import java.util.concurrent.CompletableFuture;

public interface WeatherClient {

    CompletableFuture<Weather> fetchWeather(double lat, double lon);

}
