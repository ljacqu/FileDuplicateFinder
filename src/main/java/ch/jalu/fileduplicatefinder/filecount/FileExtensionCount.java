package ch.jalu.fileduplicatefinder.filecount;

import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.math.BigDecimal;
import java.nio.file.Path;

public class FileExtensionCount implements FileCountEntry {

    private final String extension;
    private long count = 0;
    private BigDecimal totalSizeInBytes = BigDecimal.ZERO;

    public FileExtensionCount(String extension) {
        this.extension = extension;
    }

    public void add(Path file) {
        ++count;
        BigDecimal sizeOfNewFile = BigDecimal.valueOf(PathUtils.size(file));
        totalSizeInBytes = totalSizeInBytes.add(sizeOfNewFile);
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public BigDecimal getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public String getExtension() {
        return extension;
    }
}
