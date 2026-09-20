package api.places.wikipedia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record WikipediaGeosearchResponse(
        Query query,
        @JsonProperty("continue") Continuation continuation
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Query(List<Page> pages) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Page(
            long pageid,
            String title,
            int length,
            List<Coordinate> coordinates,
            List<Template> templates,
            PageProps pageprops
    ) {
        public String wikidataId() {
            return pageprops != null ? pageprops.wikibaseItem() : null;
        }

        public boolean hasTemplates() {
            return templates != null && !templates.isEmpty();
        }

        public Page withTemplates(List<Template> templates) {
            return new Page(pageid, title, length, coordinates, templates, pageprops);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Coordinate(double lat, double lon) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Template(String title) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PageProps(@JsonProperty("wikibase_item") String wikibaseItem) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Continuation(
            String tlcontinue,
            @JsonProperty("continue") String value
    ) {}
}
