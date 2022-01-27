package net.impactdev.gts.reforged.commands;

import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.reforged.price.ReforgedPrice;
import net.impactdev.gts.reforged.ui.ReforgedPriceCreatorMenu;
import net.impactdev.impactor.api.Impactor;
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
        byte form = this.get(spec.form, (byte) -1);
        boolean eggs = this.get(Arrays.stream(spec.args).filter(arg -> arg.equals("false")).map(arg -> true).findAny().orElse(false), false);
        return new ReforgedPrice(new ReforgedPrice.PokemonPriceSpecs(species, form, level, eggs));
    }

    private <T> T get(T base, T fallback) {
        return Optional.ofNullable(base).orElse(fallback);
    }
}
