package net.impactdev.gts.generations.commands;

import com.pixelmongenerations.api.pokemon.PokemonSpec;
import com.pixelmongenerations.core.enums.EnumSpecies;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.generations.price.GenerationsPrice;
import net.impactdev.gts.generations.ui.GenerationsPriceCreatorMenu;
import net.impactdev.impactor.api.Impactor;
import org.spongepowered.api.Sponge;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public class GenerationsPriceCommandCreator implements CommandGenerator.PriceGenerator<GenerationsPrice> {
    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public GenerationsPrice create(UUID source, Queue<String> args, Context context) throws Exception {
        if(args.isEmpty()) {
            context.redirected();
            new GenerationsPriceCreatorMenu(
                    Sponge.getServer().getPlayer(source).orElseThrow(() -> new IllegalStateException("Missing player source")),
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
        PokemonSpec spec = PokemonSpec.from(remaining);
        if(spec.name == null) {
            throw new IllegalArgumentException("The species of the pokemon price is required and must be accurate");
        }

        EnumSpecies species = EnumSpecies.getFromName(spec.name).get();
        int level = this.get(spec.level, -1);
        int form = this.get(spec.form, -1);
        boolean eggs = this.get(spec.extraSpecs.stream().filter(arg -> arg.getType().getKey().equals("egg")).map(arg -> true).findAny().orElse(false), false);
        return new GenerationsPrice(new GenerationsPrice.PokemonPriceSpecs(species, form, level, eggs));
    }

    private <T> T get(T base, T fallback) {
        return Optional.ofNullable(base).orElse(fallback);
    }

}
