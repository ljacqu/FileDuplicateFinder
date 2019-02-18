package ch.jalu.fileduplicatefinder.output;

import ch.jalu.fileduplicatefinder.duplicatefinder.DuplicateEntry;

import java.util.List;

public interface DuplicateEntryOutputter {

    void outputResult(List<DuplicateEntry> duplicateEntries);
}
