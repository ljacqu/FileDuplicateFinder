package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.properties.types.PropertyType;

import javax.annotation.Nullable;

/**
 * Property type that is by all Jalu file utils (JFU) properties to define the conversion from String to a property's
 * type and vice versa.
 *
 * @param <T> the property type
 */
public interface JfuPropertyType<T> extends PropertyType<T> {

    @Nullable
    @Override
    default T convert(@Nullable Object object, ConvertErrorRecorder errorRecorder) {
        if (object instanceof String) {
            return fromString((String) object, errorRecorder);
        }
        return null;
    }

    /**
     * Converts the String to the property's value, if applicable. Returns null otherwise and sets an error
     * to the given error recorder.
     * <p>
     * The error set to the error recorder is passed on to the user. An error <b>must</b> be set if this method
     * returns null.
     *
     * @param value the value to convert
     * @param errorRecorder error recorder to add an error to if conversion is not possible (or if the value was not
     *                      "fully" correct)
     * @return converted value, or null
     */
    @Nullable
    T fromString(String value, ConvertErrorRecorder errorRecorder);

}
