package ch.jalu.fileduplicatefinder.tree;

import ch.jalu.configme.properties.Property;
import ch.jalu.fileduplicatefinder.configme.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.configme.FileUtilSettings;
import ch.jalu.fileduplicatefinder.configme.property.FuDoubleProperty;
import ch.jalu.fileduplicatefinder.configme.property.FuIntegerProperty;
import ch.jalu.fileduplicatefinder.utils.FileSizeUtils;
import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.TREE_DIRECTORY_REGEX;
import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.TREE_DISPLAY_MODE;
import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.TREE_FILE_REGEX;
import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.TREE_FOLDER;

/**
 * Outputs a file tree with configurable filters.
 */
public class FileTreeRunner {

    public static final String ID = "tree";

    private static final CharMatcher FILE_SEPARATOR_MATCHER = CharMatcher.is(File.separatorChar);

    private final FileUtilConfiguration configuration;

    public FileTreeRunner(FileUtilConfiguration configuration) {
        this.configuration = configuration;
    }

    public void run() {
        Path folder = configuration.getPathOrPrompt(TREE_FOLDER);
        FileTreeGenerator fileTreeGenerator = new FileTreeGenerator(folder);

        TreeParameters params = createParams();
        FileTreeEntry treeRoot = fileTreeGenerator.generateTree();
        System.out.println("Collected all elements");

        filterRelevantEntries(treeRoot, params).forEach(elem -> printElement(elem, folder, params));
    }

    private Stream<FileTreeEntry> filterRelevantEntries(FileTreeEntry root, TreeParameters params) {
        RelevantFileEntryCollector collector = new RelevantFileEntryCollector(root);

        for (FileTreeEntry child : root.getChildren()) {
            addEntryAndChildrenToListIfRelevantRecursively(collector, child, params);
        }
        return collector.getRelevantEntriesSorted();
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
        return isRelevant;
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

    private TreeParameters createParams() {
        TreeParameters parameters = new TreeParameters();
        parameters.setDisplayMode(configuration.getEnumOrPrompt(TREE_DISPLAY_MODE));
        parameters.setFilePattern(getConfiguredPatternOrNull(TREE_FILE_REGEX));
        parameters.setDirectoryPattern(getConfiguredPatternOrNull(TREE_DIRECTORY_REGEX));
        parameters.setMinSizeBytes(getConfiguredNumberOfBytesOrNull(FileUtilSettings.TREE_FILE_MIN_SIZE_MB));
        parameters.setMaxSizeBytes(getConfiguredNumberOfBytesOrNull(FileUtilSettings.TREE_FILE_MAX_SIZE_MB));
        parameters.setMinItemsInDir(getConfiguredIntOrNullIfNegative(FileUtilSettings.TREE_MIN_ITEMS_IN_FOLDER));
        parameters.setMaxItemsInDir(getConfiguredIntOrNullIfNegative(FileUtilSettings.TREE_MAX_ITEMS_IN_FOLDER));
        parameters.setMaxSizeBytes(getConfiguredNumberOfBytesOrNull(FileUtilSettings.TREE_FILE_MAX_SIZE_MB));
        parameters.setFormatFileSize(configuration.getBoolean(FileUtilSettings.FORMAT_FILE_SIZE));
        parameters.setIndentElements(configuration.getBoolean(FileUtilSettings.TREE_INDENT_ELEMENTS));
        parameters.setShowAbsolutePath(configuration.getBoolean(FileUtilSettings.TREE_SHOW_ABSOLUTE_PATH));
        return parameters;
    }

    @Nullable
    private Long getConfiguredNumberOfBytesOrNull(FuDoubleProperty megaBytesProperty) {
        double megabytes = configuration.getDouble(megaBytesProperty);
        if (megabytes < 0.0) {
            return null;
        }
        return FileSizeUtils.megaBytesToBytes(megabytes);
    }

    @Nullable
    private Integer getConfiguredIntOrNullIfNegative(FuIntegerProperty intProperty) {
        int value = configuration.getInt(intProperty);
        return value < 0 ? null : value;
    }

    @Nullable
    private Pattern getConfiguredPatternOrNull(Property<Optional<String>> patternProperty) {
        String pattern = configuration.getStringOrPrompt(patternProperty);
        return pattern.isEmpty() ? null : Pattern.compile(pattern);
    }

    /**
     * Helper to save relevant entries in any order but to get them in original order at the end.
     */
    private static final class RelevantFileEntryCollector {

        private final Path root;
        private final Map<FileTreeEntry, Integer> orderByEntry = new HashMap<>();
        private final List<FileTreeEntry> relevantEntries = new ArrayList<>();
        private int currentPosition = 0;

        /**
         * Constructor. Registers and adds the given entry as a relevant entry (the root is always deemed relevant).
         *
         * @param rootEntry the root entry
         */
        RelevantFileEntryCollector(FileTreeEntry rootEntry) {
            this.root = rootEntry.getPath();
            registerEntry(rootEntry);
            addRelevantEntry(rootEntry);
        }

        /**
         * Called for <b>every</b> entry as soon as it's encountered as to know about its encounter order.
         *
         * @param entry the entry to register
         */
        void registerEntry(FileTreeEntry entry) {
            orderByEntry.put(entry, currentPosition);
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
         * @return relevant entries (sorted)
         */
        Stream<FileTreeEntry> getRelevantEntriesSorted() {
            return relevantEntries.stream()
                .sorted(Comparator.comparing(orderByEntry::get));
        }
    }
}
