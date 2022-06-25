package net.impactdev.gts.reforged.entry;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.api.pokemon.requirement.impl.SpeciesRequirement;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.searching.Searcher;

public class ReforgedListingSearcher implements Searcher {

    @Override
    public boolean parse(Listing listing, String input) {
        PokemonSpecification spec = PokemonSpecificationProxy.create(input.split(" "));
        if(listing.getEntry() instanceof ReforgedEntry) {
            return spec.getValue(SpeciesRequirement.class).isPresent() && spec.matches(((ReforgedEntry) listing.getEntry()).getOrCreateElement().getOrCreate());
        }

        return false;
    }

}
