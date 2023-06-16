package ch.jalu.fileduplicatefinder.configme;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.RegexProperty;
import ch.jalu.configme.properties.StringProperty;
import ch.jalu.fileduplicatefinder.configme.property.FuBooleanProperty;
import ch.jalu.fileduplicatefinder.configme.property.FuDoubleProperty;
import ch.jalu.fileduplicatefinder.configme.property.FuIntegerProperty;
import ch.jalu.fileduplicatefinder.configme.property.FuOptionalEnumProperty;
import ch.jalu.fileduplicatefinder.configme.property.FuPowerOfTwoMinusOneProperty;
import ch.jalu.fileduplicatefinder.tree.TreeDisplayMode;

import java.nio.file.Path;
import java.util.Optional;

import static ch.jalu.configme.properties.PropertyInitializer.newRegexProperty;
import static ch.jalu.configme.properties.PropertyInitializer.optionalStringProperty;
import static ch.jalu.fileduplicatefinder.configme.property.FuPathProperty.newOptionalPathProperty;

/**
 * Defines all properties for the file utils.
 */
public class FileUtilSettings implements SettingsHolder {

    @Comment("Set to always run the same task (Possible values: rename,addDate,duplicates,filecount,diff,tree)")
    public static final Property<Optional<String>> TASK = optionalStringProperty("task");

    @Comment("Format file size to human-readable units (e.g. 4.1 KB). If false, the number of bytes is always shown")
    public static final FuBooleanProperty FORMAT_FILE_SIZE = new FuBooleanProperty("core.formatFileSize", true);

    // --- Rename feature ---

    @Comment("Folder to rename files in (visits all child folders)")
    public static final Property<Optional<Path>> RENAME_FOLDER = newOptionalPathProperty("rename.folder");

    @Comment("Regex rename: regex to match files by")
    public static final RegexProperty RENAME_REGEX_FROM = newRegexProperty("rename.regex.from", "IMG_E(\\d+)\\.JPG");

    @Comment("Regex rename: what to rename files to (use $1 etc. for capturing groups)")
    public static final StringProperty RENAME_REGEX_TO = new StringProperty("rename.regex.to", "IMG_$1E\\.JPG");

    @Comment({
        "",
        "Date rename: what to rename the file to. {file} = full file name; {name} = file name without extension;",
        "  {ext} = extension (without dot), {date} = last modified date"
    })
    public static final StringProperty RENAME_DATE_TO = new StringProperty("rename.date.to", "{date}_{file}");

    @Comment("Date rename: formatting of the last modified date."
        + " See https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns")
    public static final StringProperty RENAME_DATE_DATE_FORMAT =
        new StringProperty("rename.date.dateFormat", "yyyyMMdd");

    // --- Duplicate finder feature ---

    @Comment("Path at which the processing of files starts")
    public static final Property<Optional<Path>> DUPLICATE_FOLDER = newOptionalPathProperty("duplicates.folder");

    // todo: enum
    @Comment("Algorithm used to hash the file contents with")
    public static final StringProperty DUPLICATE_HASH_ALGORITHM = new StringProperty("duplicates.hash.algorithm", "sha1");

    @Comment("If a file's size exceeds the below threshold, matching will be done by file size only")
    public static final FuDoubleProperty DUPLICATE_HASH_MAX_SIZE_MB =
        new FuDoubleProperty("duplicates.hash.maxSizeForHashingInMb", 300);

    @Comment({
        "Glob filter of files to consider (optional)",
        "  See https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)"
    })
    public static final StringProperty DUPLICATE_FILTER_BLACKLIST =
        new StringProperty("duplicates.filter.blacklist", "");
    public static final StringProperty DUPLICATE_FILTER_WHITELIST =
        new StringProperty("duplicates.filter.whitelist", "");

    @Comment("Min and max size of files to consider (optional). 0 to disable")
    public static final FuDoubleProperty DUPLICATE_FILTER_MIN_SIZE =
        new FuDoubleProperty("duplicates.filter.minSizeInMb", 0.0);

    public static final FuDoubleProperty DUPLICATE_FILTER_MAX_SIZE =
        new FuDoubleProperty("duplicates.filter.maxSizeInMb", 0.0);

    @Comment("One file in a matched group of duplicates must match the given pattern to be shown as result (optional)")
    public static final StringProperty DUPLICATE_FILTER_RESULT_WHITELIST =
        new StringProperty("duplicates.filter.resultWhitelist", "");

    @Comment({
        "The number of bytes configured will first be read and compared for files that exceed the given file size",
        "before the file's contents are hashed."
    })
    public static final FuDoubleProperty DUPLICATE_READ_BEFORE_HASH_MIN_SIZE =
        new FuDoubleProperty("duplicates.readBeforeHash.minSizeInMb", 1024);

    public static final FuIntegerProperty DUPLICATE_READ_BEFORE_HASH_BYTES_TO_READ =
        new FuIntegerProperty("duplicates.readBeforeHash.bytesToRead", 64);

    @Comment({
        "",
        "Configures what should be output. Besides \"showDuplicates\" everything else is a debug output."
    })
    public static final FuBooleanProperty DUPLICATE_OUTPUT_DUPLICATES =
        new FuBooleanProperty("duplicates.output.showDuplicates", true);

    public static final FuBooleanProperty DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT =
        new FuBooleanProperty("duplicates.output.showFolderPairCount", false);

    public static final FuBooleanProperty DUPLICATE_OUTPUT_DISTRIBUTION =
        new FuBooleanProperty("duplicates.output.showDistribution", false);

    public static final FuBooleanProperty DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH =
        new FuBooleanProperty("duplicates.output.showDifferenceReadFilesVsHash", false);

    @Comment("Interval in which to output some progress. Must be a power of 2 minus 1 (e.g. 63, 127, 255, 511)")
    public static final FuPowerOfTwoMinusOneProperty DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL =
        new FuPowerOfTwoMinusOneProperty("duplicates.output.progress.filesFoundInterval", 1023);

    public static final FuPowerOfTwoMinusOneProperty DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL =
        new FuPowerOfTwoMinusOneProperty("duplicates.output.progress.filesHashedInterval", 511);

    // --- Duplicate finder feature ---

    public static final Property<Optional<Path>> FILE_COUNT_FOLDER = newOptionalPathProperty("fileCount.folder");

    @Comment("Groups extensions, e.g. \"image .jpg,.png; text .txt,.html,.md; word .doc,.docx\"")
    public static final StringProperty FILE_COUNT_GROUPS = new StringProperty("fileCount.groups", "");

    // --- Folder diff feature ---

    public static final Property<Optional<Path>> DIFF_FOLDER1 = newOptionalPathProperty("diff.folder1");

    public static final Property<Optional<Path>> DIFF_FOLDER2 = newOptionalPathProperty("diff.folder2");

    public static final FuBooleanProperty DIFF_CHECK_BY_SIZE_AND_MODIFICATION_DATE =
        new FuBooleanProperty("diff.checkBySizeAndModificationDate", true);

    public static final FuBooleanProperty DIFF_USE_SMART_FOLDER_PREFIXES =
        new FuBooleanProperty("diff.output.smartFolderPrefixes", true);

    public static final FuPowerOfTwoMinusOneProperty DIFF_FILES_PROCESSED_INTERVAL =
        new FuPowerOfTwoMinusOneProperty("diff.output.progress.filesProcessedInterval", 1023);

    // --- File tree ---

    public static final Property<Optional<Path>> TREE_FOLDER = newOptionalPathProperty("tree.folder");

    public static final FuOptionalEnumProperty<TreeDisplayMode> TREE_DISPLAY_MODE =
        new FuOptionalEnumProperty<>("tree.displayMode", TreeDisplayMode.class);

    public static final Property<Optional<String>> TREE_FILE_REGEX =
        optionalStringProperty("tree.filter.fileRegex");

    public static final Property<Optional<String>> TREE_DIRECTORY_REGEX =
        optionalStringProperty("tree.filter.directoryRegex");

    public static final FuDoubleProperty TREE_FILE_MIN_SIZE_MB =
        new FuDoubleProperty("tree.filter.minSizeInMb", -1.0);

    public static final FuDoubleProperty TREE_FILE_MAX_SIZE_MB =
        new FuDoubleProperty("tree.filter.maxSizeInMb", -1.0);

    public static final FuIntegerProperty TREE_MIN_ITEMS_IN_FOLDER =
        new FuIntegerProperty("tree.filter.minItemsInDir", -1);

    public static final FuIntegerProperty TREE_MAX_ITEMS_IN_FOLDER =
        new FuIntegerProperty("tree.filter.maxItemsInDir", -1);

    public static final FuBooleanProperty TREE_INDENT_ELEMENTS =
        new FuBooleanProperty("tree.output.indentElements", true);
    public static final FuBooleanProperty TREE_SHOW_ABSOLUTE_PATH =
        new FuBooleanProperty("tree.output.showAbsolutePath", false);

    public static final FuPowerOfTwoMinusOneProperty TREE_FILES_PROCESSED_INTERVAL =
        new FuPowerOfTwoMinusOneProperty("tree.output.progress.filesProcessedInterval", 256);

    private FileUtilSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        conf.setComment("rename",
            "#######", "Configuration for file renaming tasks (regex, date)", "#######");
        conf.setComment("duplicates",
            "#######", "Configuration for duplicate finder", "#######");
        conf.setComment("diff",
            "#######", "Folder diff", "#######");
        conf.setComment("tree",
            "#######", "File tree", "#######");
    }
}
