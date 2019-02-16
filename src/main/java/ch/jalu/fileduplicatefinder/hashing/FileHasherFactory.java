package ch.jalu.fileduplicatefinder.hashing;

import com.google.common.hash.Hashing;

public class FileHasherFactory {

    public FileHasher createFileHasher(String algorithm, float maxSizeForHashingInMb) {
        switch (algorithm.toLowerCase()) {
            case "md5": return new DefaultFileHasher(Hashing.md5(), maxSizeForHashingInMb);
            case "sha1": return new DefaultFileHasher(Hashing.sha1(), maxSizeForHashingInMb);
            case "sha256": return new DefaultFileHasher(Hashing.sha256(), maxSizeForHashingInMb);
            case "crc32": return new DefaultFileHasher(Hashing.crc32(), maxSizeForHashingInMb);
            default:
                throw new IllegalArgumentException("Unknown algorithm '" + algorithm + "'");
        }
    }
}
