package ch.jalu.fileduplicatefinder;

import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.duplicatefinder.DuplicateEntry;
import ch.jalu.fileduplicatefinder.duplicatefinder.FileDuplicateFinder;
import ch.jalu.fileduplicatefinder.filefilter.ConfigurableFilePathMatcher;
import ch.jalu.fileduplicatefinder.filefilter.FilePathMatcher;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import ch.jalu.fileduplicatefinder.output.ConsoleResultOutputter;
import ch.jalu.fileduplicatefinder.output.DuplicateEntryOutputter;
import com.google.common.base.Preconditions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Class with main method.
 */
public class FileDuplicateFinderRunner {

    private final FileDupeFinderConfiguration configuration;
    private final FileHasherFactory fileHasherFactory;
    private final DuplicateEntryOutputter entryOutputter;
    private final long start = System.currentTimeMillis();

    private FileDuplicateFinderRunner(FileDupeFinderConfiguration configuration,
                                      FileHasherFactory fileHasherFactory,
                                      DuplicateEntryOutputter entryOutputter) {
        this.configuration = configuration;
        this.fileHasherFactory = fileHasherFactory;
        this.entryOutputter = entryOutputter;
    }

    public static void main(String... args) {
        Path userConfig = null;
        if (args != null && args.length > 0) {
            userConfig = Paths.get(args[0]);
        }

        FileDupeFinderConfiguration configuration = new FileDupeFinderConfiguration(userConfig);
        FileDuplicateFinderRunner runner = new FileDuplicateFinderRunner(
            configuration, new FileHasherFactory(), new ConsoleResultOutputter(configuration));
        runner.execute();
    }

    private void execute() {
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

        System.out.println("Took " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
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
}
