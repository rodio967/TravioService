package api.places.wikipedia.client;

import api.http.HttpJson;
import api.places.PlacesClient;
import api.places.wikipedia.dto.WikipediaExtractsResponse;
import api.places.wikipedia.dto.WikipediaGeosearchResponse;
import api.places.wikipedia.dto.WikipediaGeosearchResponse.Page;
import api.places.wikipedia.mapper.WikipediaPlaceMapper;
import model.Place;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WikipediaPlacesClient implements PlacesClient {

    private static final String BASE_URL = "https://ru.wikipedia.org/w/api.php";
    private static final String COMMON_PARAMS = "?action=query&format=json&formatversion=2";
    private static final int DESCRIPTION_CHARS = 1200;


    private final HttpJson httpJson;
    private final WikipediaGeosearchClient geosearchClient;
    private final WikidataClient wikidataClient;

    public WikipediaPlacesClient(HttpJson httpJson, WikipediaGeosearchClient geosearchClient, WikidataClient wikidataClient) {
        this.httpJson = httpJson;
        this.geosearchClient = geosearchClient;
        this.wikidataClient = wikidataClient;
    }

    @Override
    public CompletableFuture<List<Place>> fetchPlaces(double lat, double lon, int radiusMeters) {
        return geosearchClient.fetchPages(lat, lon, radiusMeters)
                .thenApply(pages -> WikipediaPlaceMapper.selectCandidates(pages))
                .thenCompose(candidates -> selectPopular(candidates))
                .thenCompose(attractions -> fetchDescriptions(attractions));
    }


    private CompletableFuture<List<Page>> selectPopular(List<Page> candidates) {
        List<String> ids = WikipediaPlaceMapper.toWikidataIds(candidates);

        return wikidataClient.fetchSitelinkCounts(ids)
                .thenApply(sitelinkCounts -> WikipediaPlaceMapper.selectPopular(candidates, sitelinkCounts));
    }

    private CompletableFuture<List<Place>> fetchDescriptions(List<Page> attractions) {
        if (attractions.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        return httpJson.fetchJson(buildDescriptionsUri(attractions), WikipediaExtractsResponse.class)
                .thenApply(descriptions -> WikipediaPlaceMapper.toPlaces(attractions, descriptions));
    }


    private URI buildDescriptionsUri(List<WikipediaGeosearchResponse.Page> attractions) {
        String pageIds = attractions.stream()
                .map(p -> String.valueOf(p.pageid()))
                .collect(Collectors.joining("|"));


        return URI.create(BASE_URL + COMMON_PARAMS
                + "&pageids=" + URLEncoder.encode(pageIds, StandardCharsets.UTF_8)
                + "&prop=extracts&exintro=1&explaintext=1&exlimit=max"
                + "&exchars=" + DESCRIPTION_CHARS);
    }
}
