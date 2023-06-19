package ch.jalu.fileduplicatefinder.folderdiff;

/**
 * Interface to be notified about the folder diff's progress.
 */
interface FolderDiffProgressCallback {

    /**
     * Called right before the first folder is read.
     */
    void startScan();

    /**
     * Called whenever a file has been found.
     */
    void notifyScanProgress();

    /**
     * Called when all files have been collected and the comparison of files will proceed.
     */
    void startAnalysis();

    /**
     * Called for every file after it has been hashed.
     */
    void notifyAnalysisProgress();

}
