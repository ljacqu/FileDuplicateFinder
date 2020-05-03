package ch.jalu.fileduplicatefinder.rename;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.RENAME_DATE_DATE_FORMAT;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.RENAME_DATE_TO;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.RENAME_FOLDER;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.RENAME_REGEX_FROM;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.RENAME_REGEX_TO;

public class FileRenameRunner {

    public static final String ID_REGEX = "rename";
    public static final String ID_DATE = "addDate";

    private final Scanner scanner;
    private final FileUtilConfiguration properties;

    public FileRenameRunner(Scanner scanner, FileUtilConfiguration properties) {
        this.scanner = scanner;
        this.properties = properties;
    }

    public void runRegexRename() {
        Path folder = getFolderFromProperties();
        Pattern fromPattern = Pattern.compile(properties.getString(RENAME_REGEX_FROM));
        String replacement = properties.getString(RENAME_REGEX_TO);
        RegexFileRenamer renamer = new RegexFileRenamer(folder, fromPattern, replacement);
        executeFileRenamer(renamer);
    }

    public void runDateRename() {
        Path folder = getFolderFromProperties();
        String replacement = properties.getString(RENAME_DATE_TO);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
            properties.getString(RENAME_DATE_DATE_FORMAT));
        ModifiedDateFileNameRenamer renamer = new ModifiedDateFileNameRenamer(folder, replacement, dateTimeFormatter);
        executeFileRenamer(renamer);
    }

    private void executeFileRenamer(FileRenamer renamer) {
        Map<String, String> replacements = renamer.generateRenamingsPreview();
        if (replacements.isEmpty()) {
            System.out.println("Nothing to rename in " + renamer.getFolder().toAbsolutePath());
            return;
        }
        System.out.println("This will rename " + replacements.size() + " files. Preview:");
        replacements.forEach((source, target) -> System.out.println(" " + source + " -> " + target));
        System.out.println("Confirm [y/n]");

        String input = scanner.nextLine();
        if ("y".equalsIgnoreCase(input)) {
            renamer.performRenamings();
        } else {
            System.out.println("Canceled renaming");
        }
    }

    private Path getFolderFromProperties() {
        String folderProperty = properties.getString(RENAME_FOLDER);
        return folderProperty == null ? Paths.get(".") : Paths.get(folderProperty);
    }
}
