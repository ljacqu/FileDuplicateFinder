package ch.jalu.fileduplicatefinder.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.math.IntMath.ceilingPowerOfTwo;

public class FileDupeFinderConfiguration {

    private String rootFolder;
    private String hashAlgorithm;
    private double maxSizeForHashingInMb;
    private int progressFilesFoundInterval;
    private int progressFilesHashedInterval;
    private boolean outputDistribution;

    private String filterWhitelist;
    private String filterBlacklist;
    private Double filterMinSizeInMb;
    private Double filterMaxSizeInMb;


    public FileDupeFinderConfiguration(Path propertyFile) {
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

        PropertiesResolver resolver = new PropertiesResolver(defaultProperties, userProperties);

        rootFolder = resolver.getString("rootFolder");
        hashAlgorithm = resolver.getString("hashAlgorithm");
        maxSizeForHashingInMb = resolver.getDouble("maxSizeForHashingInMb");
        filterWhitelist = resolver.getString("filter.whitelist");
        filterBlacklist = resolver.getString("filter.blacklist");
        filterMinSizeInMb = resolver.getDoubleOrNull("filter.fileSizeMinInMb");
        filterMaxSizeInMb = resolver.getDoubleOrNull("filter.fileSizeMaxInMb");
        progressFilesFoundInterval = ceilingPowerOfTwo(resolver.getInt("progress.filesFoundInterval")) - 1;
        progressFilesHashedInterval = ceilingPowerOfTwo(resolver.getInt("progress.filesHashedInterval")) - 1;
        outputDistribution = resolver.getBoolean("outputDistribution");
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

    public int getProgressFilesFoundInterval() {
        return progressFilesFoundInterval;
    }

    public int getProgressFilesHashedInterval() {
        return progressFilesHashedInterval;
    }

    public boolean isDistributionOutputEnabled() {
        return outputDistribution;
    }


    private static final class PropertiesResolver {

        private Properties defaultProperties;
        private Properties userProperties;

        PropertiesResolver(Properties defaultProperties, Properties userProperties) {
            this.defaultProperties = defaultProperties;
            this.userProperties = userProperties;
        }

        String getString(String key) {
            String systemProperty = System.getProperty(key);
            if (systemProperty != null) {
                return systemProperty;
            }

            return Optional.ofNullable(userProperties)
                .map(prop -> prop.getProperty(key))
                .orElseGet(() -> defaultProperties.getProperty(key));
        }

        int getInt(String key) {
            String value = getString(key);
            return Integer.parseInt(value);
        }

        double getDouble(String key) {
            String value = getString(key);
            return Double.parseDouble(value);
        }

        Double getDoubleOrNull(String key) {
            String value = getString(key);
            return (value == null || value.isEmpty()) ? null : Double.valueOf(value);
        }

        boolean getBoolean(String key) {
            String value = getString(key);
            return Boolean.parseBoolean(value);
        }
    }
}
