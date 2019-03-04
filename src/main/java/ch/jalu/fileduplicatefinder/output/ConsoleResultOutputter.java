package ch.jalu.fileduplicatefinder.output;

import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.duplicatefinder.DuplicateEntry;
import ch.jalu.fileduplicatefinder.duplicatefinder.FolderPair;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Outputs results to the console.
 */
public class ConsoleResultOutputter implements DuplicateEntryOutputter {

    private static final double BYTES_IN_KB = 1024;
    private static final double BYTES_IN_MB = 1024 * BYTES_IN_KB;
    private static final double BYTES_IN_GB = 1024 * BYTES_IN_MB;

    private final FileDupeFinderConfiguration configuration;

    public ConsoleResultOutputter(FileDupeFinderConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void outputResult(List<DuplicateEntry> duplicates) {
        System.out.println();
        if (duplicates.isEmpty()) {
            output("No duplicates found.");
        } else if (configuration.isDuplicatesOutputEnabled()) {
            long sum = 0;
            for (DuplicateEntry entry : duplicates) {
                output(formatEntry(entry));
                sum += entry.getSize() * (entry.getPaths().size() - 1);
            }

            output("Total duplicated data: " + formatSize(sum));
            output("Total: " + duplicates.size() + " duplicates");
        }
    }

    @Override
    public void outputDirectoryPairs(Map<FolderPair, Long> totalDuplicatesByFolderPair) {
        Path rootFolder = configuration.getRootFolder();
        totalDuplicatesByFolderPair.entrySet().stream()
            .sorted(Comparator.<Map.Entry<FolderPair, Long>, Long>comparing(Map.Entry::getValue).reversed())
            .map(cell -> cell.getValue() + ": " + cell.getKey().createTextOutput(rootFolder))
            .forEach(this::output);
    }

    private String formatEntry(DuplicateEntry entry) {
        String files = entry.getPaths().stream()
            .map(path -> configuration.getRootFolder().relativize(path).toString())
            .sorted()
            .collect(Collectors.joining(", "));
        String size = formatSize(entry.getSize());
        return String.format("[%s][%d] %s: %s", size, entry.getPaths().size(), entry.getHash(), files);
    }

    private String formatSize(long sizeInBytes) {
        if (sizeInBytes >= BYTES_IN_GB) {
            return divideWithOneDecimal(sizeInBytes, BYTES_IN_GB) + " GB";
        } else if (sizeInBytes >= BYTES_IN_MB) {
            return divideWithOneDecimal(sizeInBytes, BYTES_IN_MB) + " MB";
        } else if (sizeInBytes >= BYTES_IN_KB) {
            return divideWithOneDecimal(sizeInBytes, BYTES_IN_KB) + " KB";
        }
        return sizeInBytes + " B";
    }

    private static double divideWithOneDecimal(long size, double sizeFactor) {
        double sizeInUnit = size / sizeFactor;
        return Math.round(sizeInUnit * 10) / 10.0;
    }

    protected void output(String str) {
        System.out.println(str);
    }
}
