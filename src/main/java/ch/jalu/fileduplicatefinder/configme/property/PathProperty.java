package ch.jalu.fileduplicatefinder.configme.property;

import ch.jalu.configme.properties.BaseProperty;
import ch.jalu.configme.properties.OptionalProperty;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.resource.PropertyReader;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class PathProperty extends BaseProperty<Path> {

    /**
     * Constructor.
     *
     * @param path the path of the property
     */
    public PathProperty(String path) {
        super(path, Paths.get(""));
    }

    public static Property<Optional<Path>> newOptionalPathProperty(String path) {
        return new OptionalProperty<>(new PathProperty(path));
    }

    @Nullable
    @Override
    protected Path getFromReader(PropertyReader reader, ConvertErrorRecorder errorRecorder) {
        String path = reader.getString(getPath());
        if (path != null) {
            return Paths.get(path);
        }
        return null;
    }

    @Override
    public Object toExportValue(Path value) {
        return value.toString();
    }
}
