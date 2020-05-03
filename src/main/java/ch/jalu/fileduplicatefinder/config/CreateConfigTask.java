package ch.jalu.fileduplicatefinder.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class CreateConfigTask {

    public static final String ID = "createConfig";

    public void run() {
        try {
            Path newConfigFile = new ConfigFileWriter().writeConfigFile("default.properties", "");
            System.out.println("Created configuration file '" + newConfigFile + "'");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
