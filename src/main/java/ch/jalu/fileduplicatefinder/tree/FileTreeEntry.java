package ch.jalu.fileduplicatefinder.tree;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class FileTreeEntry {

    private final Path path;
    private Long size;
    @Nullable
    private List<FileTreeEntry> children;

    public FileTreeEntry(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public List<FileTreeEntry> getChildren() {
        return children == null ? Collections.emptyList() : children;
    }

    public void setChildren(List<FileTreeEntry> children) {
        this.children = children;
    }
}
