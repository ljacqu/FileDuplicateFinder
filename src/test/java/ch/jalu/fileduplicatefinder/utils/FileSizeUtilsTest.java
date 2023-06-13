package ch.jalu.fileduplicatefinder.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link FileSizeUtils}.
 */
class FileSizeUtilsTest {

    @Test
    void shouldConvertNumberInBytesToMegaBytes() {
        // given / when / then
        assertThat(FileSizeUtils.megaBytesToBytes(3)).isEqualTo(3145728);
        assertThat(FileSizeUtils.megaBytesToBytes(1.002)).isEqualTo(1050673);
        assertThat(FileSizeUtils.megaBytesToBytes(0)).isEqualTo(0);
        assertThat(FileSizeUtils.megaBytesToBytes(2.45)).isEqualTo(2569011);
    }

    @Test
    void shouldConvertFileSizeToHumanReadableText() {
        // given / when / then
        assertThat(FileSizeUtils.formatToHumanReadableSize(0)).isEqualTo("0 B");
        assertThat(FileSizeUtils.formatToHumanReadableSize(1020)).isEqualTo("1020 B");
        assertThat(FileSizeUtils.formatToHumanReadableSize(1024)).isEqualTo("1.0 KB");
        assertThat(FileSizeUtils.formatToHumanReadableSize(10_000)).isEqualTo("9.8 KB");
        assertThat(FileSizeUtils.formatToHumanReadableSize(1_048_576)).isEqualTo("1.0 MB");
        assertThat(FileSizeUtils.formatToHumanReadableSize(4_718_592)).isEqualTo("4.5 MB");
        assertThat(FileSizeUtils.formatToHumanReadableSize(4_718_900)).isEqualTo("4.5 MB");
        assertThat(FileSizeUtils.formatToHumanReadableSize(1_073_636_966)).isEqualTo("1023.9 MB");
        assertThat(FileSizeUtils.formatToHumanReadableSize(1_073_741_824)).isEqualTo("1.0 GB");
        assertThat(FileSizeUtils.formatToHumanReadableSize(3_393_024_163L)).isEqualTo("3.2 GB");
        assertThat(FileSizeUtils.formatToHumanReadableSize(293_877_016_900L)).isEqualTo("273.7 GB");
        assertThat(FileSizeUtils.formatToHumanReadableSize(1_099_511_627_775L)).isEqualTo("1024.0 GB");
        assertThat(FileSizeUtils.formatToHumanReadableSize(1_099_511_627_776L)).isEqualTo("1.0 TB");
        assertThat(FileSizeUtils.formatToHumanReadableSize(41_444_111_444_111L)).isEqualTo("37.7 TB");
    }
}
