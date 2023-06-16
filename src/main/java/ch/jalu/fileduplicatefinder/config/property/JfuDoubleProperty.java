package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import com.google.common.primitives.Doubles;

import javax.annotation.Nullable;

/**
 * Custom double property impl. for Jalu file utils (JFU).
 */
public class JfuDoubleProperty extends JfuProperty<Double> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public JfuDoubleProperty(String path, double defaultValue) {
        super(path, defaultValue, new DoublePropertyType());
    }

    private static final class DoublePropertyType implements JfuPropertyType<Double> {

        @Nullable
        @Override
        public Double fromString(String value, ConvertErrorRecorder errorRecorder) {
            Double parsed = Doubles.tryParse(value);
            if (parsed == null) {
                errorRecorder.setHasError("Invalid input; please provide a number (e.g. 3 or 3.14)");
            }
            return parsed;
        }

        @Override
        public Object toExportValue(Double value) {
            return value;
        }
    }
}
