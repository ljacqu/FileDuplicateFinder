package ch.jalu.fileduplicatefinder.filefilter;

import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import static ch.jalu.fileduplicatefinder.utils.PathUtils.megaBytesToBytes;
import static ch.jalu.fileduplicatefinder.utils.PathUtils.negatePathMatcher;

public class ConfigurableFilePathMatcher implements PathMatcher {

    private final PathMatcher whitelist;
    private final PathMatcher blacklist;
    private final PathMatcher fileSizeMinFilter;
    private final PathMatcher fileSizeMaxFilter;

    public ConfigurableFilePathMatcher(FileDupeFinderConfiguration fileDupeFinderConfiguration) {
        this.whitelist = toWildcardPattern(fileDupeFinderConfiguration.getFilterWhitelist());
        this.blacklist = negatePathMatcher(toWildcardPattern(fileDupeFinderConfiguration.getFilterBlacklist()));
        this.fileSizeMinFilter = toSizeFilter(fileDupeFinderConfiguration.getFilterMinSizeInMb(), true);
        this.fileSizeMaxFilter = toSizeFilter(fileDupeFinderConfiguration.getFilterMaxSizeInMb(), false);
    }

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
    public boolean matches(Path path) {
        if (Files.isRegularFile(path)) {
            return matches(whitelist, path)
                && matches(blacklist, path)
                && matches(fileSizeMinFilter, path)
                && matches(fileSizeMaxFilter, path);
        }
        return true;
    }

    private static boolean matches(PathMatcher matcher, Path path) {
        return matcher == null || matcher.matches(path);
    }
}
