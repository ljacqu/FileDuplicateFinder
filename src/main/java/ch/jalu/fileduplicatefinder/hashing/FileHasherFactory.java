package ch.jalu.fileduplicatefinder.hashing;

import com.google.common.hash.Hashing;

public class FileHasherFactory {

    public FileHasher createFileHasher(String algorithm) {
        switch (algorithm.toLowerCase()) {
            case "md5": return new DefaultFileHasher(Hashing.md5());
            case "sha1": return new DefaultFileHasher(Hashing.sha1());
            case "sha256": return new DefaultFileHasher(Hashing.sha256());
            case "crc32": return new DefaultFileHasher(Hashing.crc32());
            default:
                throw new IllegalArgumentException("Unknown algorithm '" + algorithm + "'");
        }
    }
}
