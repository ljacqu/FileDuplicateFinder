package ch.jalu.fileduplicatefinder.folderdiff;

import ch.jalu.fileduplicatefinder.utils.FileSizeUtils;
import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public class FileElement {

    private final Path file;
    private final String name;
    private final long size;

    FileElement(Path root, Path file) {
        this.file = file;
        this.name = root.relativize(file).toString();
        this.size = PathUtils.size(file);
    }

    public Path getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public FileTime readLastModifiedTime() {
        return PathUtils.getLastModifiedTime(file);
    }

    @Override
    public String toString() {
        return name + " (" + FileSizeUtils.formatToHumanReadableSize(size) + ")";
    }
}
