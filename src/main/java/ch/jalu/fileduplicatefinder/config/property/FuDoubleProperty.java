package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.TypeBasedProperty;
import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.properties.types.PropertyType;
import com.google.common.primitives.Doubles;

import javax.annotation.Nullable;

/**
 * Custom double property impl. for file utils (FU).
 */
public class FuDoubleProperty extends TypeBasedProperty<Double> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public FuDoubleProperty(String path, double defaultValue) {
        super(path, defaultValue, new DoublePropertyType());
    }

    private static final class DoublePropertyType implements PropertyType<Double> {

        @Nullable
        @Override
        public Double convert(@Nullable Object object, ConvertErrorRecorder errorRecorder) {
            if (object instanceof String) {
                return Doubles.tryParse((String) object);
            }
            return null;
        }

        @Override
        public Object toExportValue(Double value) {
            return value;
        }
    }
}
