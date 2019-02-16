package ch.jalu.fileduplicatefinder.hashing;

import com.google.common.base.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.io.MoreFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultFileHasher implements FileHasher {

    private static final float BYTES_IN_MEGA_BYTE = 1024 * 1024;

    private final HashFunction hashFunction;
    private final float maxSizeForHashingInMb;

    public DefaultFileHasher(HashFunction hashFunction, float maxSizeForHashingInMb) {
        this.hashFunction = hashFunction;
        this.maxSizeForHashingInMb = maxSizeForHashingInMb;
    }

    @Override
    public String calculateHash(Path path) throws IOException {
        Preconditions.checkArgument(Files.isRegularFile(path), "Path '" + path + "' must be a file");
        float filesize = Files.size(path);
        if (filesize / BYTES_IN_MEGA_BYTE > maxSizeForHashingInMb) {
            return "Size " + filesize;
        }
        return MoreFiles.asByteSource(path).hash(hashFunction).toString();
    }
}
