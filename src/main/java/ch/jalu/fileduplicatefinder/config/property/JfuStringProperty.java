package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;

/**
 * Text property.
 */
public class JfuStringProperty extends JfuProperty<String> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public JfuStringProperty(String path, String defaultValue) {
        super(path, defaultValue, new StringPropertyType());
    }

    private static final class StringPropertyType implements JfuPropertyType<String> {

        @Override
        public String fromString(String value, ConvertErrorRecorder errorRecorder) {
            return value;
        }

        @Override
        public Object toExportValue(String value) {
            return value;
        }
    }
}
