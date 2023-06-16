package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import ch.jalu.fileduplicatefinder.output.DuplicateEntryOutputter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_BLACKLIST;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_RESULT_WHITELIST;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_WHITELIST;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FOLDER;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_HASH_ALGORITHM;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT;
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

        given(configuration.getPathOrPrompt(DUPLICATE_FOLDER)).willReturn(tempFolder);
        given(configuration.getString(DUPLICATE_FILTER_WHITELIST)).willReturn("");
        given(configuration.getString(DUPLICATE_FILTER_BLACKLIST)).willReturn("");
        given(configuration.getString(DUPLICATE_HASH_ALGORITHM)).willReturn("configuredHash");
        given(configuration.getString(DUPLICATE_FILTER_RESULT_WHITELIST)).willReturn("");

        // when
        runner.run();

        // then
        verify(fileHasherFactory).createFileHasher("configuredHash");
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

        given(configuration.getPathOrPrompt(DUPLICATE_FOLDER)).willReturn(tempFolder);
        given(configuration.getString(DUPLICATE_FILTER_WHITELIST)).willReturn("");
        given(configuration.getString(DUPLICATE_FILTER_BLACKLIST)).willReturn("");
        given(configuration.getString(DUPLICATE_HASH_ALGORITHM)).willReturn("configuredHash");
        given(configuration.getBoolean(DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT)).willReturn(true);
        given(configuration.getString(DUPLICATE_FILTER_RESULT_WHITELIST)).willReturn("");

        // when
        runner.run();

        // then
        verify(fileHasherFactory).createFileHasher("configuredHash");
        verify(entryOutputter).outputResult(anyList());
        verify(folderDuplicatesCounter).getFolderToFolderDuplicateCount(anyCollection());
    }
}