package net.impactdev.gts.reforged.price;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.registries.PixelmonForms;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.items.SpriteItem;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.reforged.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.commands.ReforgedPriceCommandCreator;
import net.impactdev.gts.reforged.config.ReforgedLangConfigKeys;
import net.impactdev.gts.reforged.converter.JObjectConverter;
import net.impactdev.gts.reforged.ui.ReforgedPriceCreatorMenu;
import net.impactdev.gts.sponge.listings.makeup.SpongeDisplay;
import net.impactdev.gts.sponge.pricing.SpongePrice;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.pixelmonbridge.details.SpecKeys;
import net.impactdev.pixelmonbridge.details.components.Level;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@GTSKeyMarker({"pokemon", "reforged-price"})
public class ReforgedPrice implements SpongePrice<ReforgedPrice.PokemonPriceSpecs, StoragePosition> {

    private PokemonPriceSpecs price;

    /** The pokemon the payer pays to buy the represented Listing */
    private ReforgedPokemon payment;

    public ReforgedPrice(PokemonPriceSpecs price) {
        this.price = price;
    }

    @Override
    public PokemonPriceSpecs getPrice() {
        return this.price;
    }

    @Override
    public TextComponent getText() {
        TextComponent.Builder builder = Component.text();

        this.tryAppend(builder, b -> {
            if(this.price.getLevel() > 0) {
                b.append(Component.text("Level " + this.price.getLevel()).append(Component.space()));
            }
        });
        this.tryAppend(builder, b -> {
            Stats form = this.price.getSpecies().getForm(this.price.getForm());
            if(!form.getName().equals(PixelmonForms.NONE)) {
                b.append(Component.text(form.getLocalizedName()).append(Component.space()));
            }
        });

        builder.append(Component.text(this.price.getSpecies().getName()));
        return builder.build();
    }

    @Override
    public Display<ItemStack> getDisplay() {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        final ReforgedPokemon pokemon = new ReforgedPokemon();
        pokemon.offer(SpecKeys.SPECIES, this.price.getSpecies().getName());
        pokemon.offer(SpecKeys.LEVEL, new Level(this.price.getLevel(), 0, false));
        pokemon.offer(SpecKeys.FORM, this.price.getForm());

        PlaceholderSources sources = PlaceholderSources.builder().append(ReforgedPokemon.class, () -> pokemon).build();
        List<Component> lore = Lists.newArrayList();
        // TODO - Add lore

        ItemStack rep = ItemStack.builder()
                .from(getPicture(this.price.getSpecies(), this.price.getSpecies().getForm(this.price.getForm())))
                .add(Keys.CUSTOM_NAME, service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig()
                        .get(ReforgedLangConfigKeys.POKEMON_TITLE), sources)
                )
                .add(Keys.LORE, lore)
                .build();

        return new SpongeDisplay(rep);
    }

    @Override
    public boolean canPay(UUID payer) {
        PlayerPartyStorage storage = StorageProxy.getParty(payer);
        for(Pokemon pokemon : this.price.doesAllowEggs() ? storage.getTeam() : Arrays.asList(storage.getAll())) {
            if(this.price.matches(pokemon)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void pay(UUID payer, @NonNull Object source, @NonNull AtomicBoolean marker) {
        PlayerPartyStorage storage = StorageProxy.getParty(payer);
        Pokemon pokemon = storage.get(this.getSourceType().cast(source));
        this.payment = ReforgedPokemon.from(pokemon);
        storage.set(pokemon.getPosition(), null);
        marker.set(true);
    }

    @Override
    public boolean reward(UUID recipient) {
        PlayerPartyStorage storage = StorageProxy.getParty(recipient);
        if(storage.hasSpace()) {
            storage.add(this.payment.getOrCreate());
            return true;
        }

        PCStorage pc = StorageProxy.getPCForPlayer(recipient);
        return pc.add(this.payment.getOrCreate());
    }

    @Override
    public Class<StoragePosition> getSourceType() {
        return StoragePosition.class;
    }

    @Override
    public long calculateFee(boolean listingType) {
        return 0;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        return new JObject()
                .add("price", new JObject()
                        .add("species", this.price.getSpecies().getName())
                        .add("form", this.price.getForm())
                        .add("level", this.price.getLevel())
                        .add("allowEggs", this.price.doesAllowEggs())
                )
                .consume(o -> {
                    if(this.payment != null) {
                        o.add("payment", JObjectConverter.convert(GTSSpongeReforgedPlugin.getInstance()
                                .getManager()
                                .getInternalManager()
                                .serialize(this.payment)
                        ));
                    }
                })
                .add("version", this.getVersion());
    }

    private static ReforgedPrice deserialize(JsonObject json) {
        JsonObject price = json.getAsJsonObject("price");

        PokemonPriceSpecs specs = new PokemonPriceSpecs(
                PixelmonSpecies.get(price.get("species").getAsString()).get().getValueUnsafe(),
                price.get("form").getAsString(),
                price.get("level").getAsInt(),
                price.get("allowEggs").getAsBoolean()
        );

        ReforgedPrice result = new ReforgedPrice(specs);
        if(json.has("payment")) {
            result.payment = GTSSpongeReforgedPlugin.getInstance().getManager()
                    .getInternalManager()
                    .deserialize(json.getAsJsonObject("payment"));
        }
        return result;
    }

    private void tryAppend(TextComponent.Builder builder, Consumer<TextComponent.Builder> consumer) {
        consumer.accept(builder);
    }

    public static ItemStack getPicture(Species species, Stats form) {
        Calendar calendar = Calendar.getInstance();

        boolean aprilFools = (calendar.get(Calendar.MONTH) == Calendar.APRIL || calendar.get(Calendar.MONTH) == Calendar.JULY)
                && calendar.get(Calendar.DAY_OF_MONTH) == 1;

        Pokemon rep = PokemonFactory.create(species);
        rep.setForm(form);
        return (ItemStack) (Object) (SpriteItem.getPhoto(
                aprilFools ? PokemonFactory.create(PixelmonSpecies.BIDOOF.getValueUnsafe()) : rep
        ));
    }

    public static class PokemonPriceSpecs {

        private final Species species;
        private final String form;
        private final int level;
        private final boolean allowEggs;

        public PokemonPriceSpecs(Species species, String form, int level, boolean allowEggs) {
            this.species = species;
            this.form = form;
            this.level = level;
            this.allowEggs = allowEggs;
        }

        public Species getSpecies() {
            return this.species;
        }

        public String getForm() {
            return this.form;
        }

        public int getLevel() {
            return this.level;
        }

        public boolean doesAllowEggs() {
            return this.allowEggs;
        }

        public boolean matches(Pokemon pokemon) {
            if(pokemon == null) {
                return false;
            }

            if(!this.allowEggs) {
                if(pokemon.isEgg()) {
                    return false;
                }
            }
            
            if (pokemon.hasFlag("untradeable")) {
                return false;
            }

            return pokemon.getSpecies().equals(this.species) &&
                    pokemon.getForm().equals(this.species.getForm(this.form)) &&
                    pokemon.getPokemonLevel() >= this.level;
        }

    }

    public static class ReforgedPriceManager implements PriceManager<ReforgedPrice> {

        @Override
        public void process(PlatformPlayer target, EntryUI<?> source, BiConsumer<EntryUI<?>, Price<?, ?, ?>> callback) {
            Consumer<ReforgedPrice> processor = price -> callback.accept(source, price);
            new ReforgedPriceCreatorMenu(target, processor).open();
        }

        @Override
        public Optional<PriceSelectorUI> getSelector(PlatformPlayer viewer, Price<?, ?, ?> price, Consumer<Object> callback) {
            Preconditions.checkArgument(price instanceof ReforgedPrice, "Received invalid price option");

            PriceSelectorUI selector = new ReforgedPriceSelector(viewer, ((ReforgedPrice) price).price, callback);
            return Optional.of(selector);
        }

        @Override
        public CommandGenerator.PriceGenerator<? extends Price<?, ?, ?>> getPriceCommandCreator() {
            return new ReforgedPriceCommandCreator();
        }

        @Override
        public String getName() {
            return "Pokemon";
        }

        @Override
        public String getItemID() {
            return "pixelmon:gs_ball";
        }

        @Override
        public Deserializer<ReforgedPrice> getDeserializer() {
            return ReforgedPrice::deserialize;
        }
    }

}
