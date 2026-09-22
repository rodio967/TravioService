import api.geocoding.geoapify.GeoapifyClient;
import api.http.HttpJson;
import api.places.wikipedia.client.WikidataClient;
import api.places.wikipedia.client.WikipediaGeosearchClient;
import api.places.wikipedia.client.WikipediaPlacesClient;
import api.weather.openweather.OpenWeatherClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.Config;
import service.TravioService;
import ui.MainPresenter;
import ui.MainWindow;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.net.http.HttpClient;
import java.time.Duration;

public class Main {

    public static void main(String[] args) {

        TravioService travioService;
        try {
            travioService = createService();
        } catch (RuntimeException e) {
            showStartupError(e.getMessage());
            System.exit(1);
            return;
        }


        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setPresenter(new MainPresenter(travioService, window, SwingUtilities::invokeLater));
            window.setVisible(true);
        });
    }

    private static TravioService createService() {
        HttpJson httpJson = new HttpJson(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build(),
                new ObjectMapper());

        Config config = new Config();

        return new TravioService(
                new GeoapifyClient(httpJson, config.getEnvValue("GEOAPIFY_KEY")),
                new OpenWeatherClient(httpJson, config.getEnvValue("OPENWEATHER_KEY")),
                new WikipediaPlacesClient(
                        httpJson,
                        new WikipediaGeosearchClient(httpJson),
                        new WikidataClient(httpJson)));
    }


    private static void showStartupError(String message) {
        System.err.println(message);

        try {
            SwingUtilities.invokeAndWait(() -> JOptionPane.showMessageDialog(
                    null, message, "Travio: ошибка запуска", JOptionPane.ERROR_MESSAGE));
        } catch (Exception ignored) {}
    }
}
