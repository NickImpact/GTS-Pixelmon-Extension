package net.impactdev.gts.reforged.placeholders;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.placeholder.PlaceholderContext;
import org.spongepowered.api.placeholder.PlaceholderParser;

import java.util.function.Function;

public class PokemonPlaceholder implements PlaceholderParser {

    private final Function<Pokemon, Component> parser;

    public PokemonPlaceholder(Function<Pokemon, Component> parser) {
        this.parser = parser;
    }

    @Override
    public Component parse(PlaceholderContext context) {
        return context.associatedObject()
                .filter(source -> source instanceof ReforgedPokemon || source instanceof Pokemon)
                .map(source -> {
                    if(source instanceof ReforgedPokemon) {
                        return ((ReforgedPokemon) source).getOrCreate();
                    }

                    return (Pokemon) source;
                })
                .map(this.parser)
                .orElse(Component.empty());
    }
}
