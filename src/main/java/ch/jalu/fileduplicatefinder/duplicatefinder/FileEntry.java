package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileEntry {

    private final List<Path> paths = new ArrayList<>(5);

    public FileEntry(Path path) {
        paths.add(path);
    }

    public List<Path> getPaths() {
        return paths;
    }

    public Stream<DuplicateEntry> hashFilesAndReturnDuplicates(FileHasher fileHasher, long fileSize,
                                                               Runnable progressUpdater) {
        Multimap<String, Path> pathsByHash = HashMultimap.create(paths.size(), 2);
        for (Path path : paths) {
            try {
                pathsByHash.put(fileHasher.calculateHash(path, fileSize), path);
                progressUpdater.run();
            } catch (IOException e) {
                throw new UncheckedIOException(path.toAbsolutePath().toString(), e);
            }
        }

        return pathsByHash.asMap().entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .map(e -> new DuplicateEntry(fileSize, e.getKey(), e.getValue()));
    }
}
