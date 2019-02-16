package ch.jalu.fileduplicatefinder.hashing;

import java.io.IOException;
import java.nio.file.Path;

public interface FileHasher {

    String calculateHash(Path path) throws IOException;

}
