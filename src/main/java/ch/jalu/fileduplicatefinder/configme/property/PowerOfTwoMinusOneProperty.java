package ch.jalu.fileduplicatefinder.configme.property;

import ch.jalu.configme.properties.BaseProperty;
import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.resource.PropertyReader;

import javax.annotation.Nullable;

import static com.google.common.math.IntMath.ceilingPowerOfTwo;

/**
 * Property for integers that are a power of two minus one (e.g. 63, 127, 255, 511).
 */
public class PowerOfTwoMinusOneProperty extends BaseProperty<Integer> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public PowerOfTwoMinusOneProperty(String path, Integer defaultValue) {
        super(path, toPowerOfTwoMinusOne(defaultValue));
    }

    @Nullable
    @Override
    protected Integer getFromReader(PropertyReader reader, ConvertErrorRecorder errorRecorder) {
        Integer value = reader.getInt(getPath());
        if (value != null) {
            int powerTwoMinusOne = toPowerOfTwoMinusOne(value);
            if (value != powerTwoMinusOne) {
                errorRecorder.setHasError(value + " was not a power of two minus one");
            }
            return powerTwoMinusOne;
        }
        return null;
    }

    @Override
    public Object toExportValue(Integer value) {
        return value.toString();
    }

    @Override
    public boolean isValidValue(Integer value) {
        return value != null && value == toPowerOfTwoMinusOne(value);
    }

    public static int toPowerOfTwoMinusOne(int value) {
        return ceilingPowerOfTwo(value) - 1;
    }
}
