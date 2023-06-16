package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

public class JfuEnumProperty<E extends Enum<E>> extends JfuProperty<E> {

    /**
     * Constructor.
     *
     * @param enumType the enum type
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public JfuEnumProperty(Class<E> enumType, String path, E defaultValue) {
        super(path, defaultValue, new EnumPropertyType<>(enumType));
    }

    private static final class EnumPropertyType<E extends Enum<E>> implements JfuPropertyType<E> {

        private final Class<E> enumType;

        EnumPropertyType(Class<E> enumType) {
            this.enumType = enumType;
        }

        @Nullable
        @Override
        public E fromString(String value, ConvertErrorRecorder errorRecorder) {
            E parsed = toEnumEntry(value);
            if (parsed == null) {
                errorRecorder.setHasError("Invalid entry. Possible values: "
                    + Arrays.stream(enumType.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", ")));
            }
            return parsed;
        }

        @Override
        public Object toExportValue(E value) {
            return value;
        }

        private E toEnumEntry(String name) {
            for (E entry : enumType.getEnumConstants()) {
                if (entry.name().equalsIgnoreCase(name)) {
                    return entry;
                }
            }
            return null;
        }
    }
}
