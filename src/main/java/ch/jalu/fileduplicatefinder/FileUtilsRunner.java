package ch.jalu.fileduplicatefinder;

import ch.jalu.fileduplicatefinder.config.CreateConfigTask;
import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.duplicatefinder.FileDuplicateRunner;
import ch.jalu.fileduplicatefinder.duplicatefinder.FolderPairDuplicatesCounter;
import ch.jalu.fileduplicatefinder.filecount.FileCountRunner;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import ch.jalu.fileduplicatefinder.output.ConsoleResultOutputter;
import ch.jalu.fileduplicatefinder.rename.FileRenameRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.TASK;

/**
 * Entry class with main method. Delegates to the appropriate task runner.
 */
public class FileUtilsRunner {

    private FileUtilsRunner() {
    }

    public static void main(String... args) {
        try (Scanner scanner = new Scanner(System.in)) {
            FileUtilConfiguration configuration = createConfiguration(scanner, args);
            String task = configuration.getString(TASK);

            switch (task) {
                case CreateConfigTask.ID:
                    new CreateConfigTask().run();
                    break;
                case FileRenameRunner.ID_REGEX:
                    new FileRenameRunner(scanner, configuration).runRegexRename();
                    break;
                case FileRenameRunner.ID_DATE:
                    new FileRenameRunner(scanner, configuration).runDateRename();
                    break;
                case FileDuplicateRunner.ID:
                    createFileDuplicateRunner(configuration).run();
                    break;
                case FileCountRunner.ID:
                    new FileCountRunner(scanner, configuration).run();
                    break;
                default:
                    System.err.println("Unknown task: '" + task + "'"); // TODO create help task
            }


        } catch (StopFileUtilExecutionException e) {
            // ignore
        }
    }

    private static FileDuplicateRunner createFileDuplicateRunner(FileUtilConfiguration configuration) {
        return new FileDuplicateRunner(configuration, new FileHasherFactory(), new FolderPairDuplicatesCounter(),
            new ConsoleResultOutputter(configuration));
    }

    private static FileUtilConfiguration createConfiguration(Scanner scanner, String... args) {
        Path userConfig = null;
        if (args != null && args.length > 0) {
            userConfig = Paths.get(args[0]);
            if (Files.exists(userConfig)) {
                System.err.println("Supplied config file '" + userConfig.getFileName().toString() + "' does not exist");
                System.err.println("You can create a config file with java -jar fileutils.jar -Dtask="
                    + CreateConfigTask.ID);
                throw new StopFileUtilExecutionException();
            }
        }
        return new FileUtilConfiguration(scanner, userConfig);
    }
}
