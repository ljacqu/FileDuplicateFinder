package ch.jalu.fileduplicatefinder.output;

import ch.jalu.fileduplicatefinder.configme.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.duplicatefinder.DuplicateEntry;
import ch.jalu.fileduplicatefinder.duplicatefinder.FolderPair;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.DUPLICATE_FOLDER;
import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.DUPLICATE_OUTPUT_DUPLICATES;
import static ch.jalu.fileduplicatefinder.utils.FileSizeUtils.formatToHumanReadableSize;

/**
 * Outputs results to the console.
 */
public class ConsoleResultOutputter implements DuplicateEntryOutputter {

    private final FileUtilConfiguration configuration;

    public ConsoleResultOutputter(FileUtilConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void outputResult(List<DuplicateEntry> duplicates) {
        System.out.println();
        if (duplicates.isEmpty()) {
            output("No duplicates found.");
        } else if (configuration.getBoolean(DUPLICATE_OUTPUT_DUPLICATES)) {
            long sum = 0;
            for (DuplicateEntry entry : duplicates) {
                output(formatEntry(entry));
                sum += entry.getSize() * (entry.getPaths().size() - 1);
            }

            output("Total duplicated data: " + formatToHumanReadableSize(sum));
            output("Total: " + duplicates.size() + " duplicates");
        }
    }

    @Override
    public void outputDirectoryPairs(Map<FolderPair, Long> totalDuplicatesByFolderPair) {
        Path rootFolder = configuration.getPathOrPrompt(DUPLICATE_FOLDER);
        totalDuplicatesByFolderPair.entrySet().stream()
            .sorted(Map.Entry.<FolderPair, Long>comparingByValue().reversed())
            .map(cell -> cell.getValue() + ": " + cell.getKey().createTextOutput(rootFolder))
            .forEach(this::output);
    }

    private String formatEntry(DuplicateEntry entry) {
        String files = entry.getPaths().stream()
            .map(path -> configuration.getPathOrPrompt(DUPLICATE_FOLDER).relativize(path).toString())
            .sorted()
            .collect(Collectors.joining(", "));
        String size = formatToHumanReadableSize(entry.getSize());
        return String.format("[%s][%d] %s: %s", size, entry.getPaths().size(), entry.getHash(), files);
    }

    protected void output(String str) {
        System.out.println(str);
    }
}
