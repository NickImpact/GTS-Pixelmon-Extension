package net.impactdev.gts.generations.entry;

import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.pixelmonbridge.generations.GenerationsPokemon;

public class ChosenGenerationsEntry implements EntrySelection<GenerationsEntry> {

    private final GenerationsPokemon selection;

    public ChosenGenerationsEntry(GenerationsPokemon selection) {
        this.selection = selection;
    }

    public GenerationsPokemon getSelection() {
        return this.selection;
    }

    @Override
    public GenerationsEntry createFromSelection() {
        return new GenerationsEntry(this.selection);
    }

}
