package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileEntry {

    private final List<Path> paths = new ArrayList<>(5);
    // Lazily initialized (only when necessary)
    private Multimap<String, Path> pathsByHash;

    public FileEntry(Path path) {
        paths.add(path);
    }

    public List<Path> getPaths() {
        return paths;
    }

    public void initPathsByHash(FileHasher fileHasher) {
        pathsByHash = HashMultimap.create(paths.size(), 2);
        for (Path path : paths) {
            try {
                pathsByHash.put(fileHasher.calculateHash(path), path);
            } catch (IOException e) {
                throw new UncheckedIOException(path.toAbsolutePath().toString(), e);
            }
        }
    }

    public Multimap<String, Path> getPathsByHash() {
        return pathsByHash;
    }
}
