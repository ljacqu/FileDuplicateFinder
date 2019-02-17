package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import ch.jalu.fileduplicatefinder.utils.PathUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.MoreFiles;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
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
    private int hashingSaveCount;

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
        List<DuplicateEntry> duplicateEntries = filesBySize.entrySet().stream()
            .filter(entry -> entry.getValue().getPaths().size() > 1)
            .flatMap(hashEntriesInFileSizeAndReturnDuplicates())
            .sorted(createDuplicateEntryComparator())
            .collect(Collectors.toList());
        if (configuration.isDifferenceFromFileReadBeforeHashOutputEnabled()) {
            System.out.println("Skipped hashing " + hashingSaveCount + " files from reading bytes before hashing");
        }
        return duplicateEntries;
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

            return hashFilesAndReturnDuplicates(entry.getKey(), entry.getValue(), progressUpdater);
        };
    }

    private static Comparator<DuplicateEntry> createDuplicateEntryComparator() {
        Comparator<DuplicateEntry> comparatorByNumberOfFilesAsc = Comparator.comparing(e -> e.getPaths().size());
        return Comparator.comparing(DuplicateEntry::getSize).reversed()
            .thenComparing(comparatorByNumberOfFilesAsc.reversed());
    }

    public Stream<DuplicateEntry> hashFilesAndReturnDuplicates(long fileSize, FileEntry fileEntry,
                                                               Runnable progressUpdater) {
        if (fileSize >= configuration.getMaxSizeForHashingInBytes()) {
            return Stream.of(new DuplicateEntry(fileSize, "Size " + fileSize, fileEntry.getPaths()));
        }

        Multimap<String, Path> pathsByHash = HashMultimap.create(fileEntry.getPaths().size(), 2);
        for (Path path : getPathsToHash(fileEntry.getPaths(), fileSize)) {
            try {
                pathsByHash.put(fileHasher.calculateHash(path), path);
                progressUpdater.run();
            } catch (IOException e) {
                throw new UncheckedIOException(path.toAbsolutePath().toString(), e);
            }
        }

        return pathsByHash.asMap().entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .map(e -> new DuplicateEntry(fileSize, e.getKey(), e.getValue()));
    }

    private List<Path> getPathsToHash(List<Path> paths, long filesize) {
        if (filesize >= configuration.getFileReadBeforeHashMinSizeBytes()) {
            Multimap<WrappedByteArray, Path> files = HashMultimap.create(paths.size(), 2);
            for (Path path : paths) {
                byte[] bytes = new byte[configuration.getFileReadBeforeHashNumberOfBytes()];
                try (InputStream is = MoreFiles.asByteSource(path).openBufferedStream()) {
                    is.read(bytes);
                    files.put(new WrappedByteArray(bytes), path);
                } catch (IOException e) {
                    throw new UncheckedIOException("Could not read '" + path.toAbsolutePath() + "'", e);
                }
            }
            List<Path> filteredPaths = files.asMap().entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList());
            if (configuration.isDifferenceFromFileReadBeforeHashOutputEnabled()) {
                System.out.print(paths.size() + " -> " + filteredPaths.size() + " ");
                hashingSaveCount += paths.size() - filteredPaths.size();
            }
            return filteredPaths;
        } else {
            return paths;
        }
    }

    private static class WrappedByteArray {
        private final byte[] value;

        WrappedByteArray(byte[] value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            } else if (object instanceof WrappedByteArray) {
                return Arrays.equals(value, ((WrappedByteArray) object).value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }
    }
}
