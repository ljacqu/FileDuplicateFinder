package ch.jalu.fileduplicatefinder.filecount;

import ch.jalu.fileduplicatefinder.ExitRunnerException;
import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.output.WriterReader;
import ch.jalu.fileduplicatefinder.utils.FileSizeUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.FILE_COUNT_DETAILED_GROUPS;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.FILE_COUNT_FOLDER;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.FILE_COUNT_GROUPS;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.FORMAT_FILE_SIZE;
import static ch.jalu.fileduplicatefinder.filecount.FileCounter.NO_EXTENSION_TEXT;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public class FileCountRunner {

    public static final String ID = "filecount";

    private final WriterReader logger;

    private final FileUtilConfiguration configuration;

    public FileCountRunner(WriterReader logger, FileUtilConfiguration configuration) {
        this.logger = logger;
        this.configuration = configuration;
    }

    public void run() throws ExitRunnerException {
        Path folder = getFolderFromProperties();
        Map<String, FileCountEntry> statsByExtension = new FileCounter(folder).gatherExtensionCount();
        logger.printLn("Found " + statsByExtension.size() + " different file extensions");

        applyConfiguredGroups(statsByExtension);

        String command = "help";
        do {
            if (command.startsWith("sort ")) {
                handleSortCommand(command, statsByExtension);
            } else if (command.startsWith("group ")) {
                handleGroupCommand(command, statsByExtension, false);
            } else if (command.startsWith("groups")) {
                printGroupDefinitions(command, statsByExtension);
            } else if (command.startsWith("rmgroup ")) {
                unrollGroup(command, statsByExtension);
            } else if (command.equals("rmgroups")) {
                unrollGroups(statsByExtension);
            } else if (command.equals("details")) {
                toggleDetails();
            } else if (command.equals("saveconf")) {
                saveConfiguration(statsByExtension);
            } else if (command.equalsIgnoreCase("q") || command.equalsIgnoreCase("quit")) {
                return;
            } else if (command.equalsIgnoreCase("x") || command.equalsIgnoreCase("exit")) {
                throw new ExitRunnerException();
            } else {
                outputHelp();
            }
            logger.printLn("Command: ");
            command = logger.getNextLine();
        } while (true);
    }

    private Path getFolderFromProperties() {
        return configuration.getValueOrPrompt(FILE_COUNT_FOLDER);
    }

    private void applyConfiguredGroups(Map<String, FileCountEntry> statsByExtension) {
        String groupProperty = configuration.getValue(FILE_COUNT_GROUPS);
        if (groupProperty.isEmpty()) {
            return;
        }

        Pattern pat = Pattern.compile("\\w+ \\S+");
        for (String groupDefinition : groupProperty.split(";")) {
            groupDefinition = groupDefinition.trim();
            if (pat.matcher(groupDefinition).matches()) {
                handleGroupCommand("group " + groupDefinition, statsByExtension, true);
            } else if (!groupDefinition.isEmpty()) {
                logger.printError("Ignoring group definition '" + groupDefinition + "': invalid definition");
            }
        }
    }

    private void handleGroupCommand(String groupCommand, Map<String, FileCountEntry> statsByExtension,
                                    boolean ignoreMismatches) {
        String[] commandParts = groupCommand.split(" ");
        if (commandParts.length != 3) {
            logger.printError("Invalid group command! Expected something like 'group image .jpg,.jpeg,.png'");
            return;
        }
        String groupName = normalizeGroupNameOrPrintError(commandParts[1]);
        if (groupName == null) {
            return;
        }

        FileGroupCount group = (FileGroupCount) statsByExtension.get(groupName);
        if (group == null) {
            group = new FileGroupCount();
        }
        int initialTotalExtensions = group.getExtensions().size();

        for (String extension : commandParts[2].split(",")) {
            long oldCount = group.getCount();
            removeAndReturnExtensions(extension, statsByExtension)
                .forEach(group::add);
            group.addDefinition(extension);

            if (!ignoreMismatches && oldCount == group.getCount()) {
                logger.printLn(" Note: nothing matched the rule '" + extension + "'");
            }
        }

        if (initialTotalExtensions == group.getExtensions().size()) {
            logger.printLn("Nothing could be added to the group '" + groupName + "'");
        } else if (initialTotalExtensions == 0) {
            statsByExtension.put(groupName, group);
            logger.printLn("Created new group '" + groupName + "'");
        } else {
            logger.printLn("Updated group '" + groupName + "'");
        }
    }

    private void printGroupDefinitions(String command, Map<String, FileCountEntry> statsByExtension) {
        String[] commandParts = command.split(" ");
        if (commandParts.length == 1) {
            long totalGroups = statsByExtension.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof FileGroupCount)
                .peek(grpEntry -> {
                    logger.printLn("Group " + grpEntry.getKey() + ": "
                        + String.join(",", ((FileGroupCount) grpEntry.getValue()).getGroupDefinitions()));
                })
                .count();
            if (totalGroups == 0L) {
                logger.printLn("No groups exist");
            }
        } else if (commandParts.length == 2) {
            String groupName = normalizeGroupNameOrPrintError(commandParts[1]);
            FileGroupCount group = groupName == null ? null : getGroupOrPrintError(groupName, statsByExtension);
            if (group != null) {
                logger.printLn("group " + commandParts[1] + " "
                    + String.join(",", group.getGroupDefinitions()));
                printGroupExtensions(group.getExtensions());
            }
        }
    }

    private void unrollGroup(String command, Map<String, FileCountEntry> statsByExtension) {
        String[] commandParts = command.split(" ");
        if (commandParts.length != 2) {
            logger.printError("Invalid rmgroup command! Expected rmgroup <name>, e.g. rmgroup images");
            return;
        }

        String groupName = normalizeGroupNameOrPrintError(commandParts[1]);
        FileGroupCount group = groupName == null ? null : getGroupOrPrintError(groupName, statsByExtension);
        if (group != null) {
            List<FileExtensionCount> extensions = group.getExtensions();
            logger.printLn("Confirm removal of group '" + groupName + "'? This will restore "
                + extensions.size() + " entries:");
            printGroupExtensions(extensions);

            logger.printLn("Type 'y' to confirm removal");
            String input = logger.getNextLine();
            if ("y".equalsIgnoreCase(input)) {
                group.getExtensions().forEach(ext -> statsByExtension.put(ext.getExtension(), ext));
                statsByExtension.remove(groupName);
            } else {
                logger.printLn("Aborted removal of the group");
            }
        }
    }

    private void unrollGroups(Map<String, FileCountEntry> statsByExtension) {
        List<String> groupNames = new ArrayList<>();
        List<Map.Entry<String, FileGroupCount>> groupEntries = statsByExtension.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof FileGroupCount)
            .map(entry -> (Map.Entry<String, FileGroupCount>) (Map.Entry) entry)
            .peek(entry -> groupNames.add(entry.getKey()))
            .collect(Collectors.toList());
        if (groupEntries.isEmpty()) {
            logger.printLn("There currently are no groups.");
        } else {
            logger.printLn("Confirm removal of " + groupEntries.size() + " groups: "
                + String.join(", ", groupNames));
            logger.printLn("The statistics for the individual file extensions will be restored.");
            System.out.print("Type 'y' to confirm removal: ");
            String input = logger.getNextLine();

            if ("y".equalsIgnoreCase(input)) {
                long extensionsRestored = groupEntries.stream()
                    .peek(grp -> statsByExtension.remove(grp.getKey()))
                    .flatMap(grp -> grp.getValue().getExtensions().stream())
                    .peek(extEntry -> statsByExtension.put(extEntry.getExtension(), extEntry))
                    .count();
                logger.printLn("Restored " + extensionsRestored + " file extension entries");
            } else {
                logger.printLn("Aborted removal of groups");
            }
        }
    }

    private void toggleDetails() {
        boolean newDetailsValue = !configuration.getValue(FILE_COUNT_DETAILED_GROUPS);
        configuration.setValue(FILE_COUNT_DETAILED_GROUPS, newDetailsValue);
        logger.printLn("File extensions of groups are now " + (newDetailsValue ? "shown" : "hidden"));
    }

    private void saveConfiguration(Map<String, FileCountEntry> statsByExtension) {
        List<Map.Entry<String, FileGroupCount>> groupEntries = statsByExtension.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof FileGroupCount)
            .map(entry -> (Map.Entry<String, FileGroupCount>) (Map.Entry) entry)
            .collect(Collectors.toList());
        if (groupEntries.isEmpty()) {
            logger.printError("There are no group entries to save");
        } else {
            String groupDefinitions = groupEntries.stream()
                .map(grp -> grp.getKey() + " " + String.join(",", grp.getValue().getGroupDefinitions()))
                .collect(Collectors.joining("; "));
            configuration.setValue(FILE_COUNT_GROUPS, groupDefinitions);
            configuration.save();
            logger.printLn("Saved group definitions to the file");
        }
    }

    private void printGroupExtensions(List<FileExtensionCount> extensions) {
        boolean formatSize = configuration.getValue(FORMAT_FILE_SIZE);

        for (FileExtensionCount extension : extensions) {
            logger.printLn(" * " + formatEntry(extension.getExtension(), extension, formatSize));
        }
    }

    @Nullable
    private FileGroupCount getGroupOrPrintError(String groupName, Map<String, FileCountEntry> statsByExtension) {
        FileCountEntry entry = statsByExtension.get(groupName);
        if (entry instanceof FileGroupCount) {
            return (FileGroupCount) entry;
        } else if (entry == null) {
            logger.printError("There is no group with name '" + groupName + "'");
        } else {
            logger.printError("Entry '" + groupName + "' is a file extension, not a group!");
        }
        return null;
    }

    private Stream<FileExtensionCount> removeAndReturnExtensions(String extension,
                                                                 Map<String, FileCountEntry> statsByExtension) {
        if (extension.startsWith("p:")) {
            Pattern pattern = Pattern.compile(extension.substring(2));
            Iterator<Map.Entry<String, FileCountEntry>> it = statsByExtension.entrySet().iterator();
            List<FileExtensionCount> matchedStats = new ArrayList<>();
            while (it.hasNext()) {
                Map.Entry<String, FileCountEntry> entry = it.next();
                if (pattern.matcher(entry.getKey()).matches()) {
                    it.remove();
                    matchedStats.add((FileExtensionCount) entry.getValue());
                }
            }
            return matchedStats.stream();
        }

        String extensionInProperCase = extension.equalsIgnoreCase(NO_EXTENSION_TEXT)
            ? NO_EXTENSION_TEXT
            : extension.toLowerCase(Locale.ROOT);
        FileCountEntry entryWithExtension = statsByExtension.remove(extensionInProperCase);
        return entryWithExtension == null ? Stream.empty() : Stream.of((FileExtensionCount) entryWithExtension);
    }

    @Nullable
    private String normalizeGroupNameOrPrintError(String groupName) {
        if (groupName.equals(NO_EXTENSION_TEXT)) {
            logger.printError("Invalid group name; \"" + NO_EXTENSION_TEXT
                + "\" is how files without extension are identified");
        } else if (groupName.startsWith(".")) {
            logger.printError("Group names may not start with a dot");
        } else {
            return LOWER_CAMEL.to(UPPER_CAMEL, groupName);
        }
        return null;
    }

    private void handleSortCommand(String sortCommand, Map<String, FileCountEntry> stats) {
        String[] cmdParts = sortCommand.split(" ");
        if (cmdParts.length < 2) {
            logger.printError("Invalid sort command! Expected 'sort size' or 'sort count asc'");
            return;
        }

        boolean isDescending = cmdParts.length >= 3 && "desc".equals(cmdParts[2]);
        Comparator<FileCountEntry> comparator;

        switch (cmdParts[1]) {
            case "size":
                comparator = createComparator(FileCountEntry::getTotalSizeInBytes, isDescending);
                break;
            case "count":
                comparator = createComparator(FileCountEntry::getCount, isDescending);
                break;
            default:
                logger.printError("Unknown sort property. Use size or count");
                return;
        }

        boolean formatFileSize = configuration.getValue(FORMAT_FILE_SIZE);
        boolean includeGroupDetails = configuration.getValue(FILE_COUNT_DETAILED_GROUPS);
        FileCountTotalEntry totalEntry = new FileCountTotalEntry();

        stats.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(comparator))
            .forEach(entry -> {
                logger.printLn(formatEntry(entry.getKey(), entry.getValue(), formatFileSize));
                totalEntry.add(entry.getValue());

                if (includeGroupDetails && entry.getValue() instanceof FileGroupCount) {
                    FileGroupCount group = (FileGroupCount) entry.getValue();
                    group.getExtensions().stream()
                        .sorted(comparator)
                        .forEach(ext -> {
                            logger.printLn(" * " + formatEntry(ext.getExtension(), ext, formatFileSize));
                        });
                }
            });
        logger.printLn("");
        logger.printLn(formatEntry("Total", totalEntry, formatFileSize));
    }

    private static <V extends Comparable<V>> Comparator<FileCountEntry> createComparator(
                                                                                   Function<FileCountEntry, V> getter,
                                                                                   boolean isDescending) {
        Comparator<FileCountEntry> comparator = Comparator.comparing(getter);
        return isDescending ? comparator.reversed() : comparator;
    }

    private String formatEntry(String designation, FileCountEntry entry, boolean formatSize) {
        String size = formatSize
            ? FileSizeUtils.formatToHumanReadableSize(entry.getTotalSizeInBytes().longValue())
            : entry.getTotalSizeInBytes().toPlainString();
        return designation + ": " + entry.getCount() + " (" + size + ")";
    }

    private void outputHelp() {
        logger.printLn("- Use 'sort' to output results: sort <size/count> [desc], e.g.");
        logger.printLn("  * sort size");
        logger.printLn("  * sort count desc");
        logger.printLn("- Use 'group' to group extensions together: group <groupName> <extensionList>, e.g.");
        logger.printLn("  * group images .jpg,.jpeg,.png");
        logger.printLn("  * group text .txt,.html,.md");
        logger.printLn("  * Regex possible with 'p:', e.g. group web p:\\.x?html?,.css,p:\\.php\\d?");
        logger.printLn("- Use 'groups' to view the definition of groups: groups [name]");
        logger.printLn("  * Optionally, provide the group name to see details (e.g. 'groups images')");
        logger.printLn("- Use 'details' to show/hide the file extensions of a group in the output");
        logger.printLn("- Use 'saveconf' to save the configurations (e.g. group definitions) "
            + "to the configuration file");
        logger.printLn("- Use 'rmgroup' to remove a group.");
        logger.printLn("  * The extensions of the group will be restored as individual entries");
        logger.printLn("- Use 'rmgroups' to delete ALL groups and restore the original extensions.");
        logger.printLn("- Use 'exit' to quit.");
    }
}
