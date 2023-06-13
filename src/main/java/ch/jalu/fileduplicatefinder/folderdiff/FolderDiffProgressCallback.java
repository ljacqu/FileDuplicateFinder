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
     * Called per the configured progress iteration when a certain number of files has been found.
     *
     * @param numberOfFoundFiles the number of found files
     */
    void notifyScanProgress(int numberOfFoundFiles);

    /**
     * Called when all files have been collected and the comparison of files will proceed.
     */
    void startAnalysis();

    /**
     * Called per the configured progress iteration when a certain number of files have been processed.
     *
     * @param numberOfHandledFiles the number of files that were compared
     */
    void notifyAnalysisProgress(int numberOfHandledFiles);

}
