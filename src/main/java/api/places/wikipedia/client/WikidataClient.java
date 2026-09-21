package api.places.wikipedia.client;

import api.http.HttpJson;
import api.places.wikipedia.dto.WikidataEntitiesResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class WikidataClient {

    private static final String BASE_URL = "https://www.wikidata.org/w/api.php";

    private final HttpJson httpJson;

    public WikidataClient(HttpJson httpJson) {
        this.httpJson = httpJson;
    }

    public CompletableFuture<Map<String, Integer>> fetchSitelinkCounts(List<String> wikidataIds) {
        if (wikidataIds.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }

        return httpJson.fetchJson(buildUri(wikidataIds), WikidataEntitiesResponse.class)
                .thenApply(response -> toSitelinkCounts(response));
    }

    private URI buildUri(List<String> wikidataIds) {
        return URI.create(BASE_URL
                + "?action=wbgetentities&format=json&props=sitelinks"
                + "&ids=" + URLEncoder.encode(String.join("|", wikidataIds), StandardCharsets.UTF_8));
    }

    private Map<String, Integer> toSitelinkCounts(WikidataEntitiesResponse response) {
        if (response.entities() == null) {
            return Map.of();
        }

        return response.entities().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().sitelinks() != null ? e.getValue().sitelinks().size() : 0));
    }
}
