package ch.jalu.fileduplicatefinder.config;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.resource.PropertyReader;
import ch.jalu.configme.resource.PropertyResource;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * Property resource implementation for {@code .properties} files. Note that certain features are not supported
 * by the {@link PropertyFileReader resource reader}.
 */
public class PropertyFileResource implements PropertyResource {

    private final Path configFile;

    PropertyFileResource(Path configFile) {
        this.configFile = configFile;
    }

    @Override
    public PropertyReader createReader() {
        return new PropertyFileReader(configFile);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote
     *   Comments on intermediate paths (like {@code com.acme} for a property {@code com.acme.filter}) is not
     *   picked up by this resource implementation. Only comments on properties and their top-level parents
     *   (like {@code com}) will be considered by this class when writing.
     *
     * @param configurationData the configuration data to export
     */
    @Override
    public void exportProperties(ConfigurationData configurationData) {
        try (OutputStream os = Files.newOutputStream(configFile);
             OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writeComments(writer, "", configurationData, true);
            String lastParent = "";

            for (Property<?> property : configurationData.getProperties()) {
                String export = getExportValue(property, configurationData);
                if (export == null) {
                    continue;
                }

                String parentElem = extractParentElement(property.getPath());

                if (!Objects.equals(lastParent, parentElem) && !property.getPath().equals(parentElem)) {
                    if (!lastParent.isEmpty()) {
                        writer.append('\n');
                    }
                    writeComments(writer, parentElem, configurationData, true);
                }

                writeComments(writer, property.getPath(), configurationData, false);
                writer.append('\n').append(property.getPath()).append("=").append(export);
                lastParent = parentElem;
            }

            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write to config file", e);
        }
    }

    private static void writeComments(OutputStreamWriter writer, String path, ConfigurationData configurationData,
                                      boolean newLineAfterComments) throws IOException {
        boolean wroteText = false;
        for (String comment : configurationData.getCommentsForSection(path)) {
            if (comment.isEmpty()) {
                writer.append('\n');
            } else {
                writer.append("\n# ").append(comment);
                wroteText = true;
            }
        }
        if (newLineAfterComments && wroteText) {
            writer.append('\n');
        }
    }

    @Nullable
    private <T> String getExportValue(Property<T> property, ConfigurationData configurationData) {
        Object exportValue = property.toExportValue(configurationData.getValue(property));
        if (exportValue == null) {
            return null;
        }

        return exportValue.toString()
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private String extractParentElement(String path) {
        int dotIndex = path.indexOf('.');
        if (dotIndex > 0) {
            return path.substring(0, dotIndex);
        }
        return path;
    }

    public static final class PropertyFileReader implements PropertyReader {

        private final Properties properties;

        public PropertyFileReader(Path configFile) {
            Properties properties = new Properties();
            try (InputStream is = Files.newInputStream(configFile)) {
                properties.load(is);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            this.properties = properties;
        }

        @Override
        public boolean contains(String path) {
            // Note that intermediate paths will return false, unlike the behavior in the YAML resource reader
            return properties.containsKey(path);
        }

        @Nullable
        @Override
        public Object getObject(String path) {
            return getString(path);
        }

        @Nullable
        @Override
        public String getString(String path) {
            return properties.getProperty(path);
        }

        @Nullable
        @Override
        public Integer getInt(String path) {
            String str = getString(path);
            return str == null ? null : Ints.tryParse(str);
        }

        @Nullable
        @Override
        public Double getDouble(String path) {
            String str = getString(path);
            return str == null ? null : Doubles.tryParse(str);
        }

        @Nullable
        @Override
        public Boolean getBoolean(String path) {
            String str = getString(path);
            return ("true".equals(str) || "false".equals(str))
                ? Boolean.valueOf(str)
                : null;
        }

        @Nullable
        @Override
        public List<?> getList(String path) {
            throw new UnsupportedOperationException("Not supported by .properties");
        }

        @Override
        public Set<String> getKeys(boolean onlyLeafNodes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getChildKeys(String path) {
            throw new UnsupportedOperationException();
        }
    }
}
