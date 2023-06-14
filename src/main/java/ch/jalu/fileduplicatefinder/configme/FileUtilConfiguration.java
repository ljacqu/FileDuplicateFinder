package ch.jalu.fileduplicatefinder.configme;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.RegexProperty;
import ch.jalu.configme.properties.StringProperty;
import ch.jalu.configme.utils.Utils;
import ch.jalu.fileduplicatefinder.configme.property.FuBooleanProperty;
import ch.jalu.fileduplicatefinder.configme.property.FuDoubleProperty;
import ch.jalu.fileduplicatefinder.configme.property.FuIntegerProperty;
import ch.jalu.fileduplicatefinder.configme.property.PowerOfTwoMinusOneProperty;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;

public class FileUtilConfiguration {

    private final SettingsManager settingsManager;
    private final ScannerPropertySource scannerPropertySource;

    public FileUtilConfiguration(Scanner scanner, @Nullable Path userPropertyFile) {
        Path configFile = Objects.requireNonNullElseGet(userPropertyFile,
            () -> Paths.get("./file-utils-config.properties"));
        this.settingsManager = createSettingsManager(configFile);
        this.scannerPropertySource = new ScannerPropertySource(scanner);
    }

    public String getString(StringProperty property) {
        return fromOverridingSourceOrSettingsManager(property, Function.identity());
    }

    public Pattern getPattern(RegexProperty property) {
        return fromOverridingSourceOrSettingsManager(property, Pattern::compile);
    }

    public boolean getBoolean(FuBooleanProperty property) {
        return fromOverridingSourceOrSettingsManager(property, Boolean::parseBoolean);
    }

    public double getDouble(FuDoubleProperty property) {
        return fromOverridingSourceOrSettingsManager(property, Double::parseDouble);
    }

    public int getInt(FuIntegerProperty property) {
        return fromOverridingSourceOrSettingsManager(property, Integer::parseInt);
    }

    public int getPowerOfTwoMinusOne(PowerOfTwoMinusOneProperty property) {
        return fromOverridingSourceOrSettingsManager(property,
            str -> PowerOfTwoMinusOneProperty.toPowerOfTwoMinusOne(Integer.parseInt(str)));
    }

    public String getStringOrPrompt(Property<Optional<String>> property) {
        return fromOverridingSourceOrSettingsManager(property, Optional::of)
            .orElseGet(() -> scannerPropertySource.promptForString(property.getPath()));
    }

    public Path getPathOrPrompt(Property<Optional<Path>> property) {
        return fromOverridingSourceOrSettingsManager(property, str -> Optional.of(Paths.get(str)))
            .orElseGet(() -> Paths.get(scannerPropertySource.promptForString(property.getPath())));
    }

    @Nullable
    private String getValueFromOverridingSources(String path) {
        String value = scannerPropertySource.getValue(path);
        if (value != null) {
            return value;
        }

        return System.getProperty(path);
    }

    private <T> T fromOverridingSourceOrSettingsManager(Property<T> property,
                                                        Function<String, T> overridingValueConverter) {
        String overridingValue = getValueFromOverridingSources(property.getPath());
        if (overridingValue != null) {
            return overridingValueConverter.apply(overridingValue);
        }
        return settingsManager.getProperty(property);
    }

    private static SettingsManager createSettingsManager(Path configFile) {
        Utils.createFileIfNotExists(configFile);
        return SettingsManagerBuilder
            .withResource(new PropertyFileResource(configFile))
            .configurationData(FileUtilSettings.class)
            .useDefaultMigrationService()
            .create();
    }
}
