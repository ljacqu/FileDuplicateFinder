package ch.jalu.fileduplicatefinder.folderdiff;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link FolderDiffRunner}.
 */
class FolderDiffRunnerTest {

    @Test
    void shouldReturnFolderPrefixes() {
        // given
        FolderDiffRunner diffRunner = new FolderDiffRunner(null, null);
        String separator = File.separator;

        Path cPath1 = Paths.get("C:/fox/data");
        Path cPath2 = Paths.get("C:/cat/data");
        Path cPath3 = Paths.get("C:/data");
        Path dPath1 = Paths.get("D:/data");
        Path dPath2 = Paths.get("D:/documents/archive/offers/drafts");
        Path dPath3 = Paths.get("D:/documents/current/offers/drafts");
        Path dPath4 = Paths.get("D:/documents/current/drafts");

        // when / then
        assertThat(diffRunner.getPrefixesForFolders(cPath1, cPath2)).containsExactly("fox…" + separator, "cat…" + separator);
        assertThat(diffRunner.getPrefixesForFolders(cPath1, cPath3)).containsExactly("fox…" + separator, "C:" + separator + "…" + separator);
        assertThat(diffRunner.getPrefixesForFolders(cPath3, cPath2)).containsExactly("C:" + separator + "…" + separator, "cat…" + separator);

        assertThat(diffRunner.getPrefixesForFolders(cPath3, dPath1)).containsExactly("C:" + separator + "…" + separator, "D:" + separator + "…" + separator);
        assertThat(diffRunner.getPrefixesForFolders(dPath1, cPath2)).containsExactly("D:" + separator + "…" + separator, "cat…" + separator);

        assertThat(diffRunner.getPrefixesForFolders(dPath2, dPath3)).containsExactly("folder1" + separator, "folder2" + separator);
        assertThat(diffRunner.getPrefixesForFolders(dPath2, dPath4)).containsExactly("offers…" + separator, "current…" + separator);
        assertThat(diffRunner.getPrefixesForFolders(dPath4, dPath3)).containsExactly("current…" + separator, "offers…" + separator);
    }

}