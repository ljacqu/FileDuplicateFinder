package ch.jalu.fileduplicatefinder.config.property;

import ch.jalu.configme.properties.convertresult.PropertyValue;
import ch.jalu.configme.resource.PropertyReader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link JfuDoubleProperty}.
 */
class JfuDoublePropertyTest {

    @Test
    void shouldReadFromResource() {
        // given
        JfuDoubleProperty property = new JfuDoubleProperty("sample.size", 69);
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("sample.size")).willReturn("44.25");

        // when
        PropertyValue<Double> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(44.25);
        assertThat(propertyValue.isValidInResource()).isEqualTo(true);
    }

    @Test
    void shouldRejectInvalidValueFromResource() {
        // given
        JfuDoubleProperty property = new JfuDoubleProperty("sample.size", 7.0);
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("sample.size")).willReturn("invalid");

        // when
        PropertyValue<Double> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(7.0); // default value
        assertThat(propertyValue.isValidInResource()).isEqualTo(false);
    }

    @Test
    void shouldReturnValueForNullFromResource() {
        // given
        JfuDoubleProperty property = new JfuDoubleProperty("sample.size", 7.0);
        PropertyReader reader = mock(PropertyReader.class);
        given(reader.getObject("sample.size")).willReturn(null);

        // when
        PropertyValue<Double> propertyValue = property.determineValue(reader);

        // then
        assertThat(propertyValue.getValue()).isEqualTo(7.0); // default value
        assertThat(propertyValue.isValidInResource()).isEqualTo(false);
    }
}