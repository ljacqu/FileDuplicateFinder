package ch.jalu.fileduplicatefinder;

import ch.jalu.fileduplicatefinder.config.ConfigurationReader;
import ch.jalu.fileduplicatefinder.duplicatefinder.DuplicateEntry;
import ch.jalu.fileduplicatefinder.duplicatefinder.FileDuplicateFinder;
import ch.jalu.fileduplicatefinder.filefilter.ConfigurableFilePathMatcher;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class with main method.
 */
public class FileDuplicateFinderRunner {

    private final ConfigurationReader configurationReader;
    private final FileHasherFactory fileHasherFactory;

    private FileDuplicateFinderRunner(ConfigurationReader configurationReader, FileHasherFactory fileHasherFactory) {
        this.configurationReader = configurationReader;
        this.fileHasherFactory = fileHasherFactory;
    }

    public static void main(String... args) throws IOException {
        Path userConfig = null;
        if (args != null && args.length > 0) {
            userConfig = Paths.get(args[0]);
        }

        FileDuplicateFinderRunner runner =
            new FileDuplicateFinderRunner(new ConfigurationReader(userConfig), new FileHasherFactory());
        runner.execute();
    }

    private void execute() throws IOException {
        Path path = Paths.get(configurationReader.getRootFolder());
        System.out.println("Processing '" + path.toAbsolutePath() + "'");
        Preconditions.checkArgument(Files.exists(path),
            "Path '" + path.toAbsolutePath() + "' does not exist");
        Preconditions.checkArgument(Files.isDirectory(path),
            "Path '" + path.toAbsolutePath() + "' is not a directory");

        String hashAlgorithm = configurationReader.getHashAlgorithm();
        System.out.println("Using hash algorithm '" + hashAlgorithm + "'");
        FileHasher fileHasher = fileHasherFactory.createFileHasher(hashAlgorithm,
            configurationReader.getMaxSizeForHashingInMb());

        PathMatcher pathMatcher = new ConfigurableFilePathMatcher(configurationReader);

        List<DuplicateEntry> duplicates = processPath(path, fileHasher, pathMatcher);
        if (duplicates.isEmpty()) {
            System.out.println("No duplicates found.");
        } else {
            duplicates.forEach(entry -> {
                String files = entry.getPaths().stream()
                    .map(Path::toString)
                    .sorted()
                    .collect(Collectors.joining(", "));
                System.out.println(entry.getHash() + ": " + files);
            });
        }
    }

    private static List<DuplicateEntry> processPath(Path path, FileHasher fileHasher,
                                                    PathMatcher pathMatcher) throws IOException {
        FileDuplicateFinder fileDuplicateFinder = new FileDuplicateFinder(path, fileHasher, pathMatcher);
        fileDuplicateFinder.processFiles();
        fileDuplicateFinder.getSizeDistribution().forEach((k, v) -> {
            System.out.println(v + " groups with " + k + " entry");
        });
        return fileDuplicateFinder.returnDuplicates();
    }
}
