package ch.jalu.fileduplicatefinder.utils;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.FileTime;
import java.util.stream.Stream;

/**
 * Utilities for {@link Path} and related objects.
 */
public final class PathUtils {

    private PathUtils() {
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

    /**
     * Returns the last modified time of the given path.
     *
     * @param path the path to get the last modified time of
     * @return last modified time
     */
    public static FileTime getLastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to get last modified time of '" + path.toAbsolutePath() + "'", e);
        }
    }

    /**
     * Returns the String representation of the given path, or null if the argument is null.
     *
     * @param path the path to toString (if not null)
     * @return string of the path, or null
     */
    public static @Nullable String toStringNullSafe(@Nullable Path path) {
        return path == null ? null : path.toString();
    }
}
