package ch.jalu.fileduplicatefinder.duplicatefinder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class DuplicateEntry {

    private final long size;
    private final String hash;
    private final Collection<Path> paths;

    public DuplicateEntry(long size, String hash, List<Path> paths) {
        this.size = size;
        this.hash = hash;
        this.paths = paths;
    }

    public long getSize() {
        return size;
    }

    public String getHash() {
        return hash;
    }

    public Collection<Path> getPaths() {
        return paths;
    }
}
