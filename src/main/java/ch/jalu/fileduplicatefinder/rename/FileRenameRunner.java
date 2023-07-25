package ch.jalu.fileduplicatefinder.rename;

import ch.jalu.fileduplicatefinder.ExitRunnerException;
import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.config.FileUtilSettings;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
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

    public void runRegexRename() throws ExitRunnerException {
        runRenamer(new RegexRenamerHandler());
    }

    public void runDateRename() throws ExitRunnerException {
        runRenamer(new DateRenamerHandler());
    }

    private void runRenamer(RenamerHandler renamerHandler) throws ExitRunnerException {
        Path folder = configuration.getValueOrPrompt(FileUtilSettings.RENAME_FOLDER);
        renamerHandler.createRenamer(folder);
        renamerHandler.initializeSettings(configuration, false);
        boolean generatePreviews = true;

        do {
            if (generatePreviews) {
                generateAndConfirmRenamingsForRegex(folder, renamerHandler.generateRenamePreviews());
            }
            generatePreviews = true;

            String input = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            switch (input) {
                case "y":
                    renamerHandler.performRenamings();
                    System.out.println("Type 'q' to quit, 'n' to define a new pattern, or 'f' to change folder");
                    generatePreviews = false;
                    break;
                case "n":
                    renamerHandler.initializeSettings(configuration, true);
                    break;
                case "r":
                case "refresh":
                    // Dummy task to regenerate the renaming previews
                    break;
                case "f":
                    folder = configuration.getValueOrPrompt(FileUtilSettings.RENAME_FOLDER, true);
                    renamerHandler.createRenamer(folder);
                    break;
                case "q":
                case "quit":
                    return;
                case "x":
                case "exit":
                    throw new ExitRunnerException();
                default: // "h"
                    System.out.println("- 'h' for help");
                    System.out.println("- 'f' to change folder");
                    System.out.println("- 'r' to refresh the preview");
                    System.out.println("- 'q' to quit to main");
                    System.out.println("- 'x' to exit the application");
                    generatePreviews = false;
            }
        } while (true);
    }

    private void generateAndConfirmRenamingsForRegex(Path folder, Map<String, String> replacements) {
        if (replacements.isEmpty()) {
            System.out.println("Nothing to rename in " + folder.toAbsolutePath());
            System.out.println("Type 'n' to redefine the pattern, 'q' to quit, 'h' for help");
        } else {
            System.out.println("This will rename " + replacements.size() + " files. Preview:");
            replacements.forEach((source, target) -> System.out.println(" " + source + " -> " + target));
            System.out.println("Perform renaming [y/n]? Type 'q' to quit, 'h' for help");
        }
    }

    interface RenamerHandler {

        void createRenamer(Path folder);

        void initializeSettings(FileUtilConfiguration configuration, boolean forcePrompt);

        Map<String, String> generateRenamePreviews();

        void performRenamings();

    }

    private static final class RegexRenamerHandler implements RenamerHandler {

        private RegexFileRenamer renamer;
        private Pattern fromPattern;
        private String replacement;


        @Override
        public void createRenamer(Path folder) {
            renamer = new RegexFileRenamer(folder);
        }

        @Override
        public void initializeSettings(FileUtilConfiguration configuration, boolean forcePrompt) {
            fromPattern = configuration.getValueOrPrompt(RENAME_REGEX_FROM, forcePrompt);
            replacement = configuration.getValueOrPrompt(RENAME_REGEX_TO, forcePrompt);
        }

        @Override
        public Map<String, String> generateRenamePreviews() {
            return renamer.generateRenamingsPreview(fromPattern, replacement);
        }

        @Override
        public void performRenamings() {
            renamer.performRenamings();
        }
    }

    private static final class DateRenamerHandler implements RenamerHandler {

        private ModifiedDateFileNameRenamer renamer;
        private String replacement;
        private DateTimeFormatter dateTimeFormatter;

        @Override
        public void createRenamer(Path folder) {
            renamer = new ModifiedDateFileNameRenamer(folder);
        }

        @Override
        public void initializeSettings(FileUtilConfiguration configuration, boolean forcePrompt) {
            replacement = configuration.getValue(RENAME_DATE_TO);
            dateTimeFormatter = DateTimeFormatter.ofPattern(configuration.getValue(RENAME_DATE_DATE_FORMAT));
        }

        @Override
        public Map<String, String> generateRenamePreviews() {
            return renamer.generateRenamingsPreview(replacement, dateTimeFormatter);
        }

        @Override
        public void performRenamings() {
            renamer.performRenamings();
        }
    }
}
