package ch.jalu.fileduplicatefinder;

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

    public static void main(String... args) throws IOException {
        String folder;
        if (args != null && args.length > 0) {
            folder = args[0];
        } else {
            folder = "";
        }

        Path path = Paths.get(folder);
        System.out.println("Processing '" + path.toAbsolutePath() + "'");
        Set<Map.Entry<String, Collection<String>>> duplicates = processPath(path);

        if (duplicates.isEmpty()) {
            System.out.println("No duplicates found.");
        } else {
            duplicates.forEach(entry ->
                System.out.println(entry.getKey() + ": " + String.join(", ", entry.getValue())));
        }
    }

    private static Set<Map.Entry<String, Collection<String>>> processPath(Path path) throws IOException {
        FileDuplicateFinder fileDuplicateFinder = new FileDuplicateFinder(path, new FileHasher());
        fileDuplicateFinder.processFiles();
        return fileDuplicateFinder.returnDuplicates();
    }
}
