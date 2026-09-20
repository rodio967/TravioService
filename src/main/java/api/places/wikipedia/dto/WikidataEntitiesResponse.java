package api.places.wikipedia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WikidataEntitiesResponse(Map<String, Entity> entities) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entity(Map<String, Sitelink> sitelinks) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sitelink(String site) {}

}
