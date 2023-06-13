package ch.jalu.fileduplicatefinder.utils;

import java.text.DecimalFormat;

public final class FileSizeUtils {

    private static final long BYTES_IN_KB = 1024L;
    private static final long BYTES_IN_MB = 1024L * 1024L;
    private static final long BYTES_IN_GB = 1024L * 1024L * 1024L;
    private static final long BYTES_IN_TB = 1024L * 1024L * 1024L * 1024L;

    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("0.0");

    private FileSizeUtils() {
    }

    /**
     * Converts the given amount in megabytes to bytes.
     *
     * @param megaBytes the megabytes to convert
     * @return the bytes
     */
    public static long megaBytesToBytes(double megaBytes) {
        return Math.round(megaBytes * BYTES_IN_MB);
    }

    public static String formatToHumanReadableSize(long sizeBytes) {
        if (sizeBytes < BYTES_IN_KB) {
            return sizeBytes + " B";
        }

        String unit;
        double sizeInUnit;
        if (sizeBytes < BYTES_IN_MB) {
            unit = "KB";
            sizeInUnit = (double) sizeBytes / BYTES_IN_KB;
        } else if (sizeBytes < BYTES_IN_GB) {
            unit = "MB";
            sizeInUnit = (double) sizeBytes / BYTES_IN_MB;
        } else if (sizeBytes < BYTES_IN_TB) {
            unit = "GB";
            sizeInUnit = (double) sizeBytes / BYTES_IN_GB;
        } else { // if sizeBytes >= BYTES_IN_TB
            unit = "TB";
            sizeInUnit = (double) sizeBytes / BYTES_IN_TB;
        }
        return ONE_DECIMAL.format(sizeInUnit) + " " + unit;
    }
}
