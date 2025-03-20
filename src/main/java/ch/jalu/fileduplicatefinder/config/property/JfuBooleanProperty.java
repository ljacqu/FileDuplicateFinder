package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import org.jetbrains.annotations.Nullable;

/**
 * Custom boolean property impl. for Jalu file utils (JFU).
 */
public class JfuBooleanProperty extends JfuProperty<Boolean> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public JfuBooleanProperty(String path, boolean defaultValue) {
        super(path, defaultValue, new BooleanPropertyType());
    }

    private static final class BooleanPropertyType implements JfuPropertyType<Boolean> {

        @Nullable
        @Override
        public Boolean fromString(String value, ConvertErrorRecorder errorRecorder) {
            if ("true".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value)) {
                return true;
            } else if ("false".equalsIgnoreCase(value) || "n".equalsIgnoreCase(value)) {
                return false;
            }
            errorRecorder.setHasError("Please type 'true', 'false' (or 'y', 'n')");
            return null;
        }

        @Override
        public Object toExportValue(Boolean value) {
            return value;
        }
    }
}
