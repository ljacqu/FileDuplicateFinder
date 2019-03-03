package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.TestUtils;
import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.filefilter.FilePathMatcher;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import com.google.common.io.MoreFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link FileDuplicateFinder}.
 */
class FileDuplicateFinderTest {

    private FileDuplicateFinder fileDuplicateFinder;

    @Mock
    private FileHasher fileHasher;

    @Mock
    private FileDupeFinderConfiguration configuration;

    @Mock
    private FilePathMatcher filePathMatcher;

    private Path rootFolder = TestUtils.getTestSamplesFolder();

    @BeforeEach
    public void initFields() {
        MockitoAnnotations.initMocks(this);
        fileDuplicateFinder = new FileDuplicateFinder(rootFolder, fileHasher, filePathMatcher, configuration);
    }

    @Test
    void shouldFindDuplicates() throws IOException {
        // given
        given(filePathMatcher.shouldScan(any(Path.class))).willReturn(true);
        given(filePathMatcher.hasFileFromResultWhitelist(anyCollection())).willReturn(true);
        given(configuration.getProgressFilesFoundInterval()).willReturn(1023);
        given(configuration.getProgressFilesHashedInterval()).willReturn(1023);
        given(configuration.getMaxSizeForHashingInBytes()).willReturn(99999L);
        given(configuration.getFileReadBeforeHashMinSizeBytes()).willReturn(99999L);
        given(fileHasher.calculateHash(any(Path.class)))
            .willAnswer(invocation -> MoreFiles.getFileExtension(invocation.getArgument(0)));

        // when
        fileDuplicateFinder.processFiles();
        List<DuplicateEntry> result = fileDuplicateFinder.filterFilesForDuplicates();

        // then
        verify(fileHasher, times(6)).calculateHash(any(Path.class));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPaths()).extracting(path -> path.getFileName().toString())
            .containsExactlyInAnyOrder("test_6.csv", "test_4.csv");
    }

    @Test
    void shouldNotHashFilesBeyondSizeLimit() throws IOException {
        // given
        given(filePathMatcher.shouldScan(any(Path.class))).willReturn(true);
        given(filePathMatcher.hasFileFromResultWhitelist(anyCollection())).willReturn(true);
        given(configuration.getProgressFilesFoundInterval()).willReturn(3);
        given(configuration.getProgressFilesHashedInterval()).willReturn(3);
        given(configuration.getMaxSizeForHashingInBytes()).willReturn(1024L);
        given(configuration.getFileReadBeforeHashMinSizeBytes()).willReturn(99999L);
        given(fileHasher.calculateHash(any(Path.class)))
            .willAnswer(invocation -> MoreFiles.getFileExtension(invocation.getArgument(0)));

        // when
        fileDuplicateFinder.processFiles();
        List<DuplicateEntry> result = fileDuplicateFinder.filterFilesForDuplicates();

        // then
        verify(fileHasher, times(4)).calculateHash(any(Path.class));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPaths()).extracting(path -> path.getFileName().toString())
            .containsExactlyInAnyOrder("test_6.csv", "test_4.csv");
    }

    @Test
    void shouldReadFirstBytesBeforeHashing() throws IOException {
        // given
        given(filePathMatcher.shouldScan(any(Path.class))).willReturn(true);
        given(filePathMatcher.hasFileFromResultWhitelist(anyCollection())).willReturn(true);
        given(configuration.getProgressFilesFoundInterval()).willReturn(1023);
        given(configuration.getProgressFilesHashedInterval()).willReturn(1023);
        given(configuration.getMaxSizeForHashingInBytes()).willReturn(99999L);
        given(configuration.getFileReadBeforeHashMinSizeBytes()).willReturn(1L);
        given(configuration.getFileReadBeforeHashNumberOfBytes()).willReturn(5);
        given(fileHasher.calculateHash(any(Path.class))).willReturn("s");

        // when
        fileDuplicateFinder.processFiles();
        List<DuplicateEntry> result = fileDuplicateFinder.filterFilesForDuplicates();

        // then
        verify(fileHasher, times(4)).calculateHash(any(Path.class));
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPaths()).extracting(path -> path.getFileName().toString())
            .containsExactlyInAnyOrder("test_6.csv", "test_4.csv");
        assertThat(result.get(1).getPaths()).extracting(path -> path.getFileName().toString())
            .containsExactlyInAnyOrder("test_3.txt", "test_1.json");
    }

    @Test
    void shouldRespectFilePathMatcher() throws IOException {
        // given
        given(filePathMatcher.shouldScan(any(Path.class)))
            .willAnswer(invocation -> !invocation.getArgument(0).toString().endsWith(".txt"));
        given(filePathMatcher.hasFileFromResultWhitelist(anyCollection())).willReturn(true);
        given(configuration.getProgressFilesFoundInterval()).willReturn(1023);
        given(configuration.getProgressFilesHashedInterval()).willReturn(1023);
        given(configuration.getMaxSizeForHashingInBytes()).willReturn(99999L);
        given(configuration.getFileReadBeforeHashMinSizeBytes()).willReturn(99999L);
        given(fileHasher.calculateHash(any(Path.class)))
            .willAnswer(invocation -> MoreFiles.getFileExtension(invocation.getArgument(0)));

        // when
        fileDuplicateFinder.processFiles();
        List<DuplicateEntry> result = fileDuplicateFinder.filterFilesForDuplicates();

        // then
        verify(fileHasher, times(2)).calculateHash(any(Path.class));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPaths()).extracting(path -> path.getFileName().toString())
            .containsExactlyInAnyOrder("test_6.csv", "test_4.csv");
    }
}