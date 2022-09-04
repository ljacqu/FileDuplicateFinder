package ch.jalu.fileduplicatefinder.filecount;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;

import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.FILE_COUNT_FOLDER;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.FILE_COUNT_GROUPS;

public class FileCountRunner {

    public static final String ID = "filecount";

    private final Scanner scanner;

    private final FileUtilConfiguration properties;

    public FileCountRunner(Scanner scanner, FileUtilConfiguration properties) {
        this.scanner = scanner;
        this.properties = properties;
    }

    public void run() {
        Path folder = getFolderFromProperties();
        Map<String, FileGroupStatistics> statsByExtension = new FileCounter(folder).gatherExtensionCount();
        applyConfiguredGroups(statsByExtension);
        System.out.println("Got " + statsByExtension.size() + " entries");

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
        String folderProperty = properties.getString(FILE_COUNT_FOLDER);
        return folderProperty == null ? Paths.get(".") : Paths.get(folderProperty);
    }

    private void applyConfiguredGroups(Map<String, FileGroupStatistics> statsByExtension) {
        String groupProperty = properties.getString(FILE_COUNT_GROUPS);
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
            System.err.println("Invalid group command! Expected something like 'group image jpg,jpeg,png'");
            return;
        }
        String groupName = commandParts[1].toLowerCase(Locale.ROOT);

        FileGroupStatistics stats = statsByExtension.get(groupName);
        if (stats == null) {
            stats = new FileGroupStatistics();
        }

        for (String extension : commandParts[2].split(",")) {
            FileGroupStatistics extensionStats = statsByExtension.remove(extension.trim().toUpperCase(Locale.ROOT));
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

        Function<FileGroupStatistics, Comparable<?>> propertyComparer;
        switch (cmdParts[1]) {
            case "size":
                propertyComparer = FileGroupStatistics::getTotalFileSize;
                break;
            case "count":
                propertyComparer = FileGroupStatistics::getCount;
                break;
            default:
                System.err.println("Unknown sort property. Use size or count");
                return;
        }

        Comparator<FileGroupStatistics> comparator = Comparator.comparing((Function) propertyComparer);
        if (cmdParts.length >= 3 && "desc".equals(cmdParts[2])) {
            comparator = comparator.reversed();
        }

        stats.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(comparator))
            .forEach(entry -> {
                System.out.println(entry.getKey() + ": " + entry.getValue().getCount()
                    + " (" + entry.getValue().getTotalFileSize() + ")");
            });
    }

    private void outputHelp() {
        System.out.println("- exit to stop");
        System.out.println("- group to group extensions together: group <groupName> <extensionList>, e.g.:");
        System.out.println("  * group images jpg,jpeg,png");
        System.out.println("  * group text txt,html,md");
        System.out.println("- sort to output results: sort <size/count> [desc], e.g.");
        System.out.println("  * sort size");
        System.out.println("  * sort count desc");
    }
}
