package ch.jalu.fileduplicatefinder.filefilter;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.utils.PathUtils;

import javax.annotation.Nullable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;

import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_BLACKLIST;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_MAX_SIZE;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_MIN_SIZE;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_RESULT_WHITELIST;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_FILTER_WHITELIST;
import static ch.jalu.fileduplicatefinder.utils.FileSizeUtils.megaBytesToBytes;
import static ch.jalu.fileduplicatefinder.utils.PathUtils.negatePathMatcher;

/**
 * Path matcher configurable by {@link FileUtilConfiguration} properties.
 */
public class ConfigurableFilePathMatcher implements FilePathMatcher {

    private final @Nullable PathMatcher whitelist;
    private final @Nullable PathMatcher blacklist;
    private final @Nullable PathMatcher fileSizeMinFilter;
    private final @Nullable PathMatcher fileSizeMaxFilter;
    private final @Nullable PathMatcher duplicateResultWhitelist;

    public ConfigurableFilePathMatcher(FileUtilConfiguration configuration) {
        this.whitelist = toWildcardPattern(configuration.getString(DUPLICATE_FILTER_WHITELIST));
        this.blacklist = negatePathMatcher(toWildcardPattern(configuration.getString(DUPLICATE_FILTER_BLACKLIST)));
        this.fileSizeMinFilter = toSizeFilter(configuration.getDouble(DUPLICATE_FILTER_MIN_SIZE), true);
        this.fileSizeMaxFilter = toSizeFilter(configuration.getDouble(DUPLICATE_FILTER_MAX_SIZE), false);
        this.duplicateResultWhitelist = toWildcardPattern(configuration.getString(DUPLICATE_FILTER_RESULT_WHITELIST));
    }

    /**
     * Creates a path matcher for the given glob filter. Returns null if the filter is null or empty.
     *
     * @param filter the filter to create a path matcher for
     * @return path matcher with the filter, or null
     */
    @Nullable
    private static PathMatcher toWildcardPattern(String filter) {
        if (filter.isEmpty()) {
            return null;
        }
        try {
            return FileSystems.getDefault().getPathMatcher("glob:" + filter);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to create matcher for '" + filter + "'", e);
        }
    }

    /**
     * Creates a path matcher which matches files based on their size.
     *
     * @param sizeInMb the required size in megabytes
     * @param isMin true to match files which are at least the given size, false for a matcher {@code size >= sizeInMb}
     * @return path matcher for the given size, or null if the argument is not positive
     */
    @Nullable
    private static PathMatcher toSizeFilter(double sizeInMb, boolean isMin) {
        if (sizeInMb <= 0.0) {
            return null;
        }

        long sizeThreshold = megaBytesToBytes(sizeInMb);
        if (isMin) {
            return path -> PathUtils.size(path) >= sizeThreshold;
        } else {
            return path -> PathUtils.size(path) <= sizeThreshold;
        }
    }

    @Override
    public boolean shouldScan(Path path) {
        if (Files.isRegularFile(path)) {
            return matches(whitelist, path)
                && matches(blacklist, path)
                && matches(fileSizeMinFilter, path)
                && matches(fileSizeMaxFilter, path);
        }
        return true;
    }

    @Override
    public boolean hasFileFromResultWhitelist(Collection<Path> paths) {
        return paths.stream().anyMatch(path -> matches(duplicateResultWhitelist, path));
    }

    private static boolean matches(@Nullable PathMatcher matcher, Path path) {
        return matcher == null || matcher.matches(path);
    }
}
