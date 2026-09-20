package api.geocoding;

import model.Location;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GeoCodingClient {

    CompletableFuture<List<Location>> searchLocations(String query);


}
