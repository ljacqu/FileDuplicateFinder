package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.PropertyValue;
import ch.jalu.configme.resource.PropertyReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link JfuDirectoryProperty}.
 */
class JfuDirectoryPropertyTest {

    @Test
    void shouldReadFromResource() {
        // given
        JfuDirectoryProperty property = new JfuDirectoryProperty("system.logfile", Paths.get("."));
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("system.logfile")).willReturn("..");

        // when
        PropertyValue<Path> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(Paths.get(".."));
        assertThat(propertyValue.isValidInResource()).isEqualTo(true);
    }

    @Test
    void shouldReturnDefaultForNullValueFromResource() {
        // given
        JfuDirectoryProperty property = new JfuDirectoryProperty("system.logfile", Paths.get("."));
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("system.logfile")).willReturn(null);

        // when
        PropertyValue<Path> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(Paths.get(".")); // default value
        assertThat(propertyValue.isValidInResource()).isEqualTo(false);
    }

    @Test
    void shouldReturnDefaultForNonExistentDirectory() {
        // given
        JfuDirectoryProperty property = new JfuDirectoryProperty("system.logfile", Paths.get("."));
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("system.logfile")).willReturn("does-not-exist-23987");

        // when
        PropertyValue<Path> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(Paths.get(".")); // default value
        assertThat(propertyValue.isValidInResource()).isEqualTo(false);
    }
}
