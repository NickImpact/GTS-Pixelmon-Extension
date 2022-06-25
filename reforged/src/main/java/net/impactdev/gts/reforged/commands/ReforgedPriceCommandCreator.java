package net.impactdev.gts.reforged.commands;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.api.pokemon.requirement.impl.FormRequirement;
import com.pixelmonmod.api.pokemon.requirement.impl.LevelRequirement;
import com.pixelmonmod.api.pokemon.requirement.impl.SpeciesRequirement;
import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.reforged.price.ReforgedPrice;
import net.impactdev.gts.reforged.ui.ReforgedPriceCreatorMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import org.spongepowered.api.Sponge;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public class ReforgedPriceCommandCreator implements CommandGenerator.PriceGenerator<ReforgedPrice> {
    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public ReforgedPrice create(UUID source, Queue<String> args, Context context) throws Exception {
        if(args.isEmpty()) {
            context.redirected();
            new ReforgedPriceCreatorMenu(
                    PlatformPlayer.from(Sponge.server().player(source).orElseThrow(() -> new IllegalStateException("Missing player source"))),
                    price -> {
                        if(context.type().equals(Auction.class)) {
                            throw new IllegalStateException("Price resolution for invalid listing type");
                        }

                        Impactor.getInstance().getRegistry().get(ListingManager.class)
                                .list(source, BuyItNow.builder()
                                        .entry(context.entry().map(EntrySelection::createFromSelection).get())
                                        .price(price)
                                        .expiration(LocalDateTime.now().plusSeconds(context.time()))
                                        .lister(source)
                                        .build()
                                );
                    }
            );
            return null;
        }

        String[] remaining = args.toArray(new String[]{});
        PokemonSpecification spec = PokemonSpecificationProxy.create(remaining);
        Optional<RegistryValue<Species>> key = spec.getValue(SpeciesRequirement.class);
        if(!key.isPresent()) {
            throw new IllegalArgumentException("The species of the pokemon price is required and must be accurate");
        }

        Species species = key.get().getValueUnsafe();
        Integer level = this.get(spec.getValue(LevelRequirement.class).orElse(null), -1);
        String form = this.get(spec.getValue(FormRequirement.class).orElse(null), "");
        //boolean eggs = this.get(Arrays.stream(spec.args).filter(arg -> arg.equals("false")).map(arg -> true).findAny().orElse(false), false);
        return new ReforgedPrice(new ReforgedPrice.PokemonPriceSpecs(species, form, level, false));
    }

    private <T> T get(T base, T fallback) {
        return Optional.ofNullable(base).orElse(fallback);
    }
}
