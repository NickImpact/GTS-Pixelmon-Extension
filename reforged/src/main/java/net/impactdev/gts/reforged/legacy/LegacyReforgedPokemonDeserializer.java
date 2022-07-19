package net.impactdev.gts.reforged.legacy;

import com.google.gson.JsonObject;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.reforged.entry.ReforgedEntry;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;

public class LegacyReforgedPokemonDeserializer implements Storable.Deserializer<ReforgedEntry> {

    @Override
    public ReforgedEntry deserialize(JsonObject object) {
        Pokemon pokemon = PokemonFactory.create(GsonUtils.deserialize(object));
        return new ReforgedEntry(ReforgedPokemon.from(pokemon));
    }

}
