package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.hashing.FileHasher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileDuplicateFinder {

    private final Path rootFolder;
    private final FileHasher fileHasher;
    private final PathMatcher pathMatcher;
    private final Map<Long, FileEntry> filesBySize = new HashMap<>();
    private int count;

    public FileDuplicateFinder(Path rootFolder, FileHasher fileHasher, PathMatcher pathMatcher) {
        this.rootFolder = rootFolder;
        this.fileHasher = fileHasher;
        this.pathMatcher = pathMatcher;
    }

    public void processFiles() throws IOException {
        processPath(rootFolder);
        System.out.println("Finished after processing " + count + " files");
    }

    private void processPath(Path path) throws IOException {
        if (pathMatcher.matches(path)) {
            if (Files.isDirectory(path)) {
                for (Path child : Files.list(path).collect(Collectors.toList())) {
                    processPath(child);
                }
            } else {
                long fileSize = Files.size(path);
                FileEntry fileEntry = filesBySize.get(fileSize);
                if (fileEntry == null) {
                    filesBySize.put(fileSize, new FileEntry(path));
                } else {
                    fileEntry.getPaths().add(path);
                }
                if ((++count & 511) == 0) {
                    System.out.println("Processed " + count + " files");
                }
            }
        }
    }

    public List<DuplicateEntry> returnDuplicates() {
        return filesBySize.values().stream()
            .filter(value -> value.getPaths().size() > 1)
            .peek(value -> value.initPathsByHash(fileHasher))
            .map(value -> value.getPathsByHash().asMap())
            .flatMap(map -> map.entrySet().stream())
            .filter(entry -> entry.getValue().size() > 1)
            .map(entry -> new DuplicateEntry(entry.getKey(), entry.getValue()))
            .sorted(Comparator.<DuplicateEntry>comparingInt(e -> e.getPaths().size()).reversed())
            .collect(Collectors.toList());
    }

    public Map<Integer, Integer> getSizeDistribution() {
        // probably there is a better way of doing this?
        return filesBySize.values().stream()
            .collect(Collectors.groupingBy(e -> e.getPaths().size()))
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
    }
}
