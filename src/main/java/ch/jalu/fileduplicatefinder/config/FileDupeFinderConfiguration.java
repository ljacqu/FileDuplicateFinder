package ch.jalu.fileduplicatefinder.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static ch.jalu.fileduplicatefinder.utils.PathUtils.megaBytesToBytes;
import static com.google.common.math.IntMath.ceilingPowerOfTwo;

/**
 * Configuration for the file duplicate finder application.
 */
public class FileDupeFinderConfiguration {

    private Path rootFolder;
    private String hashAlgorithm;
    private long maxSizeForHashingInBytes;
    private int progressFilesFoundInterval;
    private int progressFilesHashedInterval;

    private boolean outputDistribution;
    private boolean outputDuplicates;
    private boolean outputDifferenceFromFileReadBeforeHash;
    private boolean outputFolderPairCount;

    private String filterWhitelist;
    private String filterBlacklist;
    private Double filterMinSizeInMb;
    private Double filterMaxSizeInMb;

    private String filterDuplicatesWhitelist;

    private long fileReadBeforeHashMinSizeBytes;
    private int fileReadBeforeHashNumberOfBytes;

    /**
     * Constructor.
     *
     * @param userPropertyFile configuration file provided by the user
     */
    public FileDupeFinderConfiguration(Path userPropertyFile) {
        initialize(userPropertyFile);
    }

    private void initialize(Path userPropertyFile) {
        Properties defaultProperties;
        Properties userProperties;
        try {
            defaultProperties = createDefaultProperties();
            userProperties = createUserPropertiesOrNull(userPropertyFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        PropertyResolver resolver = new PropertyResolver(defaultProperties, userProperties);

        rootFolder = Paths.get(resolver.getString("rootFolder"));
        hashAlgorithm = resolver.getString("hashAlgorithm");
        maxSizeForHashingInBytes = megaBytesToBytes(resolver.getDouble("maxSizeForHashingInMb"));
        filterWhitelist = resolver.getString("filter.whitelist");
        filterBlacklist = resolver.getString("filter.blacklist");
        filterMinSizeInMb = resolver.getDoubleOrNull("filter.fileSizeMinInMb");
        filterMaxSizeInMb = resolver.getDoubleOrNull("filter.fileSizeMaxInMb");
        filterDuplicatesWhitelist = resolver.getString("filter.duplicatesWhitelist");
        progressFilesFoundInterval = ceilingPowerOfTwo(resolver.getInt("progress.filesFoundInterval")) - 1;
        progressFilesHashedInterval = ceilingPowerOfTwo(resolver.getInt("progress.filesHashedInterval")) - 1;
        outputDistribution = resolver.getBoolean("output.showDistribution");
        outputDuplicates = resolver.getBoolean("output.showDuplicates");
        outputDifferenceFromFileReadBeforeHash = resolver.getBoolean("output.showDifferenceByFileReadBeforeHash");
        outputFolderPairCount = resolver.getBoolean("output.showFolderPairCount");
        fileReadBeforeHashMinSizeBytes = megaBytesToBytes(resolver.getDouble("fileReadBeforeHash.minFileSizeMb"));
        fileReadBeforeHashNumberOfBytes = resolver.getInt("fileReadBeforeHash.numberOfBytes");
    }

    private Properties createDefaultProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("default.properties")) {
            properties.load(is);
        }
        return properties;
    }

    private Properties createUserPropertiesOrNull(Path configFile) throws IOException {
        if (configFile == null) {
            return null;
        } else if (!Files.exists(configFile)) {
            System.out.println("Skipping config file '" + configFile + "' as it does not exist");
            return null;
        }

        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(configFile)) {
            properties.load(is);
        }
        return properties;
    }

    public Path getRootFolder() {
        return rootFolder;
    }

    public long getMaxSizeForHashingInBytes() {
        return maxSizeForHashingInBytes;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public String getFilterWhitelist() {
        return filterWhitelist;
    }

    public String getFilterBlacklist() {
        return filterBlacklist;
    }

    public Double getFilterMinSizeInMb() {
        return filterMinSizeInMb;
    }

    public Double getFilterMaxSizeInMb() {
        return filterMaxSizeInMb;
    }

    public String getFilterDuplicatesWhitelist() {
        return filterDuplicatesWhitelist;
    }

    public int getProgressFilesFoundInterval() {
        return progressFilesFoundInterval;
    }

    public int getProgressFilesHashedInterval() {
        return progressFilesHashedInterval;
    }

    public boolean isDistributionOutputEnabled() {
        return outputDistribution;
    }

    public boolean isDuplicatesOutputEnabled() {
        return outputDuplicates;
    }

    public boolean isDifferenceFromFileReadBeforeHashOutputEnabled() {
        return outputDifferenceFromFileReadBeforeHash;
    }

    public boolean isOutputFolderPairCount() {
        return outputFolderPairCount;
    }

    public long getFileReadBeforeHashMinSizeBytes() {
        return fileReadBeforeHashMinSizeBytes;
    }

    public int getFileReadBeforeHashNumberOfBytes() {
        return fileReadBeforeHashNumberOfBytes;
    }
}
