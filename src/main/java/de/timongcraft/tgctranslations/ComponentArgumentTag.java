package de.timongcraft.tgctranslations;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Resolver for {@link ComponentLike} argument tags.
 * This class implements {@link TagResolver} to resolve argument tags within strings in the minimessage format.
 * <p>
 * Note: Format '&lt;arg:ARG_INDEX&gt;' where ARG_INDEX is the index of the argument to resolve.
 * If no argument index is provided (meaning '&lt;arg&gt;'), it defaults to 0.
 */
@ApiStatus.Internal
public class ComponentArgumentTag implements TagResolver {

    private static final String[] ALIASES = {"arg", "argument"};

    private final List<? extends ComponentLike> argumentComponents;

    public ComponentArgumentTag(List<? extends ComponentLike> argumentComponents) {
        this.argumentComponents = Objects.requireNonNull(argumentComponents, "argumentComponents");
    }

    @Override
    public Tag resolve(@NotNull String tagName, ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        int index;
        if (arguments.hasNext()) {
            index = arguments.pop().asInt()
                    .orElseThrow(() -> ctx.newException("Invalid argument index", arguments));
        } else {
            index = 0;
        }

        if (index < 0 || index >= argumentComponents.size()) {
            throw ctx.newException("Invalid argument index", arguments);
        }

        return Tag.selfClosingInserting(argumentComponents.get(index));
    }

    @Override
    public boolean has(@NotNull String tagName) {
        for (String alias : ALIASES) {
            if (alias.equals(tagName)) {
                return true;
            }
        }
        return false;
    }

}