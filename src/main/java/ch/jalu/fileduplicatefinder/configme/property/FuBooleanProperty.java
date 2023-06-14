package ch.jalu.fileduplicatefinder.configme.property;

import ch.jalu.configme.properties.TypeBasedProperty;
import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.properties.types.PropertyType;

import javax.annotation.Nullable;

public class FuBooleanProperty extends TypeBasedProperty<Boolean> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public FuBooleanProperty(String path, Boolean defaultValue) {
        super(path, defaultValue, new BooleanPropertyType());
    }

    private static final class BooleanPropertyType implements PropertyType<Boolean> {

        @Nullable
        @Override
        public Boolean convert(@Nullable Object object, ConvertErrorRecorder errorRecorder) {
            if (object instanceof String) {
                return Boolean.parseBoolean((String) object);
            } else if (object instanceof Boolean) {
                return (Boolean) object;
            }
            return null;
        }

        @Override
        public Object toExportValue(Boolean value) {
            return value;
        }
    }
}
