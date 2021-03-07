package net.impactdev.gts.reforged.sponge.placeholders;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.util.function.Function;

public class PokemonPlaceholder implements PlaceholderParser {

    private final String id;
    private final String name;
    private final Function<Pokemon, Text> parser;

    public PokemonPlaceholder(String id, String name, Function<Pokemon, Text> parser) {
        this.id = id;
        this.name = name;
        this.parser = parser;
    }

    @Override
    public Text parse(PlaceholderContext context) {
        return context.getAssociatedObject()
                .filter(source -> source instanceof ReforgedPokemon || source instanceof Pokemon)
                .map(source -> {
                    if(source instanceof ReforgedPokemon) {
                        return ((ReforgedPokemon) source).getOrCreate();
                    }

                    return (Pokemon) source;
                })
                .map(this.parser)
                .orElse(Text.EMPTY);
    }

    @Override
    public String getId() {
        return "gts-reforged:" + this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
