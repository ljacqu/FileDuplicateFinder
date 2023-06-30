package ch.jalu.fileduplicatefinder.hashing;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Hashing algorithms files are hashed with to check for equality.
 */
public enum HashingAlgorithm {

    /** Guava's "good fast hash". See {@link Hashing#goodFastHash} for which guarantees can be made. */
    GFH(Hashing.goodFastHash(128)),

    /** CRC32. */
    CRC32(Hashing.crc32()),

    /** SHA1 (deprecated by Guava). */
    SHA1(Hashing.sha1()),

    /** SHA256. */
    SHA256(Hashing.sha256());

    private final HashFunction hashFunction;

    HashingAlgorithm(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public HashFunction getHashFunction() {
        return hashFunction;
    }
}
