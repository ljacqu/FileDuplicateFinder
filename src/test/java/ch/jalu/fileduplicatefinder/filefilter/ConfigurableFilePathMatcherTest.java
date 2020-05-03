package ch.jalu.fileduplicatefinder.filefilter;

import ch.jalu.fileduplicatefinder.TestUtils;
import ch.jalu.fileduplicatefinder.config.FileUtilProperties;
import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
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
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        given(configuration.getDoubleOrNull(FileUtilProperties.DUPLICATE_FILTER_MIN_SIZE)).willReturn(null);
        given(configuration.getDoubleOrNull(FileUtilProperties.DUPLICATE_FILTER_MAX_SIZE)).willReturn(null);

        // when
        ConfigurableFilePathMatcher result = new ConfigurableFilePathMatcher(configuration);

        // then
        Set<String> fileNames = getMatchingFilesFromSampleFolder(result);
        assertThat(fileNames).containsExactlyInAnyOrder(
            "test_1.json", "test_2.txt", "test_3.txt", "test_4.csv", "test_5.csv", "test_6.csv", "test_7.xml");
    }

    @Test
    void shouldFilterByWhitelistAndMaxSize() throws IOException {
        // given
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        given(configuration.getString(FileUtilProperties.DUPLICATE_FILTER_WHITELIST)).willReturn("**/*.csv");
        given(configuration.getDoubleOrNull(FileUtilProperties.DUPLICATE_FILTER_MAX_SIZE)).willReturn(0.001);

        // when
        ConfigurableFilePathMatcher result = new ConfigurableFilePathMatcher(configuration);

        // then
        Set<String> fileNames = getMatchingFilesFromSampleFolder(result);
        assertThat(fileNames).containsExactlyInAnyOrder("test_5.csv");
    }

    @Test
    void shouldFilterByMinSizeAndBlacklist() throws IOException {
        // given
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        given(configuration.getDoubleOrNull(FileUtilProperties.DUPLICATE_FILTER_MIN_SIZE)).willReturn(0.00001);
        given(configuration.getDoubleOrNull(FileUtilProperties.DUPLICATE_FILTER_MAX_SIZE)).willReturn(null);
        given(configuration.getString(FileUtilProperties.DUPLICATE_FILTER_BLACKLIST)).willReturn("**/test*.txt");

        // when
        ConfigurableFilePathMatcher result = new ConfigurableFilePathMatcher(configuration);

        // then
        Set<String> fileNames = getMatchingFilesFromSampleFolder(result);
        assertThat(fileNames).containsExactlyInAnyOrder("test_1.json", "test_4.csv", "test_5.csv", "test_6.csv", "test_7.xml");
    }

    @Test
    void shouldNotFilterFolders() {
        // given
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        given(configuration.getString(FileUtilProperties.DUPLICATE_FILTER_WHITELIST)).willReturn("whitelistThatWillNotMatchAnything");
        ConfigurableFilePathMatcher pathMatcher = new ConfigurableFilePathMatcher(configuration);

        // when
        boolean result = pathMatcher.shouldScan(TestUtils.getTestSamplesFolder());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldThrowForInvalidGlobSyntax() {
        // given
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        given(configuration.getString(FileUtilProperties.DUPLICATE_FILTER_BLACKLIST)).willReturn("[!]");

        // when / then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> new ConfigurableFilePathMatcher(configuration))
            .withMessage("Failed to create matcher for '[!]'")
            .withCauseInstanceOf(PatternSyntaxException.class);
    }

    @Test
    void shouldReturnIfListContainsPathMatchingFilter() {
        // given
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        given(configuration.getString(FileUtilProperties.DUPLICATE_FILTER_RESULT_WHITELIST)).willReturn("*.csv");
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
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        given(configuration.getString(FileUtilProperties.DUPLICATE_FILTER_RESULT_WHITELIST)).willReturn(null);
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