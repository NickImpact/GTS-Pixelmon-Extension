package net.impactdev.gts.generations.sponge.legacy;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.generations.sponge.entry.GenerationsEntry;

public class LegacyGenerationsPokemonDeserializer implements Storable.Deserializer<GenerationsEntry> {
    @Override
    public GenerationsEntry deserialize(JsonObject object) {
        return null;
    }
}
