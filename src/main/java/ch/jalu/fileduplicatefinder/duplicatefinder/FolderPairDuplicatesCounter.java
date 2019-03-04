package ch.jalu.fileduplicatefinder.duplicatefinder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Groups duplicate entries by folder and returns the number of duplicated files within folder pairs.
 */
public class FolderPairDuplicatesCounter {

    /**
     * Returns the number of duplicated files by folder pair.
     *
     * @param duplicateEntries duplicated entries to aggregate
     * @return number of duplicated files by folder pair
     */
    public Map<FolderPair, Long> getFolderToFolderDuplicateCount(Collection<DuplicateEntry> duplicateEntries) {
        return duplicateEntries.stream()
            .map(entry -> collectionToList(entry.getPaths()))
            .flatMap(paths -> processAllPaths(paths).stream())
            .collect(Collectors.groupingBy(k -> k, Collectors.counting()));
    }

    private List<FolderPair> processAllPaths(List<Path> paths) {
        List<FolderPair> pairs = new ArrayList<>(paths.size() * (paths.size() - 1));
        for (int i = 0; i < paths.size(); ++i) {
            for (int j = i + 1; j < paths.size(); ++j) {
                pairs.add(new FolderPair(paths.get(i).getParent(), paths.get(j).getParent()));
            }
        }
        return pairs;
    }

    private static <T> List<T> collectionToList(Collection<T> coll) {
        if (coll instanceof List<?>) {
            return (List<T>) coll;
        } else {
            return new ArrayList<>(coll);
        }
    }

}
