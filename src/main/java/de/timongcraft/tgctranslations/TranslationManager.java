package de.timongcraft.tgctranslations;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Manages translations for different locales using {@link Language} implementations.
 * Implements {@link TranslationRegistry} to provide translation lookups through net.kiory's adventure library.
 */
@SuppressWarnings("unused")
public class TranslationManager implements TranslationRegistry {

    private final Map<Locale, Language> languages = new HashMap<>();
    private final Logger logger;
    private final TranslationKeyManager keyManager;
    private final Key identificationKey;
    private final @Nullable Path overridePath;
    private Locale defaultLocale = Locale.US;

    /**
     * Constructs a new {@code TranslationManager}.
     *
     * @param logger            the logger for logging errors while loading {@link ResourceFileLanguage}s
     * @param keyManager        the manager for checking translation keys
     * @param identificationKey the {@link Key} identifying this translation manager
     * @param overridePath      the {@link Path} to the override language folder (null to disable overrides)
     */
    public TranslationManager(Logger logger, TranslationKeyManager keyManager, Key identificationKey, @Nullable Path overridePath) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.keyManager = Objects.requireNonNull(keyManager, "keyManager");
        this.identificationKey = Objects.requireNonNull(identificationKey, "identificationKey");
        this.overridePath = Objects.requireNonNull(overridePath, "overridePath");
    }

    /**
     * Loads a language from the specified file and locale.
     *
     * @param locale           the locale of the language to load
     * @param languageFileName the name of the language file (may also be in the override {@link Path} if {@link #overridePath} is not null)
     */
    public void loadLanguage(Locale locale, String languageFileName) {
        addLanguage(new ResourceFileLanguage(logger, keyManager, identificationKey.namespace(),
                Objects.requireNonNull(locale, "locale"),
                Objects.requireNonNull(languageFileName, "languageFileName"),
                overridePath));
    }

    /**
     * Registers this translation manager to be used in global translations.
     */
    public void register() {
        GlobalTranslator.translator().addSource(this);
    }

    /**
     * Unregisters this translation manager from global translations.
     */
    public void unregister() {
        GlobalTranslator.translator().removeSource(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(@NotNull String key) {
        return keyManager.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Key name() {
        return identificationKey;
    }

    /**
     * Unused method placeholder.
     */
    @Override
    public MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component translate(TranslatableComponent component, @NotNull Locale locale) {
        final String literalTranslation = translate0(component.key(), locale);
        if (literalTranslation == null) return null;

        Component resultingComponent;

        if (component.arguments().isEmpty()) {
            resultingComponent = MiniMessage.miniMessage().deserialize(literalTranslation);
        } else {
            List<Component> translatedArguments = new ArrayList<>(component.arguments().size());
            for (TranslationArgument argument : component.arguments())
                translatedArguments.add(GlobalTranslator.render(argument.asComponent(), locale));
            resultingComponent = MiniMessage.miniMessage().deserialize(literalTranslation,
                    new MiniMessageArgumentTag(translatedArguments));
        }

        for (Map.Entry<TextDecoration, TextDecoration.State> entry : component.decorations().entrySet())
            if (resultingComponent.decoration(entry.getKey()) == TextDecoration.State.NOT_SET)
                resultingComponent = resultingComponent.decoration(entry.getKey(), entry.getValue());
        resultingComponent = resultingComponent.colorIfAbsent(component.color());

        if (component.children().isEmpty()) {
            return resultingComponent;
        } else {
            return resultingComponent.children(
                    Stream.concat(
                            resultingComponent.children().stream(),
                            component.children().stream()
                    ).toList()
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void defaultLocale(@NotNull Locale locale) {
        defaultLocale = locale;
    }

    /**
     * Unused method placeholder.
     */
    @Override
    public void register(@NotNull String key, @NotNull Locale locale, @NotNull MessageFormat format) {
        throw new IllegalStateException("Unsupported operation");
    }

    /**
     * Unused method placeholder.
     */
    @Override
    public void unregister(@NotNull String key) {
        throw new IllegalStateException("Unsupported operation");
    }

    /**
     * Retrieves the map of loaded languages.
     */
    public Map<Locale, Language> getLanguages() {
        return languages;
    }

    private void addLanguage(Language language) {
        languages.put(language.getLocale(), language);
    }

    private String translate0(String key, Locale locale) {
        if (!contains(key)) return null;
        Language language = languages.get(locale);
        if (language == null) {
            if (locale == defaultLocale) return null;
            return translate0(key, defaultLocale);
        }

        return language.translate(key);
    }

}