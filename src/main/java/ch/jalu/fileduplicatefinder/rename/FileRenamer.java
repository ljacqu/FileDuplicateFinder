package ch.jalu.fileduplicatefinder.rename;

import ch.jalu.fileduplicatefinder.output.WriterReader;
import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class FileRenamer {

    private final Path folder;
    private final WriterReader logger;
    private Map<String, String> renamings;

    public FileRenamer(Path folder, WriterReader logger) {
        this.folder = checkNotNull(folder, "folder");
        this.logger = logger;
    }

    protected void renameFileAndOutputResult(String sourceToTargetTxt, Path source, Path target) {
        if (!Files.isRegularFile(source)) {
            logger.printError("Skip " + sourceToTargetTxt + ": not a file");
        } else if (Files.exists(target)) {
            logger.printError("Skip " + sourceToTargetTxt + ": new name already exists");
        } else {
            try {
                Files.move(source, target);
                logger.printLn(sourceToTargetTxt);
            } catch (IOException e) {
                logger.printError("Skip " + sourceToTargetTxt + ": Threw ["
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
            logger.printError("Expected preview to be run first");
            return;
        } else if (renamings.isEmpty()) {
            logger.printLn("Nothing to rename!");
            return;
        }

        renamings.forEach((sourceName, targetName) -> {
            Path source = folder.resolve(sourceName);
            Path target = folder.resolve(targetName);
            renameFileAndOutputResult(sourceName + " -> " + targetName, source, target);
        });
        logger.printLn("Processed all files.");

        renamings = null;
    }

}
