package net.impactdev.gts.reforged.manager;

import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.util.Version;
import net.impactdev.gts.reforged.commands.ReforgedEntryCommandCreator;
import net.impactdev.gts.reforged.entry.ReforgedEntry;
import net.impactdev.gts.reforged.ui.ReforgedEntryMenu;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.pixelmonbridge.reforged.ReforgedDataManager;

import java.util.function.Supplier;

public final class ReforgedPokemonDataManager implements EntryManager<ReforgedEntry> {

    private final ReforgedDataManager manager = new ReforgedDataManager();

    public ReforgedDataManager getInternalManager() {
        return this.manager;
    }

    @Override
    public Class<ReforgedEntry> supported() {
        return ReforgedEntry.class;
    }

    @Override
    public Class<?> getBlacklistType() {
        return Species.class;
    }

    @Override
    public Supplier<EntryUI<?>> getSellingUI(PlatformPlayer player) {
        return () -> new ReforgedEntryMenu(player);
    }

    @Override
    public void supplyDeserializers() {}

    @Override
    public CommandGenerator.EntryGenerator<? extends EntrySelection<? extends Entry<?, ?>>> getEntryCommandCreator() {
        return new ReforgedEntryCommandCreator();
    }

    @Override
    public boolean supports(Version game, int content) {
        return true;
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
