package ch.jalu.fileduplicatefinder.duplicatefinder;

import java.nio.file.Path;
import java.util.Collection;

public class DuplicateEntry {

    private final String hash;
    private final Collection<Path> paths;

    public DuplicateEntry(String hash, Collection<Path> paths) {
        this.hash = hash;
        this.paths = paths;
    }

    public String getHash() {
        return hash;
    }

    public Collection<Path> getPaths() {
        return paths;
    }
}
