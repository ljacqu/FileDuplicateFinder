package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.properties.convertresult.PropertyValue;
import ch.jalu.configme.resource.PropertyReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;

public class JfuOptionalProperty<T> extends JfuProperty<Optional<T>> {

    private final Property<T> baseProperty;

    public JfuOptionalProperty(JfuProperty<T> baseProperty) {
        super(baseProperty.getPath(), Optional.of(baseProperty.getDefaultValue()), new JfuOptionalType<>(baseProperty));
        this.baseProperty = baseProperty;
    }

    public static JfuOptionalProperty<Path> newOptionalDirectoryProperty(String path) {
        return new JfuOptionalProperty<>(new JfuDirectoryProperty(path, Paths.get(".")));
    }

    public static JfuOptionalProperty<String> newOptionalStringProperty(String path) {
        return new JfuOptionalProperty<>(new JfuStringProperty(path, ""));
    }

    public static JfuOptionalProperty<Pattern> newOptionalRegexProperty(String path) {
        return new JfuOptionalProperty<>(new JfuRegexProperty(path, Pattern.compile("")));
    }

    /**
     * Copied from {@link ch.jalu.configme.properties.OptionalProperty#determineValue}.
     */
    @Override
    public PropertyValue<Optional<T>> determineValue(PropertyReader reader) {
        PropertyValue<T> basePropertyValue = baseProperty.determineValue(reader);
        Optional<T> value = basePropertyValue.isValidInResource()
            ? Optional.ofNullable(basePropertyValue.getValue())
            : Optional.empty();

        // Propagate the false "valid" property if the reader has a value at the base property's path
        // and the base property says it's invalid -> triggers a rewrite to get rid of the invalid value.
        boolean isWrongInResource = !basePropertyValue.isValidInResource() && reader.contains(baseProperty.getPath());
        return isWrongInResource
            ? PropertyValue.withValueRequiringRewrite(value)
            : PropertyValue.withValidValue(value);
    }

    @Override
    public boolean isValidValue(Optional<T> value) {
        return super.isValidValue(value) && value.map(baseProperty::isValidValue).orElse(true);
    }

    private static final class JfuOptionalType<T> implements JfuPropertyType<Optional<T>> {

        private final JfuPropertyType<T> baseType;

        JfuOptionalType(JfuProperty<T> baseType) {
            this.baseType = baseType.getType();
        }

        @Override
        public Optional<T> fromString(String value, ConvertErrorRecorder errorRecorder) {
            return value.isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(baseType.fromString(value, errorRecorder));
        }

        @Override
        public Object toExportValue(Optional<T> value) {
            return value.map(baseType::toExportValue).orElse(null);
        }
    }
}
