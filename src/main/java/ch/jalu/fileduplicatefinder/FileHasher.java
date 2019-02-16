package ch.jalu.fileduplicatefinder;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.common.io.MoreFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHasher {

    public String calculateHash(Path path) throws IOException {
        Preconditions.checkArgument(Files.isRegularFile(path), "Path '" + path + "' must be a file");
        return MoreFiles.asByteSource(path).hash(Hashing.sha1()).toString();
    }
}
