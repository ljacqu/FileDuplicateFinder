package ch.jalu.fileduplicatefinder;

import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.duplicatefinder.DuplicateEntry;
import ch.jalu.fileduplicatefinder.duplicatefinder.FileDuplicateFinder;
import ch.jalu.fileduplicatefinder.filefilter.ConfigurableFilePathMatcher;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import com.google.common.base.Preconditions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class with main method.
 */
public class FileDuplicateFinderRunner {

    private final FileDupeFinderConfiguration configuration;
    private final FileHasherFactory fileHasherFactory;
    private final long start = System.currentTimeMillis();

    private FileDuplicateFinderRunner(FileDupeFinderConfiguration configuration,
                                      FileHasherFactory fileHasherFactory) {
        this.configuration = configuration;
        this.fileHasherFactory = fileHasherFactory;
    }

    public static void main(String... args) {
        Path userConfig = null;
        if (args != null && args.length > 0) {
            userConfig = Paths.get(args[0]);
        }

        FileDuplicateFinderRunner runner =
            new FileDuplicateFinderRunner(new FileDupeFinderConfiguration(userConfig), new FileHasherFactory());
        runner.execute();
    }

    private void execute() {
        Path path = Paths.get(configuration.getRootFolder());
        System.out.println("Processing '" + path.toAbsolutePath() + "'");
        Preconditions.checkArgument(Files.exists(path),
            "Path '" + path.toAbsolutePath() + "' does not exist");
        Preconditions.checkArgument(Files.isDirectory(path),
            "Path '" + path.toAbsolutePath() + "' is not a directory");

        String hashAlgorithm = configuration.getHashAlgorithm();
        FileHasher fileHasher = fileHasherFactory.createFileHasher(hashAlgorithm);

        PathMatcher pathMatcher = new ConfigurableFilePathMatcher(configuration);

        List<DuplicateEntry> duplicates = findDuplicates(path, fileHasher, pathMatcher);
        System.out.println();
        if (duplicates.isEmpty()) {
            System.out.println("No duplicates found.");
        } else if (configuration.isDuplicatesOutputEnabled()) {
            duplicates.forEach(entry -> {
                String files = entry.getPaths().stream()
                    .map(Path::toString)
                    .sorted()
                    .collect(Collectors.joining(", "));
                System.out.println(entry.getHash() + ": " + files);
            });
            System.out.println("Total: " + duplicates.size() + " duplicates");
        }

        System.out.println("Took " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }

    private List<DuplicateEntry> findDuplicates(Path path, FileHasher fileHasher, PathMatcher pathMatcher) {
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
