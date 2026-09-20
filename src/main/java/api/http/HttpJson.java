package api.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class HttpJson {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final String USER_AGENT = "Travio/1.0 (desktop app)"; // для wikipedia

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpJson(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public <T> CompletableFuture<T> fetchJson(URI uri, Class<T> type) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> parse(response, type));
    }


    private <T> T parse(HttpResponse<String> response, Class<T> type) {
        int status = response.statusCode();

        if (status == 429) throw new RuntimeException("сервис временно ограничил запросы, попробуйтек через минуту");
        if (status / 100 != 2) throw new RuntimeException("Сервис " + response.uri().getHost() + " ответил " + status);

        try {
            return objectMapper.readValue(response.body(), type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error: ошибка парсинга ответа " + response.uri().getHost(), e);
        }

    }
}
