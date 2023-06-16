package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.PropertyValue;
import ch.jalu.configme.resource.PropertyReader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link JfuBooleanProperty}.
 */
class JfuBooleanPropertyTest {

    @Test
    void shouldReadFromResource() {
        // given
        JfuBooleanProperty property = new JfuBooleanProperty("example.isTest", true);
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("example.isTest")).willReturn("false");

        // when
        PropertyValue<Boolean> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(false);
        assertThat(propertyValue.isValidInResource()).isEqualTo(true);
    }

    @Test
    void shouldRejectInvalidValueFromResource() {
        // given
        JfuBooleanProperty property = new JfuBooleanProperty("example.isTest", true);
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("example.isTest")).willReturn(2);

        // when
        PropertyValue<Boolean> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(true); // default value
        assertThat(propertyValue.isValidInResource()).isEqualTo(false);
    }

    @Test
    void shouldReturnValueForNullFromResource() {
        // given
        JfuBooleanProperty property = new JfuBooleanProperty("example.isTest", true);
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("example.isTest")).willReturn(null);

        // when
        PropertyValue<Boolean> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(true); // default value
        assertThat(propertyValue.isValidInResource()).isEqualTo(false);
    }
}