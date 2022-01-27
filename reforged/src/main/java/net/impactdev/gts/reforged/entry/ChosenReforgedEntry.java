package net.impactdev.gts.reforged.entry;

import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;

public class ChosenReforgedEntry implements EntrySelection<ReforgedEntry> {

    private final ReforgedPokemon selection;

    public ChosenReforgedEntry(ReforgedPokemon selection) {
        this.selection = selection;
    }

    public ReforgedPokemon getSelection() {
        return this.selection;
    }

    @Override
    public ReforgedEntry createFromSelection() {
        return new ReforgedEntry(this.selection);
    }
}
