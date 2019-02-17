package ch.jalu.fileduplicatefinder.hashing;

import com.google.common.base.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.io.MoreFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultFileHasher implements FileHasher {

    private final HashFunction hashFunction;

    public DefaultFileHasher(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    @Override
    public String calculateHash(Path path) throws IOException {
        Preconditions.checkArgument(Files.isRegularFile(path), "Path '" + path + "' must be a file");
        return MoreFiles.asByteSource(path).hash(hashFunction).toString();
    }
}
