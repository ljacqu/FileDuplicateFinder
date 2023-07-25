package ch.jalu.fileduplicatefinder.rename;

import ch.jalu.fileduplicatefinder.utils.PathUtils;
import com.google.common.io.MoreFiles;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ModifiedDateFileNameRenamer extends FileRenamer {

    public ModifiedDateFileNameRenamer(Path folder) {
        super(folder);
    }

    public Map<String, String> generateRenamingsPreview(String replacementPattern, DateTimeFormatter dateFormatter) {
        Map<String, String> renamings = new HashMap<>();
        streamFiles()
            .forEach(file -> {
                LocalDateTime modifiedDate = getLastModifiedDate(file);
                String formattedDate = dateFormatter.format(modifiedDate);
                String filename = file.getFileName().toString();
                String newName = replacementPattern
                    .replace("{file}", filename)
                    .replace("{date}", formattedDate)
                    .replace("{name}", MoreFiles.getNameWithoutExtension(file))
                    .replace("{ext}", MoreFiles.getFileExtension(file));
                renamings.put(filename, newName);
            });
        setRenamings(renamings);
        return renamings;
    }

    private static LocalDateTime getLastModifiedDate(Path file) {
        Instant instant = PathUtils.getLastModifiedTime(file).toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
