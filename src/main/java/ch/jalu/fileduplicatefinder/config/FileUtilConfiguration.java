package ch.jalu.fileduplicatefinder.config;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.utils.Utils;
import ch.jalu.fileduplicatefinder.config.property.JfuOptionalProperty;
import ch.jalu.fileduplicatefinder.config.property.JfuProperty;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Provides values for all configurations.
 */
public class FileUtilConfiguration {

    private final SettingsManager settingsManager;
    private final ScannerPropertySource scannerPropertySource;

    /**
     * Constructor.
     *
     * @param scanner scanner instance to get user input when needed
     * @param userPropertyFile custom path to the configuration file (nullable); a default name is used if null
     */
    public FileUtilConfiguration(Scanner scanner, @Nullable Path userPropertyFile) {
        Path configFile = Objects.requireNonNullElseGet(userPropertyFile,
            () -> Paths.get("./file-utils.properties"));
        this.settingsManager = createSettingsManager(configFile);
        this.scannerPropertySource = new ScannerPropertySource(scanner);
    }

    public <T> T getValue(JfuProperty<T> property) {
        return getValue(property, false);
    }

    public <T> T getValue(JfuProperty<T> property, boolean forcePrompt) {
        return getValue0(property, forcePrompt, Function.identity());
    }

    public <T> T getValueOrPrompt(JfuOptionalProperty<T> property) {
        return getValueOrPrompt(property, false);
    }

    public <T> T getValueOrPrompt(JfuOptionalProperty<T> property, boolean forcePrompt) {
        return getValue0(property, forcePrompt, opt -> opt.orElse(null));
    }

    public <T> void setValue(JfuProperty<T> property, T value) {
        settingsManager.setProperty(property, value);
    }

    public void save() {
        settingsManager.save();
    }

    private <T, R> R getValue0(JfuProperty<T> property, boolean forcePrompt, Function<T, R> resultTransformer) {
        // 1. Get existing value and output in case there is any error (this informs the user in case it was weird in
        //    the properties file or on the command line)
        ValueOrError<T> valueOrError = fromOverridingSourceOrSettingsManager(property);
        if (valueOrError.getErrorReason() != null) {
            System.err.println("Configured value for '" + property.getPath() + "' is invalid: "
                + valueOrError.getErrorReason());
        }

        // 2. If there is a value, return it if we don't need to force the user to respecify the value.
        R oldValue = valueOrError.getValue() == null ? null : resultTransformer.apply(valueOrError.getValue());
        if (!forcePrompt && oldValue != null) {
            return oldValue;
        }

        // 3. Inform the user what he has to input
        String prevValue = oldValue == null
            ? ""
            : " (current value: \"" + property.toExportValue(valueOrError.getValue()) + "\")";
        System.out.println("Please enter a value for '" + property.getPath() + "'" + prevValue + ":");

        // 4. Get input and validate (repeating the process until we have a valid input)
        while (true) {
            String strValue = scannerPropertySource.promptStringAndRegister(property.getPath());
            valueOrError = property.fromString(strValue);

            if (valueOrError.getErrorReason() != null) {
                System.err.println(valueOrError.getErrorReason());
            } else {
                R result = resultTransformer.apply(valueOrError.getValue());
                if (result == null) {
                    System.err.println("Please provide a value");
                } else {
                    return result;
                }
            }

            System.out.println("Please enter a value for '" + property.getPath() + "':");
        }
    }

    private <T> ValueOrError<T> fromOverridingSourceOrSettingsManager(JfuProperty<T> property) {
        String overridingValue = getValueFromOverridingSources(property.getPath());
        if (overridingValue != null) {
            return property.fromString(overridingValue);
        }
        return ValueOrError.forValue(settingsManager.getProperty(property));
    }

    @Nullable
    private String getValueFromOverridingSources(String path) {
        String value = scannerPropertySource.getValue(path);
        if (value != null) {
            return value;
        }

        return System.getProperty(path);
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
