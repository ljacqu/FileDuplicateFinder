package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.filefilter.FilePathMatcher;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private final FilePathMatcher pathMatcher;
    private final FileDupeFinderConfiguration configuration;

    private final Map<Long, List<Path>> pathsBySize = new HashMap<>();
    private int count;
    private int hashingSaveCount;

    public FileDuplicateFinder(Path rootFolder, FileHasher fileHasher, FilePathMatcher pathMatcher,
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
        if (pathMatcher.shouldScan(path)) {
            if (Files.isDirectory(path)) {
                PathUtils.list(path).forEach(this::processPath);
            } else if (Files.isRegularFile(path)) {
                long fileSize = PathUtils.size(path);
                List<Path> paths = pathsBySize.computeIfAbsent(fileSize, s -> new ArrayList<>(5));
                paths.add(path);

                if ((++count & configuration.getProgressFilesFoundInterval()) == 0) {
                    System.out.println("Found " + count + " files");
                }
            }
        }
    }

    public List<DuplicateEntry> filterFilesForDuplicates() {
        System.out.println();
        System.out.print("Hashing files");
        List<DuplicateEntry> duplicateEntries = pathsBySize.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .flatMap(hashEntriesInFileSizeAndReturnDuplicates())
            .sorted(createDuplicateEntryComparator())
            .collect(Collectors.toList());
        if (configuration.isDifferenceFromFileReadBeforeHashOutputEnabled()) {
            System.out.println("Skipped hashing " + hashingSaveCount + " files from reading bytes before hashing");
        }
        return duplicateEntries;
    }

    public Map<Integer, Long> getSizeDistribution() {
        return pathsBySize.values().stream()
            .collect(Collectors.groupingBy(List::size, Collectors.counting()));
    }

    private Function<Map.Entry<Long, List<Path>>, Stream<DuplicateEntry>> hashEntriesInFileSizeAndReturnDuplicates() {
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

    private Stream<DuplicateEntry> hashFilesAndReturnDuplicates(long fileSize, List<Path> paths,
                                                                Runnable progressUpdater) {
        if (fileSize >= configuration.getMaxSizeForHashingInBytes()) {
            return Stream.of(new DuplicateEntry(fileSize, "Size " + fileSize, paths));
        }

        Multimap<String, Path> pathsByHash = HashMultimap.create(paths.size(), 2);
        for (Path path : getPathsToHash(paths, fileSize)) {
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
                .filter(e -> e.getValue().size() > 1 && pathMatcher.hasFileFromResultWhitelist(e.getValue()))
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList());
            if (configuration.isDifferenceFromFileReadBeforeHashOutputEnabled()) {
                System.out.print(paths.size() + " -> " + filteredPaths.size() + " ");
                hashingSaveCount += paths.size() - filteredPaths.size();
            }
            return filteredPaths;
        } else {
            return pathMatcher.hasFileFromResultWhitelist(paths) ? paths : Collections.emptyList();
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
