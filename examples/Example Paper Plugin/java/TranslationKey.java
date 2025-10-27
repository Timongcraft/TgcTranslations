package my.package;

import de.timongcraft.tgctranslations.TranslationKeyManager;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

public class TranslationKey implements TranslationKeyManager {

    static final Key IDENTIFICATION_KEY = Key.key("my_plugin", "translations");
    private static final List<String> ADVENTURE_KEYS = new ArrayList<>();

    public static final String MY_TRANSLATION_KEY = registerKey("xy.my_translation");

    private static String registerKey(String rawKey) {
        String key = TranslationKeyManager.getPrefix(IDENTIFICATION_KEY) + rawKey;
        ADVENTURE_KEYS.add(key);
        return key;
    }

    TranslationKey() {}

    @Override
    public boolean hasKey(String key) {
        for (String fqKey : ADVENTURE_KEYS)
            if (fqKey.equals(key))
                return true;
        return false;
    }

}