package de.timongcraft.tgctranslations.lang;

import java.util.Locale;

/**
 * Represents a language with methods to get the {@link Locale} and translate translation keys.
 */
public interface Language {

    /**
     * Gets the locale of this language.
     */
    Locale getLocale();

    /**
     * Checks if the language contains the specified key.
     */
    boolean hasKey(String key);

    /**
     * Translates the given translation key to its corresponding value.
     * <p>
     * Note: The value may use {@link net.kyori.adventure.text.minimessage.MiniMessage} formatting.
     */
    String translate(String translationKey);

}