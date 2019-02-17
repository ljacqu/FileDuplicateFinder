package ch.jalu.fileduplicatefinder.utils;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

public final class PathUtils {

    private static final double BYTES_IN_ONE_MEGA_BYTE = 1024 * 1024;

    private PathUtils() {
    }

    public static double getFileSizeInMegaBytes(Path path) {
        Preconditions.checkArgument(Files.isRegularFile(path), "Path '" + path.toAbsolutePath() + "' is not a file");
        try {
            long sizeInBytes = Files.size(path);
            return getFileSizeInMegaBytes(sizeInBytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to get size of '" + path.toAbsolutePath() + "'", e);
        }
    }

    public static double getFileSizeInMegaBytes(long sizeInBytes) {
        return sizeInBytes / BYTES_IN_ONE_MEGA_BYTE;
    }

    public static PathMatcher negatePathMatcher(PathMatcher pathMatcher) {
        return pathMatcher == null ? null : p -> !pathMatcher.matches(p);
    }
}
