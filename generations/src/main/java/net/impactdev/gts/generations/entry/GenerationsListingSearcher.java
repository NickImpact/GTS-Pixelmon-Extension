package net.impactdev.gts.generations.entry;

import com.pixelmongenerations.api.pokemon.PokemonSpec;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.searching.Searcher;

public class GenerationsListingSearcher implements Searcher
{
    @Override
    public boolean parse(Listing listing, String input) {
        PokemonSpec spec = new PokemonSpec(input.split(" "));
        if(listing.getEntry() instanceof GenerationsEntry) {
            return spec.name != null && spec.matches(((GenerationsEntry) listing.getEntry()).getOrCreateElement().getOrCreate());
        }

        return false;
    }
}