package ch.jalu.fileduplicatefinder.config;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.utils.SettingsHolderClassValidator;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for {@link FileUtilSettings}.
 */
class FileUtilSettingsTest {

    @Test
    void shouldHaveValidProperties() {
        SettingsHolderClassValidator validator = new FileUtilSettingsValidator();
        validator.validate(FileUtilSettings.class);
    }

    @Test
    void shouldNotHaveCommentsOnIntermediatePaths() {
        // given
        ConfigurationData configData = ConfigurationDataBuilder.createConfiguration(FileUtilSettings.class);
        Set<String> checkedPaths = new HashSet<>();

        // when / then
        for (Property<?> property : configData.getProperties()) {
            checkAllIntermediatePaths(configData, property.getPath(), checkedPaths);
        }
    }

    private void checkAllIntermediatePaths(ConfigurationData configurationData,
                                           String path,
                                           Set<String> processedPaths) {
        int lastDotIndex = path.lastIndexOf('.');
        String parent = lastDotIndex >= 0 ? path.substring(0, lastDotIndex) : null;
        while (parent != null && parent.indexOf('.') >= 0) {
            if (!processedPaths.add(parent)) {
                return; // parent already checked
            }

            if (!configurationData.getCommentsForSection(parent).isEmpty()) {
                fail("Found comments set to intermediate path '" + parent
                    + "', but the .properties resource cannot write such comments!");
            }

            parent = parent.substring(0, parent.lastIndexOf('.'));
        }
    }

    private static final class FileUtilSettingsValidator extends SettingsHolderClassValidator {

        /*
         * Overridden to set a larger admissible max comment length.
         */
        @Override
        public void validate(Iterable<Class<? extends SettingsHolder>> settingHolders) {
            validateAllPropertiesAreConstants(settingHolders);
            validateSettingsHolderClassesFinal(settingHolders);
            validateClassesHaveHiddenNoArgConstructor(settingHolders);

            // Note: creating the ConfigurationData with the default builder validates that
            // no properties have overlapping paths
            ConfigurationData configurationData = createConfigurationData(settingHolders);
            validateHasCommentOnEveryProperty(configurationData, null);
            validateCommentLengthsAreWithinBounds(configurationData, null, 120);
            validateHasAllEnumEntriesInComment(configurationData, null);
        }
    }
}