package ch.jalu.fileduplicatefinder.filefilter;

import ch.jalu.fileduplicatefinder.TestUtils;
import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.Set;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link ConfigurableFilePathMatcher}.
 */
class ConfigurableFilePathMatcherTest {

    @Test
    void shouldMatchAllFilesByDefault() throws IOException {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        given(configuration.getFilterMinSizeInMb()).willReturn(null);
        given(configuration.getFilterMaxSizeInMb()).willReturn(null);

        // when
        ConfigurableFilePathMatcher result = new ConfigurableFilePathMatcher(configuration);

        // then
        Set<String> fileNames = getMatchingFilesFromSampleFolder(result);
        assertThat(fileNames).containsExactlyInAnyOrder(
            "test_1.json", "test_2.txt", "test_3.txt", "test_4.csv", "test_5.csv", "test_6.csv");
    }

    @Test
    void shouldFilterByWhitelistAndMaxSize() throws IOException {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        given(configuration.getFilterWhitelist()).willReturn("**/*.csv");
        given(configuration.getFilterMaxSizeInMb()).willReturn(0.001);

        // when
        ConfigurableFilePathMatcher result = new ConfigurableFilePathMatcher(configuration);

        // then
        Set<String> fileNames = getMatchingFilesFromSampleFolder(result);
        assertThat(fileNames).containsExactlyInAnyOrder("test_5.csv");
    }

    @Test
    void shouldFilterByMinSizeAndBlacklist() throws IOException {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        given(configuration.getFilterMinSizeInMb()).willReturn(0.00001);
        given(configuration.getFilterMaxSizeInMb()).willReturn(null);
        given(configuration.getFilterBlacklist()).willReturn("**/test*.txt");

        // when
        ConfigurableFilePathMatcher result = new ConfigurableFilePathMatcher(configuration);

        // then
        Set<String> fileNames = getMatchingFilesFromSampleFolder(result);
        assertThat(fileNames).containsExactlyInAnyOrder("test_1.json", "test_4.csv", "test_5.csv", "test_6.csv");
    }

    @Test
    void shouldNotFilterFolders() {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        given(configuration.getFilterWhitelist()).willReturn("whitelistThatWillNotMatchAnything");
        ConfigurableFilePathMatcher pathMatcher = new ConfigurableFilePathMatcher(configuration);

        // when
        boolean result = pathMatcher.matches(TestUtils.getTestSamplesFolder());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldThrowForInvalidGlobSyntax() {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        given(configuration.getFilterBlacklist()).willReturn("[!]");

        // when / then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> new ConfigurableFilePathMatcher(configuration))
            .withMessage("Failed to create matcher for '[!]'")
            .withCauseInstanceOf(PatternSyntaxException.class);
    }

    private static Set<String> getMatchingFilesFromSampleFolder(PathMatcher pathMatcher) throws IOException {
        return Files.list(TestUtils.getTestSamplesFolder())
            .filter(pathMatcher::matches)
            .map(path -> path.getFileName().toString())
            .collect(Collectors.toSet());
    }
}