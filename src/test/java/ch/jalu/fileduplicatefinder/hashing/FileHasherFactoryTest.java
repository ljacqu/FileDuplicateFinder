package ch.jalu.fileduplicatefinder.hashing;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

/**
 * Test for {@link FileHasherFactory}.
 */
class FileHasherFactoryTest {

    @Test
    void shouldCreateFileHasher() {
        // given
        List<String> algorithms = Arrays.asList("sha1", "sha256", "md5", "crc32");
        FileHasherFactory fileHasherFactory = new FileHasherFactory();

        // when / then
        algorithms.forEach(algorithm -> {
            assertThat(fileHasherFactory.createFileHasher(algorithm))
                .as("File hasher '" + algorithm + "'")
                .isNotNull();
        });
    }

    @Test
    void shouldThrowForUnknownAlgorithm() {
        // given
        String algorithm = "bogus";
        FileHasherFactory fileHasherFactory = new FileHasherFactory();

        // when / then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> fileHasherFactory.createFileHasher(algorithm))
            .withMessage("Unknown algorithm 'bogus'");
    }
}