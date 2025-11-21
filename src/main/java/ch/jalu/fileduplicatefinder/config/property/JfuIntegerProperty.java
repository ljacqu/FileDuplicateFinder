package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import com.google.common.primitives.Ints;
import org.jetbrains.annotations.Nullable;

/**
 * Custom integer property impl. for Jalu file utils (JFU).
 */
public class JfuIntegerProperty extends JfuProperty<Integer> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public JfuIntegerProperty(String path, int defaultValue) {
        super(path, defaultValue, new IntegerPropertyType());
    }

    static final class IntegerPropertyType implements JfuPropertyType<Integer> {

        @Override
        public @Nullable Integer fromString(String value, ConvertErrorRecorder errorRecorder) {
            Integer parsed = Ints.tryParse(value);
            if (parsed == null) {
                errorRecorder.setHasError("Invalid input; please provide an integer (e.g. 3 or 20)");
            }
            return parsed;
        }

        @Override
        public Object toExportValue(Integer value) {
            return value;
        }
    }
}