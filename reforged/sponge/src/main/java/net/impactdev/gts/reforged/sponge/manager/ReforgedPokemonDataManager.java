package net.impactdev.gts.reforged.sponge.manager;

import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.reforged.sponge.entry.ReforgedEntry;
import net.impactdev.gts.reforged.sponge.ui.ReforgedEntryMenu;
import net.impactdev.pixelmonbridge.reforged.ReforgedDataManager;
import org.spongepowered.api.entity.living.player.Player;

import java.util.function.Supplier;

public final class ReforgedPokemonDataManager implements EntryManager<ReforgedEntry, Player> {

    private final ReforgedDataManager manager = new ReforgedDataManager();

    public ReforgedDataManager getInternalManager() {
        return this.manager;
    }

    @Override
    public Class<?> getBlacklistType() {
        return EnumSpecies.class;
    }

    @Override
    public Supplier<EntryUI<?, ?, ?>> getSellingUI(Player player) {
        return () -> new ReforgedEntryMenu(player);
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
    public Storable.Deserializer<ReforgedEntry> getDeserializer() {
        return json -> new ReforgedEntry(this.manager.deserialize(json.getAsJsonObject("pokemon")));
    }

}
