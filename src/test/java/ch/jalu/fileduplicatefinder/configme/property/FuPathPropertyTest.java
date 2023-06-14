package ch.jalu.fileduplicatefinder.configme.property;

import ch.jalu.configme.properties.convertresult.PropertyValue;
import ch.jalu.configme.resource.PropertyReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link FuPathProperty}.
 */
class FuPathPropertyTest {

    @Test
    void shouldReadFromResource() {
        // given
        FuPathProperty property = new FuPathProperty("system.logfile");
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getString("system.logfile")).willReturn("new.log");

        // when
        PropertyValue<Path> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(Paths.get("new.log"));
        assertThat(propertyValue.isValidInResource()).isEqualTo(true);
    }

    @Test
    void shouldReturnDefaultForNullValueFromResource() {
        // given
        FuPathProperty property = new FuPathProperty("system.logfile");
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getString("system.logfile")).willReturn(null);

        // when
        PropertyValue<Path> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(Paths.get("")); // default value
        assertThat(propertyValue.isValidInResource()).isEqualTo(false);
    }
}
