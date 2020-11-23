package net.impactdev.gts.reforged.sponge.price;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumNoForm;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import com.pixelmonmod.pixelmon.items.ItemPixelmonSprite;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.util.TriConsumer;
import net.impactdev.gts.reforged.sponge.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.sponge.config.ReforgedLangConfigKeys;
import net.impactdev.gts.reforged.sponge.converter.JObjectConverter;
import net.impactdev.gts.reforged.sponge.ui.ReforgedPriceCreatorMenu;
import net.impactdev.gts.sponge.listings.makeup.SpongeDisplay;
import net.impactdev.gts.sponge.pricing.SpongePrice;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.gui.UI;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.pixelmonbridge.details.SpecKeys;
import net.impactdev.pixelmonbridge.details.components.Level;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;
import net.kyori.text.TextComponent;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@GTSKeyMarker("reforged-price")
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
        TextComponent.Builder builder = TextComponent.builder();

        this.tryAppend(builder, b -> {
            if(this.price.getLevel() > 0) {
                b.append("Level " + this.price.getLevel()).append(TextComponent.space());
            }
        });
        this.tryAppend(builder, b -> {
            IEnumForm form = this.price.getSpecies().getFormEnum(this.price.getForm());
            if(form.getForm() > 0) {
                b.append(form.getLocalizedName()).append(TextComponent.space());
            }
        });

        builder.append(this.price.getSpecies().getPokemonName());

        return builder.build();
    }

    @Override
    public Display<ItemStack> getDisplay() {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        final ReforgedPokemon pokemon = new ReforgedPokemon();
        pokemon.offer(SpecKeys.SPECIES, this.price.getSpecies().getPokemonName());
        pokemon.offer(SpecKeys.LEVEL, new Level(this.price.getLevel(), 0, false));
        pokemon.offer(SpecKeys.FORM, this.price.getForm());

        List<Text> lore = Lists.newArrayList();
        // TODO - Add lore

        ItemStack rep = ItemStack.builder()
                .from(getPicture(this.price.getSpecies(), this.price.getSpecies().getFormEnum(this.price.getForm())))
                .add(Keys.DISPLAY_NAME, service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig()
                        .get(ReforgedLangConfigKeys.POKEMON_TITLE), Lists.newArrayList(() -> pokemon))
                )
                .add(Keys.ITEM_LORE, lore)
                .build();

        return new SpongeDisplay(rep);
    }

    @Override
    public boolean canPay(UUID payer) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(payer);
        for(Pokemon pokemon : this.price.doesAllowEggs() ? storage.getTeam() : Arrays.asList(storage.getAll())) {
            if(this.price.matches(pokemon)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void pay(UUID payer, Object source) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(payer);
        Pokemon pokemon = storage.get(this.getSourceType().cast(source));
        this.payment = ReforgedPokemon.from(pokemon);
        storage.set(pokemon.getPosition(), null);
    }

    @Override
    public boolean reward(UUID recipient) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(recipient);
        if(storage.hasSpace()) {
            storage.add(this.payment.getOrCreate());
            return true;
        }

        PCStorage pc = Pixelmon.storageManager.getPCForPlayer(recipient);
        return pc.add(this.payment.getOrCreate());
    }

    @Override
    public Class<StoragePosition> getSourceType() {
        return StoragePosition.class;
    }

    @Override
    public long calculateFee(boolean listingType) {
        return Math.max(0, 0);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        return new JObject()
                .add("price", new JObject()
                        .add("species", this.price.getSpecies().getPokemonName())
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
                EnumSpecies.getFromNameAnyCase(price.get("species").getAsString()),
                price.get("form").getAsInt(),
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

    public static ItemStack getPicture(EnumSpecies species, IEnumForm form) {
        Calendar calendar = Calendar.getInstance();

        boolean aprilFools = false;
        if(calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            aprilFools = true;
        }

        Pokemon rep = Pixelmon.pokemonFactory.create(species);
        rep.setForm(form);
        return (ItemStack) (Object) (ItemPixelmonSprite.getPhoto(
                aprilFools ? Pixelmon.pokemonFactory.create(EnumSpecies.Bidoof) : rep
        ));
    }

    public static class PokemonPriceSpecs {

        private final EnumSpecies species;
        private final int form;
        private final int level;
        private final boolean allowEggs;

        public PokemonPriceSpecs(EnumSpecies species, int form, int level, boolean allowEggs) {
            this.species = species;
            this.form = form;
            this.level = level;
            this.allowEggs = allowEggs;
        }

        public EnumSpecies getSpecies() {
            return this.species;
        }

        public int getForm() {
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

            return pokemon.getSpecies().equals(this.species) &&
                    pokemon.getSpecies().getFormEnum(pokemon.getForm()).equals(this.species.getFormEnum(this.form)) &&
                    pokemon.getLevel() >= this.level;
        }

    }

    public static class ReforgedPriceManager implements PriceManager<ReforgedPrice, Player> {

        @Override
        public TriConsumer<Player, EntryUI<?, ?, ?>, BiConsumer<EntryUI<?, ?, ?>, Price<?, ?, ?>>> process() {
            return (viewer, ui, callback) -> {
                Consumer<ReforgedPrice> processor = price -> callback.accept(ui, price);
                new ReforgedPriceCreatorMenu(viewer, processor).open();
            };
        }

        @Override
        public <U extends UI<?, ?, ?, ?>> Optional<PriceSelectorUI<U>> getSelector(Player viewer, Price<?, ?, ?> price, Consumer<Object> callback) {
            Preconditions.checkArgument(price instanceof ReforgedPrice, "Received invalid price option");

            PriceSelectorUI<U> selector = (PriceSelectorUI<U>) new ReforgedPriceSelector(viewer, ((ReforgedPrice) price).price, callback);
            return Optional.of(selector);
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
