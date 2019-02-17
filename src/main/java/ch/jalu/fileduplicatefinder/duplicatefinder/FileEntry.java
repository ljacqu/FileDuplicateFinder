package ch.jalu.fileduplicatefinder.duplicatefinder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileEntry {

    private final List<Path> paths = new ArrayList<>(5);

    public FileEntry(Path path) {
        paths.add(path);
    }

    public List<Path> getPaths() {
        return paths;
    }
}
