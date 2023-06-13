package ch.jalu.fileduplicatefinder.folderdiff;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import ch.jalu.fileduplicatefinder.utils.PathUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DIFF_FOLDER1;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DIFF_FOLDER2;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DIFF_USE_SMART_FOLDER_PREFIXES;

public class FolderDiffRunner {

    public static final String ID = "diff";

    private final FileUtilConfiguration configuration;
    private final FileHasherFactory fileHasherFactory;

    public FolderDiffRunner(FileUtilConfiguration configuration, FileHasherFactory fileHasherFactory) {
        this.configuration = configuration;
        this.fileHasherFactory = fileHasherFactory;
    }

    public void run() {
        Path folder1 = configuration.getPath(DIFF_FOLDER1);
        Path folder2 = configuration.getPath(DIFF_FOLDER2);

        System.out.println("Processing files");
        List<FileDifference> differences = new FolderDiffAnalyzer(folder1, folder2, configuration, fileHasherFactory)
            .collectDifferences(this::outputProgress);

        System.out.println();
        outputDifferences(folder1, folder2, differences);
    }

    private void outputDifferences(Path folder1, Path folder2, List<FileDifference> differences) {
        outputTotal(differences);

        String folder1Prefix;
        String folder2Prefix;
        if (configuration.getBoolean(DIFF_USE_SMART_FOLDER_PREFIXES)) {
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
                    System.out.println("+ " + folder2Prefix + diff.getFolder2Element());
                } else if (diff.getFolder2Element() == null) {
                    System.out.println("- " + folder1Prefix + diff.getFolder1Element());
                } else {
                    String actionPrefix = diff.wasModified() ? "*" : ">";
                    System.out.println(actionPrefix + " " + folder1Prefix + diff.getFolder1Element().getName()
                        + " -> " + folder2Prefix + diff.getFolder2Element().getName());
                }
            });
    }

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
            System.out.println("Did not find any differences! Both folders match perfectly.");
        } else {
            List<String> descriptions = List.of(
                countByType.get("+") + " additions (+)",
                countByType.get("-") + " removals (-)",
                countByType.get("*") + " modifications (*)",
                countByType.get(">") + " renamings (>)");

            System.out.println("Found " + total + " changes: " +
                descriptions.stream().filter(count -> !count.startsWith("null")).collect(Collectors.joining(", ")));
        }
    }

    private void outputProgress(long files) {
        System.out.print(". ");
    }

    private String[] getPrefixesForFolders(Path folder1, Path folder2) {
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
            return parent.getFileName().toString();
        }
        return null;
    }
}
