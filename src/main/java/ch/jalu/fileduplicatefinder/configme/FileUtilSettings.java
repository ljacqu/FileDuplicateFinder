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
public final class FileUtilSettings implements SettingsHolder {

    @Comment("Set to always run the same task (Possible values: rename,addDate,duplicates,filecount,diff,tree)")
    public static final Property<Optional<String>> TASK = optionalStringProperty("core.task");

    @Comment("Format file size to human-readable units (e.g. 4.1 KB). If false, the number of bytes is always shown")
    public static final FuBooleanProperty FORMAT_FILE_SIZE = new FuBooleanProperty("core.formatFileSize", true);

    // --- Rename feature ---

    @Comment("Folder to rename files in (visits all child folders)")
    public static final Property<Optional<Path>> RENAME_FOLDER = newOptionalPathProperty("rename.folder");

    @Comment("Regex rename: regex to match files by (e.g. IMG_E(\\d+)\\.JPG)")
    public static final RegexProperty RENAME_REGEX_FROM = newRegexProperty("rename.regex.from", "IMG_E(\\d+)\\.JPG");

    @Comment("Regex rename: what to rename files to (use $1 etc. for capturing groups, e.g. IMG_$1E.JPG)")
    public static final StringProperty RENAME_REGEX_TO = new StringProperty("rename.regex.to", "IMG_$1E\\.JPG");

    @Comment({
        "",
        "Date rename: what to rename the file to. Placeholders:",
        "  {file} = full file name; {name} = file name without extension;",
        "  {ext} = extension (without dot), {date} = last modified date"
    })
    public static final StringProperty RENAME_DATE_TO = new StringProperty("rename.date.to", "{date}_{file}");

    @Comment({
        "Date rename: formatting of the last modified date.",
        "  See https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns"
    })
    public static final StringProperty RENAME_DATE_DATE_FORMAT =
        new StringProperty("rename.date.dateFormat", "yyyyMMdd");

    // --- Duplicate finder feature ---

    @Comment("Path to the folder to search inside for duplicates")
    public static final Property<Optional<Path>> DUPLICATE_FOLDER = newOptionalPathProperty("duplicates.folder");

    // todo: enum
    @Comment("Algorithm used to hash the file contents with")
    public static final StringProperty DUPLICATE_HASH_ALGORITHM = new StringProperty("duplicates.hash.algorithm", "sha1");

    @Comment({
        "If a file's size exceeds the below threshold (in MB), the file will not be sized.",
        "Rather, if the size is identical, it will be considered as a duplicate."
    })
    public static final FuDoubleProperty DUPLICATE_HASH_MAX_SIZE_MB =
        new FuDoubleProperty("duplicates.hash.maxSizeForHashingInMb", 300);

    @Comment({
        "Glob filter of files to ignore (empty to skip)",
        "  See https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)"
    })
    public static final StringProperty DUPLICATE_FILTER_BLACKLIST =
        new StringProperty("duplicates.filter.blacklist", "");
    @Comment("Glob filter of files to consider (empty to skip). See link on the above property.")
    public static final StringProperty DUPLICATE_FILTER_WHITELIST =
        new StringProperty("duplicates.filter.whitelist", "");

    @Comment("Min size of files to consider (in MB). Use 0 for no minimum file size.")
    public static final FuDoubleProperty DUPLICATE_FILTER_MIN_SIZE =
        new FuDoubleProperty("duplicates.filter.minSizeInMb", 0.0);

    @Comment("Max size of files to consider (in MB). Use 0 for no maximum file size.")
    public static final FuDoubleProperty DUPLICATE_FILTER_MAX_SIZE =
        new FuDoubleProperty("duplicates.filter.maxSizeInMb", 0.0);

    @Comment({
        "One file in a matched group of duplicates must match the given glob pattern for the group",
        "to be included in the result (empty to skip)."
    })
    public static final StringProperty DUPLICATE_FILTER_RESULT_WHITELIST =
        new StringProperty("duplicates.filter.resultWhitelist", "");

    @Comment({
        "The number of bytes configured in the property below will first be read and compared for files",
        "that exceed the file size configured in this property before the file's contents are hashed."
    })
    public static final FuDoubleProperty DUPLICATE_READ_BEFORE_HASH_MIN_SIZE =
        new FuDoubleProperty("duplicates.readBeforeHash.minSizeInMb", 1024);

    @Comment("The number of bytes to read from large files (see above property) before they are hashed")
    public static final FuIntegerProperty DUPLICATE_READ_BEFORE_HASH_BYTES_TO_READ =
        new FuIntegerProperty("duplicates.readBeforeHash.bytesToRead", 64);

    @Comment({
        "",
        "Configures what should be output. Should only be changed for debugging."
    })
    public static final FuBooleanProperty DUPLICATE_OUTPUT_DUPLICATES =
        new FuBooleanProperty("duplicates.output.showDuplicates", true);

    @Comment("(Debug) Shows the total number of found duplicates by folder pairs")
    public static final FuBooleanProperty DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT =
        new FuBooleanProperty("duplicates.output.showFolderPairCount", false);

    @Comment("(Debug) Lists the number of files grouped by their file size (no hashing)")
    public static final FuBooleanProperty DUPLICATE_OUTPUT_DISTRIBUTION =
        new FuBooleanProperty("duplicates.output.showDistribution", false);

    @Comment("(Debug) Logs which large files were not hashed because their initial contents were compared first")
    public static final FuBooleanProperty DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH =
        new FuBooleanProperty("duplicates.output.showDifferenceReadFilesVsHash", false);

    @Comment({
        "Interval in which a small output occurs to show progress.",
        "Must be a power of 2 minus 1 (e.g. 63, 127, 255, 511)"
    })
    public static final FuPowerOfTwoMinusOneProperty DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL =
        new FuPowerOfTwoMinusOneProperty("duplicates.output.progress.filesFoundInterval", 1023);

    @Comment("Number of files hashed after which a small output is made to show progress. Must be a power of 2 minus 1.")
    public static final FuPowerOfTwoMinusOneProperty DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL =
        new FuPowerOfTwoMinusOneProperty("duplicates.output.progress.filesHashedInterval", 511);

    // --- File count feature ---

    @Comment("Folder to aggregate the files in")
    public static final Property<Optional<Path>> FILE_COUNT_FOLDER = newOptionalPathProperty("fileCount.folder");

    @Comment({
        "Groups extensions to be shown as only one entry, separate by semi-colon.",
        "Useful to get an overview by file type (not file _extension_) when there are a lot of results.",
        "You can configure groups on the fly after running the 'filecount' command.",
        "Example: image .jpg,.png; text .txt,.html,.md; word .doc,.docx"
    })
    public static final StringProperty FILE_COUNT_GROUPS = new StringProperty("fileCount.groups", "");

    // --- Folder diff feature ---

    @Comment("The first folder to consider")
    public static final Property<Optional<Path>> DIFF_FOLDER1 = newOptionalPathProperty("diff.folder1");

    @Comment("The second folder to diff the first one with")
    public static final Property<Optional<Path>> DIFF_FOLDER2 = newOptionalPathProperty("diff.folder2");

    @Comment({
        "If true, a file is considered identical if the name, size and modification dates matches.",
        "When set to false, the modification date is not considered and the file contents are hashed instead."
    })
    public static final FuBooleanProperty DIFF_CHECK_BY_SIZE_AND_MODIFICATION_DATE =
        new FuBooleanProperty("diff.checkBySizeAndModificationDate", true);

    @Comment({
        "When enabled, the full path of the folders are inspected and a part is chosen to reference them.",
        "Set to false for output that uses folder1/ and folder2/ for the inspected folders."
    })
    public static final FuBooleanProperty DIFF_USE_SMART_FOLDER_PREFIXES =
        new FuBooleanProperty("diff.output.smartFolderPrefixes", true);

    @Comment("Number of files after which a small output is made to indicate progress. Must be a power of 2 minus 1")
    public static final FuPowerOfTwoMinusOneProperty DIFF_FILES_PROCESSED_INTERVAL =
        new FuPowerOfTwoMinusOneProperty("diff.output.progress.filesProcessedInterval", 1023);

    // --- File tree ---

    @Comment("The folder whose contents should be listed")
    public static final Property<Optional<Path>> TREE_FOLDER = newOptionalPathProperty("tree.folder");

    @Comment("The element types to show in the output. (Possible values: ALL, DIRECTORIES, FILES)")
    public static final FuOptionalEnumProperty<TreeDisplayMode> TREE_DISPLAY_MODE =
        new FuOptionalEnumProperty<>("tree.displayMode", TreeDisplayMode.class);

    @Comment({
        "Regex a file name must match to be included in the tree output. Empty to disable.",
        "The file name is given relative to the folder, e.g. if C:/acme/test is the folder to list, then a file at",
        "C:/acme/test/bills.doc will be tested against this regex as 'bills.doc'."
    })
    public static final Property<Optional<String>> TREE_FILE_REGEX =
        optionalStringProperty("tree.filter.fileRegex");

    @Comment("Regex a _directory_ name must match to be included in the tree output.")
    public static final Property<Optional<String>> TREE_DIRECTORY_REGEX =
        optionalStringProperty("tree.filter.directoryRegex");

    @Comment("Minimum size in MB a file must have to be included in the tree. -1 to disable.")
    public static final FuDoubleProperty TREE_FILE_MIN_SIZE_MB =
        new FuDoubleProperty("tree.filter.minSizeInMb", -1.0);

    @Comment("Maximum size in MB a file may have to be included in the tree. -1 to disable.")
    public static final FuDoubleProperty TREE_FILE_MAX_SIZE_MB =
        new FuDoubleProperty("tree.filter.maxSizeInMb", -1.0);

    @Comment({
        "Minimum number of items (directories, files) a directory must have to be included in the tree.",
        "-1 to disable."
    })
    public static final FuIntegerProperty TREE_MIN_ITEMS_IN_FOLDER =
        new FuIntegerProperty("tree.filter.minItemsInDir", -1);

    @Comment({
        "Maximum number of items (directories, files) a directory may have to be included in the tree.",
        "-1 to disable."
    })
    public static final FuIntegerProperty TREE_MAX_ITEMS_IN_FOLDER =
        new FuIntegerProperty("tree.filter.maxItemsInDir", -1);

    @Comment("Indent the output by level as to represent a tree.")
    public static final FuBooleanProperty TREE_INDENT_ELEMENTS =
        new FuBooleanProperty("tree.output.indentElements", true);

    @Comment({
        "If true, the absolute path to the files will be shown instead of the path relative to the provided folder.",
        "Disable indentation (one property above) if this is enabled for better output."
    })
    public static final FuBooleanProperty TREE_SHOW_ABSOLUTE_PATH =
        new FuBooleanProperty("tree.output.showAbsolutePath", false);

    @Comment("Number of files after which a small output is made to indicate progress. Must be a power of 2 minus 1.")
    public static final FuPowerOfTwoMinusOneProperty TREE_FILES_PROCESSED_INTERVAL =
        new FuPowerOfTwoMinusOneProperty("tree.output.progress.filesProcessedInterval", 255);

    private FileUtilSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        conf.setComment("rename",
            "#######", "Configuration for file renaming tasks (regex, date)", "#######");
        conf.setComment("duplicates",
            "#######", "Configuration for duplicate finder", "#######");
        conf.setComment("filecount",
            "#######", "File counter", "#######");
        conf.setComment("diff",
            "#######", "Folder diff", "#######");
        conf.setComment("tree",
            "#######", "File tree", "#######");
    }
}
