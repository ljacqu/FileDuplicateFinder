package ch.jalu.fileduplicatefinder.folderdiff;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.hashing.FileHasher;
import ch.jalu.fileduplicatefinder.hashing.FileHasherFactory;
import ch.jalu.fileduplicatefinder.hashing.HashingAlgorithm;
import ch.jalu.fileduplicatefinder.utils.FileSizeUtils;
import ch.jalu.fileduplicatefinder.utils.PathUtils;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DIFF_CHECK_BY_SIZE_AND_MODIFICATION_DATE;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_HASH_ALGORITHM;
import static ch.jalu.fileduplicatefinder.config.FileUtilSettings.DUPLICATE_HASH_MAX_SIZE_MB;

/**
 * Compares all files (recursively) of two given folders and returns the differences it finds: new or deleted files,
 * modified files as well as moved files.
 * <p>
 * Files that have the exact same contents but a different file name will be considered as renamed, files with the
 * same name but different contents are considered modified. This tool does not otherwise associate files with each
 * other, i.e. if a file was renamed and slightly modified, one file will be listed as deleted and the other file
 * will be listed as newly created.
 */
public class FolderDiffAnalyzer {

    private final Path folder1;
    private final Path folder2;
    private final FileUtilConfiguration configuration;
    private final FileHasher fileHasher;

    // Configs
    private long maxSizeBytesForHashing;
    private boolean checkSizeAndModificationDate;


    public FolderDiffAnalyzer(Path folder1, Path folder2, FileUtilConfiguration configuration,
                              FileHasherFactory fileHasherFactory) {
        this.folder1 = folder1;
        this.folder2 = folder2;
        this.configuration = configuration;

        HashingAlgorithm hashAlgorithm = configuration.getValue(DUPLICATE_HASH_ALGORITHM);
        this.fileHasher = fileHasherFactory.createFileHasher(hashAlgorithm);
    }

    public List<FileDifference> collectDifferences(FolderDiffProgressCallback progressCallback) {
        Preconditions.checkArgument(Files.isDirectory(folder1),
            "Path '" + folder1.toAbsolutePath() + "' is not a directory");
        Preconditions.checkArgument(Files.isDirectory(folder2),
            "Path '" + folder2.toAbsolutePath() + "' is not a directory");

        maxSizeBytesForHashing = FileSizeUtils.megaBytesToBytes(configuration.getValue(DUPLICATE_HASH_MAX_SIZE_MB));
        checkSizeAndModificationDate = configuration.getValue(DIFF_CHECK_BY_SIZE_AND_MODIFICATION_DATE);

        ProgressHandler progressHandler = new ProgressHandler(progressCallback);
        progressCallback.startScan();
        LinkedHashMap<String, FileElement> folder1ElementsByRelPath = new LinkedHashMap<>();
        process(folder1, folder1, folder1ElementsByRelPath, progressHandler);

        LinkedHashMap<String, FileElement> folder2ElementsByRelPath = new LinkedHashMap<>();
        process(folder2, folder2, folder2ElementsByRelPath, progressHandler);

        progressCallback.startAnalysis();
        return findDifferences(folder1ElementsByRelPath, folder2ElementsByRelPath, progressHandler);
    }

    private List<FileDifference> findDifferences(LinkedHashMap<String, FileElement> folder1ElementsByRelPath,
                                                 LinkedHashMap<String, FileElement> folder2ElementsByRelPath,
                                                 ProgressHandler progressHandler) {
        List<FileDifference> differences = new ArrayList<>();

        Map<String, List<FileElement>> unmatchedF1ElementsByHash = new LinkedHashMap<>();
        folder1ElementsByRelPath.forEach((f1Path, f1Elem) -> {
            FileElement f2Elem = folder2ElementsByRelPath.get(f1Path);
            if (f2Elem == null) {
                String f1Hash = createHashOrSizeString(f1Elem);
                unmatchedF1ElementsByHash.computeIfAbsent(f1Hash, k -> new ArrayList<>()).add(f1Elem);
            } else if (!filesMatchByConfiguredProperties(f1Elem, f2Elem)) {
                differences.add(new FileDifference(f1Elem, f2Elem));
            }
            progressHandler.incrementAnalyzedFiles();
        });

        Map<String, List<FileElement>> unmatchedF2ElementsByHash = new LinkedHashMap<>();
        folder2ElementsByRelPath.forEach((f2Path, f2Elem) -> {
            if (!folder1ElementsByRelPath.containsKey(f2Path)) {
                String f2Hash = createHashOrSizeString(f2Elem);
                unmatchedF2ElementsByHash.computeIfAbsent(f2Hash, k -> new ArrayList<>()).add(f2Elem);
            }
            progressHandler.incrementAnalyzedFiles();
        });

        unmatchedF1ElementsByHash.forEach((f1Hash, f1Elems) -> {
            List<FileElement> f2Elems = unmatchedF2ElementsByHash.get(f1Hash);
            if (f2Elems == null) {
                f2Elems = Collections.emptyList();
            }

            Iterator<FileElement> f1It = f1Elems.iterator();
            Iterator<FileElement> f2It = f2Elems.iterator();
            while (f1It.hasNext() && f2It.hasNext()) {
                differences.add(new FileDifference(f1It.next(), f2It.next()));
            }
            while (f1It.hasNext()) {
                differences.add(new FileDifference(f1It.next(), null));
            }
            while (f2It.hasNext()) {
                differences.add(new FileDifference(null, f2It.next()));
            }
        });
        return differences;
    }

    private boolean filesMatchByConfiguredProperties(FileElement f1Elem, FileElement f2Elem) {
        if (f1Elem.getSize() == f2Elem.getSize()) {
            if (checkSizeAndModificationDate) {
                return f1Elem.readLastModifiedTime().equals(f2Elem.readLastModifiedTime());
            } else {
                return createHashOrSizeString(f1Elem).equals(createHashOrSizeString(f2Elem));
            }
        }
        return false;
    }

    private void process(Path root, Path folder, Map<String, FileElement> elemsByRelativePath,
                         ProgressHandler progressHandler) {
        PathUtils.list(folder).forEach(element -> {
            if (Files.isRegularFile(element)) {
                FileElement fileElement = new FileElement(root, element);
                elemsByRelativePath.put(fileElement.getName(), fileElement);
                progressHandler.incrementScannedFiles();
            } else if (Files.isDirectory(element)) {
                process(root, element, elemsByRelativePath, progressHandler);
            }
        });
    }

    private String createHashOrSizeString(FileElement fileElement) {
        if (maxSizeBytesForHashing > 0 && fileElement.getSize() > maxSizeBytesForHashing) {
            return "size=" + fileElement.getSize();
        }

        Path file = fileElement.getFile();
        try {
            return fileHasher.calculateHash(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to hash contents of file '" + file.toAbsolutePath() + "'", e);
        }
    }

    private static final class ProgressHandler {

        private final FolderDiffProgressCallback folderDiffProgressCallback;

        /**
         * Constructor.
         *
         * @param folderDiffProgressCallback progress callback to use
         */
        ProgressHandler(FolderDiffProgressCallback folderDiffProgressCallback) {
            this.folderDiffProgressCallback = folderDiffProgressCallback;
        }

        void incrementScannedFiles() {
            folderDiffProgressCallback.notifyScanProgress();
        }

        void incrementAnalyzedFiles() {
            folderDiffProgressCallback.notifyAnalysisProgress();
        }
    }
}
