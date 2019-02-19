package ch.jalu.fileduplicatefinder.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;

/**
 * Utilities for {@link Path} and related objects.
 */
public final class PathUtils {

    private static final int BYTES_IN_ONE_MEGA_BYTE = 1024 * 1024;

    private PathUtils() {
    }

    /**
     * Converts the given amount in megabytes to bytes.
     *
     * @param megaBytes the megabytes to convert
     * @return the bytes
     */
    public static long megaBytesToBytes(double megaBytes) {
        return Math.round(megaBytes * BYTES_IN_ONE_MEGA_BYTE);
    }

    /**
     * Negates a path matcher in a null-safe manner.
     *
     * @param pathMatcher the path matcher
     * @return negated path matcher, or null if the matcher is null
     */
    public static PathMatcher negatePathMatcher(PathMatcher pathMatcher) {
        return pathMatcher == null ? null : p -> !pathMatcher.matches(p);
    }

    /**
     * Returns a stream of files which are entries in the given directory.
     *
     * @param path the folder whose files to return
     * @return stream of paths inside the given directory
     */
    public static Stream<Path> list(Path path) {
        try {
            return Files.list(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list '" + path.toAbsolutePath() + "'", e);
        }
    }

    /**
     * Returns the size in bytes of the given path.
     *
     * @param path the file to get the size of
     * @return size in bytes
     */
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
