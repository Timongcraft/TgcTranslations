package de.timongcraft.tgctranslations.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

record ComponentTranslatableImpl(String key) implements ComponentTranslatable {

    @NotNull
    @Override
    public String translationKey() {
        return key;
    }

    @NotNull
    @Override
    public TranslatableComponent asComponent() {
        return Component.translatable(key);
    }

}