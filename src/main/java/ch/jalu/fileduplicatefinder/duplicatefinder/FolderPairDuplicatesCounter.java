package ch.jalu.fileduplicatefinder.duplicatefinder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Groups duplicate entries by folder and returns the number of duplicated files within folder pairs.
 */
public class FolderPairDuplicatesCounter {

    private Map<FolderPair, Integer[]> countByFolderPair = new HashMap<>();

    /**
     * Returns the number of duplicated files by folder pair.
     *
     * @param duplicateEntries duplicated entries to aggregate
     * @return number of duplicated files by folder pair, ready to be output
     */
    public List<String> getFolderToFolderDuplicateCount(Collection<DuplicateEntry> duplicateEntries) {
        duplicateEntries.stream()
            .map(entry -> collectionToList(entry.getPaths()))
            .forEach(this::processAllPaths);

        return countByFolderPair.entrySet().stream()
            .sorted(comparatorByCountDesc())
            .map(cell -> cell.getValue()[0] + ": " + cell.getKey())
            .collect(Collectors.toList());
    }

    private void processAllPaths(List<Path> paths) {
        for (int i = 0; i < paths.size(); ++i) {
            for (int j = i + 1; j < paths.size(); ++j) {
                addCount(paths.get(i), paths.get(j));
            }
        }
    }

    private void addCount(Path file1, Path file2) {
        FolderPair folderPair = new FolderPair(file1.getParent(), file2.getParent());
        Integer[] count = countByFolderPair.computeIfAbsent(folderPair, k -> new Integer[]{ 0 });
        ++count[0];
    }

    private static Comparator<Map.Entry<FolderPair, Integer[]>> comparatorByCountDesc() {
        Comparator<Map.Entry<FolderPair, Integer[]>> comparator = Comparator.comparing(entry -> entry.getValue()[0]);
        return comparator.reversed();
    }

    private static <T> List<T> collectionToList(Collection<T> coll) {
        if (coll instanceof List<?>) {
            return (List<T>) coll;
        } else {
            return new ArrayList<>(coll);
        }
    }

    /** Pair of folders. */
    public static class FolderPair {

        private final Path folder1;
        private final Path folder2;

        /**
         * Constructor. Ensures that folder1 comes before folder2 ({@link Path#compareTo}).
         *
         * @param folder1 first folder
         * @param folder2 second folder
         */
        public FolderPair(Path folder1, Path folder2) {
            if (folder1.compareTo(folder2) > 0) {
                this.folder1 = folder2;
                this.folder2 = folder1;
            } else {
                this.folder1 = folder1;
                this.folder2 = folder2;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof FolderPair) {
                FolderPair that = (FolderPair) obj;
                return Objects.equals(this.folder1, that.folder1)
                    && Objects.equals(this.folder2, that.folder2);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(folder1, folder2);
        }

        @Override
        public String toString() {
            return folder1.equals(folder2)
                ? "within " + folder1
                : folder1 + " - " + folder2;
        }
    }
}
