package ch.jalu.fileduplicatefinder.rename;

import ch.jalu.fileduplicatefinder.configme.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.configme.FileUtilSettings;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.RENAME_DATE_DATE_FORMAT;
import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.RENAME_DATE_TO;
import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.RENAME_REGEX_FROM;
import static ch.jalu.fileduplicatefinder.configme.FileUtilSettings.RENAME_REGEX_TO;

public class FileRenameRunner {

    public static final String ID_REGEX = "rename";
    public static final String ID_DATE = "addDate";

    private final Scanner scanner;
    private final FileUtilConfiguration configuration;

    public FileRenameRunner(Scanner scanner, FileUtilConfiguration configuration) {
        this.scanner = scanner;
        this.configuration = configuration;
    }

    public void runRegexRename() {
        Path folder = getFolderFromProperties();
        Pattern fromPattern = configuration.getPattern(RENAME_REGEX_FROM);
        String replacement = configuration.getString(RENAME_REGEX_TO);
        RegexFileRenamer renamer = new RegexFileRenamer(folder, fromPattern, replacement);
        executeFileRenamer(renamer);
    }

    public void runDateRename() {
        Path folder = getFolderFromProperties();
        String replacement = configuration.getString(RENAME_DATE_TO);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
            configuration.getString(RENAME_DATE_DATE_FORMAT));
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
        return configuration.getPathOrPrompt(FileUtilSettings.RENAME_FOLDER);
    }
}
