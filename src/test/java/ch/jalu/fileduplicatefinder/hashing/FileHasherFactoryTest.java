package ch.jalu.fileduplicatefinder.hashing;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Test for {@link FileHasherFactory}.
 */
class FileHasherFactoryTest {

    @Test
    void shouldCreateFileHasher() {
        // given
        List<HashingAlgorithm> algorithms = Arrays.asList(HashingAlgorithm.values());
        FileHasherFactory fileHasherFactory = new FileHasherFactory();

        // when / then
        algorithms.forEach(algorithm -> {
            assertThat(fileHasherFactory.createFileHasher(algorithm))
                .as("File hasher '" + algorithm + "'")
                .isNotNull();
        });
    }
}