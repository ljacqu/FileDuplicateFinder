package ch.jalu.fileduplicatefinder.config;

import ch.jalu.fileduplicatefinder.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

/**
 * Test for {@link FileDupeFinderConfiguration}.
 */
class FileDupeFinderConfigurationTest {

    @Test
    void shouldLoadPropertiesFromDefault() {
        // given / when
        FileDupeFinderConfiguration configuration = new FileDupeFinderConfiguration(null);

        // then
        assertThat(configuration.getRootFolder()).isNotNull();
        assertThat(configuration.getHashAlgorithm()).isNotNull();
        assertThat(configuration.getMaxSizeForHashingInBytes()).isGreaterThan(0);
        assertThat(configuration.getProgressFilesFoundInterval()).isGreaterThan(0);
        assertThat(configuration.getProgressFilesHashedInterval()).isGreaterThan(0);

        assertThat(configuration.isDuplicatesOutputEnabled()).isTrue();
        assertThat(configuration.isDistributionOutputEnabled()).isFalse();
        assertThat(configuration.isOutputFolderPairCount()).isFalse();

        assertThat(configuration.getFilterMinSizeInMb()).isNull();
        assertThat(configuration.getFilterMaxSizeInMb()).isNull();

        assertThat(configuration.getFileReadBeforeHashMinSizeBytes()).isGreaterThan(0);
        assertThat(configuration.getFileReadBeforeHashNumberOfBytes()).isGreaterThan(0);
    }

    @Test
    void shouldLoadPropertiesFromDefaultAndUserConfig() {
        // given
        Path configFile = TestUtils.getConfigSampleFile();

        // when
        FileDupeFinderConfiguration configuration = new FileDupeFinderConfiguration(configFile);

        // then
        assertThat(configuration.getFilterBlacklist()).isEqualTo("blacklist");
        assertThat(configuration.getFilterWhitelist()).isEqualTo("whitelist");
        assertThat(configuration.getFilterMinSizeInMb()).isEqualTo(123);
        assertThat(configuration.getFilterMaxSizeInMb()).isEqualTo(234);
        assertThat(configuration.getFilterDuplicatesWhitelist()).isEqualTo("dupes");
        assertThat(configuration.isDistributionOutputEnabled()).isTrue();
        assertThat(configuration.isOutputFolderPairCount()).isTrue();

        // Check that files are taken over from default
        assertThat(configuration.getHashAlgorithm()).isEqualTo("sha1");
        assertThat(configuration.isDuplicatesOutputEnabled()).isTrue();
    }

    @Test
    void shouldWrapIoExceptionWhenReadingUserProperty() {
        // given
        Path configFile = TestUtils.getTestSamplesFolder();

        // when / then
        assertThatExceptionOfType(UncheckedIOException.class)
            .isThrownBy(() -> new FileDupeFinderConfiguration(configFile))
            .withCause(new IOException("Is a directory"));
    }

    @Test
    void shouldIgnoreUserConfigIfItDoesNotExist() {
        // given
        Path configFile = Paths.get("bogus");

        // when
        new FileDupeFinderConfiguration(configFile);

        // then - nothing happens
    }
}