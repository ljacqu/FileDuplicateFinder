package ch.jalu.fileduplicatefinder;

import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.duplicatefinder.FolderPairDuplicatesCounter;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import ch.jalu.fileduplicatefinder.output.DuplicateEntryOutputter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test for {@link FileDuplicateFinderRunner}.
 */
class FileDuplicateFinderRunnerTest {

    @Test
    void shouldRun(@TempDir Path tempFolder) {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        FileHasherFactory fileHasherFactory = mock(FileHasherFactory.class);
        DuplicateEntryOutputter entryOutputter = mock(DuplicateEntryOutputter.class);
        FolderPairDuplicatesCounter folderDuplicatesCounter = mock(FolderPairDuplicatesCounter.class);
        FileDuplicateFinderRunner runner = new FileDuplicateFinderRunner(configuration, fileHasherFactory, entryOutputter, folderDuplicatesCounter);

        given(configuration.getRootFolder()).willReturn(tempFolder);
        given(configuration.getHashAlgorithm()).willReturn("configuredHash");

        // when
        runner.execute();

        // then
        verify(fileHasherFactory).createFileHasher("configuredHash");
        verify(entryOutputter).outputResult(anyList());
        verifyZeroInteractions(folderDuplicatesCounter);
    }

    @Test
    void shouldRunIncludingFolderDuplicates(@TempDir Path tempFolder) {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        FileHasherFactory fileHasherFactory = mock(FileHasherFactory.class);
        DuplicateEntryOutputter entryOutputter = mock(DuplicateEntryOutputter.class);
        FolderPairDuplicatesCounter folderDuplicatesCounter = mock(FolderPairDuplicatesCounter.class);
        FileDuplicateFinderRunner runner = new FileDuplicateFinderRunner(configuration, fileHasherFactory, entryOutputter, folderDuplicatesCounter);

        given(configuration.getRootFolder()).willReturn(tempFolder);
        given(configuration.getHashAlgorithm()).willReturn("configuredHash");
        given(configuration.isOutputFolderPairCount()).willReturn(true);

        // when
        runner.execute();

        // then
        verify(fileHasherFactory).createFileHasher("configuredHash");
        verify(entryOutputter).outputResult(anyList());
        verify(folderDuplicatesCounter).getFolderToFolderDuplicateCount(anyCollection());
    }
}