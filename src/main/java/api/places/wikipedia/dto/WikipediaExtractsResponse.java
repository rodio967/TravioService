package api.places.wikipedia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WikipediaExtractsResponse(Query query) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Query(List<Page> pages) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Page(
            long pageid,
            String extract
    ) {}

}
