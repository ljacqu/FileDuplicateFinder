package ch.jalu.fileduplicatefinder.filecount;

import java.math.BigDecimal;

public class FileCountTotalEntry implements FileCountEntry {

    private long count = 0;
    private BigDecimal totalSizeInBytes = BigDecimal.ZERO;

    public void add(FileCountEntry entry) {
        count += entry.getCount();
        totalSizeInBytes = totalSizeInBytes.add(entry.getTotalSizeInBytes());
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public BigDecimal getTotalSizeInBytes() {
        return totalSizeInBytes;
    }
}
