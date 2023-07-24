package ch.jalu.fileduplicatefinder.rename;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.config.FileUtilSettings;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;

import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.RENAME_DATE_DATE_FORMAT;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.RENAME_DATE_TO;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.RENAME_REGEX_FROM;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.RENAME_REGEX_TO;

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
        Path folder = configuration.getValueOrPrompt(FileUtilSettings.RENAME_FOLDER);
        RegexFileRenamer renamer = new RegexFileRenamer(folder);

        Pattern fromPattern = configuration.getValueOrPrompt(RENAME_REGEX_FROM);
        String replacement = configuration.getValueOrPrompt(RENAME_REGEX_TO);
        while (true) {
            Optional<Boolean> doRenaming =
                generateAndConfirmRenamingsForRegex(folder, renamer.generateRenamingsPreview(fromPattern, replacement));
            if (doRenaming.isEmpty()) {
                System.out.println("Canceled the renaming.");
                break;
            } else if (doRenaming.get()) {
                renamer.performRenamings();
                break;
            } else { // !doRenaming.get()
                fromPattern = configuration.getValueOrPrompt(RENAME_REGEX_FROM, true);
                replacement = configuration.getValueOrPrompt(RENAME_REGEX_TO, true);
            }
        }
    }

    public void runDateRename() {
        Path folder = configuration.getValueOrPrompt(FileUtilSettings.RENAME_FOLDER);
        ModifiedDateFileNameRenamer renamer = new ModifiedDateFileNameRenamer(folder);

        String replacement = configuration.getValue(RENAME_DATE_TO);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
            configuration.getValue(RENAME_DATE_DATE_FORMAT));

        while (true) {
            Optional<Boolean> doRenaming =
                generateAndConfirmRenamingsForRegex(folder,
                    renamer.generateRenamingsPreview(replacement, dateTimeFormatter));
            if (doRenaming.isEmpty()) {
                System.out.println("Canceled the renaming.");
                break;
            } else if (doRenaming.get()) {
                renamer.performRenamings();
                break;
            } else { // !doRenaming.get()
                replacement = configuration.getValue(RENAME_DATE_TO, true);
                dateTimeFormatter = DateTimeFormatter.ofPattern(
                    configuration.getValue(RENAME_DATE_DATE_FORMAT, true));
            }
        }
    }

    private Optional<Boolean> generateAndConfirmRenamingsForRegex(Path folder, Map<String, String> replacements) {
        if (replacements.isEmpty()) {
            System.out.println("Nothing to rename in " + folder.toAbsolutePath());
            System.out.println("Type 'n' to redefine the pattern, 'q' to quit");
        } else {
            System.out.println("This will rename " + replacements.size() + " files. Preview:");
            replacements.forEach((source, target) -> System.out.println(" " + source + " -> " + target));
            System.out.println("Confirm (y/n, q to quit)");
        }

        String input = scanner.nextLine();
        if ("y".equalsIgnoreCase(input)) {
            return Optional.of(true);
        } else if ("n".equalsIgnoreCase(input)) {
            return Optional.of(false);
        }
        return Optional.empty();
    }
}
