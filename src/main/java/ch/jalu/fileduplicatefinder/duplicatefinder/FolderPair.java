package ch.jalu.fileduplicatefinder.duplicatefinder;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Pair of folders in which there are duplicates.
 */
public class FolderPair {

    private final Path folder1;
    private final Path folder2;

    /**
     * Constructor. Ensures that folder1 comes before folder2 ({@link Path#compareTo}).
     *
     * @param folder1 first folder
     * @param folder2 second folder
     */
    public FolderPair(Path folder1, Path folder2) {
        if (folder1.compareTo(folder2) > 0) {
            this.folder1 = folder2;
            this.folder2 = folder1;
        } else {
            this.folder1 = folder1;
            this.folder2 = folder2;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof FolderPair) {
            FolderPair that = (FolderPair) obj;
            return Objects.equals(this.folder1, that.folder1)
                && Objects.equals(this.folder2, that.folder2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(folder1, folder2);
    }

    /**
     * Creates a text output of this instance.
     *
     * @param root the folder to relativize the folder paths with
     * @return textual representation of the folder pair
     */
    public String createTextOutput(Path root) {
        return folder1.equals(folder2)
            ? "within " + root.relativize(folder1)
            : root.relativize(folder1) + " - " + root.relativize(folder2);
    }
}
