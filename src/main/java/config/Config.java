package config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {

    private static final Path ENV_FILE = Path.of(".env");

    private final Properties properties = new Properties();

    public Config() {
        loadEnvFile();
    }

    private void loadEnvFile() {
        if (!Files.exists(ENV_FILE)) return;

        try (BufferedReader br = new BufferedReader(new FileReader(ENV_FILE.toFile()))) {
            properties.load(br);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать " + ENV_FILE.toAbsolutePath(), e);
        }
    }

    public String getEnvValue(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) return value;

        value = properties.getProperty(key);
        if (value != null && !value.isBlank()) return value;

        throw new IllegalStateException("Не задан " + key
                + ": укажите его в переменной окружения или в файле " + ENV_FILE.toAbsolutePath());
    }
}
