package ch.jalu.fileduplicatefinder.rename;

import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class FileRenamer {

    private final Path folder;
    private final Pattern pattern;
    private final String replacement;
    private Map<String, String> renamingPreview;

    public FileRenamer(Path folder, Pattern pattern, String replacement) {
        this.folder = checkNotNull(folder, "folder");
        this.pattern = checkNotNull(pattern, "pattern");
        this.replacement = checkNotNull(replacement, "replacement");
    }

    public Map<String, String> generateRenamingsPreview() {
        renamingPreview = new LinkedHashMap<>();
        PathUtils.list(folder)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                String fileName = file.getFileName().toString();
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    renamingPreview.put(fileName, matcher.replaceAll(replacement));
                }
            });
        return renamingPreview;
    }

    public void performRenamings() {
        if (renamingPreview == null) {
            throw new IllegalStateException("Expected preview to be run first");
        } else if (renamingPreview.isEmpty()) {
            System.out.println("Nothing to rename!");
            return;
        }

        renamingPreview.forEach((sourceName, targetName) -> {
            Path source = folder.resolve(sourceName);
            Path target = folder.resolve(targetName);
            renameFileAndOutputResult(sourceName + " -> " + targetName, source, target);
        });
        System.out.println("Processed all files.");
        renamingPreview = null;
    }

    private static void renameFileAndOutputResult(String sourceToTargetTxt, Path source, Path target) {
        if (!Files.isRegularFile(source)) {
            System.err.println("Skip " + sourceToTargetTxt + ": not a file");
        } else if (Files.exists(target)) {
            System.err.println("Skip " + sourceToTargetTxt + ": new name already exists");
        } else {
            try {
                Files.move(source, target);
                System.out.println(sourceToTargetTxt);
            } catch (IOException e) {
                System.err.println("Skip " + sourceToTargetTxt + ": Threw ["
                    + e.getClass().getSimpleName() + "]: " + e.getMessage());
            }
        }
    }
}
