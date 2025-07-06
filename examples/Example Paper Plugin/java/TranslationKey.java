package my.package;

import de.timongcraft.tgctranslations.TranslationKeyManager;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

public class TranslationKey implements TranslationKeyManager {

    static final Key IDENTIFICATION_KEY = Key.key("my_plugin", "translations");
    private static final String NAMESPACE = IDENTIFICATION_KEY.namespace() + ":";
    private static final List<String> RAW_ADVENTURE_KEYS = new ArrayList<>();

    public static final String MY_TRANSLATION_KEY = registerKey("xy.my_translation");

    @Override
    public boolean containsKey(String translationKey) {
        for (String rawAdventureKey : RAW_ADVENTURE_KEYS)
            if ((NAMESPACE + rawAdventureKey).equals(translationKey))
                return true;
        return false;
    }

    @Override
    public boolean containsRawKey(String rawTranslationKey) {
        for (String rawAdventureKey : RAW_ADVENTURE_KEYS)
            if (rawAdventureKey.equals(rawTranslationKey))
                return true;
        return false;
    }

    private static String registerKey(String rawAdventureKey) {
        RAW_ADVENTURE_KEYS.add(rawAdventureKey);
        return NAMESPACE + rawAdventureKey;
    }

}