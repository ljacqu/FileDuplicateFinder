package ch.jalu.fileduplicatefinder.config;

import java.util.Optional;
import java.util.Properties;

class PropertyResolver {

    private final Properties defaultProperties;
    private final Properties userProperties;

    PropertyResolver(Properties defaultProperties, Properties userProperties) {
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
