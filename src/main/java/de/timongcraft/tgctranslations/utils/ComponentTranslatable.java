package de.timongcraft.tgctranslations.utils;

import java.util.Locale;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translatable;
import org.jetbrains.annotations.NotNull;

public interface ComponentTranslatable extends Translatable, ComponentLike {

    static ComponentTranslatable ofTranslationKey(String key) {
        return new ComponentTranslatableImpl(key);
    }

    @NotNull
    @Override
    TranslatableComponent asComponent();

    default String asPlain(Audience audience) {
        return asPlain(audience.getOrDefault(Identity.LOCALE, Locale.getDefault()));
    }

    default String asPlain(Locale locale) {
        return PlainTextComponentSerializer.plainText().serialize(GlobalTranslator.render(asComponent(), locale));
    }

}