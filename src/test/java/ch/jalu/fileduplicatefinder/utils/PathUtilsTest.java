package ch.jalu.fileduplicatefinder.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Test for {@link PathUtils}.
 */
class PathUtilsTest {

    @Test
    void shouldConvertNumberInBytesToMegaBytes() {
        // given / when / then
        assertThat(PathUtils.megaBytesToBytes(3)).isEqualTo(3145728);
        assertThat(PathUtils.megaBytesToBytes(1.002)).isEqualTo(1050673);
        assertThat(PathUtils.megaBytesToBytes(0)).isEqualTo(0);
        assertThat(PathUtils.megaBytesToBytes(2.45)).isEqualTo(2569011);
    }
}