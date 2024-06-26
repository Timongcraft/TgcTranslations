package my.package;

import my.package.translations.TranslationManager;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {

    private TranslationManager translationManager;

    @Override
    public void onLoad() {
        translationManager = new TranslationManager(getLogger(), getDataFolder().toPath().resolve("lang"));
        translationManager.load();
    }

    @Override
    public void onDisable() {
        translationManager.unload();
    }

}