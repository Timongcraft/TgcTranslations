package my.package.translations;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Logger;

public class TranslationManager extends de.timongcraft.tgctranslations.TranslationManager {

    public static final Key IDENTIFICATION_KEY = Key.key("my_plugin", "translations");

    public TranslationManager(Logger logger, @Nullable Path overridePath) {
        super(logger, new TranslationKey(), IDENTIFICATION_KEY, overridePath);
    }

    public void load() {
        loadLanguage(Locale.US, "en_us.json");

        register();
    }

    public void unload() {
        unregister();

        getLanguages().clear();
    }

}