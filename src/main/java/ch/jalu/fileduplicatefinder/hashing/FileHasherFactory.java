package ch.jalu.fileduplicatefinder.hashing;

/**
 * Creates {@link FileHasher} instances.
 */
public class FileHasherFactory {

    /**
     * Returns a file hasher with the given hash algorithm.
     *
     * @param algorithm the algorithm to use
     * @return file hasher using the given algorithm
     */
    public FileHasher createFileHasher(HashingAlgorithm algorithm) {
        return new DefaultFileHasher(algorithm.getHashFunction());
    }
}
