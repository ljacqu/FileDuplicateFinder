package ch.jalu.fileduplicatefinder.config;

public final class FileUtilProperties {

    public static final String TASK = "task";

    public static final String FORMAT_FILE_SIZE = "core.formatFileSize";

    // --- Rename feature ---

    public static final String RENAME_FOLDER = "rename.folder";

    public static final String RENAME_REGEX_FROM = "rename.regex.from";

    public static final String RENAME_REGEX_TO = "rename.regex.to";

    public static final String RENAME_DATE_TO = "rename.date.to";

    public static final String RENAME_DATE_DATE_FORMAT = "rename.date.dateFormat";

    // --- Duplicate finder feature ---

    public static final String DUPLICATE_FOLDER = "duplicates.folder";

    public static final String DUPLICATE_HASH_ALGORITHM = "duplicates.hash.algorithm";

    public static final String DUPLICATE_HASH_MAX_SIZE_MB = "duplicates.hash.maxSizeForHashingInMb";

    public static final String DUPLICATE_FILTER_WHITELIST = "duplicates.filter.whitelist";

    public static final String DUPLICATE_FILTER_BLACKLIST = "duplicates.filter.blacklist";

    public static final String DUPLICATE_FILTER_MIN_SIZE = "duplicates.filter.minSizeInMb";

    public static final String DUPLICATE_FILTER_MAX_SIZE = "duplicates.filter.maxSizeInMb";

    public static final String DUPLICATE_FILTER_RESULT_WHITELIST = "duplicates.filter.resultWhitelist";

    public static final String DUPLICATE_READ_BEFORE_HASH_MIN_SIZE = "duplicates.readBeforeHash.minSizeInMb";

    public static final String DUPLICATE_READ_BEFORE_HASH_BYTES_TO_READ = "duplicates.readBeforeHash.bytesToRead";

    public static final String DUPLICATE_OUTPUT_DUPLICATES = "duplicates.output.showDuplicates";

    public static final String DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT = "duplicates.output.showFolderPairCount";

    public static final String DUPLICATE_OUTPUT_DISTRIBUTION = "duplicates.output.showDistribution";

    public static final String DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH = "duplicates.output.showDifferenceReadFilesVsHash";

    public static final String DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL = "duplicates.output.progress.filesFoundInterval";

    public static final String DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL = "duplicates.output.progress.filesHashedInterval";

    // --- Duplicate finder feature ---

    public static final String FILE_COUNT_FOLDER = "fileCount.folder";

    // Groups extensions, e.g. "image .jpg,.png; text .txt,.html,.md; word .doc,.docx"
    public static final String FILE_COUNT_GROUPS = "fileCount.groups";

    // --- Folder diff feature ---

    public static final String DIFF_FOLDER1 = "diff.folder1";

    public static final String DIFF_FOLDER2 = "diff.folder2";

    public static final String DIFF_CHECK_BY_SIZE_AND_MODIFICATION_DATE = "diff.checkBySizeAndModificationDate";

    public static final String DIFF_USE_SMART_FOLDER_PREFIXES = "diff.output.smartFolderPrefixes";

    public static final String DIFF_FILES_PROCESSED_INTERVAL = "diff.output.progress.filesProcessedInterval";


    private FileUtilProperties() {
    }
}
