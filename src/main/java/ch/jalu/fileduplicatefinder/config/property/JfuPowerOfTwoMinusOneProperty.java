package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import org.jetbrains.annotations.Nullable;

import static com.google.common.math.IntMath.ceilingPowerOfTwo;

/**
 * Property for integers that are a power of two minus one (e.g. 63, 127, 255, 511).
 */
public class JfuPowerOfTwoMinusOneProperty extends JfuProperty<Integer> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public JfuPowerOfTwoMinusOneProperty(String path, Integer defaultValue) {
        super(path, toPowerOfTwoMinusOne(defaultValue), new PowerOfTwoMinusOneType());
    }

    @Override
    public boolean isValidValue(Integer value) {
        return value != null && value == toPowerOfTwoMinusOne(value);
    }

    public static int toPowerOfTwoMinusOne(int value) {
        return ceilingPowerOfTwo(value) - 1;
    }

    private static final class PowerOfTwoMinusOneType implements JfuPropertyType<Integer> {

        private final JfuIntegerProperty.IntegerPropertyType intType = new JfuIntegerProperty.IntegerPropertyType();

        @Override
        public @Nullable Integer fromString(String value, ConvertErrorRecorder errorRecorder) {
            Integer intValue = intType.fromString(value, errorRecorder);
            if (intValue == null) {
                return null;
            }

            int powerTwoMinusOne = toPowerOfTwoMinusOne(intValue);
            if (powerTwoMinusOne != intValue) {
                errorRecorder.setHasError(intValue + " is not a power of two minus one. (Valid examples: 63, 127)");
            }
            return powerTwoMinusOne;
        }

        @Override
        public Object toExportValue(Integer value) {
            return value;
        }
    }
}
