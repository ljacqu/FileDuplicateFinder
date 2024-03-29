package ch.jalu.fileduplicatefinder.duplicatefinder;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.duplicatefinder.output.DuplicateEntryOutputter;
import ch.jalu.fileduplicatefinder.filefilter.ConfigurableFilePathMatcher;
import ch.jalu.fileduplicatefinder.filefilter.FilePathMatcher;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import ch.jalu.fileduplicatefinder.hashing.HashingAlgorithm;
import ch.jalu.fileduplicatefinder.output.TaskWriterReader;
import com.google.common.base.Preconditions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FOLDER;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_HASH_ALGORITHM;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_DISTRIBUTION;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT;

public class FileDuplicateRunner {

    public static final String ID = "duplicates";

    private final FileUtilConfiguration configuration;
    private final FileHasherFactory fileHasherFactory;
    private final FolderPairDuplicatesCounter folderPairDuplicatesCounter;
    private final DuplicateEntryOutputter entryOutputter;
    private final TaskWriterReader logger;
    private final long start = System.currentTimeMillis();

    public FileDuplicateRunner(FileUtilConfiguration configuration, FileHasherFactory fileHasherFactory,
                               FolderPairDuplicatesCounter folderPairDuplicatesCounter,
                               DuplicateEntryOutputter entryOutputter, TaskWriterReader logger) {
        this.configuration = configuration;
        this.fileHasherFactory = fileHasherFactory;
        this.folderPairDuplicatesCounter = folderPairDuplicatesCounter;
        this.entryOutputter = entryOutputter;
        this.logger = logger;
    }

    public void run() {
        Path path = configuration.getValueOrPrompt(DUPLICATE_FOLDER);
        logger.printLn("Processing '" + path.toAbsolutePath() + "'");
        Preconditions.checkArgument(Files.exists(path),
            "Path '" + path.toAbsolutePath() + "' does not exist");
        Preconditions.checkArgument(Files.isDirectory(path),
            "Path '" + path.toAbsolutePath() + "' is not a directory");

        HashingAlgorithm hashAlgorithm = configuration.getValue(DUPLICATE_HASH_ALGORITHM);
        FileHasher fileHasher = fileHasherFactory.createFileHasher(hashAlgorithm);

        FilePathMatcher pathMatcher = new ConfigurableFilePathMatcher(configuration);

        List<DuplicateEntry> duplicates = findDuplicates(path, fileHasher, pathMatcher);
        entryOutputter.outputResult(duplicates);

        if (configuration.getValue(DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT)) {
            logger.printNewLine();
            logger.printLn("Folder duplicates");

            Map<FolderPair, Long> duplicatesByFolderPair = folderPairDuplicatesCounter
                .getFolderToFolderDuplicateCount(duplicates);
            entryOutputter.outputDirectoryPairs(duplicatesByFolderPair);
        }

        logger.printLn("Took " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }

    private List<DuplicateEntry> findDuplicates(Path path, FileHasher fileHasher, FilePathMatcher pathMatcher) {
        FileDuplicateFinder fileDuplicateFinder =
            new FileDuplicateFinder(path, fileHasher, pathMatcher, configuration, logger);
        fileDuplicateFinder.processFiles();
        if (configuration.getValue(DUPLICATE_OUTPUT_DISTRIBUTION)) {
            fileDuplicateFinder.getSizeDistribution().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> logger.printLn(e.getValue() + " file size entries with " + e.getKey() + " files"));
        }
        return fileDuplicateFinder.filterFilesForDuplicates();
    }
}
