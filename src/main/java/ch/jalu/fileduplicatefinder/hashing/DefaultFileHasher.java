package ch.jalu.fileduplicatefinder.hashing;

import ch.jalu.fileduplicatefinder.utils.PathUtils;
import com.google.common.base.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.io.MoreFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultFileHasher implements FileHasher {


    private final HashFunction hashFunction;
    private final double maxSizeForHashingInMb;

    public DefaultFileHasher(HashFunction hashFunction, double maxSizeForHashingInMb) {
        this.hashFunction = hashFunction;
        this.maxSizeForHashingInMb = maxSizeForHashingInMb;
    }

    @Override
    public String calculateHash(Path path) throws IOException {
        Preconditions.checkArgument(Files.isRegularFile(path), "Path '" + path + "' must be a file");
        long filesize = Files.size(path);
        if (PathUtils.getFileSizeInMegaBytes(filesize) > maxSizeForHashingInMb) {
            return "Size " + filesize;
        }
        return MoreFiles.asByteSource(path).hash(hashFunction).toString();
    }
}
