package api.weather.openweather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherResponse(
        Main main,
        List<Description> weather,
        Wind wind
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(double temp,
                       @JsonProperty("feels_like")
                       double feelsLike) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Description(String description, String icon) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wind(double speed) {}
}
