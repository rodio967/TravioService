package ui;

import model.Location;
import model.Place;

import java.util.List;

public interface MainView {

    void showLocations(List<Location> locations);

    void showWeather(String text);

    void showPlaces(List<Place> places);

    void showDescription(String text);

    void showStatus(String text);

    void setSearchEnabled(boolean enabled);

    void clearResults();
}
