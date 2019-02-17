package ch.jalu.fileduplicatefinder.hashing;

import com.google.common.base.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.io.MoreFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ch.jalu.fileduplicatefinder.utils.PathUtils.megaBytesToBytes;

public class DefaultFileHasher implements FileHasher {

    private final HashFunction hashFunction;
    private final long maxSizeForHashingInBytes;

    public DefaultFileHasher(HashFunction hashFunction, double maxSizeForHashingInMb) {
        this.hashFunction = hashFunction;
        this.maxSizeForHashingInBytes = megaBytesToBytes(maxSizeForHashingInMb);
    }

    @Override
    public String calculateHash(Path path, long fileSize) throws IOException {
        Preconditions.checkArgument(Files.isRegularFile(path), "Path '" + path + "' must be a file");
        if (fileSize > maxSizeForHashingInBytes) {
            return "Size " + fileSize;
        }
        return MoreFiles.asByteSource(path).hash(hashFunction).toString();
    }
}
