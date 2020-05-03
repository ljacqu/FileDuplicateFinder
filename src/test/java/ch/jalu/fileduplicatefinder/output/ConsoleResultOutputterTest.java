package ch.jalu.fileduplicatefinder.output;

import ch.jalu.fileduplicatefinder.config.FileUtilConfiguration;
import ch.jalu.fileduplicatefinder.duplicatefinder.DuplicateEntry;
import ch.jalu.fileduplicatefinder.duplicatefinder.FolderPair;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_FOLDER;
import static ch.jalu.fileduplicatefinder.config.FileUtilProperties.DUPLICATE_OUTPUT_DUPLICATES;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link ConsoleResultOutputter}.
 */
class ConsoleResultOutputterTest {

    @Test
    void shouldHandleMissingDuplicates() {
        // given
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        TestConsoleResultOutputter resultOutputter = new TestConsoleResultOutputter(configuration);

        // when
        resultOutputter.outputResult(Collections.emptyList());

        // then
        assertThat(resultOutputter.getLines()).containsExactly("No duplicates found.");
    }

    @Test
    void shouldOutputDuplicates() {
        // given
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        String root = "root/folder/";
        given(configuration.getPath(DUPLICATE_FOLDER)).willReturn(Paths.get(root));
        given(configuration.getBoolean(DUPLICATE_OUTPUT_DUPLICATES)).willReturn(true);
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
        assertOutput(resultOutputter.getLines()).containsExactlyWithRightSeparator(
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
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        given(configuration.getBoolean(DUPLICATE_OUTPUT_DUPLICATES)).willReturn(false);
        TestConsoleResultOutputter resultOutputter = new TestConsoleResultOutputter(configuration);
        List<DuplicateEntry> duplicates = asList(
            new DuplicateEntry(347, "hash1",
                asList(Paths.get("foo.txt"), Paths.get("test/test.txt"))),
            new DuplicateEntry(127451, "someHash",
                asList(Paths.get("file1"), Paths.get("dir/file2.ext"), Paths.get("123/file3.pdf"))));

        // when
        resultOutputter.outputResult(duplicates);

        // then
        assertThat(resultOutputter.getLines()).isEmpty();
    }

    @Test
    void shouldOutputDuplicatesByFolderPair() {
        // given
        FileUtilConfiguration configuration = mock(FileUtilConfiguration.class);
        given(configuration.getPath(DUPLICATE_FOLDER)).willReturn(Paths.get("the-root"));
        TestConsoleResultOutputter resultOutputter = new TestConsoleResultOutputter(configuration);
        Map<FolderPair, Long> duplicatesByPair = ImmutableMap.of(
            new FolderPair(Paths.get("the-root/test"), Paths.get("the-root/test/first")), 2L,
            new FolderPair(Paths.get("the-root/test"), Paths.get("the-root/test")), 8L,
            new FolderPair(Paths.get("the-root/test/first"), Paths.get("the-root/test/second")), 10L);

        // when
        resultOutputter.outputDirectoryPairs(duplicatesByPair);

        // then
        assertOutput(resultOutputter.getLines()).containsExactlyWithRightSeparator(
            "10: test/first - test/second",
            "8: within test",
            "2: test - test/first");
    }

    private static ConsoleOutputAssertion assertOutput(List<String> actual) {
        return new ConsoleOutputAssertion(actual);
    }

    private static final class TestConsoleResultOutputter extends ConsoleResultOutputter {

        private final List<String> lines = new ArrayList<>();

        public TestConsoleResultOutputter(FileUtilConfiguration configuration) {
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

    private static final class ConsoleOutputAssertion extends ListAssert<String> {

        ConsoleOutputAssertion(List<String> actual) {
            super(actual);
        }

        /**
         * Same as {@link #containsExactly(Object[])} but replaces slashes
         * in the expected lines with {@link File#separator}.
         */
        ListAssert<String> containsExactlyWithRightSeparator(String... expectedLines) {
            String[] adaptedLines = Arrays.stream(expectedLines)
                .map(line -> line.replace("/", File.separator))
                .toArray(String[]::new);
            return containsExactly(adaptedLines);
        }
    }
}