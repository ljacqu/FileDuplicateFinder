package ch.jalu.fileduplicatefinder.filecount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FileGroupCount implements FileCountEntry {

    private long count = 0;
    private BigDecimal totalSizeInBytes = BigDecimal.ZERO;

    private final List<FileExtensionCount> extensions = new ArrayList<>();
    private final Set<String> groupDefinitions = new LinkedHashSet<>();

    public void add(FileExtensionCount stats) {
        count += stats.getCount();
        totalSizeInBytes = totalSizeInBytes.add(stats.getTotalSizeInBytes());
        extensions.add(stats);
    }

    public void addDefinition(String definition) {
        groupDefinitions.add(definition);
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public BigDecimal getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public List<FileExtensionCount> getExtensions() {
        return extensions;
    }

    public Set<String> getGroupDefinitions() {
        return groupDefinitions;
    }
}
