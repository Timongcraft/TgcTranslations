package de.timongcraft.tgctranslations;

import de.timongcraft.tgctranslations.lang.Language;
import de.timongcraft.tgctranslations.lang.StreamBasedLanguage;
import de.timongcraft.tgctranslations.utils.ResourceUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Manages translations for different locales using {@link Language} implementations.
 * Implements {@link TranslationRegistry} to provide translation lookups through net.kiory's adventure library.
 */
@SuppressWarnings("unused")
public class TranslationManager implements TranslationRegistry {

    public final TranslatableComponentRenderer<Locale> renderer = TranslatableComponentRenderer.usingTranslationSource(this);
    private final Map<Locale, Language> languages = new HashMap<>();
    private final Logger logger;
    private final TranslationKeyManager keyManager;
    private final Key identificationKey;
    private final String resourceFolderPath;
    private final @Nullable Path overridesFolderPath;
    private Locale defaultLocale; // parameter > Locale.getDefault() > Locale.US > first loaded

    /**
     * Constructs a new {@code TranslationManager}.
     *
     * @param logger            the logger used to report issues during language loading
     * @param keyManager        the translation key manager used to validate keys
     * @param identificationKey the {@link Key} identifying this translation manager
     */
    public TranslationManager(Logger logger, TranslationKeyManager keyManager, Key identificationKey,
                              String resourceFolderPath) {
        this(logger, keyManager, identificationKey, resourceFolderPath, null);
    }

    /**
     * Constructs a new {@code TranslationManager}.
     *
     * @param logger              the logger used to report issues during language loading
     * @param keyManager          the translation key manager used to validate keys
     * @param identificationKey   the {@link Key} identifying this translation manager
     * @param overridesFolderPath the {@link Path} to the overrides language folder (null to disable overrides)
     */
    public TranslationManager(Logger logger, TranslationKeyManager keyManager, Key identificationKey,
                              String resourceFolderPath,
                              @Nullable Path overridesFolderPath) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.keyManager = Objects.requireNonNull(keyManager, "keyManager");
        this.identificationKey = Objects.requireNonNull(identificationKey, "identificationKey");
        this.resourceFolderPath = Objects.requireNonNull(resourceFolderPath, "resourceFolderPath");
        this.overridesFolderPath = overridesFolderPath;
    }

    /**
     * Constructs a new {@code TranslationManager}.
     *
     * @param logger              the logger used to report issues during language loading
     * @param keyManager          the translation key manager used to validate keys
     * @param identificationKey   the {@link Key} identifying this translation manager
     * @param defaultLocale       the optional {@link Locale} used as default
     * @param overridesFolderPath the {@link Path} to the overrides language folder (null to disable overrides)
     */
    public TranslationManager(Logger logger, TranslationKeyManager keyManager, Key identificationKey, @Nullable Locale defaultLocale,
                              String resourceFolderPath,
                              @Nullable Path overridesFolderPath) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.keyManager = Objects.requireNonNull(keyManager, "keyManager");
        this.identificationKey = Objects.requireNonNull(identificationKey, "identificationKey");
        this.defaultLocale = defaultLocale;
        this.resourceFolderPath = Objects.requireNonNull(resourceFolderPath, "resourceFolderPath");
        this.overridesFolderPath = overridesFolderPath;
    }

    public void loadLanguages() {
        Map<String, InputStream> internalDefinitions = ResourceUtils.getFileStreams(resourceFolderPath, keyManager.getClass().getClassLoader(), logger);
        Map<String, Path> overrideDefinitions = new HashMap<>();

        if (overridesFolderPath != null && Files.exists(overridesFolderPath)) {
            try (Stream<Path> paths = Files.walk(overridesFolderPath, 1)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".json"))
                        .forEach(path -> overrideDefinitions.put(path.getFileName().toString(), path));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Unable to read language overrides from " + overridesFolderPath, e);
            }
        }

        Set<String> combinedFileNames = new HashSet<>() {{
            addAll(internalDefinitions.keySet());
            addAll(overrideDefinitions.keySet());
        }};

        for (String fileName : combinedFileNames) {
            addLanguage(new StreamBasedLanguage(
                    logger,
                    keyManager,
                    identificationKey.namespace(),
                    Locale.forLanguageTag(fileName.substring(0, fileName.length() - 5).replace("_", "-")),
                    internalDefinitions.get(fileName),
                    overrideDefinitions.get(fileName)
            ));
        }
    }

    /**
     * Registers this translation manager to be used in global translations.
     */
    public void load() {
        loadLanguages();

        if (defaultLocale == null) {
            calcDefaultLocale();
        }

        GlobalTranslator.translator().addSource(this);
    }

    /**
     * Unregisters this translation manager from global translations.
     */
    public void unload() {
        GlobalTranslator.translator().removeSource(this);

        languages.clear();
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
        final String literalTranslation = translateLiteral(component.key(), locale);
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

    private String translateLiteral(String key, Locale locale) {
        if (!contains(key)) return null;
        Language language = languages.get(locale);
        if (language == null) {
            if (locale == defaultLocale) return null;
            return translateLiteral(key, defaultLocale);
        }

        return language.translate(key);
    }

    private void calcDefaultLocale() {
        Locale systemLocale = Locale.getDefault();

        for (Locale loadedLocale : languages.keySet()) {
            if (!loadedLocale.equals(systemLocale)) continue;
            defaultLocale = systemLocale;
            return;
        }

        for (Locale loadedLocale : languages.keySet()) {
            if (!loadedLocale.equals(Locale.US)) continue;
            defaultLocale = Locale.US;
            return;
        }

        defaultLocale = languages.keySet().stream().findFirst().orElse(Locale.US);
    }

    public Locale defaultLocale() {
        return defaultLocale;
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
        throw new UnsupportedOperationException();
    }

    /**
     * Unused method placeholder.
     */
    @Override
    public void unregister(@NotNull String key) {
        throw new UnsupportedOperationException();
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

}