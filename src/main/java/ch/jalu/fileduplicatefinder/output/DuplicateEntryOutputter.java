package ch.jalu.fileduplicatefinder.output;

import ch.jalu.fileduplicatefinder.duplicatefinder.DuplicateEntry;
import ch.jalu.fileduplicatefinder.duplicatefinder.FolderPair;

import java.util.List;
import java.util.Map;

/**
 * Outputter of results.
 */
public interface DuplicateEntryOutputter {

    /**
     * Outputs the duplicate files which have been found.
     *
     * @param duplicateEntries the duplicate files
     */
    void outputResult(List<DuplicateEntry> duplicateEntries);

    /**
     * Outputs the number of duplicates by folder pair.
     *
     * @param totalDuplicatesByFolderPair number of duplicated files by folder pair
     */
    void outputDirectoryPairs(Map<FolderPair, Long> totalDuplicatesByFolderPair);

}
