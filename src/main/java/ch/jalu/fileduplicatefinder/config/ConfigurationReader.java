package ch.jalu.fileduplicatefinder.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class ConfigurationReader {

    private String rootFolder;
    private String hashAlgorithm;
    private double maxSizeForHashingInMb;

    private String filterWhitelist;
    private String filterBlacklist;
    private Double filterMinSizeInMb;
    private Double filterMaxSizeInMb;


    public ConfigurationReader(Path propertyFile) {
        initialize(propertyFile);
    }

    private void initialize(Path propertyFile) {
        Properties defaultProperties;
        Properties userProperties;
        try {
            defaultProperties = createDefaultProperties();
            userProperties = createUserPropertiesOrNull(propertyFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        rootFolder = getProperty("rootFolder", userProperties, defaultProperties);
        hashAlgorithm = getProperty("hashAlgorithm", userProperties, defaultProperties);
        maxSizeForHashingInMb = Double.valueOf(getProperty("maxSizeForHashingInMb", userProperties, defaultProperties));
        filterWhitelist = getProperty("filter.whitelist", userProperties, defaultProperties);
        filterBlacklist = getProperty("filter.blacklist", userProperties, defaultProperties);
        filterMinSizeInMb = toDoubleNullSafe(getProperty("filter.fileSizeMinInMb", userProperties, defaultProperties));
        filterMaxSizeInMb = toDoubleNullSafe(getProperty("filter.fileSizeMaxInMb", userProperties, defaultProperties));
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public double getMaxSizeForHashingInMb() {
        return maxSizeForHashingInMb;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public String getFilterWhitelist() {
        return filterWhitelist;
    }

    public String getFilterBlacklist() {
        return filterBlacklist;
    }

    public Double getFilterMinSizeInMb() {
        return filterMinSizeInMb;
    }

    public Double getFilterMaxSizeInMb() {
        return filterMaxSizeInMb;
    }

    private static String getProperty(String key, Properties userProperties, Properties defaultProperties) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null) {
            return systemProperty;
        }

        return Optional.ofNullable(userProperties)
            .map(prop -> prop.getProperty(key))
            .orElseGet(() -> defaultProperties.getProperty(key));
    }

    private Properties createDefaultProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("default.properties")) {
            properties.load(is);
        }
        return properties;
    }

    private Properties createUserPropertiesOrNull(Path configFile) throws IOException {
        if (configFile == null) {
            return null;
        } else if (!Files.exists(configFile)) {
            System.out.println("Skipping config file '" + configFile + "' as it does not exist");
            return null;
        }

        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(configFile)) {
            properties.load(is);
        }
        return properties;
    }

    private static Double toDoubleNullSafe(String str) {
        return str != null && !str.isEmpty() ? Double.valueOf(str) : null;
    }
}
