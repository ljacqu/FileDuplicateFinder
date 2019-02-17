package ch.jalu.fileduplicatefinder.filefilter;

import ch.jalu.fileduplicatefinder.config.ConfigurationReader;
import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import static ch.jalu.fileduplicatefinder.utils.PathUtils.negatePathMatcher;

public class ConfigurableFilePathMatcher implements PathMatcher {

    private final PathMatcher whitelist;
    private final PathMatcher blacklist;
    private final PathMatcher fileSizeMinFilter;
    private final PathMatcher fileSizeMaxFilter;

    public ConfigurableFilePathMatcher(ConfigurationReader configurationReader) {
        this.whitelist = toWildcardPattern(configurationReader.getFilterWhitelist());
        this.blacklist = negatePathMatcher(toWildcardPattern(configurationReader.getFilterBlacklist()));
        this.fileSizeMinFilter = toSizeFilter(configurationReader.getFilterMinSizeInMb(), 1);
        this.fileSizeMaxFilter = toSizeFilter(configurationReader.getFilterMaxSizeInMb(), -1);
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

    private static PathMatcher toSizeFilter(Double sizeInMb, int requiredCompareToValue) {
        if (sizeInMb == null) {
            return null;
        }
        return path -> {
            Double fileSize = PathUtils.getFileSizeInMegaBytes(path);
            int sizeComparedToThreshold = fileSize.compareTo(sizeInMb);
            return sizeComparedToThreshold == 0 || sizeComparedToThreshold == requiredCompareToValue;
        };
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
