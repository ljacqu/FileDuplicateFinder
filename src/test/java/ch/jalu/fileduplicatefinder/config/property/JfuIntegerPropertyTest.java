package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.PropertyValue;
import ch.jalu.configme.resource.PropertyReader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link JfuIntegerProperty}.
 */
class JfuIntegerPropertyTest {

    @Test
    void shouldReadFromResource() {
        // given
        JfuIntegerProperty property = new JfuIntegerProperty("sample.size", 69);
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("sample.size")).willReturn("75");

        // when
        PropertyValue<Integer> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(75);
        assertThat(propertyValue.isValidInResource()).isEqualTo(true);
    }

    @Test
    void shouldRejectInvalidValueFromResource() {
        // given
        JfuIntegerProperty property = new JfuIntegerProperty("sample.size", 1);
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("sample.size")).willReturn("invalid");

        // when
        PropertyValue<Integer> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(1); // default value
        assertThat(propertyValue.isValidInResource()).isEqualTo(false);
    }

    @Test
    void shouldReturnValueForNullFromResource() {
        // given
        JfuIntegerProperty property = new JfuIntegerProperty("sample.size", 1);
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("sample.size")).willReturn(null);

        // when
        PropertyValue<Integer> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(1); // default value
        assertThat(propertyValue.isValidInResource()).isEqualTo(false);
    }
}