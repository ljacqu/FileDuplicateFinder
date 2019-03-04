package ch.jalu.fileduplicatefinder.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Test for {@link ConfigFileWriter}.
 */
class ConfigFileWriterTest {

    @Test
    void shouldCopyFile(@TempDir Path tempFolder) throws IOException {
        // given
        ConfigFileWriter configFileWriter = new ConfigFileWriter();

        // when
        Path path = configFileWriter.writeConfigFile("default.properties", tempFolder.toAbsolutePath().toString());

        // then
        assertThat(path).exists();
        assertThat(path.getFileName().toString()).isEqualTo("config.properties");
    }

    @Test
    void shouldNotCopyFileIfFileExists(@TempDir Path tempFolder) throws IOException {
        // given
        Path configFile = Paths.get(tempFolder.toAbsolutePath().toString(), "config.properties");
        Files.createFile(configFile);
        ConfigFileWriter configFileWriter = new ConfigFileWriter();

        // when / then
        assertThatExceptionOfType(IOException.class)
            .isThrownBy(() -> configFileWriter.writeConfigFile("default.properties", tempFolder.toAbsolutePath().toString()))
            .withMessageEndingWith("already exists");
    }

}