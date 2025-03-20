package ch.jalu.fileduplicatefinder.folderdiff;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import ch.jalu.fileduplicatefinder.output.WriterReader;
import ch.jalu.fileduplicatefinder.utils.ConsoleProgressListener;
import ch.jalu.fileduplicatefinder.utils.PathUtils;
import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DIFF_FILES_PROCESSED_INTERVAL;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DIFF_FOLDER1;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DIFF_FOLDER2;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DIFF_USE_SMART_FOLDER_PREFIXES;
import static com.google.common.base.MoreObjects.firstNonNull;

public class FolderDiffRunner {

    public static final String ID = "diff";

    private final FileUtilConfiguration configuration;
    private final FileHasherFactory fileHasherFactory;
    private final WriterReader logger;

    public FolderDiffRunner(FileUtilConfiguration configuration, FileHasherFactory fileHasherFactory,
                            WriterReader logger) {
        this.configuration = configuration;
        this.fileHasherFactory = fileHasherFactory;
        this.logger = logger;
    }

    public void run() {
        Path folder1 = configuration.getValueOrPrompt(DIFF_FOLDER1);
        Path folder2 = configuration.getValueOrPrompt(DIFF_FOLDER2);

        int notificationInterval = configuration.getValue(DIFF_FILES_PROCESSED_INTERVAL);
        List<FileDifference> differences = new FolderDiffAnalyzer(folder1, folder2, configuration, fileHasherFactory)
            .collectDifferences(new ProgressUpdater(notificationInterval));

        System.out.println();
        System.out.println();
        outputDifferences(folder1, folder2, differences);
    }

    private void outputDifferences(Path folder1, Path folder2, List<FileDifference> differences) {
        outputTotal(differences);

        String folder1Prefix;
        String folder2Prefix;
        if (configuration.getValue(DIFF_USE_SMART_FOLDER_PREFIXES)) {
            String[] prefixes = getPrefixesForFolders(folder1, folder2);
            folder1Prefix = prefixes[0];
            folder2Prefix = prefixes[1];
        } else {
            folder1Prefix = "folder1" + File.separator;
            folder2Prefix = "folder2" + File.separator;
        }

        differences.stream()
            .sorted(Comparator.comparing(FileDifference::getSortCodeForDiffType))
            .forEach(diff -> {
                if (diff.getFolder1Element() == null) {
                    logger.printLn("+ " + folder2Prefix + diff.getFolder2Element());
                } else if (diff.getFolder2Element() == null) {
                    logger.printLn("- " + folder1Prefix + diff.getFolder1Element());
                } else {
                    String actionPrefix = diff.wasModified() ? "*" : ">";
                    logger.printLn(actionPrefix + " " + folder1Prefix + diff.getFolder1Element().getName()
                        + " -> " + folder2Prefix + diff.getFolder2Element().getName());
                }
            });
    }

    /**
     * Outputs a total row with a summary of the differences that were found.
     *
     * @param differences the differences to analyze
     */
    private void outputTotal(List<FileDifference> differences) {
        Map<String, Integer> countByType = new HashMap<>();
        int total = 0;
        for (FileDifference diff : differences) {
            String symbol;
            if (diff.getFolder1Element() == null) {
                symbol = "+";
            } else if (diff.getFolder2Element() == null) {
                symbol = "-";
            } else if (diff.wasModified()) {
                symbol = "*";
            } else {
                symbol = ">";
            }
            countByType.merge(symbol, 1, Integer::sum);
            ++total;
        }

        if (total == 0) {
            logger.printLn("Did not find any differences! Both folders match perfectly.");
        } else {
            List<String> descriptions = List.of(
                countByType.get("+") + " additions (+)",
                countByType.get("-") + " removals (-)",
                countByType.get("*") + " modifications (*)",
                countByType.get(">") + " renamings (>)");

            logger.printLn("Found " + total + " changes: " +
                descriptions.stream().filter(count -> !count.startsWith("null")).collect(Collectors.joining(", ")));
        }
    }

    /**
     * Returns an array with two elements that should be used as textual representation of the two folders that
     * were chosen. This method attempts to find the most relevant path elements of the folders that differ from each
     * other. If nothing can be found, "folder1" and "folder2" are returned.
     *
     * @param folder1 the folder that was diffed
     * @param folder2 second folder the first folder was diffed with
     * @return array with two elements for the two folders
     */
    @VisibleForTesting
    String[] getPrefixesForFolders(Path folder1, Path folder2) {
        String separator = File.separator;
        String folder1Name = folder1.getFileName().toString();
        String folder2Name = folder2.getFileName().toString();
        if (!folder1Name.equals(folder2Name)) {
            return new String[]{ folder1Name + separator, folder2Name + separator };
        }

        String folder1Parent = getParentName(folder1);
        String folder2Parent = getParentName(folder2);
        if (folder1Parent != null && folder2Parent != null && !folder1Parent.equals(folder2Parent)) {
            return new String[]{ folder1Parent + "…" + separator, folder2Parent + "…" + separator };
        }

        String folder1Root = PathUtils.toStringNullSafe(folder1.getRoot());
        String folder2Root = PathUtils.toStringNullSafe(folder2.getRoot());
        if (folder1Root != null && folder2Root != null
                && !folder1Root.startsWith(".") && !folder1Root.equals(folder2Root)) {
            return new String[]{ folder1Root + "…" + separator, folder2Root + "…" + separator };
        }
        return new String[]{ "folder1" + separator, "folder2" + separator };
    }

    @Nullable
    private String getParentName(Path path) {
        Path parent = path.getParent();
        if (parent != null) {
            Path parentFileName = parent.getFileName(); // null for Windows drives, e.g. for Paths.get("C:/")
            return firstNonNull(parentFileName, parent).toString();
        }
        return null;
    }

    /**
     * Logs the progress of the folder diff to the console.
     */
    private final class ProgressUpdater implements FolderDiffProgressCallback {

        private final ConsoleProgressListener scanProgressListener;
        private final ConsoleProgressListener analysisProgressListener;

        ProgressUpdater(int notificationInterval) {
            this.scanProgressListener = new ConsoleProgressListener(notificationInterval);
            this.analysisProgressListener = new ConsoleProgressListener(notificationInterval);
        }

        @Override
        public void startScan() {
            logger.print("Scanning files:  ");
        }

        @Override
        public void notifyScanProgress() {
            scanProgressListener.notifyItemProcessed();
        }

        @Override
        public void startAnalysis() {
            logger.printLn("");
            logger.print("Comparing files: ");
        }

        @Override
        public void notifyAnalysisProgress() {
            analysisProgressListener.notifyItemProcessed();
        }
    }
}
