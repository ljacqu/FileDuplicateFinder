package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JfuDirectoryProperty extends JfuProperty<Path> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param defaultValue the default value of the property
     */
    public JfuDirectoryProperty(String path, Path defaultValue) {
        super(path, defaultValue, new JfuDirectoryPropertyType());
    }

    private static final class JfuDirectoryPropertyType implements JfuPropertyType<Path> {

        @Override
        public @Nullable Path fromString(String value, ConvertErrorRecorder errorRecorder) {
            Path path = Paths.get(value);
            if (Files.isDirectory(path)) {
                return path;
            }

            errorRecorder.setHasError("Path '" + value + "' is not a directory");
            return null;
        }

        @Override
        public Object toExportValue(Path value) {
            return value.toAbsolutePath().normalize().toString();
        }
    }
}
