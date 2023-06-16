package ch.jalu.fileduplicatefinder.tree;

import ch.jalu.fileduplicatefinder.configme.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.configme.FileUtilSettings;
import ch.jalu.fileduplicatefinder.utils.FileSizeUtils;
import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

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

    private List<FileTreeEntry> filterRelevantEntries(FileTreeEntry root, TreeParameters params) {
        List<FileTreeEntry> relevantEntries = new ArrayList<>();
        relevantEntries.add(root);

        root.getChildren().forEach(child -> addItems(root.getPath(), child, relevantEntries, params));
        relevantEntries.sort(Comparator.comparing(FileTreeEntry::getPath));
        return relevantEntries;
    }

    private boolean addItems(Path root, FileTreeEntry entry, List<FileTreeEntry> relevantEntries,
                             TreeParameters params) {
        String relativeName = root.relativize(entry.getPath()).toString();
        boolean hasMatch = params.matchesRegex(entry.getPath(), relativeName)
            && params.matchesSizeFilter(entry.getPath());

        for (FileTreeEntry child : entry.getChildren()) {
            hasMatch |= addItems(root, child, relevantEntries, params);
        }
        // todo: If we're configured to be outputting absolute paths, we shouldn't add intermediate parents that didn't match per se
        if (hasMatch) {
            relevantEntries.add(entry);
        }
        return hasMatch;
    }

    private void printElement(FileTreeEntry entry, Path root, TreeParameters params) {
        String nameRelativeToRoot = root.relativize(entry.getPath()).toString();
        int level = FILE_SEPARATOR_MATCHER.countIn(nameRelativeToRoot);

        if (params.matchesTypeFilter(entry.getPath()) || nameRelativeToRoot.isEmpty()) {
            String indent = (params.isIndentElements() && !nameRelativeToRoot.isEmpty())
                ? Strings.repeat("  ", level) + "- "
                : "";
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
        TreeDisplayMode displayMode = configuration.getEnumOrPrompt(TREE_DISPLAY_MODE);

        String fileRegex = configuration.getStringOrPrompt(TREE_FILE_REGEX);
        Pattern filePattern = fileRegex.isEmpty() ? null : Pattern.compile(fileRegex);
        String directoryRegex = configuration.getStringOrPrompt(TREE_DIRECTORY_REGEX);
        Pattern directoryPattern = directoryRegex.isEmpty() ? null : Pattern.compile(directoryRegex);

        boolean formatFileSize = configuration.getBoolean(FileUtilSettings.FORMAT_FILE_SIZE);
        boolean indentElements = configuration.getBoolean(FileUtilSettings.TREE_INDENT_ELEMENTS);
        boolean showAbsolutePath = configuration.getBoolean(FileUtilSettings.TREE_SHOW_ABSOLUTE_PATH);
        Long minSizeBytes =
            convertMbToBytesIfNotNegative(configuration.getDouble(FileUtilSettings.TREE_FILE_MIN_SIZE_MB));
        Long maxSizeBytes =
            convertMbToBytesIfNotNegative(configuration.getDouble(FileUtilSettings.TREE_FILE_MAX_SIZE_MB));

        TreeParameters parameters = new TreeParameters();
        parameters.setDisplayMode(displayMode);
        parameters.setFilePattern(filePattern);
        parameters.setDirectoryPattern(directoryPattern);
        parameters.setMinSizeBytes(minSizeBytes);
        parameters.setMaxSizeBytes(maxSizeBytes);
        parameters.setFormatFileSize(formatFileSize);
        parameters.setIndentElements(indentElements);
        parameters.setShowAbsolutePath(showAbsolutePath);
        return parameters;
    }

    @Nullable
    private static Long convertMbToBytesIfNotNegative(double megabytes) {
        if (megabytes < 0.0) {
            return null;
        }
        return FileSizeUtils.megaBytesToBytes(megabytes);
    }
}
