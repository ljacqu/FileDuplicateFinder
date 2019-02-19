package ch.jalu.fileduplicatefinder.hashing;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Hashes a file's contents.
 */
public interface FileHasher {

    /**
     * Computes a hash of the file's contents.
     *
     * @param path the file to hash
     * @return the hash
     * @throws IOException .
     */
    String calculateHash(Path path) throws IOException;

}
