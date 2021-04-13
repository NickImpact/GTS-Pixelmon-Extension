package net.impactdev.gts.generations.legacy;

import com.google.gson.JsonObject;
import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.core.config.PixelmonEntityList;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.generations.entry.GenerationsEntry;
import net.impactdev.pixelmonbridge.generations.GenerationsPokemon;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class LegacyGenerationsPokemonDeserializer implements Storable.Deserializer<GenerationsEntry> {
    @Override
    public GenerationsEntry deserialize(JsonObject object) {
        EntityPixelmon result = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(
                GsonUtils.deserialize(object),
                FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld()
        );

        return new GenerationsEntry(GenerationsPokemon.from(result));
    }
}
