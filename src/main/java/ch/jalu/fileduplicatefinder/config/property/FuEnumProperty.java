package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.TypeBasedProperty;
import ch.jalu.configme.properties.types.EnumPropertyType;

/**
 * Property for an enum value. Custom implementation for file utils (FU) to be able to use the property's enum type for
 * more functionality.
 *
 * @param <E> the enum type
 */
public class FuEnumProperty<E extends Enum<E>> extends TypeBasedProperty<E> {

    private final Class<E> enumType;

    /**
     * Constructor.
     *
     * @param type the enum type
     * @param path the path of the property
     * @param defaultValue the default value
     */
    public FuEnumProperty(Class<E> type, String path, E defaultValue) {
        super(path, defaultValue, EnumPropertyType.of(type));
        this.enumType = type;
    }

    public E toEnumEntry(String str) {
        for (E enumConstant : enumType.getEnumConstants()) {
            if (str.equalsIgnoreCase(enumConstant.name())) {
                return enumConstant;
            }
        }
        throw new IllegalArgumentException("Unknown entry '" + str + "' for enum " + enumType.getSimpleName());
    }
}