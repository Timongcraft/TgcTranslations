package my.package;

import de.timongcraft.tgctranslations.TranslationManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {

    private static TranslationManager translationManager;

    @Override
    public void onLoad() {
        translationManager = new TranslationManager(getLogger(), new TranslationKey(), TranslationKey.IDENTIFICATION_KEY,
                "my_plugin/lang", getDataPath().resolve("lang"));
        translationManager.load();
    }

    @Override
    public void onDisable() {
        translationManager.unload();
    }

}