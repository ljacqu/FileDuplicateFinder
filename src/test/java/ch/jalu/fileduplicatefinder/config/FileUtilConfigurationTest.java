package ch.jalu.fileduplicatefinder.config;

import ch.jalu.fileduplicatefinder.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_FILTER_BLACKLIST;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_FILTER_MAX_SIZE;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_FILTER_MIN_SIZE;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_FILTER_RESULT_WHITELIST;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_FILTER_WHITELIST;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_HASH_ALGORITHM;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_HASH_MAX_SIZE_MB;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_OUTPUT_DISTRIBUTION;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_OUTPUT_DUPLICATES;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_READ_BEFORE_HASH_BYTES_TO_READ;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_READ_BEFORE_HASH_MIN_SIZE;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.TASK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

/**
 * Test for {@link FileUtilConfiguration}.
 */
class FileUtilConfigurationTest {

    @AfterEach
    void unsetProperty() {
        System.clearProperty("duplicates.filter.minSizeInMb");
        System.clearProperty("duplicates.output.showDistribution");
    }

    @Test
    void shouldLoadPropertiesFromDefault() {
        // given
        Scanner scanner = new Scanner("should\nnever\nbe\nused");

        // when
        FileUtilConfiguration configuration = new FileUtilConfiguration(scanner, null);

        // then
        // assertThat(configuration.getPath(DUPLICATE_FOLDER)).isNotNull();
        assertThat(configuration.getString(DUPLICATE_HASH_ALGORITHM)).isNotNull();
        assertThat(configuration.getDouble(DUPLICATE_HASH_MAX_SIZE_MB)).isGreaterThan(0);
        assertThat(configuration.getDouble(DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL)).isGreaterThan(0);
        assertThat(configuration.getDouble(DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL)).isGreaterThan(0);

        assertThat(configuration.getBoolean(DUPLICATE_OUTPUT_DUPLICATES)).isTrue();
        assertThat(configuration.getBoolean(DUPLICATE_OUTPUT_DISTRIBUTION)).isFalse();
        assertThat(configuration.getBoolean(DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT)).isFalse();

        assertThat(configuration.getDoubleOrNull(DUPLICATE_FILTER_MIN_SIZE)).isNull();
        assertThat(configuration.getDoubleOrNull(DUPLICATE_FILTER_MAX_SIZE)).isNull();

        assertThat(configuration.getInt(DUPLICATE_READ_BEFORE_HASH_MIN_SIZE)).isGreaterThan(0);
        assertThat(configuration.getInt(DUPLICATE_READ_BEFORE_HASH_BYTES_TO_READ)).isGreaterThan(0);
    }

    @Test
    void shouldLoadPropertiesFromDefaultAndUserConfig() {
        // given
        Scanner scanner = new Scanner("should\nnever\nbe\nused");
        Path configFile = TestUtils.getConfigSampleFile();

        // when
        FileUtilConfiguration configuration = new FileUtilConfiguration(scanner, configFile);

        // then
        assertThat(configuration.getString(DUPLICATE_FILTER_BLACKLIST)).isEqualTo("blacklist");
        assertThat(configuration.getString(DUPLICATE_FILTER_WHITELIST)).isEqualTo("whitelist");
        assertThat(configuration.getDoubleOrNull(DUPLICATE_FILTER_MIN_SIZE)).isEqualTo(123.0);
        assertThat(configuration.getDoubleOrNull(DUPLICATE_FILTER_MAX_SIZE)).isEqualTo(234.0);
        assertThat(configuration.getString(DUPLICATE_FILTER_RESULT_WHITELIST)).isEqualTo("dupes");
        assertThat(configuration.getBoolean(DUPLICATE_OUTPUT_DISTRIBUTION)).isTrue();
        assertThat(configuration.getBoolean(DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT)).isTrue();

        // Check that files are taken over from default
        assertThat(configuration.getString(DUPLICATE_HASH_ALGORITHM)).isEqualTo("sha1");
        assertThat(configuration.getBoolean(DUPLICATE_OUTPUT_DUPLICATES)).isTrue();
    }

    @Test
    void shouldWrapIoExceptionWhenReadingUserProperty() {
        // given
        Scanner scanner = new Scanner("should\nnever\nbe\nused");
        Path configFile = TestUtils.getTestSamplesFolder();

        // when / then
        assertThatExceptionOfType(UncheckedIOException.class)
            .isThrownBy(() -> new FileUtilConfiguration(scanner, configFile))
            .withCauseInstanceOf(IOException.class);
    }

    @Test
    void shouldIgnoreUserConfigIfItDoesNotExist() {
        // given
        Scanner scanner = new Scanner("should\nnever\nbe\nused");
        Path configFile = Paths.get("bogus");

        // when
        new FileUtilConfiguration(scanner, configFile);

        // then - nothing happens
    }

    @Test
    void shouldTakeValueFromSystemProperty() {
        // given
        Scanner scanner = new Scanner("should\nnever\nbe\nused");
        Path configFile = TestUtils.getConfigSampleFile();
        System.setProperty("duplicates.filter.minSizeInMb", "74.7");
        System.setProperty("duplicates.output.showDistribution", "true");

        // when
        FileUtilConfiguration configuration = new FileUtilConfiguration(scanner, configFile);

        // then
        assertThat(configuration.getDouble(DUPLICATE_FILTER_MIN_SIZE)).isEqualTo(74.7);
        assertThat(configuration.getBoolean(DUPLICATE_OUTPUT_DISTRIBUTION)).isTrue();
    }

    @Test
    void shouldReturnEmptyOptionalForMissingProperties() {
        // given
        Scanner scanner = new Scanner("should\nnever\nbe\nused");
        Path configFile = TestUtils.getConfigSampleFile();
        System.setProperty("duplicates.output.showDistribution", "true");

        // when
        FileUtilConfiguration configuration = new FileUtilConfiguration(scanner, configFile);

        // then
        assertThat(configuration.getStringOptional(TASK)).isEmpty();
        assertThat(configuration.getBooleanOptional(DUPLICATE_OUTPUT_DISTRIBUTION)).hasValue(true);
    }
}