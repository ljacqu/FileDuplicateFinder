package ch.jalu.fileduplicatefinder.configme.property;

import ch.jalu.configme.properties.TypeBasedProperty;
import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.properties.types.PropertyType;
import com.google.common.primitives.Ints;

import javax.annotation.Nullable;

/**
 * Custom integer property impl. for file utils (FU).
 */
public class FuIntegerProperty extends TypeBasedProperty<Integer> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public FuIntegerProperty(String path, int defaultValue) {
        super(path, defaultValue, new IntegerPropertyType());
    }

    private static final class IntegerPropertyType implements PropertyType<Integer> {

        @Nullable
        @Override
        public Integer convert(@Nullable Object object, ConvertErrorRecorder errorRecorder) {
            if (object instanceof String) {
                return Ints.tryParse((String) object);
            }
            return null;
        }

        @Override
        public Object toExportValue(Integer value) {
            return value;
        }
    }
}