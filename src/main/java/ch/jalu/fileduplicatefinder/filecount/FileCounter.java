package ch.jalu.fileduplicatefinder.filecount;

import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class FileCounter {

    private final Path folder;

    public FileCounter(Path folder) {
        this.folder = checkNotNull(folder);
    }

    public Map<String, FileGroupStatistics> gatherExtensionCount() {
        Map<String, FileGroupStatistics> countByExtension = new HashMap<>();
        countExtensions(folder, countByExtension);
        return countByExtension;
    }

    private void countExtensions(Path folder, Map<String, FileGroupStatistics> countByExtension) {
        PathUtils.list(folder).forEach(element -> {
            if (Files.isDirectory(element)) {
                countExtensions(element, countByExtension);
            } else if (Files.isRegularFile(element)) {
                String extension = getExtension(element);
                FileGroupStatistics statistics = countByExtension.get(extension);
                if (statistics == null) {
                    countByExtension.put(extension, new FileGroupStatistics(element));
                } else {
                    statistics.add(element);
                }

            }
        });
    }

    private static String getExtension(Path path) {
        String filename = path.getFileName().toString();
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "File";
        }
        return filename.substring(lastDotIndex + 1).toUpperCase(Locale.ROOT);
    }
}
