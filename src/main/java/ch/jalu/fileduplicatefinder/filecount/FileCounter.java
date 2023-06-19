package ch.jalu.fileduplicatefinder.filecount;

import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class FileCounter {

    public static final String NO_EXTENSION_TEXT = "File";

    private final Path folder;

    public FileCounter(Path folder) {
        this.folder = checkNotNull(folder);
    }

    public Map<String, FileCountEntry> gatherExtensionCount() {
        Map<String, FileExtensionCount> countByExtension = new HashMap<>();
        countExtensions(folder, countByExtension);
        return (Map) countByExtension;
    }

    private void countExtensions(Path folder, Map<String, FileExtensionCount> countByExtension) {
        PathUtils.list(folder).forEach(element -> {
            if (Files.isDirectory(element)) {
                countExtensions(element, countByExtension);
            } else if (Files.isRegularFile(element)) {
                String extension = getExtension(element);
                FileExtensionCount count = countByExtension.computeIfAbsent(extension, FileExtensionCount::new);
                count.add(element);
            }
        });
    }

    private static String getExtension(Path path) {
        String filename = path.getFileName().toString();
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "File";
        }
        return "." + filename.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
