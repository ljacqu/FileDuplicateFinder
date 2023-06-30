package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;

import javax.annotation.Nullable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Property for regex patterns.
 */
public class JfuRegexProperty extends JfuProperty<Pattern> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public JfuRegexProperty(String path, Pattern defaultValue) {
        super(path, defaultValue, new RegexPropertyType());
    }

    private static final class RegexPropertyType implements JfuPropertyType<Pattern> {

        @Nullable
        @Override
        public Pattern fromString(String value, ConvertErrorRecorder errorRecorder) {
            try {
                return Pattern.compile(value);
            } catch (PatternSyntaxException e) {
                errorRecorder.setHasError("Invalid syntax: [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
            }
            return null;
        }

        @Override
        public Object toExportValue(Pattern value) {
            return value.pattern();
        }
    }
}
