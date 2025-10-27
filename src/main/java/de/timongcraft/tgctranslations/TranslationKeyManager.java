package de.timongcraft.tgctranslations;

import net.kyori.adventure.key.Key;

public interface TranslationKeyManager {

    static String getPrefix(Key identificationKey) {
        return identificationKey.namespace() + ".";
    }

    /**
     * Checks if the key manager contains the specified key.
     */
    boolean hasKey(String key);

}