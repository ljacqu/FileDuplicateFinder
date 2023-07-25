package ch.jalu.fileduplicatefinder.rename;

import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class FileRenamer {

    private final Path folder;
    private Map<String, String> renamings;

    public FileRenamer(Path folder) {
        this.folder = checkNotNull(folder, "folder");
    }

    protected static void renameFileAndOutputResult(String sourceToTargetTxt, Path source, Path target) {
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

    protected Stream<Path> streamFiles() {
        return PathUtils.list(folder)
            .filter(Files::isRegularFile);
    }

    protected void setRenamings(Map<String, String> renamings) {
        this.renamings = renamings;
    }

    public void performRenamings() {
        if (renamings == null) {
            System.err.println("Expected preview to be run first");
            return;
        } else if (renamings.isEmpty()) {
            System.out.println("Nothing to rename!");
            return;
        }

        renamings.forEach((sourceName, targetName) -> {
            Path source = folder.resolve(sourceName);
            Path target = folder.resolve(targetName);
            renameFileAndOutputResult(sourceName + " -> " + targetName, source, target);
        });
        System.out.println("Processed all files.");

        renamings = null;
    }

}
