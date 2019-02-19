package ch.jalu.fileduplicatefinder.hashing;

import ch.jalu.fileduplicatefinder.TestUtils;
import com.google.common.hash.Hashing;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Test for {@link DefaultFileHasher}.
 */
class DefaultFileHasherTest {

    private static Path folder = TestUtils.getTestSamplesFolder();

    @Test
    void shouldHashFile() throws IOException {
        // given
        Path file = folder.resolve("test_3.txt");
        FileHasher fileHasher = new DefaultFileHasher(Hashing.sha256());

        // when / then
        assertThat(fileHasher.calculateHash(file))
            .isEqualTo("bc5b37c5c197287669e35ea60a7819d09cd7b0ef26194485d926d526af1d70e0");
    }

    @Test
    void shouldThrowIfPathIsNotAFile() {
        // given
        Path file = folder;
        FileHasher fileHasher = new DefaultFileHasher(Hashing.sha256());

        // when / then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> fileHasher.calculateHash(file))
            .withMessageEndingWith("must be a file");
    }
}