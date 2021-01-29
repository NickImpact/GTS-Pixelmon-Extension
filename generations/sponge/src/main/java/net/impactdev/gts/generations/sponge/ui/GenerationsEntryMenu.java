package net.impactdev.gts.generations.sponge.ui;

import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.common.ui.Historical;
import net.impactdev.gts.generations.sponge.entry.GenerationsEntry;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.pixelmonbridge.generations.GenerationsPokemon;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.function.Supplier;

public class GenerationsEntryMenu extends AbstractSpongeEntryUI<GenerationsEntryMenu.Chosen> implements Historical<SpongeMainPageProvider> {

    public GenerationsEntryMenu(Player viewer) {
        super(viewer);
    }

    @Override
    public Optional<Supplier<SpongeMainPageProvider>> getParent() {
        return Optional.empty();
    }

    @Override
    protected Text getTitle() {
        return null;
    }

    @Override
    protected InventoryDimension getDimensions() {
        return null;
    }

    @Override
    protected SpongeLayout getDesign() {
        return null;
    }

    @Override
    protected EntrySelection<? extends SpongeEntry<?>> getSelection() {
        return null;
    }

    @Override
    protected int getPriceSlot() {
        return 0;
    }

    @Override
    protected int getSelectionTypeSlot() {
        return 0;
    }

    @Override
    protected int getTimeSlot() {
        return 0;
    }

    @Override
    protected double getMinimumMonetaryPrice(Chosen chosen) {
        return 0;
    }

    @Override
    public SpongeIcon createChosenIcon() {
        return null;
    }

    protected static class Chosen implements EntrySelection<GenerationsEntry> {

        private final GenerationsPokemon selection;

        public Chosen(GenerationsPokemon selection) {
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

}
