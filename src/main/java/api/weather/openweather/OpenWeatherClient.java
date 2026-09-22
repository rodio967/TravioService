package api.weather.openweather;

import api.http.HttpJson;
import api.weather.WeatherClient;
import model.Weather;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpenWeatherClient implements WeatherClient {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    private final HttpJson httpJson;
    private final String apiKey;

    public OpenWeatherClient(HttpJson httpJson, String apiKey) {
        this.httpJson = httpJson;
        this.apiKey = apiKey;
    }

    @Override
    public CompletableFuture<Weather> fetchWeather(double lat, double lon) {
        URI uri = buildUri(lat, lon);

        return httpJson.fetchJson(uri, OpenWeatherResponse.class)
                .thenApply(weatherResponse -> toWeather(weatherResponse));
    }

    private URI buildUri(double lat, double lon) {
        return URI.create(BASE_URL
                + "?lat=" + lat
                + "&lon=" + lon
                + "&appid=" + apiKey
                + "&units=metric"
                + "&lang=ru");
    }

    private Weather toWeather(OpenWeatherResponse weatherResponse) {
        OpenWeatherResponse.Main main = weatherResponse.main();
        if (main == null) {
            throw new RuntimeException("сервис погоды вернул ответ без температуры");
        }

        List<OpenWeatherResponse.Description> weather = weatherResponse.weather();
        String description = (weather == null || weather.isEmpty() || weather.get(0).description() == null)
                ? ""
                : weather.get(0).description();

        OpenWeatherResponse.Wind wind = weatherResponse.wind();
        double windMs = wind != null ? wind.speed() : 0.0;

        return new Weather(main.temp(),
                main.feelsLike(),
                description,
                windMs);
    }
}
