package ch.jalu.fileduplicatefinder.hashing;

import java.io.IOException;
import java.nio.file.Path;

public interface FileHasher {

    String calculateHash(Path path, long fileSize) throws IOException;

}
