package ch.jalu.fileduplicatefinder.duplicatefinder;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
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
            newHashSet(Paths.get("test/sub/test.pdf"), Paths.get("test/other/foo.txt")));
        DuplicateEntry entry3 = new DuplicateEntry(123, "ghi",
            newHashSet(Paths.get("test/3.xml"), Paths.get("test/other/bar.html"), Paths.get("test/sub/baz.jpg")));

        // when
        List<String> result = new FolderPairDuplicatesCounter().getFolderToFolderDuplicateCount(Arrays.asList(entry1, entry2, entry3));

        // then
        assertThat(result).hasSize(4);
        assertThat(result.get(0)).isEqualTo("3: test - test/sub");
        assertThat(result.get(1)).isEqualTo("2: test/other - test/sub");
        assertThat(result.get(2)).isEqualTo("1: within test");
        assertThat(result.get(3)).isEqualTo("1: test - test/other");
    }

}