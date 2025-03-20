package ch.jalu.fileduplicatefinder;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.config.FileUtilSettings;
import ch.jalu.fileduplicatefinder.duplicatefinder.FileDuplicateRunner;
import ch.jalu.fileduplicatefinder.duplicatefinder.FolderPairDuplicatesCounter;
import ch.jalu.fileduplicatefinder.duplicatefinder.output.ConsoleResultOutputter;
import ch.jalu.fileduplicatefinder.filecount.FileCountRunner;
import ch.jalu.fileduplicatefinder.folderdiff.FolderDiffRunner;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import ch.jalu.fileduplicatefinder.output.RootWriterReader;
import ch.jalu.fileduplicatefinder.output.TaskWriterReader;
import ch.jalu.fileduplicatefinder.output.WriterReader;
import ch.jalu.fileduplicatefinder.rename.FileRenameRunner;
import ch.jalu.fileduplicatefinder.tree.FileTreeRunner;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Entry class with main method. Delegates to the appropriate task runner.
 */
public class FileUtilsRunner {

    private FileUtilsRunner() {
    }

    public static void main(String... args) {
        try (Scanner scanner = new Scanner(System.in)) {
            FileUtilConfiguration configuration = createConfiguration(scanner, args);
            if (configuration == null) {
                return;
            }

            RootWriterReader logger = new RootWriterReader(scanner);
            String task = configuration.getValueOrPrompt(FileUtilSettings.TASK);
            do {
                switch (task) {
                    case FileRenameRunner.ID_REGEX:
                        new FileRenameRunner(logger.createWriterReaderForTask("rename"), configuration)
                            .runRegexRename();
                        break;
                    case FileRenameRunner.ID_DATE:
                        new FileRenameRunner(logger.createWriterReaderForTask("rename"), configuration)
                            .runDateRename();
                        break;
                    case FileDuplicateRunner.ID:
                        createFileDuplicateRunner(configuration, logger).run();
                        break;
                    case FileCountRunner.ID:
                        new FileCountRunner(logger.createWriterReaderForTask("count"), configuration).run();
                        break;
                    case FolderDiffRunner.ID:
                        WriterReader diffLogger = logger.createWriterReaderForTask("diff");
                        new FolderDiffRunner(configuration, new FileHasherFactory(), diffLogger).run();
                        break;
                    case FileTreeRunner.ID:
                        new FileTreeRunner(scanner, configuration).run();
                        break;
                    case "exit":
                    case "x":
                    case "q":
                    case "quit":
                        return;
                    default:
                        String taskList = FileRenameRunner.ID_REGEX
                            + ", " + FileRenameRunner.ID_DATE
                            + ", " + FileDuplicateRunner.ID
                            + ", " + FileCountRunner.ID
                            + ", " + FolderDiffRunner.ID
                            + ", " + FileTreeRunner.ID;
                        System.err.println("Unknown task '" + task + "'. Possible tasks: " + taskList);
                }

                System.out.println("Task: (q to quit)");
                task = configuration.getValueOrPrompt(FileUtilSettings.TASK, true);
            } while (true);
        } catch (ExitRunnerException ignore) {
            // Nothing to do
        }
    }

    private static FileDuplicateRunner createFileDuplicateRunner(FileUtilConfiguration configuration,
                                                                 RootWriterReader logger) {
        TaskWriterReader contextLogger = logger.createWriterReaderForTask("duplicates");
        return new FileDuplicateRunner(configuration, new FileHasherFactory(), new FolderPairDuplicatesCounter(),
            new ConsoleResultOutputter(configuration, contextLogger), contextLogger);
    }

    @Nullable
    private static FileUtilConfiguration createConfiguration(Scanner scanner, String... args) {
        Path userConfig = null;
        if (args != null && args.length > 0) {
            userConfig = Paths.get(args[0]);
            if (!Files.exists(userConfig)) {
                System.err.println("Supplied config file '" + userConfig.getFileName().toString() + "' does not exist");
                return null;
            }
        }
        return new FileUtilConfiguration(scanner, userConfig);
    }
}
