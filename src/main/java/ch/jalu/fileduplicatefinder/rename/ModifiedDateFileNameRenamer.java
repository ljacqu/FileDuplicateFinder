package ch.jalu.fileduplicatefinder.rename;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ModifiedDateFileNameRenamer extends FileRenamer {

    private final String replacementPattern;
    private final DateTimeFormatter dateFormatter;
    private Map<String, String> renamings;

    public ModifiedDateFileNameRenamer(Path folder, String replacementPattern, DateTimeFormatter dateFormatter) {
        super(folder);
        this.replacementPattern = replacementPattern;
        this.dateFormatter = dateFormatter;
    }

    public Map<String, String> generateRenamingsPreview() {
        renamings = new HashMap<>();
        streamFiles()
            .forEach(file -> {
                LocalDateTime modifiedDate = getLastModifiedDate(file);
                String formattedDate = dateFormatter.format(modifiedDate);
                String filename = file.getFileName().toString();
                String newName = replacementPattern
                    .replace("{file}", filename)
                    .replace("{date}", formattedDate)
                    .replace("{name}", com.google.common.io.Files.getNameWithoutExtension(filename))
                    .replace("{ext}", com.google.common.io.Files.getFileExtension(filename));
                renamings.put(filename, newName);
            });
        return renamings;
    }

    @Override
    public Map<String, String> getRenamings() {
        return renamings;
    }

    private static LocalDateTime getLastModifiedDate(Path file) {
        try {
            Instant instant = Files.getLastModifiedTime(file).toInstant();
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to get last modified date for " + file, e);
        }
    }
}
