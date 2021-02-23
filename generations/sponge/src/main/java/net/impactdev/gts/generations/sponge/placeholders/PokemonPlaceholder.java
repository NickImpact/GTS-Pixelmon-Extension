package net.impactdev.gts.generations.sponge.placeholders;

import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import net.impactdev.pixelmonbridge.generations.GenerationsPokemon;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.util.function.Function;

public class PokemonPlaceholder implements PlaceholderParser {

    private final String id;
    private final String name;
    private final Function<EntityPixelmon, Text> parser;

    public PokemonPlaceholder(String id, String name, Function<EntityPixelmon, Text> parser) {
        this.id = id;
        this.name = name;
        this.parser = parser;
    }

    @Override
    public Text parse(PlaceholderContext context) {
        return context.getAssociatedObject()
                .filter(source -> source instanceof GenerationsPokemon || source instanceof EntityPixelmon)
                .map(source -> {
                    if(source instanceof GenerationsPokemon) {
                        return ((GenerationsPokemon) source).getOrCreate();
                    }

                    return (EntityPixelmon) source;
                })
                .map(this.parser)
                .orElse(Text.EMPTY);
    }

    @Override
    public String getId() {
        return "gts-generations:" + this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
