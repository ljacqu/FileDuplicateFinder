package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import ch.jalu.fileduplicatefinder.hashing.HashingAlgorithm;
import ch.jalu.fileduplicatefinder.duplicatefinder.output.DuplicateEntryOutputter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_BLACKLIST;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_MAX_SIZE;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_MIN_SIZE;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_RESULT_WHITELIST;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_WHITELIST;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FOLDER;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_HASH_ALGORITHM;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_DISTRIBUTION;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Test for {@link FileDuplicateRunner}.
 */
class FileDuplicateRunnerTest {

    @Test
    void shouldRun(@TempDir Path tempFolder) {
        // given
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        FileHasherFactory fileHasherFactory = mock(FileHasherFactory.class);
        DuplicateEntryOutputter entryOutputter = mock(DuplicateEntryOutputter.class);
        FolderPairDuplicatesCounter folderDuplicatesCounter = mock(FolderPairDuplicatesCounter.class);
        FileDuplicateRunner runner = new FileDuplicateRunner(configuration, fileHasherFactory, folderDuplicatesCounter, entryOutputter);

        given(configuration.getValueOrPrompt(DUPLICATE_FOLDER)).willReturn(tempFolder);
        given(configuration.getValue(DUPLICATE_FILTER_WHITELIST)).willReturn("");
        given(configuration.getValue(DUPLICATE_FILTER_BLACKLIST)).willReturn("");
        given(configuration.getValue(DUPLICATE_FILTER_MIN_SIZE)).willReturn(0.0);
        given(configuration.getValue(DUPLICATE_FILTER_MAX_SIZE)).willReturn(0.0);
        given(configuration.getValue(DUPLICATE_HASH_ALGORITHM)).willReturn(HashingAlgorithm.CRC32);
        given(configuration.getValue(DUPLICATE_FILTER_RESULT_WHITELIST)).willReturn("");
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL)).willReturn(1023);
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL)).willReturn(1023);
        given(configuration.getValue(DUPLICATE_OUTPUT_DISTRIBUTION)).willReturn(false);
        given(configuration.getValue(DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH)).willReturn(false);
        given(configuration.getValue(DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT)).willReturn(false);

        // when
        runner.run();

        // then
        verify(fileHasherFactory).createFileHasher(HashingAlgorithm.CRC32);
        verify(entryOutputter).outputResult(anyList());
        verifyNoInteractions(folderDuplicatesCounter);
    }

    @Test
    void shouldRunIncludingFolderDuplicates(@TempDir Path tempFolder) {
        // given
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        FileHasherFactory fileHasherFactory = mock(FileHasherFactory.class);
        DuplicateEntryOutputter entryOutputter = mock(DuplicateEntryOutputter.class);
        FolderPairDuplicatesCounter folderDuplicatesCounter = mock(FolderPairDuplicatesCounter.class);
        FileDuplicateRunner runner = new FileDuplicateRunner(configuration, fileHasherFactory, folderDuplicatesCounter, entryOutputter);

        given(configuration.getValueOrPrompt(DUPLICATE_FOLDER)).willReturn(tempFolder);
        given(configuration.getValue(DUPLICATE_FILTER_WHITELIST)).willReturn("");
        given(configuration.getValue(DUPLICATE_FILTER_BLACKLIST)).willReturn("");
        given(configuration.getValue(DUPLICATE_FILTER_MIN_SIZE)).willReturn(0.0);
        given(configuration.getValue(DUPLICATE_FILTER_MAX_SIZE)).willReturn(0.0);
        given(configuration.getValue(DUPLICATE_HASH_ALGORITHM)).willReturn(HashingAlgorithm.GFH);
        given(configuration.getValue(DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT)).willReturn(true);
        given(configuration.getValue(DUPLICATE_FILTER_RESULT_WHITELIST)).willReturn("");
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL)).willReturn(1023);
        given(configuration.getValue(DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL)).willReturn(1023);
        given(configuration.getValue(DUPLICATE_OUTPUT_DISTRIBUTION)).willReturn(false);
        given(configuration.getValue(DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH)).willReturn(false);

        // when
        runner.run();

        // then
        verify(fileHasherFactory).createFileHasher(HashingAlgorithm.GFH);
        verify(entryOutputter).outputResult(anyList());
        verify(folderDuplicatesCounter).getFolderToFolderDuplicateCount(anyCollection());
    }
}