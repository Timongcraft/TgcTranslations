package de.timongcraft.tgctranslations;

public interface TranslationKeyManager {

    /**
     * Checks if the key manager contains the specified key.
     */
    boolean containsKey(String key);

    /**
     * Checks if the key manager contains the specified raw key i.e. without namespace.
     */
    boolean containsRawKey(String rawKey);

}