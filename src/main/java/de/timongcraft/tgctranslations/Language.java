package de.timongcraft.tgctranslations;

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
     * Translates the given translation key to its corresponding value.
     * <p>
     * Note: The value may be in {@link net.kyori.adventure.text.minimessage.MiniMessage} format.
     */
    String translate(String translationKey);

}