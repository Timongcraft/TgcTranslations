package de.timongcraft.tgctranslations;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@link Language} interface that loads translations from resources.
 */
@ApiStatus.Internal
public class ResourceFileLanguage implements Language {

    private final Logger logger;
    private final TranslationKeyManager keyManager;
    private final String namespace;
    private final Locale locale;
    private final Map<String, String> translations = new HashMap<>();

    /**
     * Constructs a new {@code ResourceFileLanguage}.
     *
     * @param logger           the logger for logging messages
     * @param keyManager       the manager for handling translation keys
     * @param namespace        the namespace for the translation keys
     * @param locale           the locale of the language
     * @param languageFileName the name of the language file (located under '/lang' in the resources)
     * @param overridePath     the {@link Path} to the override language folder (null to disable overrides)
     */
    public ResourceFileLanguage(Logger logger, TranslationKeyManager keyManager, String namespace,
                                Locale locale, String languageFileName,
                                @Nullable Path overridePath) {
        this.logger = logger;
        this.keyManager = keyManager;
        this.namespace = namespace.endsWith(":") ? namespace : namespace + ":";
        this.locale = locale;

        try (InputStream resourceStream = keyManager.getClass().getClassLoader()
                .getResourceAsStream("lang/" + languageFileName)) {

            loadTranslations(resourceStream, "resources", languageFileName);

            if (overridePath != null) {
                Path langFilePath = overridePath.resolve(languageFileName);
                if (Files.exists(langFilePath)) {
                    loadTranslations(new FileInputStream(langFilePath.toFile()), "overrides", languageFileName);
                }
            }

        } catch (Exception e) {
            logger.log(Level.WARNING,
                    "Unable to load language " + locale.getDisplayName() + " from " + languageFileName,
                    e
            );
        }
    }

    private void loadTranslations(InputStream inputStream, String source, String languageFileName) {
        if (inputStream != null) {
            for (Map.Entry<String, JsonElement> entry : JsonParser
                    .parseReader(new InputStreamReader(inputStream)).getAsJsonObject().entrySet()) {
                if (entry.getValue() instanceof JsonPrimitive primitive && primitive.isString()) {
                    if (!keyManager.containsRawKey(entry.getKey())) {
                        logger.log(Level.WARNING,
                                "Language " + locale.getDisplayName() + " from " + languageFileName +
                                        " contains unknown translation key (" + entry.getKey() + ") sourced from " + source);
                        continue;
                    }

                    translations.put(namespace + entry.getKey(), primitive.getAsString());
                } else {
                    logger.warning(
                            "Language " + locale.getDisplayName() + " from " + languageFileName +
                                    " contains none-language value entries sourced from " + source
                    );
                }
            }
        } else {
            logger.warning(
                    "Unable to load language " + locale.getDisplayName() + " from " + languageFileName +
                            " sourced from " + source
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable String translate(String key) {
        return translations.get(key);
    }

}