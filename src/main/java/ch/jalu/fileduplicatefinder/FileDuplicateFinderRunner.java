package ch.jalu.fileduplicatefinder;

import ch.jalu.fileduplicatefinder.config.ConfigFileWriter;
import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.duplicatefinder.DuplicateEntry;
import ch.jalu.fileduplicatefinder.duplicatefinder.FileDuplicateFinder;
import ch.jalu.fileduplicatefinder.duplicatefinder.FolderPair;
import ch.jalu.fileduplicatefinder.duplicatefinder.FolderPairDuplicatesCounter;
import ch.jalu.fileduplicatefinder.filefilter.ConfigurableFilePathMatcher;
import ch.jalu.fileduplicatefinder.filefilter.FilePathMatcher;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import ch.jalu.fileduplicatefinder.output.ConsoleResultOutputter;
import ch.jalu.fileduplicatefinder.output.DuplicateEntryOutputter;
import ch.jalu.fileduplicatefinder.rename.FileRenamer;
import ch.jalu.fileduplicatefinder.rename.ModifiedDateFileNameRenamer;
import ch.jalu.fileduplicatefinder.rename.RegexFileRenamer;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Class with main method.
 */
public class FileDuplicateFinderRunner {

    private final FileDupeFinderConfiguration configuration;
    private final FileHasherFactory fileHasherFactory;
    private final DuplicateEntryOutputter entryOutputter;
    private final FolderPairDuplicatesCounter folderPairDuplicatesCounter;
    private final long start = System.currentTimeMillis();

    FileDuplicateFinderRunner(FileDupeFinderConfiguration configuration,
                              FileHasherFactory fileHasherFactory,
                              DuplicateEntryOutputter entryOutputter,
                              FolderPairDuplicatesCounter folderPairDuplicatesCounter) {
        this.configuration = configuration;
        this.fileHasherFactory = fileHasherFactory;
        this.entryOutputter = entryOutputter;
        this.folderPairDuplicatesCounter = folderPairDuplicatesCounter;
    }

    public static void main(String... args) {
        if (System.getProperty("createConfig") != null) {
            createConfigFile();
            return;
        }

        if (System.getProperty("rename") != null) {
            performRenaming();
            return;
        }

        Path userConfig = null;
        if (args != null && args.length > 0) {
            userConfig = Paths.get(args[0]);
        }

        FileDupeFinderConfiguration configuration = new FileDupeFinderConfiguration(userConfig);
        FileDuplicateFinderRunner runner = new FileDuplicateFinderRunner(
            configuration,
            new FileHasherFactory(),
            new ConsoleResultOutputter(configuration),
            new FolderPairDuplicatesCounter());
        runner.execute();
    }

    void execute() {
        Path path = configuration.getRootFolder();
        System.out.println("Processing '" + path.toAbsolutePath() + "'");
        Preconditions.checkArgument(Files.exists(path),
            "Path '" + path.toAbsolutePath() + "' does not exist");
        Preconditions.checkArgument(Files.isDirectory(path),
            "Path '" + path.toAbsolutePath() + "' is not a directory");

        String hashAlgorithm = configuration.getHashAlgorithm();
        FileHasher fileHasher = fileHasherFactory.createFileHasher(hashAlgorithm);

        FilePathMatcher pathMatcher = new ConfigurableFilePathMatcher(configuration);

        List<DuplicateEntry> duplicates = findDuplicates(path, fileHasher, pathMatcher);
        entryOutputter.outputResult(duplicates);

        if (configuration.isOutputFolderPairCount()) {
            System.out.println();
            System.out.println("Folder duplicates");

            Map<FolderPair, Long> duplicatesByFolderPair = folderPairDuplicatesCounter
                .getFolderToFolderDuplicateCount(duplicates);
            entryOutputter.outputDirectoryPairs(duplicatesByFolderPair);
        }

        System.out.println("Took " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }

    static void performRenaming() {
        String type = System.getProperty("type");
        RenameType renameType;
        if ("r".equalsIgnoreCase(type) || "regex".equalsIgnoreCase(type)) {
            renameType = RenameType.REGEX;
        } else if ("d".equalsIgnoreCase(type) || "date".equalsIgnoreCase(type)) {
            renameType = RenameType.MODIFIED_DATE;
        } else {
            throw new IllegalStateException("Must supply type=regex or type=date");
        }

        FileRenamer renamer = createRenamer(renameType);

        Map<String, String> replacements = renamer.generateRenamingsPreview();
        if (replacements.isEmpty()) {
            System.out.println("Nothing to rename in " + renamer.getFolder().toAbsolutePath());
            return;
        }
        System.out.println("This will rename " + replacements.size() + " files. Preview:");
        replacements.forEach((source, target) -> System.out.println(" " + source + " -> " + target));
        System.out.println("Confirm [y/n]");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if ("y".equalsIgnoreCase(input)) {
            renamer.performRenamings();
        } else {
            System.out.println("Canceled renaming");
        }
    }

    private List<DuplicateEntry> findDuplicates(Path path, FileHasher fileHasher, FilePathMatcher pathMatcher) {
        FileDuplicateFinder fileDuplicateFinder = new FileDuplicateFinder(path, fileHasher, pathMatcher, configuration);
        fileDuplicateFinder.processFiles();
        if (configuration.isDistributionOutputEnabled()) {
            fileDuplicateFinder.getSizeDistribution().entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> System.out.println(e.getValue() + " file size entries with " + e.getKey() + " files"));
        }
        return fileDuplicateFinder.filterFilesForDuplicates();
    }

    private static void createConfigFile() {
        try {
            Path newConfigFile = new ConfigFileWriter().writeConfigFile("default.properties", "");
            System.out.println("Created configuration file '" + newConfigFile + "'");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static FileRenamer createRenamer(RenameType renameType) {
        String folderProperty = System.getProperty("folder");
        Path folder = folderProperty == null ? Paths.get(".") : Paths.get(folderProperty);

        if (renameType == RenameType.REGEX) {
            Pattern renamePattern = Pattern.compile(System.getProperty("rename"));
            String replacement = System.getProperty("to");
            if (replacement == null) {
                throw new IllegalStateException("Must provide a replacement as 'to' option: "
                    + "java -Drename=\"IMG_E(\\d)\\\\.png\" -Dto=\"IMG_\\$1E.png [-Dfolder ~/images]");
            }
            return new RegexFileRenamer(folder, renamePattern, replacement);

        } else if (renameType == RenameType.MODIFIED_DATE) {
            String replacement = System.getProperty("to");
            DateTimeFormatter dateFormat = Optional.ofNullable(System.getProperty("date"))
                .map(DateTimeFormatter::ofPattern)
                .orElse(DateTimeFormatter.ISO_LOCAL_DATE);
            return new ModifiedDateFileNameRenamer(folder, replacement, dateFormat);
        } else {
            throw new IllegalStateException("Unhandled renaming type: " + renameType);
        }
    }

    private enum RenameType {
        REGEX,

        MODIFIED_DATE

    }
}
