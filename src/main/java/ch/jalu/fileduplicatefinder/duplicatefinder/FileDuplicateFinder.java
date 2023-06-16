package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.filefilter.FilePathMatcher;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import ch.jalu.fileduplicatefinder.utils.PathUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
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

import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_HASH_MAX_SIZE_MB;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_READ_BEFORE_HASH_BYTES_TO_READ;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_READ_BEFORE_HASH_MIN_SIZE;
import static ch.jalu.fileduplicatefinder.utils.FileSizeUtils.megaBytesToBytes;

public class FileDuplicateFinder {

    private final Path rootFolder;
    private final FileHasher fileHasher;
    private final FilePathMatcher pathMatcher;
    private final FileUtilConfiguration configuration;

    private final int progressFilesFound;
    private final int progressFilesHashed;

    private final Map<Long, List<Path>> pathsBySize = new HashMap<>();
    private int count;
    private int hashingSaveCount;

    public FileDuplicateFinder(Path rootFolder, FileHasher fileHasher, FilePathMatcher pathMatcher,
                               FileUtilConfiguration configuration) {
        this.rootFolder = rootFolder;
        this.fileHasher = fileHasher;
        this.pathMatcher = pathMatcher;
        this.configuration = configuration;

        this.progressFilesFound = configuration.getPowerOfTwoMinusOne(DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL);
        this.progressFilesHashed = configuration.getPowerOfTwoMinusOne(DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL);
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

                if ((++count & progressFilesFound) == 0) {
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
        if (configuration.getBoolean(DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH)) {
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
                if ((++hashedFiles[0] & progressFilesHashed) == 0) {
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
        double maxSizeMegabytes = configuration.getDouble(DUPLICATE_HASH_MAX_SIZE_MB);
        if (maxSizeMegabytes > 0 && fileSize >= megaBytesToBytes(maxSizeMegabytes)) {
            return Stream.of(new DuplicateEntry(fileSize, "Size " + fileSize, paths));
        }

        ListMultimap<String, Path> pathsByHash = ArrayListMultimap.create(paths.size(), 2);
        for (Path path : getPathsToHash(paths, fileSize)) {
            try {
                pathsByHash.put(fileHasher.calculateHash(path), path);
                progressUpdater.run();
            } catch (IOException e) {
                throw new UncheckedIOException(path.toAbsolutePath().toString(), e);
            }
        }

        return Multimaps.asMap(pathsByHash).entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .map(e -> new DuplicateEntry(fileSize, e.getKey(), e.getValue()));
    }

    private List<Path> getPathsToHash(List<Path> paths, long filesize) {
        if (filesize >= megaBytesToBytes(configuration.getDouble(DUPLICATE_READ_BEFORE_HASH_MIN_SIZE))) {
            Multimap<WrappedByteArray, Path> files = HashMultimap.create(paths.size(), 2);
            for (Path path : paths) {
                byte[] bytes = new byte[configuration.getInt(DUPLICATE_READ_BEFORE_HASH_BYTES_TO_READ)];
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
            if (configuration.getBoolean(DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH)) {
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
