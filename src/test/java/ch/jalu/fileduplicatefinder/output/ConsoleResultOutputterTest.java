package ch.jalu.fileduplicatefinder.output;

import ch.jalu.fileduplicatefinder.config.FileDupeFinderConfiguration;
import ch.jalu.fileduplicatefinder.duplicatefinder.DuplicateEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link ConsoleResultOutputter}.
 */
class ConsoleResultOutputterTest {

    @Test
    void shouldHandleMissingDuplicates() {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        TestConsoleResultOutputter resultOutputter = new TestConsoleResultOutputter(configuration);

        // when
        resultOutputter.outputResult(Collections.emptyList());

        // then
        assertThat(resultOutputter.getLines()).containsExactly("No duplicates found.");
    }

    @Test
    void shouldOutputDuplicates() {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        String root = "root/folder/";
        given(configuration.getRootFolder()).willReturn(Paths.get(root));
        given(configuration.isDuplicatesOutputEnabled()).willReturn(true);
        TestConsoleResultOutputter resultOutputter = new TestConsoleResultOutputter(configuration);
        List<DuplicateEntry> duplicates = asList(
            new DuplicateEntry(347, "hash1",
                asList(Paths.get(root, "foo.txt"), Paths.get(root, "test/test.txt"))),
            new DuplicateEntry((long) (120.3 * 1024), "someHash",
                asList(Paths.get(root, "file1"), Paths.get(root, "dir/file2.ext"), Paths.get(root, "123/file3.pdf"))),
            new DuplicateEntry((long) (381.4 * 1024 * 1024), "e44",
                asList(Paths.get(root, "abc/def/f1.png"), Paths.get(root, "abc/def/f2.png"))),
            new DuplicateEntry((long) (6.8 * 1024 * 1024 * 1024), "98c9ad6f0e",
                asList(Paths.get(root, "ab.zip"), Paths.get(root, "cd.zip"))));

        // when
        resultOutputter.outputResult(duplicates);

        // then
        assertThat(resultOutputter.getLines()).containsExactly(
            "[347 B][2] hash1: foo.txt, test/test.txt",
            "[120.3 KB][3] someHash: 123/file3.pdf, dir/file2.ext, file1",
            "[381.4 MB][2] e44: abc/def/f1.png, abc/def/f2.png",
            "[6.8 GB][2] 98c9ad6f0e: ab.zip, cd.zip",
            "Total duplicated data: 7.2 GB",
            "Total: 4 duplicates");
    }

    @Test
    void shouldNotOutputDuplicates() {
        // given
        FileDupeFinderConfiguration configuration = mock(FileDupeFinderConfiguration.class);
        given(configuration.isDuplicatesOutputEnabled()).willReturn(false);
        TestConsoleResultOutputter resultOutputter = new TestConsoleResultOutputter(configuration);
        List<DuplicateEntry> duplicates = asList(
            new DuplicateEntry(347, "hash1",
                asList(Paths.get("foo.txt"), Paths.get("test/test.txt"))),
            new DuplicateEntry(127451, "someHash",
                asList(Paths.get("file1"), Paths.get("dir/file2.ext"), Paths.get("123/file3.pdf"))));

        // when
        resultOutputter.outputResult(duplicates);

        // then
        verify(configuration).isDuplicatesOutputEnabled();
        assertThat(resultOutputter.getLines()).isEmpty();
    }

    private final class TestConsoleResultOutputter extends ConsoleResultOutputter {

        private final List<String> lines = new ArrayList<>();

        public TestConsoleResultOutputter(FileDupeFinderConfiguration configuration) {
            super(configuration);
        }

        @Override
        protected void output(String str) {
            lines.add(str);
        }

        List<String> getLines() {
            return lines;
        }
    }
}