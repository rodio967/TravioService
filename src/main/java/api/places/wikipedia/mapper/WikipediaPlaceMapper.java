package api.places.wikipedia.mapper;

import api.places.wikipedia.dto.WikipediaExtractsResponse;
import api.places.wikipedia.dto.WikipediaGeosearchResponse;
import model.Place;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public final class WikipediaPlaceMapper {

    private static final int MAX_CANDIDATES = 50;
    private static final int MAX_RESULTS = 15;

    private WikipediaPlaceMapper() {}

    public static List<WikipediaGeosearchResponse.Page> selectCandidates(List<WikipediaGeosearchResponse.Page> pages) {
        return pages.stream()
                .filter(p -> p.title() != null && !p.title().isBlank())
                .filter(p -> p.coordinates() != null && !p.coordinates().isEmpty())
                .filter(p -> p.wikidataId() != null)
                .filter(WikipediaGeosearchResponse.Page::hasTemplates)
                .sorted(Comparator.comparingInt(WikipediaGeosearchResponse.Page::length).reversed())
                .limit(MAX_CANDIDATES)
                .toList();
    }

    public static List<String> toWikidataIds(List<WikipediaGeosearchResponse.Page> candidates) {
        return candidates.stream()
                .map(WikipediaGeosearchResponse.Page::wikidataId)
                .toList();
    }

    public static List<WikipediaGeosearchResponse.Page> selectPopular(
            List<WikipediaGeosearchResponse.Page> candidates,
            Map<String, Integer> sitelinkCounts
    ) {
        return candidates.stream()
                .sorted(Comparator.comparingInt(
                        (WikipediaGeosearchResponse.Page p) -> sitelinkCounts.getOrDefault(p.wikidataId(), 0)
                ).reversed())
                .limit(MAX_RESULTS)
                .toList();
    }

    public static List<Place> toPlaces(List<WikipediaGeosearchResponse.Page> attractions,
                                       WikipediaExtractsResponse descriptions) {
        Map<Long, String> extractById = getPages(descriptions).stream()
                .filter(p -> p.extract() != null)
                .collect(Collectors.toMap(WikipediaExtractsResponse.Page::pageid,
                        WikipediaExtractsResponse.Page::extract,
                        (a, b) -> a));

        return attractions.stream()
                .map(p -> new Place(
                        p.title(),
                        extractById.getOrDefault(p.pageid(), ""),
                        p.coordinates().get(0).lat(),
                        p.coordinates().get(0).lon()))
                .toList();
    }

    private static List<WikipediaExtractsResponse.Page> getPages(WikipediaExtractsResponse response) {
        if (response.query() == null || response.query().pages() == null) {
            return List.of();
        }

        return response.query().pages();
    }
}
