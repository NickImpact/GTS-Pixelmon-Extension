package net.impactdev.gts.generations.sponge.manager;

import com.pixelmongenerations.core.enums.EnumSpecies;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.generations.sponge.entry.GenerationsEntry;
import net.impactdev.gts.generations.sponge.ui.GenerationsEntryMenu;
import net.impactdev.pixelmonbridge.generations.GenerationsDataManager;
import org.spongepowered.api.entity.living.player.Player;

import java.util.function.Supplier;

public class GenerationsPokemonDataManager implements EntryManager<GenerationsEntry, Player> {

    private final GenerationsDataManager manager = new GenerationsDataManager();

    public GenerationsDataManager getInternalManager() {
        return this.manager;
    }

    @Override
    public Class<?> getBlacklistType() {
        return EnumSpecies.class;
    }

    @Override
    public Supplier<EntryUI<?, ?, ?>> getSellingUI(Player player) {
        return () -> new GenerationsEntryMenu(player);
    }

    @Override
    public void supplyDeserializers() {
    }

    @Override
    public String getName() {
        return "Pokemon";
    }

    @Override
    public String getItemID() {
        return "pixelmon:gs_ball";
    }

    @Override
    public Storable.Deserializer<GenerationsEntry> getDeserializer() {
        return json -> new GenerationsEntry(this.manager.deserialize(json.getAsJsonObject("pokemon")));
    }
}