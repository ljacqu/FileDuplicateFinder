package ch.jalu.fileduplicatefinder.duplicatefinder;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link FolderPairDuplicatesCounter}.
 */
class FolderPairDuplicatesCounterTest {

    @Test
    void shouldCountDuplicates() {
        // given
        DuplicateEntry entry1 = new DuplicateEntry(555, "def",
            newArrayList(Paths.get("test/one.txt"), Paths.get("test/two.txt"), Paths.get("test/sub/test.txt")));
        DuplicateEntry entry2 = new DuplicateEntry(876, "abc",
            newArrayList(Paths.get("test/sub/test.pdf"), Paths.get("test/other/foo.txt")));
        DuplicateEntry entry3 = new DuplicateEntry(123, "ghi",
            newArrayList(Paths.get("test/3.xml"), Paths.get("test/other/bar.html"), Paths.get("test/sub/baz.jpg")));

        // when
        Map<FolderPair, Long> result = new FolderPairDuplicatesCounter().getFolderToFolderDuplicateCount(Arrays.asList(entry1, entry2, entry3));

        // then
        Path testFolder = Paths.get("test");
        Path subFolder = Paths.get("test/sub");
        Path otherFolder = Paths.get("test/other");

        assertThat(result)
            .hasSize(4)
            .containsEntry(new FolderPair(testFolder, subFolder), 3L)
            .containsEntry(new FolderPair(otherFolder, subFolder), 2L)
            .containsEntry(new FolderPair(testFolder, testFolder), 1L)
            .containsEntry(new FolderPair(testFolder, otherFolder), 1L);
    }

}