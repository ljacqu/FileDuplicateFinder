package ch.jalu.fileduplicatefinder.tree;

import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Collects all files and directories and calculates the file size of the directory by summing up all its files.
 */
public class FileTreeGenerator {

    private final Path root;

    public FileTreeGenerator(Path root) {
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Path '" + root + "' is not a directory");
        }
        this.root = root;
    }

    public FileTreeEntry generateTree() {
        return visit(root);
    }

    private FileTreeEntry visit(Path path) {
        if (Files.isRegularFile(path)) {
            FileTreeEntry entry = new FileTreeEntry(path);
            entry.setSize(PathUtils.size(path));
            return entry;
        } else if (Files.isDirectory(path)) {
            long[] totalSize = { 0 };
            List<FileTreeEntry> childElements = PathUtils.list(path)
                .map(this::visit)
                .filter(Objects::nonNull)
                .peek(entry -> totalSize[0] += entry.getSize())
                .collect(Collectors.toUnmodifiableList());

            FileTreeEntry entry = new FileTreeEntry(path);
            entry.setSize(totalSize[0]);
            entry.setChildren(childElements);
            return entry;
        }
        return null;
    }
}