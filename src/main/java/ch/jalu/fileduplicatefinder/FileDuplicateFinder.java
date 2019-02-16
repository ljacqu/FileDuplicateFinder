package ch.jalu.fileduplicatefinder;

import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FileDuplicateFinder {

    private final Path rootFolder;
    private final FileHasher fileHasher;

    private final Multimap<String, String> filesByHash = HashMultimap.create();

    public FileDuplicateFinder(Path rootFolder, FileHasher fileHasher) {
        this.rootFolder = rootFolder;
        this.fileHasher = fileHasher;
    }

    public void processFiles() throws IOException {
        processPath(rootFolder);
        System.out.println("Finished after processing " + filesByHash.size() + " files");
    }

    private void processPath(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            for (Path child : Files.list(path).collect(Collectors.toList())) {
                processPath(child);
            }
        } else {
            String hash = fileHasher.calculateHash(path);
            filesByHash.put(hash, path.toString());
            if ((filesByHash.size() & 511) == 0) {
                System.out.println("Processed " + filesByHash.size() + " files");
            }
        }
    }

    public Set<Map.Entry<String, Collection<String>>> returnDuplicates() {
        return filesByHash.asMap().entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(Collectors.toSet());
    }
}
