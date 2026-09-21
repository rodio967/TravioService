package api.places.wikipedia.client;

import api.http.HttpJson;
import api.places.wikipedia.dto.WikipediaGeosearchResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WikipediaGeosearchClient {

    private static final String BASE_URL = "https://ru.wikipedia.org/w/api.php";
    private static final String COMMON_PARAMS = "?action=query&format=json&formatversion=2";

    private static final int MAX_RADIUS_METERS = 10_000;
    private static final int PAGES_LIMIT = 500;

    private static final String ATTRACTION_TEMPLATES = Stream.of(
            "Достопримечательность", "Музей", "Театр", "Памятник", "Мемориал",
            "Культовое сооружение", "Храм", "Собор", "Монастырь", "Мечеть", "Синагога",
            "Дворец", "Крепость", "Башня", "Мост", "Фонтан",
            "Парк", "Сад", "Ботанический сад", "Зоопарк", "Площадь",
            "Библиотека", "Гостиница", "Стадион", "Некрополь",
            "Всемирное наследие", "Культурное наследие народов РФ"
    ).map(name -> "Шаблон:" + name).collect(Collectors.joining("|"));

    private final HttpJson httpJson;

    public WikipediaGeosearchClient(HttpJson httpJson) {
        this.httpJson = httpJson;
    }

    public CompletableFuture<List<WikipediaGeosearchResponse.Page>> fetchPages(double lat, double lon, int radiusMeters) {
        URI uri = buildUri(lat, lon, radiusMeters);

        return httpJson.fetchJson(uri, WikipediaGeosearchResponse.class)
                .thenCompose(first -> fetchRemainingTemplates(uri, first));
    }


    private CompletableFuture<List<WikipediaGeosearchResponse.Page>> fetchRemainingTemplates(URI uri, WikipediaGeosearchResponse first) {
        WikipediaGeosearchResponse.Continuation continuation = first.continuation();
        if (continuation == null || continuation.tlcontinue() == null) {
            return CompletableFuture.completedFuture(getPages(first));
        }

        return httpJson.fetchJson(buildContinuationUri(uri, continuation), WikipediaGeosearchResponse.class)
                .thenApply(response -> mergeTemplates(getPages(first), getPages(response)));
    }

    private List<WikipediaGeosearchResponse.Page> mergeTemplates(List<WikipediaGeosearchResponse.Page> pages, List<WikipediaGeosearchResponse.Page> rest) {
        Map<Long, List<WikipediaGeosearchResponse.Template>> restTemplates = rest.stream()
                .filter(WikipediaGeosearchResponse.Page::hasTemplates)
                .collect(Collectors.toMap(WikipediaGeosearchResponse.Page::pageid, WikipediaGeosearchResponse.Page::templates, (a, b) -> a));


        return pages.stream()
                .map(p -> p.hasTemplates() ? p : p.withTemplates(restTemplates.get(p.pageid())))
                .toList();
    }

    private URI buildUri(double lat, double lon, int radiusMeters) {
        return URI.create(BASE_URL + COMMON_PARAMS
                + "&generator=geosearch"
                + "&ggscoord=" + encode(lat + "|" + lon)
                + "&ggsradius=" + Math.min(radiusMeters, MAX_RADIUS_METERS)
                + "&ggslimit=" + PAGES_LIMIT
                + "&prop=" + encode("info|coordinates|templates|pageprops")
                + "&colimit=max"
                + "&tltemplates=" + encode(ATTRACTION_TEMPLATES)
                + "&tllimit=max"
                + "&ppprop=wikibase_item");
    }

    private URI buildContinuationUri(URI uri, WikipediaGeosearchResponse.Continuation continuation) {
        return URI.create(uri
                + "&tlcontinue=" + encode(continuation.tlcontinue())
                + "&continue=" + encode(continuation.value()));
    }

    private List<WikipediaGeosearchResponse.Page> getPages(WikipediaGeosearchResponse response) {
        if (response.query() == null || response.query().pages() == null) {
            return List.of();
        }

        return response.query().pages();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }


}
