package ch.jalu.fileduplicatefinder.filefilter;

import ch.jalu.fileduplicatefinder.TestUtils;
import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
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
        boolean result = pathMatcher.shouldScan(TestUtils.getTestSamplesFolder());

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

    @Test
    void shouldReturnIfListContainsPathMatchingFilter() {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        given(configuration.getFilterDuplicatesWhitelist()).willReturn("*.csv");
        List<Path> paths1 = Arrays.asList(Paths.get("test.txt"), Paths.get("test.csv"), Paths.get("file.pdf"));
        List<Path> paths2 = Arrays.asList(Paths.get("test.txt"), Paths.get("test.pdf"), Paths.get("file.doc"));
        ConfigurableFilePathMatcher pathMatcher = new ConfigurableFilePathMatcher(configuration);

        // when
        boolean result1 = pathMatcher.hasFileFromResultWhitelist(paths1);
        boolean result2 = pathMatcher.hasFileFromResultWhitelist(paths2);

        // then
        assertThat(result1).isTrue();
        assertThat(result2).isFalse();
    }

    @Test
    void shouldPassIfDuplicatesResultFilterIsNull() {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        given(configuration.getFilterDuplicatesWhitelist()).willReturn(null);
        List<Path> paths1 = Arrays.asList(Paths.get("test.txt"), Paths.get("test.csv"), Paths.get("file.pdf"));
        List<Path> paths2 = Arrays.asList(Paths.get("test.txt"), Paths.get("test.pdf"), Paths.get("file.doc"));
        ConfigurableFilePathMatcher pathMatcher = new ConfigurableFilePathMatcher(configuration);

        // when
        boolean result1 = pathMatcher.hasFileFromResultWhitelist(paths1);
        boolean result2 = pathMatcher.hasFileFromResultWhitelist(paths2);

        // then
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
    }

    private static Set<String> getMatchingFilesFromSampleFolder(FilePathMatcher pathMatcher) throws IOException {
        return Files.list(TestUtils.getTestSamplesFolder())
            .filter(pathMatcher::shouldScan)
            .map(path -> path.getFileName().toString())
            .collect(Collectors.toSet());
    }
}