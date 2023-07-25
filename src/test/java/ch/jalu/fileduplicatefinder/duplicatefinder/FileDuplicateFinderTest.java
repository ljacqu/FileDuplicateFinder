package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.TestUtils;
import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.filefilter.FilePathMatcher;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import ch.jalu.fileduplicatefinder.output.TaskWriterReader;
import com.google.common.io.MoreFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_HASH_MAX_SIZE_MB;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_READ_BEFORE_HASH_BYTES_TO_READ;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_READ_BEFORE_HASH_MIN_SIZE;
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
    private FileUtilConfiguration configuration;

    @Mock
    private FilePathMatcher filePathMatcher;

    @Mock
    private TaskWriterReader logger;

    private Path rootFolder = TestUtils.getTestSamplesFolder();

    @BeforeEach
    public void initFields() {
        MockitoAnnotations.initMocks(this);
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL)).willReturn(1023);
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL)).willReturn(1023);
        fileDuplicateFinder = new FileDuplicateFinder(rootFolder, fileHasher, filePathMatcher, configuration, logger);
    }

    @Test
    void shouldFindDuplicates() throws IOException {
        // given
        given(filePathMatcher.shouldScan(any(Path.class))).willReturn(true);
        given(filePathMatcher.hasFileFromResultWhitelist(anyCollection())).willReturn(true);
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL)).willReturn(1023);
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL)).willReturn(1023);
        given(configuration.getValue(DUPLICATE_HASH_MAX_SIZE_MB)).willReturn(9.0);
        given(configuration.getValue(DUPLICATE_READ_BEFORE_HASH_MIN_SIZE)).willReturn(5.0);
        given(configuration.getValue(DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH)).willReturn(false);
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
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL)).willReturn(3);
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL)).willReturn(3);
        given(configuration.getValue(DUPLICATE_HASH_MAX_SIZE_MB)).willReturn(0.0009); // approx 1 KB
        given(configuration.getValue(DUPLICATE_READ_BEFORE_HASH_MIN_SIZE)).willReturn(0.1);
        given(configuration.getValue(DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH)).willReturn(false);
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
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL)).willReturn(1023);
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL)).willReturn(1023);
        given(configuration.getValue(DUPLICATE_HASH_MAX_SIZE_MB)).willReturn(0.5);
        given(configuration.getValue(DUPLICATE_READ_BEFORE_HASH_MIN_SIZE)).willReturn(0.0000001);
        given(configuration.getValue(DUPLICATE_READ_BEFORE_HASH_BYTES_TO_READ)).willReturn(5);
        given(configuration.getValue(DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH)).willReturn(false);
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
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL)).willReturn(1023);
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL)).willReturn(1023);
        given(configuration.getValue(DUPLICATE_HASH_MAX_SIZE_MB)).willReturn(0.5);
        given(configuration.getValue(DUPLICATE_READ_BEFORE_HASH_MIN_SIZE)).willReturn(555.0);
        given(configuration.getValue(DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH)).willReturn(false);
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