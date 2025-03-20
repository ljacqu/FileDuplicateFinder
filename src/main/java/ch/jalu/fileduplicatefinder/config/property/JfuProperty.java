package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.TypeBasedProperty;
import ch.jalu.fileduplicatefinder.config.JfuErrorRecorder;
import ch.jalu.fileduplicatefinder.config.ValueOrError;

/**
 * Parent property type of all properties used in the Jalu file utils (JFU).
 * Unlike the standard property implementations by ConfigMe, this property type converts solely from String to its
 * type; since this project uses a .properties file, all values are read as strings. Furthermore, the conversion from
 * String to the type is exposed so that it can also be used for additional property sources (command line values or
 * values provided by the user via Scanner).
 *
 * @param <T> the property type
 */
public abstract class JfuProperty<T> extends TypeBasedProperty<T> {

    private final JfuPropertyType<T> propertyType;

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     * @param type the property type
     */
    public JfuProperty(String path, T defaultValue, JfuPropertyType<T> type) {
        super(path, defaultValue, type);
        this.propertyType = type;
    }

    /**
     * Converts the string input to a value according to the property's type, or returns an error if not possible.
     *
     * @param value the string value to convert
     * @return object with the converted value, or an error
     */
    public ValueOrError<T> fromString(String value) {
        JfuErrorRecorder errorRecorder = new JfuErrorRecorder();
        T converted = propertyType.fromString(value, errorRecorder);
        return converted == null
            ? ValueOrError.forError(errorRecorder.getErrorReason())
            : ValueOrError.forValue(converted);
    }

    @Override
    public JfuPropertyType<T> getType() {
        return propertyType;
    }
}
