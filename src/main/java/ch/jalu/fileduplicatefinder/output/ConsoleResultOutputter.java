package ch.jalu.fileduplicatefinder.output;

import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.duplicatefinder.DuplicateEntry;

import java.util.List;
import java.util.stream.Collectors;

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
            System.out.println("No duplicates found.");
        } else if (configuration.isDuplicatesOutputEnabled()) {
            long sum = 0;
            for (DuplicateEntry entry : duplicates) {
                System.out.println(formatEntry(entry));
                sum += entry.getSize() * (entry.getPaths().size() - 1);
            }

            System.out.println("Total duplicated data: " + formatSize(sum));
            System.out.println("Total: " + duplicates.size() + " duplicates");
        }
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
}
