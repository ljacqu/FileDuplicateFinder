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

        rootFolder = Optional.ofNullable(userProperties).map(prop -> prop.getProperty("rootfolder"))
            .orElseGet(() -> defaultProperties.getProperty("rootfolder"));
        hashAlgorithm = Optional.ofNullable(userProperties).map(prop -> prop.getProperty("hashalgorithm"))
            .orElseGet(() -> defaultProperties.getProperty("hashalgorithm"));
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    private Properties createDefaultProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("default.properties")) {
            properties.load(is);
        }
        return properties;
    }

    private Properties createUserPropertiesOrNull(Path configFile) throws IOException {
        if (configFile == null || !Files.exists(configFile)) {
            return null;
        }

        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(configFile)) {
            properties.load(is);
        }
        return properties;
    }
}
