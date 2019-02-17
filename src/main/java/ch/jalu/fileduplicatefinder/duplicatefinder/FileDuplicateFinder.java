package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileDuplicateFinder {

    private final Path rootFolder;
    private final FileHasher fileHasher;
    private final PathMatcher pathMatcher;
    private final FileDupeFinderConfiguration configuration;

    private final Map<Long, FileEntry> filesBySize = new HashMap<>();
    private int count;

    public FileDuplicateFinder(Path rootFolder, FileHasher fileHasher, PathMatcher pathMatcher,
                               FileDupeFinderConfiguration configuration) {
        this.rootFolder = rootFolder;
        this.fileHasher = fileHasher;
        this.pathMatcher = pathMatcher;
        this.configuration = configuration;
    }

    public void processFiles() {
        processPath(rootFolder);
        System.out.println("Found total " + count + " files");
    }

    private void processPath(Path path) {
        if (pathMatcher.matches(path)) {
            if (Files.isDirectory(path)) {
                PathUtils.list(path).forEach(this::processPath);
            } else if (Files.isRegularFile(path)) {
                long fileSize = PathUtils.size(path);
                FileEntry fileEntry = filesBySize.get(fileSize);
                if (fileEntry == null) {
                    filesBySize.put(fileSize, new FileEntry(path));
                } else {
                    fileEntry.getPaths().add(path);
                }
                if ((++count & configuration.getProgressFilesFoundInterval()) == 0) {
                    System.out.println("Found " + count + " files");
                }
            }
        }
    }

    public List<DuplicateEntry> filterFilesForDuplicates() {
        System.out.println();
        System.out.print("Hashing files");
        return filesBySize.entrySet().stream()
            .filter(entry -> entry.getValue().getPaths().size() > 1)
            .flatMap(hashEntriesInFileSizeAndReturnDuplicates())
            .sorted(createDuplicateEntryComparator())
            .collect(Collectors.toList());
    }

    public Map<Integer, Integer> getSizeDistribution() {
        // probably there is a better way of doing this?
        return filesBySize.values().stream()
            .collect(Collectors.groupingBy(e -> e.getPaths().size()))
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
    }

    private Function<Map.Entry<Long, FileEntry>, Stream<DuplicateEntry>> hashEntriesInFileSizeAndReturnDuplicates() {
        final int[] hashedFiles = {0};
        return entry -> {
            Runnable progressUpdater = () -> {
                if ((++hashedFiles[0] & configuration.getProgressFilesHashedInterval()) == 0) {
                    System.out.println();
                    System.out.print("Hashed " + hashedFiles[0] + " files");
                } else if ((hashedFiles[0] & 15) == 0) {
                    System.out.print(" . ");
                }
            };

            return entry.getValue().hashFilesAndReturnDuplicates(fileHasher, entry.getKey(), progressUpdater);
        };
    }

    private static Comparator<DuplicateEntry> createDuplicateEntryComparator() {
        Comparator<DuplicateEntry> comparatorByNumberOfFilesAsc = Comparator.comparing(e -> e.getPaths().size());

        return comparatorByNumberOfFilesAsc.reversed()
            .thenComparing(Comparator.comparing(DuplicateEntry::getSize).reversed());
    }
}
