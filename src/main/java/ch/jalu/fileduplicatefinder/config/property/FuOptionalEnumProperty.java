package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.EnumProperty;
import ch.jalu.configme.properties.OptionalProperty;

/**
 * Optional property of an enum type. Custom implementation to be able to use the property's enum type for
 * more functionality.
 *
 * @param <E> the enum type
 */
public class FuOptionalEnumProperty<E extends Enum<E>> extends OptionalProperty<E> {

    private final Class<E> enumType;

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param type the enum type
     */
    public FuOptionalEnumProperty(String path, Class<E> type) {
        super(new EnumProperty<>(type, path, type.getEnumConstants()[0]));
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
