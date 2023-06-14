package ch.jalu.fileduplicatefinder.filecount;

import ch.jalu.fileduplicatefinder.configme.FileUtilConfiguration;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;

import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.FILE_COUNT_FOLDER;
import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.FILE_COUNT_GROUPS;
import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.FORMAT_FILE_SIZE;
import static ch.jalu.fileduplicatefinder.utils.FileSizeUtils.formatToHumanReadableSize;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public class FileCountRunner {

    public static final String ID = "filecount";

    private final Scanner scanner;

    private final FileUtilConfiguration configuration;

    public FileCountRunner(Scanner scanner, FileUtilConfiguration configuration) {
        this.scanner = scanner;
        this.configuration = configuration;
    }

    public void run() {
        Path folder = getFolderFromProperties();
        Map<String, FileGroupStatistics> statsByExtension = new FileCounter(folder).gatherExtensionCount();
        applyConfiguredGroups(statsByExtension);
        System.out.println("Found " + statsByExtension.size() + " different file extensions");

        String command = "help";
        while (true) {
            if ("exit".equals(command)) {
                break;
            } else if (command.startsWith("group ")) {
                handleGroupCommand(command, statsByExtension);
            } else if (command.startsWith("sort ")) {
                handleSortCommand(command, statsByExtension);
            } else {
                outputHelp();
            }
            System.out.print("Command: ");
            command = scanner.nextLine();
        }
    }

    private Path getFolderFromProperties() {
        return configuration.getPathOrPrompt(FILE_COUNT_FOLDER);
    }

    private void applyConfiguredGroups(Map<String, FileGroupStatistics> statsByExtension) {
        String groupProperty = configuration.getString(FILE_COUNT_GROUPS);
        if (groupProperty.isEmpty()) {
            return;
        }

        Pattern pat = Pattern.compile("\\w+ \\w+(,\\w+)*");
        for (String groupDefinition : groupProperty.split(";")) {
            groupDefinition = groupDefinition.trim();
            if (pat.matcher(groupDefinition).matches()) {
                handleGroupCommand("group " + groupDefinition, statsByExtension);
            } else if (!groupDefinition.isEmpty()) {
                System.err.println("Ignoring group definition '" + groupDefinition + "': invalid definition");
            }
        }
    }

    private void handleGroupCommand(String group, Map<String, FileGroupStatistics> statsByExtension) {
        String[] commandParts = group.split(" ");
        if (commandParts.length != 3) {
            System.err.println("Invalid group command! Expected something like 'group image .jpg,.jpeg,.png'");
            return;
        }
        String groupName = LOWER_CAMEL.to(UPPER_CAMEL, commandParts[1]);

        FileGroupStatistics stats = statsByExtension.get(groupName);
        if (stats == null) {
            stats = new FileGroupStatistics();
        }

        for (String extension : commandParts[2].split(",")) {
            FileGroupStatistics extensionStats = statsByExtension.remove(extension.trim().toLowerCase(Locale.ROOT));
            if (extensionStats != null) {
                stats.add(extensionStats);
            }
        }

        if (statsByExtension.get(groupName) == null && stats.getCount() > 0) {
            statsByExtension.put(groupName, stats);
            System.out.println("Created new group '" + groupName + "'");
        } else {
            System.out.println("Updated group '" + groupName + "'");
        }
    }

    private void handleSortCommand(String sortCommand, Map<String, FileGroupStatistics> stats) {
        String[] cmdParts = sortCommand.split(" ");
        if (cmdParts.length < 2) {
            System.err.println("Invalid sort command! Expected 'sort size' or 'sort count asc'");
            return;
        }

        Function<FileGroupStatistics, Comparable<?>> propertyToCompareGetter;
        switch (cmdParts[1]) {
            case "size":
                propertyToCompareGetter = FileGroupStatistics::getTotalFileSize;
                break;
            case "count":
                propertyToCompareGetter = FileGroupStatistics::getCount;
                break;
            default:
                System.err.println("Unknown sort property. Use size or count");
                return;
        }

        Comparator<FileGroupStatistics> comparator = Comparator.comparing((Function) propertyToCompareGetter);
        if (cmdParts.length >= 3 && "desc".equals(cmdParts[2])) {
            comparator = comparator.reversed();
        }
        boolean formatFileSize = configuration.getBoolean(FORMAT_FILE_SIZE);

        stats.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(comparator))
            .forEach(entry -> {
                String fileSize = formatFileSize
                    ? formatToHumanReadableSize(entry.getValue().getTotalFileSize().longValue())
                    : String.valueOf(entry.getValue().getTotalFileSize());
                System.out.println(entry.getKey() + ": " + entry.getValue().getCount() + " (" + fileSize + ")");
            });
    }

    private void outputHelp() {
        System.out.println("- exit to stop");
        System.out.println("- group to group extensions together: group <groupName> <extensionList>, e.g.:");
        System.out.println("  * group images .jpg,.jpeg,.png");
        System.out.println("  * group text .txt,.html,.md");
        System.out.println("- sort to output results: sort <size/count> [desc], e.g.");
        System.out.println("  * sort size");
        System.out.println("  * sort count desc");
    }
}
