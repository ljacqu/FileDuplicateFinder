package ch.jalu.fileduplicatefinder.config;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;

import static com.google.common.math.IntMath.ceilingPowerOfTwo;

public class FileUtilConfiguration {

    private final Scanner scanner;
    private final Properties defaultProperties;
    @Nullable
    private final Properties userProperties;
    private final Map<String, String> valuesFromScanner = new HashMap<>();

    public FileUtilConfiguration(Scanner scanner, @Nullable Path userPropertyFile) {
        this.scanner = scanner;
        try {
            defaultProperties = createDefaultProperties();
            userProperties = createUserPropertiesOrNull(userPropertyFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getString(String key) {
        Optional<String> stringFromPrimarySources = getStringOptional(key);
        if (stringFromPrimarySources.isPresent()) {
            return stringFromPrimarySources.get();
        }

        System.out.println("Provide value for '" + key + "':");
        String valueFromScanner = scanner.nextLine();
        valuesFromScanner.put(key, valueFromScanner);
        return valueFromScanner;
    }

    public Optional<String> getStringOptional(String key) {
        if (valuesFromScanner.containsKey(key)) {
            return Optional.of(valuesFromScanner.get(key));
        }

        String systemProperty = System.getProperty(key);
        if (systemProperty != null) {
            return Optional.of(systemProperty);
        }

        return Optional.ofNullable(userProperties)
            .map(prop -> prop.getProperty(key))
            .or(() -> Optional.ofNullable(defaultProperties.getProperty(key)));
    }

    public int getInt(String key) {
        String value = getString(key);
        return Integer.parseInt(value);
    }

    public int getPowerOfTwoMinusOne(String key) {
        int value = getInt(key);
        return ceilingPowerOfTwo(value) - 1;
    }

    public double getDouble(String key) {
        String value = getString(key);
        return Double.parseDouble(value);
    }

    public Double getDoubleOrNull(String key) {
        String value = getString(key);
        return (value == null || value.isEmpty()) ? null : Double.valueOf(value);
    }

    public boolean getBoolean(String key) {
        String value = getString(key);
        return Boolean.parseBoolean(value);
    }

    public Optional<Boolean> getBooleanOptional(String key) {
        return getStringOptional(key)
            .map(Boolean::parseBoolean);
    }

    public Path getPath(String key) {
        String value = getString(key);
        return Paths.get(value);
    }


    private Properties createDefaultProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("default.properties")) {
            properties.load(is);
        }
        return properties;
    }

    @Nullable
    private Properties createUserPropertiesOrNull(@Nullable Path configFile) throws IOException {
        if (configFile == null) {
            return null;
        } else if (!Files.exists(configFile)) {
            System.err.println("Skipping config file '" + configFile + "' as it does not exist");
            return null;
        }

        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(configFile)) {
            properties.load(is);
        }
        return properties;
    }
}
