package ch.jalu.fileduplicatefinder.tree;

import ch.jalu.fileduplicatefinder.utils.PathUtils;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class TreeParameters {

    private TreeDisplayMode displayMode;

    @Nullable
    private Pattern filePattern;
    @Nullable
    private Pattern directoryPattern;
    private boolean hasAnyPattern;
    @Nullable
    private Long minSizeBytes;
    @Nullable
    private Long maxSizeBytes;

    private boolean showAbsolutePath;
    private boolean indentElements;
    private boolean formatFileSize;


    public boolean matchesRegex(Path path, String nameToRoot) {
        if (hasAnyPattern) {
            if (Files.isRegularFile(path)) {
                // todo detail this logic
                return filePattern != null && filePattern.matcher(nameToRoot).matches();
            } else if (Files.isDirectory(path)) {
                return directoryPattern != null && directoryPattern.matcher(nameToRoot).matches();
            } else {
                throw new IllegalArgumentException("Path '" + path + "' has unknown type");
            }
        }
        return true;
    }

    public boolean matchesSizeFilter(Path path) {
        if ((minSizeBytes != null || maxSizeBytes != null) && Files.isRegularFile(path)) {
            long size = PathUtils.size(path);
            return (minSizeBytes == null || size >= minSizeBytes) && (maxSizeBytes == null || size <= maxSizeBytes);
        }
        return true;
    }

    public boolean matchesTypeFilter(Path path) {
        switch (displayMode) {
            case ALL:
                return true;
            case DIRECTORIES:
                return Files.isDirectory(path);
            case FILES:
                return Files.isRegularFile(path);
            default:
                throw new IllegalArgumentException("Unknown display mode type");
        }
    }


    // Getters and setters

    public void setDisplayMode(TreeDisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public void setFilePattern(@Nullable Pattern filePattern) {
        hasAnyPattern = filePattern != null || directoryPattern != null;
        this.filePattern = filePattern;
    }

    public void setDirectoryPattern(@Nullable Pattern directoryPattern) {
        hasAnyPattern = filePattern != null || directoryPattern != null;
        this.directoryPattern = directoryPattern;
    }

    public boolean isShowAbsolutePath() {
        return showAbsolutePath;
    }

    public void setShowAbsolutePath(boolean showAbsolutePath) {
        this.showAbsolutePath = showAbsolutePath;
    }

    public boolean isFormatFileSize() {
        return formatFileSize;
    }

    public void setFormatFileSize(boolean formatFileSize) {
        this.formatFileSize = formatFileSize;
    }

    public boolean isIndentElements() {
        return indentElements;
    }

    public void setIndentElements(boolean indentElements) {
        this.indentElements = indentElements;
    }

    public void setMinSizeBytes(@Nullable Long minSizeBytes) {
        this.minSizeBytes = minSizeBytes;
    }

    public void setMaxSizeBytes(@Nullable Long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }
}
