package ui;

import model.Location;
import model.Place;
import model.Weather;
import service.TravioService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class MainPresenter {

    private final TravioService travioService;
    private final MainView view;
    private final Executor uiExecutor;

    private Location currentLocation;

    public MainPresenter(TravioService travioService, MainView view, Executor uiExecutor) {
        this.travioService = travioService;
        this.view = view;
        this.uiExecutor = uiExecutor;
    }

    public void onSearch(String query) {
        String trimmed = query.trim();

        if (trimmed.isBlank()) {
            view.showStatus("Введите название города");
            return;
        }

        currentLocation = null;
        view.setSearchEnabled(false);
        view.showStatus("Поиск локаций");
        view.clearResults();

        travioService.searchLocations(trimmed)
                .whenCompleteAsync((locations, error) -> {
                    view.setSearchEnabled(true);
                    if (error != null) {
                        view.showStatus("Не удалось найти локации: " + errorMessage(error));
                        return;
                    }

                    if (locations.isEmpty()) {
                        view.showStatus("Ничего не найдено");
                        return;
                    }

                    view.showLocations(locations);
                    view.showStatus("Найдено локаций " + locations.size());
                }, uiExecutor);
    }

    public void onLocationSelected(Location location) {
        currentLocation = location;

        view.showStatus("Загрузка погоды и мест");
        view.showWeather("Загрузка");
        view.showPlaces(List.of());
        view.showDescription("");

        CompletableFuture<Weather> weatherFuture = travioService.fetchWeather(location);
        CompletableFuture<List<Place>> placesFuture = travioService.fetchPlaces(location);

        weatherFuture.whenCompleteAsync((weather, error) -> {
            if (isStale(location)) return;

            if (error != null) {
                view.showWeather("Погода недоступна: " + errorMessage(error));
            } else {
                view.showWeather(formatWeather(weather));
            }
        }, uiExecutor);

        placesFuture.whenCompleteAsync((places, error) -> {
            if (isStale(location)) return;

            if (error == null) view.showPlaces(places);
        }, uiExecutor);

        CompletableFuture.allOf(weatherFuture, placesFuture)
                .whenCompleteAsync((ignored, ignoredError) -> {
                    if (isStale(location)) return;

                    view.showStatus(summarize(weatherFuture, placesFuture));
                }, uiExecutor);
    }

    public void onPlaceSelected(Place place) {
        view.showDescription(place != null ? place.description() : "");
    }

    private String summarize(CompletableFuture<Weather> weatherFuture, CompletableFuture<List<Place>> placesFuture) {
        List<String> problems = new ArrayList<>();

        if (weatherFuture.isCompletedExceptionally()) problems.add("Погода недоступна");
        if (placesFuture.isCompletedExceptionally()) problems.add("Места недоступны: " + errorMessage(placesFuture.exceptionNow()));

        if (problems.isEmpty()) return "Мест найдено " + placesFuture.join().size();

        return String.join("; ", problems);
    }

    private boolean isStale(Location location) {
        return !Objects.equals(currentLocation, location);
    }


    private String errorMessage(Throwable error) {
        Throwable cause = error instanceof CompletionException && error.getCause() != null
                ? error.getCause()
                : error;

        return cause.getMessage();
    }

    private String formatWeather(Weather w) {
        return "Температура: " + w.tempC() + " C\n"
                + "Ощущается как: " + w.feelsLike() + " C\n"
                + w.description() + "\n"
                + "Ветер: " + w.windMs() + " м/с";
    }
}
