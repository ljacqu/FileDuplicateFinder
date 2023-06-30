package ch.jalu.fileduplicatefinder.tree;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.config.property.JfuDoubleProperty;
import ch.jalu.fileduplicatefinder.config.property.JfuIntegerProperty;
import ch.jalu.fileduplicatefinder.config.property.JfuRegexProperty;
import ch.jalu.fileduplicatefinder.utils.ConsoleProgressListener;
import ch.jalu.fileduplicatefinder.utils.FileSizeUtils;
import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.FORMAT_FILE_SIZE;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_DIRECTORY_REGEX;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_FILES_PROCESSED_INTERVAL;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_FILE_MAX_SIZE_MB;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_FILE_MIN_SIZE_MB;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_FILE_REGEX;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_FOLDER;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_INDENT_ELEMENTS;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_MAX_ITEMS_IN_FOLDER;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_MIN_ITEMS_IN_FOLDER;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_OUTPUT_ELEMENT_TYPES;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_SHOW_ABSOLUTE_PATH;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.TREE_SORT_FILES_BY_SIZE;

/**
 * Outputs a file tree with configurable filters.
 */
public class FileTreeRunner {

    public static final String ID = "tree";

    private static final CharMatcher FILE_SEPARATOR_MATCHER = CharMatcher.is(File.separatorChar);

    private final Scanner scanner;
    private final FileUtilConfiguration configuration;

    public FileTreeRunner(Scanner scanner, FileUtilConfiguration configuration) {
        this.scanner = scanner;
        this.configuration = configuration;
    }

    public void run() {
        Path folder = configuration.getValueOrPrompt(TREE_FOLDER);
        FileTreeGenerator fileTreeGenerator = new FileTreeGenerator(folder);

        System.out.print("Collecting all items in " + folder.toAbsolutePath().normalize() + ":");
        ConsoleProgressListener progressCallback = new ConsoleProgressListener(
            configuration.getValue(TREE_FILES_PROCESSED_INTERVAL));
        FileTreeEntry treeRoot = fileTreeGenerator.generateTree(progressCallback::notifyItemProcessed);
        int totalItems = progressCallback.getCount();
        System.out.println("\nFound " + totalItems + " files and directories");

        TreeParameters params = createParams(false);
        List<FileTreeEntry> relevantEntries = filterAndOutputRelevantEntries(treeRoot, params, totalItems);

        String task = "help";
        boolean previousTaskWasHelp = false;
        do {
            boolean currentTaskIsHelp = false;

            switch (task) {
                case "dump":
                    throw new UnsupportedOperationException("Not yet implemented"); // TODO :)

                case "config":
                    params = createParams(true);
                    relevantEntries = filterAndOutputRelevantEntries(treeRoot, params, totalItems);
                    break;

                case "debug":
                    System.out.println("Conflicting filters: " + params.hasConflictingFilters());
                    filterRelevantEntries(treeRoot, params, true);
                    break;

                default:
                    if (!previousTaskWasHelp || "help".equals(task)) { // Always show 'help' if explicitly requested
                        System.out.println();
                        System.out.println("- Type 'dump' to dump the results to a file");
                        System.out.println("- Type 'config' to reconfigure all parameters");
                        System.out.println("- Type 'debug' to debug the parameters");
                        System.out.println("- Type 'help' to see this help");
                        System.out.println("- Type 'exit' to stop");
                    }
                    currentTaskIsHelp = true;
            }
            previousTaskWasHelp = currentTaskIsHelp;

            System.out.print("Command: ");
            task = scanner.nextLine();
        } while (!task.equals("exit"));
    }

    private List<FileTreeEntry> filterAndOutputRelevantEntries(FileTreeEntry treeRoot, TreeParameters params,
                                                               int totalItems) {
        List<FileTreeEntry> relevantEntries = filterRelevantEntries(treeRoot, params, false);

        System.out.println("Matched " + relevantEntries.size() + " out of " + totalItems + " items");
        if (relevantEntries.size() == 1) {
            System.out.println("Note: The root is never filtered out.");
        }
        if (params.hasConflictingFilters()) {
            System.out.println("Warning: Conflicting filters were found, which may never match any items. "
                + "Please recheck your filters.");
        }
        System.out.println();

        Path root = treeRoot.getPath();
        relevantEntries.forEach(elem -> printElement(elem, root, params));
        return relevantEntries;
    }

    private List<FileTreeEntry> filterRelevantEntries(FileTreeEntry root, TreeParameters params,
                                                      boolean printDebug) {
        RelevantFileEntryCollector collector = new RelevantFileEntryCollector(root, printDebug);

        for (FileTreeEntry child : root.getChildren()) {
            addEntryAndChildrenToListIfRelevantRecursively(collector, child, params);
        }
        return collector.getRelevantEntriesSorted(params);
    }

    private boolean addEntryAndChildrenToListIfRelevantRecursively(RelevantFileEntryCollector collector,
                                                                   FileTreeEntry entry, TreeParameters params) {
        collector.registerEntry(entry);

        Path path = entry.getPath();
        String relativeName = collector.getNameRelativeToRoot(path);
        boolean isMatch = params.matchesRegexFilters(path, relativeName)
            && params.matchesSizeFilters(entry)
            && params.matchesItemsInDirFilters(entry);

        boolean hasRelevantChild = false;
        for (FileTreeEntry child : entry.getChildren()) {
            hasRelevantChild |= addEntryAndChildrenToListIfRelevantRecursively(collector, child, params);
        }

        boolean isRelevant = isMatch || (!params.isShowAbsolutePath() && hasRelevantChild);
        if (isRelevant) {
            collector.addRelevantEntry(entry);
        }

        if (collector.isDebug()) {
            String filterInfo = isMatch
                ? "passed"
                : createDebugTextForChecks(relativeName, entry, params) + "; hasRelevantChild=" + hasRelevantChild;
            System.out.println(relativeName + ": " + filterInfo);
        }
        return isRelevant;
    }

    private String createDebugTextForChecks(String relativeName, FileTreeEntry entry, TreeParameters params) {
        return "regex=" + params.matchesRegexFilters(entry.getPath(), relativeName)
            + ", size=" + params.matchesSizeFilters(entry)
            + ", itemsInDir=" + params.matchesItemsInDirFilters(entry);
    }

    private void printElement(FileTreeEntry entry, Path root, TreeParameters params) {
        final String nameRelativeToRoot = root.relativize(entry.getPath()).toString();
        final int level = FILE_SEPARATOR_MATCHER.countIn(nameRelativeToRoot);
        final boolean isRoot = nameRelativeToRoot.isEmpty();

        if (params.matchesTypeFilter(entry.getPath()) || isRoot) {
            String indent = (params.isIndentElements() && !isRoot)
                ? Strings.repeat("  ", level) + "- "
                : "";
            if (isRoot) {
                indent = "Folder: ";
            }

            String filename = params.isShowAbsolutePath()
                ? entry.getPath().toAbsolutePath().toString()
                : (params.isIndentElements() ? entry.getPath().getFileName().toString() : nameRelativeToRoot);
            String fileSize = params.isFormatFileSize()
                ? FileSizeUtils.formatToHumanReadableSize(entry.getSize())
                : entry.getSize().toString();

            System.out.println(indent + filename + " (" + fileSize + ")");
        }
    }

    private TreeParameters createParams(boolean forcePrompt) {
        TreeParameters parameters = new TreeParameters();

        // Filter configs
        parameters.setFilePattern(getConfiguredPatternOrNull(TREE_FILE_REGEX, forcePrompt));
        parameters.setDirectoryPattern(getConfiguredPatternOrNull(TREE_DIRECTORY_REGEX, forcePrompt));
        parameters.setMinSizeBytes(getConfiguredNumberOfBytesOrNull(TREE_FILE_MIN_SIZE_MB, forcePrompt));
        parameters.setMaxSizeBytes(getConfiguredNumberOfBytesOrNull(TREE_FILE_MAX_SIZE_MB, forcePrompt));
        parameters.setMinItemsInDir(getConfiguredIntOrNullIfNegative(TREE_MIN_ITEMS_IN_FOLDER, forcePrompt));
        parameters.setMaxItemsInDir(getConfiguredIntOrNullIfNegative(TREE_MAX_ITEMS_IN_FOLDER, forcePrompt));

        // Output configs
        parameters.setDisplayMode(configuration.getValue(TREE_OUTPUT_ELEMENT_TYPES, forcePrompt));
        parameters.setSortBySize(configuration.getValue(TREE_SORT_FILES_BY_SIZE, forcePrompt));
        parameters.setFormatFileSize(configuration.getValue(FORMAT_FILE_SIZE, forcePrompt));

        if (!parameters.isSortBySize() || parameters.getDisplayMode() == TreeDisplayMode.ALL) {
            parameters.setIndentElements(configuration.getValue(TREE_INDENT_ELEMENTS, forcePrompt));
        }
        parameters.setShowAbsolutePath(configuration.getValue(TREE_SHOW_ABSOLUTE_PATH, forcePrompt));

        return parameters;
    }

    @Nullable
    private Long getConfiguredNumberOfBytesOrNull(JfuDoubleProperty megaBytesProperty, boolean forcePrompt) {
        double megabytes = configuration.getValue(megaBytesProperty, forcePrompt);
        if (megabytes < 0.0) {
            return null;
        }
        return FileSizeUtils.megaBytesToBytes(megabytes);
    }

    @Nullable
    private Integer getConfiguredIntOrNullIfNegative(JfuIntegerProperty intProperty, boolean forcePrompt) {
        int value = configuration.getValue(intProperty, forcePrompt);
        return value < 0 ? null : value;
    }

    @Nullable
    private Pattern getConfiguredPatternOrNull(JfuRegexProperty patternProperty,
                                               boolean forcePrompt) {
        Pattern pattern = configuration.getValue(patternProperty, forcePrompt);
        return pattern.pattern().isEmpty() ? null : pattern;
    }

    /**
     * Helper to save relevant entries in any order but to get them in original order at the end.
     */
    private static final class RelevantFileEntryCollector {

        private final Path root;
        private final boolean isDebug;
        private final Map<Path, Integer> orderByEntry = new HashMap<>();
        private final Set<FileTreeEntry> relevantEntries = new HashSet<>();
        private int currentPosition = 0;

        /**
         * Constructor. Registers and adds the given entry as a relevant entry (the root is always deemed relevant).
         *
         * @param rootEntry the root entry
         * @param isDebug defines whether we should log debug output
         */
        RelevantFileEntryCollector(FileTreeEntry rootEntry, boolean isDebug) {
            this.root = rootEntry.getPath();
            this.isDebug = isDebug;
            registerEntry(rootEntry);
            addRelevantEntry(rootEntry);
        }

        boolean isDebug() {
            return isDebug;
        }

        /**
         * Called for <b>every</b> entry as soon as it's encountered as to know about its encounter order.
         *
         * @param entry the entry to register
         */
        void registerEntry(FileTreeEntry entry) {
            orderByEntry.put(entry.getPath(), currentPosition);
            ++currentPosition;
        }

        /**
         * Returns the path's file name relative to the root file of this collector.
         *
         * @param path the path to relativize
         * @return file name relative to the root
         */
        String getNameRelativeToRoot(Path path) {
            return root.relativize(path).toString();
        }

        /**
         * Adds the given entry to this collector as a relevant entry.
         *
         * @param entry the relevant entry to add
         */
        void addRelevantEntry(FileTreeEntry entry) {
            relevantEntries.add(entry);
        }

        /**
         * Returns all relevant entries by original encounter order.
         *
         * @param params tree parameters to sort by
         * @return relevant entries (sorted)
         */
        List<FileTreeEntry> getRelevantEntriesSorted(TreeParameters params) {
            if (params.isSortBySize() && params.getDisplayMode() == TreeDisplayMode.ALL) {
                return sortByHierarchyAndSize();
            }

            Comparator<FileTreeEntry> treeEntryComparator = params.isSortBySize()
                ? Comparator.comparing(FileTreeEntry::getSize)
                : Comparator.comparing(entry -> orderByEntry.get(entry.getPath()));

            return relevantEntries.stream()
                .sorted(treeEntryComparator)
                .collect(Collectors.toUnmodifiableList());
        }

        List<FileTreeEntry> sortByHierarchyAndSize() {
            FileTreeEntry rootEntry = findRootTreeEntry();
            return streamThroughEntryAndChildrenSortedBySize(rootEntry)
                .collect(Collectors.toUnmodifiableList());
        }

        private Stream<FileTreeEntry> streamThroughEntryAndChildrenSortedBySize(FileTreeEntry parent) {
            Stream<FileTreeEntry> selfStream = Stream.of(parent);

            Stream<FileTreeEntry> relevantSortedChildren = parent.getChildren().stream()
                .filter(relevantEntries::contains)
                .sorted(Comparator.comparing(FileTreeEntry::getSize))
                .flatMap(this::streamThroughEntryAndChildrenSortedBySize);

            return Stream.concat(selfStream, relevantSortedChildren);
        }

        private FileTreeEntry findRootTreeEntry() {
            for (FileTreeEntry entry : relevantEntries) {
                if (entry.getPath().equals(root)) {
                    return entry;
                }
            }

            throw new IllegalStateException("Could not find root entry");
        }
    }
}
