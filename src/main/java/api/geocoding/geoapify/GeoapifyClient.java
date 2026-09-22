package api.geocoding.geoapify;

import api.geocoding.GeoCodingClient;
import api.http.HttpJson;
import model.Location;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class GeoapifyClient implements GeoCodingClient {

    private static final String BASE_URL = "https://api.geoapify.com/v1/geocode/search";
    private static final int LIMIT = 5;

    private final HttpJson httpJson;
    private final String apiKey;

    public GeoapifyClient(HttpJson httpJson, String apiKey) {
        this.httpJson = httpJson;
        this.apiKey = apiKey;
    }

    @Override
    public CompletableFuture<List<Location>> searchLocations(String query) {
        URI uri = buildUri(query);

        return httpJson.fetchJson(uri, GeoapifyResponse.class)
                .thenApply(response -> toLocations(response));
    }

    private URI buildUri(String query) {
        return URI.create(BASE_URL
                + "?text=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&format=json"
                + "&lang=ru"
                + "&limit=" + LIMIT
                + "&apiKey=" + apiKey);
    }

    private List<Location> toLocations(GeoapifyResponse response) {
        if (response.results() == null) {
            return List.of();
        }

        Set<String> seen = new HashSet<>();
        return response.results().stream()
                .map(result -> toLocation(result))
                .filter(location -> seen.add(location.name() + "|" + location.country()))
                .toList();
    }


    private Location toLocation(GeoapifyResponse.Result result) {
        String name = result.city() != null ? result.city() : result.formatted();

        return new Location(
                name,
                result.country(),
                result.city(),
                result.lat(),
                result.lon()
        );
    }
}
