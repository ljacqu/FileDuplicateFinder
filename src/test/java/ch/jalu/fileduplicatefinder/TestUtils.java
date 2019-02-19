package ch.jalu.fileduplicatefinder;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class TestUtils {

    private TestUtils() {
    }

    public static Path getTestSamplesFolder() {
        try {
            return Paths.get(TestUtils.class.getClassLoader().getResource("files").toURI());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static Path getConfigSampleFile() {
        try {
            return Paths.get(TestUtils.class.getClassLoader().getResource("configuration/test-config.properties")
                .toURI());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}