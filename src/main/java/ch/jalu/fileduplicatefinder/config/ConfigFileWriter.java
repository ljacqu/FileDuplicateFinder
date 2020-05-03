package ch.jalu.fileduplicatefinder.config;

import ch.jalu.fileduplicatefinder.FileUtilsRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Copies the properties file from the JAR to the file system in which the JAR is being executed.
 */
public class ConfigFileWriter {

    /**
     * Writes the config file to "config.properties" in the current executing directory. Throws an
     * exception if it is not possible.
     *
     * @param propertiesFile file within the jar to copy from
     * @param folder the folder to write in
     * @return the file that was created
     * @throws IOException upon error
     */
    public Path writeConfigFile(String propertiesFile, String folder) throws IOException {
        Path config = Paths.get(folder, "config.properties");
        if (Files.exists(config)) {
            throw new IOException("Cannot create '" + config + "' as it already exists");
        }

        try (InputStream is = FileUtilsRunner.class.getClassLoader().getResourceAsStream(propertiesFile)) {
            Files.copy(is, config);
        }
        return config;
    }
}
