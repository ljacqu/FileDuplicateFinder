package ch.jalu.fileduplicatefinder.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.stream.Stream;

import static ch.jalu.fileduplicatefinder.TestUtils.getTestSamplesFolder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;


/**
 * Test for {@link PathUtils}.
 */
class PathUtilsTest {

    private static Path folder = getTestSamplesFolder();

    @Test
    void shouldNegatePathMatcher() {
        // given
        PathMatcher pathMatcher1 = Objects::nonNull;
        PathMatcher pathMatcher2 = null;

        // when
        PathMatcher result1 = PathUtils.negatePathMatcher(pathMatcher1);
        PathMatcher result2 = PathUtils.negatePathMatcher(pathMatcher2);

        // then
        assertThat(result1.matches(null)).isTrue();
        assertThat(result1.matches(Paths.get(""))).isFalse();
        assertThat(result2).isNull();
    }

    @Test
    void shouldListFiles() {
        // given
        Path file = folder;

        // when
        Stream<Path> result = PathUtils.list(file);

        // then
        Stream<String> fileNamesStream = result.map(p -> p.getFileName().toString());
        assertThat(fileNamesStream)
            .containsExactlyInAnyOrder("test_1.json", "test_2.txt", "test_3.txt",
                "test_4.csv", "test_5.csv", "test_6.csv", "test_7.xml");
    }

    @Test
    void shouldWrapIoExceptionWhenListingFiles() {
        // given
        Path file = Paths.get("bogus");

        // when / then
        assertThatExceptionOfType(UncheckedIOException.class)
            .isThrownBy(() -> PathUtils.list(file))
            .withCauseInstanceOf(NoSuchFileException.class);
    }

    @Test
    void shouldReturnSizeOfFile() {
        // given
        Path file = folder.resolve("test_2.txt");

        // when
        long size = PathUtils.size(file);

        // then
        assertThat(size).isEqualTo(15);
    }

    @Test
    void shouldThrowIfPathIsNotAFile() {
        // given
        Path file = Paths.get("bogus");

        // when / then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> PathUtils.size(file))
            .withMessageEndingWith("is not a file");
    }

    @Test
    void shouldReturnLastModifiedTime() throws IOException {
        // given
        Path file = folder.resolve("test_4.csv");
        ZonedDateTime date = ZonedDateTime.of(2022, 9, 4, 15, 13, 11, 0, ZoneId.systemDefault());
        Files.setLastModifiedTime(file, FileTime.from(Instant.from(date)));

        // when
        FileTime lastModified = PathUtils.getLastModifiedTime(file);

        // then
        Instant instant = lastModified.toInstant();
        LocalDate lastModifiedLocalDate = LocalDate.ofInstant(instant, ZoneOffset.UTC);
        assertThat(lastModifiedLocalDate).isEqualTo(LocalDate.of(2022, 9, 4));
    }

    @Test
    void shouldWrapExceptionIfLastModifiedTimeCannotBeRetrieved() {
        // given
        Path file = folder.resolve("bogus");

        // when / then
        assertThatExceptionOfType(UncheckedIOException.class)
            .isThrownBy(() -> PathUtils.getLastModifiedTime(file));
    }

    @Test
    void shouldReturnToStringForPathsInNullSafeManner() {
        // given / when / then
        assertThat(PathUtils.toStringNullSafe(Paths.get("example.txt"))).isEqualTo("example.txt");
        assertThat(PathUtils.toStringNullSafe(null)).isNull();
    }
}