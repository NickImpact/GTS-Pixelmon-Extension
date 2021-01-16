package net.impactdev.gts.generations.sponge.entry;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.prices.PriceControlled;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.pixelmonbridge.generations.GenerationsPokemon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GenerationsEntry extends SpongeEntry<GenerationsPokemon> implements PriceControlled {

    private GenerationsPokemon pokemon;

    @Override
    public GenerationsPokemon getOrCreateElement() {
        return this.pokemon;
    }

    @Override
    public TextComponent getName() {
        return Component.text(this.pokemon.getOrCreate().getSpecies().getPokemonName());
    }

    @Override
    public TextComponent getDescription() {
        return null;
    }

    @Override
    public Display<ItemStack> getDisplay(UUID viewer, Listing listing) {
        return null;
    }

    @Override
    public boolean give(UUID receiver) {
        return false;
    }

    @Override
    public boolean take(UUID depositor) {
        return false;
    }

    @Override
    public Optional<String> getThumbnailURL() {
        return Optional.empty();
    }

    @Override
    public List<String> getDetails() {
        return null;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public JObject serialize() {
        return null;
    }

    @Override
    public double getMin() {
        return 0;
    }

    @Override
    public double getMax() {
        return 0;
    }

}
