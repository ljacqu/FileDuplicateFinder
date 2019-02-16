package ch.jalu.fileduplicatefinder;

import ch.jalu.fileduplicatefinder.config.ConfigurationReader;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

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

        String hashAlgorithm = configurationReader.getHashAlgorithm();
        System.out.println("Using hash algorithm '" + hashAlgorithm + "'");
        FileHasher fileHasher = fileHasherFactory.createFileHasher(hashAlgorithm,
            configurationReader.getMaxSizeForHashingInMb());

        Set<Map.Entry<String, Collection<String>>> duplicates = processPath(path, fileHasher);
        if (duplicates.isEmpty()) {
            System.out.println("No duplicates found.");
        } else {
            duplicates.forEach(entry ->
                System.out.println(entry.getKey() + ": " + String.join(", ", entry.getValue())));
        }
    }

    private static Set<Map.Entry<String, Collection<String>>> processPath(Path path,
                                                                          FileHasher fileHasher) throws IOException {
        FileDuplicateFinder fileDuplicateFinder = new FileDuplicateFinder(path, fileHasher);
        fileDuplicateFinder.processFiles();
        return fileDuplicateFinder.returnDuplicates();
    }
}
