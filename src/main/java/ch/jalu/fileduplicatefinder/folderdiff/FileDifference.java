package ch.jalu.fileduplicatefinder.folderdiff;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

public class FileDifference {

    @Nullable
    private final FileElement folder1Element;
    @Nullable
    private final FileElement folder2Element;

    public FileDifference(@Nullable FileElement folder1Element, @Nullable FileElement folder2Element) {
        this.folder1Element = folder1Element;
        this.folder2Element = folder2Element;
        Preconditions.checkArgument(folder1Element != null || folder2Element != null,
            "At least one folder element must be not null");
    }

    @Nullable
    public FileElement getFolder1Element() {
        return folder1Element;
    }

    @Nullable
    public FileElement getFolder2Element() {
        return folder2Element;
    }

    public boolean wasModified() {
        return folder1Element != null && folder2Element != null
            && folder1Element.getName().equals(folder2Element.getName());
    }

    public int getSortCodeForDiffType() {
        if (folder1Element == null) {
            return 3; // New file
        } else if (folder2Element == null) {
            return 0; // Deleted file
        }
        return wasModified()
            ? 1  // Modified file
            : 2; // Moved file
    }
}
