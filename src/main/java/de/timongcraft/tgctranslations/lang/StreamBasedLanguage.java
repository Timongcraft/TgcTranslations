package de.timongcraft.tgctranslations.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import de.timongcraft.tgctranslations.TranslationKeyManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
 * Implementation of the {@link Language} interface using a stream for base translation
 */
@ApiStatus.Internal
public class StreamBasedLanguage implements Language {

    private final Locale locale;
    private final Map<String, String> translations = new HashMap<>();

    /**
     * Constructs a new {@link StreamBasedLanguage}.
     *
     * @param logger       the logger used to report issues during loading
     * @param keyManager   the translation key manager used to validate keys
     * @param prefix       the prefix for the translation keys
     * @param locale       the locale of this language
     * @param stream       the optional stream of the internal language definition
     * @param overridePath the optional path of the override language file
     */
    public StreamBasedLanguage(Logger logger, TranslationKeyManager keyManager, String prefix,
                               Locale locale, @Nullable InputStream stream,
                               @Nullable Path overridePath) {
        this.locale = locale;

        if (stream != null) {
            loadTranslations(stream, Source.INTERNAL, prefix, logger, keyManager);
        }

        if (overridePath != null && Files.exists(overridePath)) {
            try {
                loadTranslations(new FileInputStream(overridePath.toFile()), Source.USER_OVERRIDES, prefix, logger, keyManager);
            } catch (FileNotFoundException e) {
                logger.log(Level.WARNING, Source.USER_OVERRIDES.logName + " for language " + locale.toLanguageTag()
                        + " specified but not existent");
            }
        }
    }

    private void loadTranslations(InputStream stream, Source source, String prefix, Logger logger, TranslationKeyManager keyManager) {
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            for (Map.Entry<String, JsonElement> entry : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
                if (entry.getValue() instanceof JsonPrimitive primitive && primitive.isString()) {
                    if (!keyManager.hasKey(prefix + entry.getKey())) {
                        logger.log(Level.WARNING, source.logName + " for language " + locale.toLanguageTag()
                                + " contain unknown translation (Key: " + entry.getKey() + ")");
                        continue;
                    }

                    translations.put(prefix + entry.getKey(), primitive.getAsString());
                } else {
                    logger.log(Level.WARNING, source.logName + " for language " + locale.toLanguageTag()
                            + " contain unknown json entry (Key: " + entry.getKey() + ")");
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            logger.log(Level.WARNING, source.logName + " for language " + locale.toLanguageTag()
                    + " could not be loaded", e);
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
    public boolean hasKey(String key) {
        return translations.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable String translate(String key) {
        return translations.get(key);
    }

    private enum Source {

        INTERNAL("Built-in definition"),
        USER_OVERRIDES("Overrides");

        private final String logName;

        Source(String logName) {
            this.logName = logName;
        }

    }

}