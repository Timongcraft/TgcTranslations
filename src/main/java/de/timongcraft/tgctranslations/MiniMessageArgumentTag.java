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
 * Resolver for minimessage argument tags.
 * This class implements {@link TagResolver} to resolve argument tags within strings in the minimessage format.
 * <p>
 * Note: Format '&lt;arg:ARG_NUMBER&gt;' where ARG_NUMBER is the index of the argument to resolve.
 */
@ApiStatus.Internal
public class MiniMessageArgumentTag implements TagResolver {

    private static final String[] ALIASES = {"arg", "argument"};

    private final List<? extends ComponentLike> argumentComponents;

    /**
     * Constructs a new MiniMessageArgumentTag instance with the given argument components.
     *
     * @param argumentComponents The list of components servings as arguments
     * @throws NullPointerException if argumentComponents is null
     */
    public MiniMessageArgumentTag(List<? extends ComponentLike> argumentComponents) {
        this.argumentComponents = Objects.requireNonNull(argumentComponents, "argumentComponents");
    }

    /**
     * Resolves the specified argument tag number.
     *
     * @return The resolved tag.
     * @throws ParsingException if there is an error during parsing.
     */
    @Override
    public Tag resolve(@NotNull String tagName, ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        int index = arguments.popOr("No argument number provided")
                .asInt().orElseThrow(() -> ctx.newException("Invalid argument number", arguments));

        if (index < 0 || index >= argumentComponents.size())
            throw ctx.newException("Invalid argument number", arguments);

        return Tag.selfClosingInserting(argumentComponents.get(index));
    }

    /**
     * Checks whether the tag is an argument tag.
     */
    @Override
    public boolean has(@NotNull String tagName) {
        for (String alias : ALIASES)
            if (alias.equals(tagName))
                return true;
        return false;
    }

}