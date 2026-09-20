package api.places;

import model.Place;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PlacesClient {

    CompletableFuture<List<Place>> fetchPlaces(double lat, double lon, int radiusMeters);

}
