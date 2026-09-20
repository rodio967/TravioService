package api.geocoding.geoapify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoapifyResponse(List<Result> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            String formatted,
            String country,
            String city,
            String state,
            double lat,
            double lon
    ) {}
}
