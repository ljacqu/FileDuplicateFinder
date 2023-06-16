package ch.jalu.fileduplicatefinder.config;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.utils.SettingsHolderClassValidator;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link FileUtilSettings}.
 */
class FileUtilSettingsTest {

    @Test
    void shouldHaveValidProperties() {
        SettingsHolderClassValidator validator = new FileUtilSettingsValidator();
        validator.validate(FileUtilSettings.class);
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