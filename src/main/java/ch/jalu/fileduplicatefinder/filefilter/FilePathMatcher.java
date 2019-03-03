package ch.jalu.fileduplicatefinder.filefilter;

import java.nio.file.Path;
import java.util.Collection;

public interface FilePathMatcher {

    boolean shouldScan(Path path);

    boolean hasFileFromResultWhitelist(Collection<Path> paths);

}
