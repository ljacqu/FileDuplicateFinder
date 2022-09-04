package ch.jalu.fileduplicatefinder.filecount;

import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.math.BigDecimal;
import java.nio.file.Path;

public class FileGroupStatistics {

    private long count;
    private BigDecimal totalFileSize; // bytes

    public FileGroupStatistics() {
        this.count = 0;
        this.totalFileSize = BigDecimal.ZERO;
    }

    public FileGroupStatistics(Path file) {
        this.count = 1;
        this.totalFileSize = BigDecimal.valueOf(PathUtils.size(file));
    }

    public void add(Path file) {
        ++count;
        BigDecimal sizeOfNewFile = BigDecimal.valueOf(PathUtils.size(file));
        totalFileSize = totalFileSize.add(sizeOfNewFile);
    }

    public void add(FileGroupStatistics stats) {
        this.count += stats.count;
        this.totalFileSize = this.totalFileSize.add(stats.totalFileSize);
    }

    public long getCount() {
        return count;
    }

    public BigDecimal getTotalFileSize() {
        return totalFileSize;
    }
}
