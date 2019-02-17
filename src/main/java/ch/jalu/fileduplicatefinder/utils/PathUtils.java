package ch.jalu.fileduplicatefinder.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;

public final class PathUtils {

    private static final int BYTES_IN_ONE_MEGA_BYTE = 1024 * 1024;

    private PathUtils() {
    }

    public static long megaBytesToBytes(double megaBytes) {
        return Math.round(megaBytes * BYTES_IN_ONE_MEGA_BYTE);
    }

    public static PathMatcher negatePathMatcher(PathMatcher pathMatcher) {
        return pathMatcher == null ? null : p -> !pathMatcher.matches(p);
    }

    public static Stream<Path> list(Path path) {
        try {
            return Files.list(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list '" + path.toAbsolutePath() + "'", e);
        }
    }

    public static long size(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Path '" + path.toAbsolutePath() + "' is not a file");
        }

        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to get size of '" + path.toAbsolutePath() + "'", e);
        }
    }
}
