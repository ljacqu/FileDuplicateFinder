package ch.jalu.fileduplicatefinder.filefilter;

import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;

import static ch.jalu.fileduplicatefinder.utils.PathUtils.megaBytesToBytes;
import static ch.jalu.fileduplicatefinder.utils.PathUtils.negatePathMatcher;

/**
 * Path matcher configurable by {@link FileDupeFinderConfiguration} properties.
 */
public class ConfigurableFilePathMatcher implements FilePathMatcher {

    private final PathMatcher whitelist;
    private final PathMatcher blacklist;
    private final PathMatcher fileSizeMinFilter;
    private final PathMatcher fileSizeMaxFilter;
    private final PathMatcher duplicateResultWhitelist;

    public ConfigurableFilePathMatcher(FileDupeFinderConfiguration fileDupeFinderConfiguration) {
        this.whitelist = toWildcardPattern(fileDupeFinderConfiguration.getFilterWhitelist());
        this.blacklist = negatePathMatcher(toWildcardPattern(fileDupeFinderConfiguration.getFilterBlacklist()));
        this.fileSizeMinFilter = toSizeFilter(fileDupeFinderConfiguration.getFilterMinSizeInMb(), true);
        this.fileSizeMaxFilter = toSizeFilter(fileDupeFinderConfiguration.getFilterMaxSizeInMb(), false);
        this.duplicateResultWhitelist = toWildcardPattern(fileDupeFinderConfiguration.getFilterDuplicatesWhitelist());
    }

    /**
     * Creates a path matcher for the given glob filter. Returns null if the filter is null or empty.
     *
     * @param filter the filter to create a path matcher for
     * @return path matcher with the filter, or null
     */
    private static PathMatcher toWildcardPattern(String filter) {
        if (filter == null || filter.isEmpty()) {
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
     * @return path matcher for the given size, or null if the argument is null
     */
    private static PathMatcher toSizeFilter(Double sizeInMb, boolean isMin) {
        if (sizeInMb == null) {
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

    private static boolean matches(PathMatcher matcher, Path path) {
        return matcher == null || matcher.matches(path);
    }
}
