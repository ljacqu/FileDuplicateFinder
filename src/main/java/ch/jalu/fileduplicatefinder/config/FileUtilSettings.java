package ch.jalu.fileduplicatefinder.config;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.fileduplicatefinder.config.property.JfuBooleanProperty;
import ch.jalu.fileduplicatefinder.config.property.JfuDoubleProperty;
import ch.jalu.fileduplicatefinder.config.property.JfuEnumProperty;
import ch.jalu.fileduplicatefinder.config.property.JfuIntegerProperty;
import ch.jalu.fileduplicatefinder.config.property.JfuOptionalProperty;
import ch.jalu.fileduplicatefinder.config.property.JfuPowerOfTwoMinusOneProperty;
import ch.jalu.fileduplicatefinder.config.property.JfuRegexProperty;
import ch.jalu.fileduplicatefinder.config.property.JfuStringProperty;
import ch.jalu.fileduplicatefinder.hashing.HashingAlgorithm;
import ch.jalu.fileduplicatefinder.tree.TreeDisplayMode;

import java.nio.file.Path;
import java.util.regex.Pattern;

import static ch.jalu.fileduplicatefinder.config.property.JfuOptionalProperty.newOptionalDirectoryProperty;
import static ch.jalu.fileduplicatefinder.config.property.JfuOptionalProperty.newOptionalStringProperty;

/**
 * Defines all properties for the file utils.
 */
public final class FileUtilSettings implements SettingsHolder {

    @Comment("Set to always run the same task (Possible values: rename,addDate,duplicates,filecount,diff,tree)")
    public static final JfuOptionalProperty<String> TASK = newOptionalStringProperty("core.task");

    @Comment("Format file size to human-readable units (e.g. 4.1 KB). If false, the number of bytes is always shown")
    public static final JfuBooleanProperty FORMAT_FILE_SIZE = new JfuBooleanProperty("core.formatFileSize", true);

    // --- Rename feature ---

    @Comment("Folder to rename files in (visits all child folders)")
    public static final JfuOptionalProperty<Path> RENAME_FOLDER = newOptionalDirectoryProperty("rename.folder");

    @Comment("Regex rename: regex to match files by (e.g. IMG_E(\\d+)\\.JPG)")
    public static final JfuRegexProperty RENAME_REGEX_FROM =
        new JfuRegexProperty("rename.regex.from", Pattern.compile("IMG_E(\\d+)\\.JPG"));

    @Comment("Regex rename: what to rename files to (use $1 etc. for capturing groups, e.g. IMG_$1E.JPG)")
    public static final JfuStringProperty RENAME_REGEX_TO = new JfuStringProperty("rename.regex.to", "IMG_$1E.JPG");

    @Comment({
        "",
        "Date rename: what to rename the file to. Placeholders:",
        "  {file} = full file name; {name} = file name without extension;",
        "  {ext} = extension (without dot), {date} = last modified date"
    })
    public static final JfuStringProperty RENAME_DATE_TO = new JfuStringProperty("rename.date.to", "{date}_{file}");

    @Comment({
        "Date rename: formatting of the last modified date.",
        "  See https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns"
    })
    public static final JfuStringProperty RENAME_DATE_DATE_FORMAT =
        new JfuStringProperty("rename.date.dateFormat", "yyyyMMdd");

    // --- Duplicate finder feature ---

    @Comment("Path to the folder to search inside for duplicates")
    public static final JfuOptionalProperty<Path> DUPLICATE_FOLDER = newOptionalDirectoryProperty("duplicates.folder");

    @Comment({
        "Algorithm used to hash the file contents with. (Supported values: GFH, CRC32, SHA1, SHA256)",
        "GFH is Guava's 'good fast hash' with 128 bits. "
            + "These hashes from GFH are not the same when the program is rerun later!"
    })
    public static final JfuEnumProperty<HashingAlgorithm> DUPLICATE_HASH_ALGORITHM =
        new JfuEnumProperty<>(HashingAlgorithm.class, "duplicates.hash.algorithm", HashingAlgorithm.GFH);

    @Comment({
        "If a file's size exceeds the below threshold (in MB), the file will not be sized.",
        "Rather, if the size is identical, it will be considered as a duplicate."
    })
    public static final JfuDoubleProperty DUPLICATE_HASH_MAX_SIZE_MB =
        new JfuDoubleProperty("duplicates.hash.maxSizeForHashingInMb", 300);

    @Comment({
        "Glob filter of files to ignore (empty to skip)",
        "  See https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)"
    })
    public static final JfuStringProperty DUPLICATE_FILTER_BLACKLIST =
        new JfuStringProperty("duplicates.filter.blacklist", "");
    @Comment("Glob filter of files to consider (empty to skip). See link on the above property.")
    public static final JfuStringProperty DUPLICATE_FILTER_WHITELIST =
        new JfuStringProperty("duplicates.filter.whitelist", "");

    @Comment("Min size of files to consider (in MB). Use 0 for no minimum file size.")
    public static final JfuDoubleProperty DUPLICATE_FILTER_MIN_SIZE =
        new JfuDoubleProperty("duplicates.filter.minSizeInMb", 0.0);

    @Comment("Max size of files to consider (in MB). Use 0 for no maximum file size.")
    public static final JfuDoubleProperty DUPLICATE_FILTER_MAX_SIZE =
        new JfuDoubleProperty("duplicates.filter.maxSizeInMb", 0.0);

    @Comment({
        "One file in a matched group of duplicates must match the given glob pattern for the group",
        "to be included in the result (empty to skip)."
    })
    public static final JfuStringProperty DUPLICATE_FILTER_RESULT_WHITELIST =
        new JfuStringProperty("duplicates.filter.resultWhitelist", "");

    @Comment({
        "The number of bytes configured in the property below will first be read and compared for files",
        "that exceed the file size configured in this property before the file's contents are hashed."
    })
    public static final JfuDoubleProperty DUPLICATE_READ_BEFORE_HASH_MIN_SIZE =
        new JfuDoubleProperty("duplicates.readBeforeHash.minSizeInMb", 1024);

    @Comment("The number of bytes to read from large files (see above property) before they are hashed")
    public static final JfuIntegerProperty DUPLICATE_READ_BEFORE_HASH_BYTES_TO_READ =
        new JfuIntegerProperty("duplicates.readBeforeHash.bytesToRead", 64);

    @Comment({
        "",
        "Configures what should be output. Should only be changed for debugging."
    })
    public static final JfuBooleanProperty DUPLICATE_OUTPUT_DUPLICATES =
        new JfuBooleanProperty("duplicates.output.showDuplicates", true);

    @Comment("(Debug) Shows the total number of found duplicates by folder pairs")
    public static final JfuBooleanProperty DUPLICATE_OUTPUT_FOLDER_PAIR_COUNT =
        new JfuBooleanProperty("duplicates.output.showFolderPairCount", false);

    @Comment("(Debug) Lists the number of files grouped by their file size (no hashing)")
    public static final JfuBooleanProperty DUPLICATE_OUTPUT_DISTRIBUTION =
        new JfuBooleanProperty("duplicates.output.showDistribution", false);

    @Comment("(Debug) Logs which large files were not hashed because their initial contents were compared first")
    public static final JfuBooleanProperty DUPLICATE_OUTPUT_DIFFERENCE_READ_FILES_VS_HASH =
        new JfuBooleanProperty("duplicates.output.showDifferenceReadFilesVsHash", false);

    @Comment({
        "Interval in which a small output occurs to show progress.",
        "Must be a power of 2 minus 1 (e.g. 63, 127, 255, 511)"
    })
    public static final JfuPowerOfTwoMinusOneProperty DUPLICATE_OUTPUT_PROGRESS_FILES_FOUND_INTERVAL =
        new JfuPowerOfTwoMinusOneProperty("duplicates.output.progress.filesFoundInterval", 1023);

    @Comment("Number of files hashed after which a small output is made to show progress. Must be a power of 2 minus 1.")
    public static final JfuPowerOfTwoMinusOneProperty DUPLICATE_OUTPUT_PROGRESS_FILES_HASHED_INTERVAL =
        new JfuPowerOfTwoMinusOneProperty("duplicates.output.progress.filesHashedInterval", 511);

    // --- File count feature ---

    @Comment("Folder to aggregate the files in")
    public static final JfuOptionalProperty<Path> FILE_COUNT_FOLDER = newOptionalDirectoryProperty("fileCount.folder");

    @Comment({
        "Groups extensions to be shown as only one entry, separate by semi-colon.",
        "Useful to get an overview by file type (not file _extension_) when there are a lot of results.",
        "You can configure groups on the fly after running the 'filecount' command.",
        "Example: image .jpg,.png; text .txt,.html,.md; word .doc,.docx"
    })
    public static final JfuStringProperty FILE_COUNT_GROUPS = new JfuStringProperty("fileCount.groups", "");

    @Comment("Output the individual stats of extensions that have been grouped?")
    public static final JfuBooleanProperty FILE_COUNT_DETAILED_GROUPS =
        new JfuBooleanProperty("fileCount.output.includeGroupDetails", false);

    // --- Folder diff feature ---

    @Comment("The first folder to consider")
    public static final JfuOptionalProperty<Path> DIFF_FOLDER1 = newOptionalDirectoryProperty("diff.folder1");

    @Comment("The second folder to diff the first one with")
    public static final JfuOptionalProperty<Path> DIFF_FOLDER2 = newOptionalDirectoryProperty("diff.folder2");

    @Comment({
        "If true, a file is considered identical if the name, size and modification dates matches.",
        "When set to false, the modification date is not considered and the file contents are hashed instead."
    })
    public static final JfuBooleanProperty DIFF_CHECK_BY_SIZE_AND_MODIFICATION_DATE =
        new JfuBooleanProperty("diff.checkBySizeAndModificationDate", true);

    @Comment({
        "When enabled, the full path of the folders are inspected and a part is chosen to reference them.",
        "Set to false for output that uses folder1/ and folder2/ for the inspected folders."
    })
    public static final JfuBooleanProperty DIFF_USE_SMART_FOLDER_PREFIXES =
        new JfuBooleanProperty("diff.output.smartFolderPrefixes", true);

    @Comment("Number of files after which a small output is made to indicate progress. Must be a power of 2 minus 1")
    public static final JfuPowerOfTwoMinusOneProperty DIFF_FILES_PROCESSED_INTERVAL =
        new JfuPowerOfTwoMinusOneProperty("diff.output.progress.filesProcessedInterval", 1023);

    // --- File tree ---

    @Comment("The folder whose contents should be listed")
    public static final JfuOptionalProperty<Path> TREE_FOLDER = newOptionalDirectoryProperty("tree.folder");

    @Comment({
        "Regex a file name must match to be included in the tree output. Empty to disable.",
        "The file name is given relative to the folder, e.g. if C:/acme/test is the folder to list, then a file at",
        "C:/acme/test/bills.doc will be tested against this regex as 'bills.doc'."
    })
    public static final JfuRegexProperty TREE_FILE_REGEX =
        new JfuRegexProperty("tree.filter.fileRegex", Pattern.compile(""));

    @Comment("Regex a _directory_ name must match to be included in the tree output.")
    public static final JfuRegexProperty TREE_DIRECTORY_REGEX =
        new JfuRegexProperty("tree.filter.directoryRegex", Pattern.compile(""));

    @Comment("Minimum size in MB a file must have to be included in the tree. -1 to disable.")
    public static final JfuDoubleProperty TREE_FILE_MIN_SIZE_MB =
        new JfuDoubleProperty("tree.filter.minSizeInMb", -1.0);

    @Comment("Maximum size in MB a file may have to be included in the tree. -1 to disable.")
    public static final JfuDoubleProperty TREE_FILE_MAX_SIZE_MB =
        new JfuDoubleProperty("tree.filter.maxSizeInMb", -1.0);

    @Comment({
        "Minimum number of items (directories, files) a directory must have to be included in the tree.",
        "-1 to disable."
    })
    public static final JfuIntegerProperty TREE_MIN_ITEMS_IN_FOLDER =
        new JfuIntegerProperty("tree.filter.minItemsInDir", -1);

    @Comment({
        "Maximum number of items (directories, files) a directory may have to be included in the tree.",
        "-1 to disable."
    })
    public static final JfuIntegerProperty TREE_MAX_ITEMS_IN_FOLDER =
        new JfuIntegerProperty("tree.filter.maxItemsInDir", -1);

    @Comment("The element types to show in the output. (Possible values: ALL, DIRECTORIES, FILES)")
    public static final JfuEnumProperty<TreeDisplayMode> TREE_OUTPUT_ELEMENT_TYPES =
        new JfuEnumProperty<>(TreeDisplayMode.class, "tree.output.elementTypes", TreeDisplayMode.ALL);

    @Comment("Sort by file size (if false, sorting is done alphabetically)")
    public static final JfuBooleanProperty TREE_SORT_FILES_BY_SIZE =
        new JfuBooleanProperty("tree.output.sortBySize", false);

    @Comment("Indent the output by level as to represent a tree. Not possible if sorting results by size.")
    public static final JfuBooleanProperty TREE_INDENT_ELEMENTS =
        new JfuBooleanProperty("tree.output.indentElements", true);

    @Comment({
        "If true, the absolute path to the files will be shown instead of the path relative to the provided folder.",
        "Disable indentation (one property above) if this is enabled for better output."
    })
    public static final JfuBooleanProperty TREE_SHOW_ABSOLUTE_PATH =
        new JfuBooleanProperty("tree.output.showAbsolutePath", false);

    @Comment("Number of files after which a small output is made to indicate progress. Must be a power of 2 minus 1.")
    public static final JfuPowerOfTwoMinusOneProperty TREE_FILES_PROCESSED_INTERVAL =
        new JfuPowerOfTwoMinusOneProperty("tree.output.progress.filesProcessedInterval", 255);

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
