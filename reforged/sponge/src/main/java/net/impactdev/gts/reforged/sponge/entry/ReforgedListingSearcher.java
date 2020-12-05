package net.impactdev.gts.reforged.sponge.entry;

import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.searching.Searcher;

public class ReforgedListingSearcher implements Searcher {

    @Override
    public boolean parse(Listing listing, String input) {
        PokemonSpec spec = new PokemonSpec(input.split(" "));
        if(listing.getEntry() instanceof ReforgedEntry) {
            return spec.name != null && spec.matches(((ReforgedEntry) listing.getEntry()).getOrCreateElement().getOrCreate());
        }

        return false;
    }

}
