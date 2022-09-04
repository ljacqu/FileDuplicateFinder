package ch.jalu.fileduplicatefinder.hashing;

import com.google.common.hash.Hashing;

import java.util.Locale;

/**
 * Creates {@link FileHasher} instances.
 */
public class FileHasherFactory {

    /**
     * Returns a file hasher with the given hash algorithm or throws an exception
     * if the hash algorithm is unknown.
     *
     * @param algorithm the algorithm to use
     * @return file hasher using the given algorithm
     */
    @SuppressWarnings("deprecation")
    public FileHasher createFileHasher(String algorithm) {
        switch (algorithm.toLowerCase(Locale.ROOT)) {
            case "md5": return new DefaultFileHasher(Hashing.md5());
            case "sha1": return new DefaultFileHasher(Hashing.sha1());
            case "sha256": return new DefaultFileHasher(Hashing.sha256());
            case "crc32": return new DefaultFileHasher(Hashing.crc32());
            default:
                throw new IllegalArgumentException("Unknown algorithm '" + algorithm + "'");
        }
    }
}
