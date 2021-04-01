package net.impactdev.gts.generations.sponge.price;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.pixelmongenerations.api.pokemon.PokemonSpec;
import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.common.item.ItemPixelmonSprite;
import com.pixelmongenerations.core.enums.EnumSpecies;
import com.pixelmongenerations.core.enums.forms.IEnumForm;
import com.pixelmongenerations.core.storage.ComputerBox;
import com.pixelmongenerations.core.storage.PixelmonStorage;
import com.pixelmongenerations.core.storage.PlayerComputerStorage;
import com.pixelmongenerations.core.storage.PlayerStorage;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.util.TriConsumer;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.generations.sponge.GTSSpongeGenerationsPlugin;
import net.impactdev.gts.generations.sponge.config.GenerationsLangConfigKeys;
import net.impactdev.gts.generations.sponge.converter.JObjectConverter;
import net.impactdev.gts.generations.sponge.ui.GenerationsPriceCreatorMenu;
import net.impactdev.gts.sponge.listings.makeup.SpongeDisplay;
import net.impactdev.gts.sponge.pricing.SpongePrice;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.gui.UI;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.pixelmonbridge.details.SpecKeys;
import net.impactdev.pixelmonbridge.details.components.Level;
import net.impactdev.pixelmonbridge.generations.GenerationsPokemon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@GTSKeyMarker({"pokemon", "reforged-price"})
public class GenerationsPrice implements SpongePrice<GenerationsPrice.PokemonPriceSpecs, EntityPixelmon>
{
    private PokemonPriceSpecs price;

    /** The pokemon the payer pays to buy the represented Listing */
    private GenerationsPokemon payment;

    public GenerationsPrice(PokemonPriceSpecs price) {
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
            IEnumForm form = this.price.getSpecies().getFormEnum(this.price.getForm());
            if(form.getForm() > 0) {
                b.append(Component.text(form.getProperName()).append(Component.space()));
            }
        });

        builder.append(Component.text(this.price.getSpecies().getPokemonName()));

        return builder.build();
    }

    @Override
    public Display<ItemStack> getDisplay() {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        final GenerationsPokemon pokemon = new GenerationsPokemon();
        pokemon.offer(SpecKeys.SPECIES, this.price.getSpecies().getPokemonName());
        pokemon.offer(SpecKeys.LEVEL, new Level(this.price.getLevel(), 0, false));
        pokemon.offer(SpecKeys.FORM, this.price.getForm());

        List<Text> lore = Lists.newArrayList();
        // TODO - Add lore

        ItemStack rep = ItemStack.builder()
                .from(getPicture(this.price.getSpecies(), this.price.getSpecies().getFormEnum(this.price.getForm())))
                .add(Keys.DISPLAY_NAME, service.parse(GTSSpongeGenerationsPlugin.getInstance().getMsgConfig()
                        .get(GenerationsLangConfigKeys.POKEMON_TITLE), Lists.newArrayList(() -> pokemon))
                )
                .add(Keys.ITEM_LORE, lore)
                .build();

        return new SpongeDisplay(rep);
    }

    @Override
    public boolean canPay(UUID payer) {
        PlayerStorage storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(payer).get();
        for(NBTTagCompound nbt : this.price.doesAllowEggs() ? storage.getTeam() : Arrays.asList(storage.partyPokemon))
        {
            if(nbt == null)
                continue;

            EntityPixelmon pokemon = new EntityPixelmon(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
            pokemon.readFromNBT(nbt);

            if(this.price.matches(pokemon)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void pay(UUID payer, @NonNull Object source, @NonNull AtomicBoolean marker) {
        Impactor.getInstance().getScheduler().executeSync(() -> {
            PlayerStorage storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(payer).get();
            EntityPixelmon pokemon = storage.getPokemon(this.getSourceType().cast(source).getPokemonId(), FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
            this.payment = GenerationsPokemon.from(pokemon);
            storage.removeFromPartyPlayer(storage.getPosition(pokemon.getPokemonId()));
            marker.set(true);
        });
    }

    @Override
    public boolean reward(UUID recipient) {
        Impactor.getInstance().getScheduler().executeSync(() -> {
            PlayerStorage storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(recipient).get();
            if(storage.hasSpace()) {
                storage.addToParty(this.payment.getOrCreate());
                return;
            }

            PlayerComputerStorage pc = PixelmonStorage.computerManager.getPlayerStorageOffline(FMLCommonHandler.instance().getMinecraftServerInstance(), recipient);
            pc.playerStorage = storage;
            pc.addToComputer(this.payment.getOrCreate());
        });
        return true;
    }

    @Override
    public Class<EntityPixelmon> getSourceType() {
        return EntityPixelmon.class;
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
                        o.add("payment", JObjectConverter.convert(GTSSpongeGenerationsPlugin.getInstance()
                                .getManager()
                                .getInternalManager()
                                .serialize(this.payment)
                        ));
                    }
                })
                .add("version", this.getVersion());
    }

    private static GenerationsPrice deserialize(JsonObject json) {
        JsonObject price = json.getAsJsonObject("price");

        PokemonPriceSpecs specs = new PokemonPriceSpecs(
                EnumSpecies.getFromNameAnyCase(price.get("species").getAsString()),
                price.get("form").getAsInt(),
                price.get("level").getAsInt(),
                price.get("allowEggs").getAsBoolean()
        );

        GenerationsPrice result = new GenerationsPrice(specs);
        if(json.has("payment")) {
            result.payment = GTSSpongeGenerationsPlugin.getInstance().getManager()
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
        EntityPixelmon tmp = new PokemonSpec(species.name, form.getFormSuffix()).create(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
        return (ItemStack) (Object) (ItemPixelmonSprite.getPhoto(
                aprilFools ? new PokemonSpec(EnumSpecies.Bidoof.name).create(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld()) : tmp
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

        public boolean matches(EntityPixelmon pokemon) {
            if(pokemon == null) {
                return false;
            }

            if(!this.allowEggs) {
                if(pokemon.isEgg) {
                    return false;
                }
            }

            return pokemon.getSpecies().equals(this.species) &&
                    pokemon.getSpecies().getFormEnum(pokemon.getForm()).equals(this.species.getFormEnum(this.form)) &&
                    pokemon.level.getLevel() >= this.level;
        }

    }

    public static class GenerationsPriceManager implements PriceManager<GenerationsPrice, Player>
    {

        @Override
        public TriConsumer<Player, EntryUI<?, ?, ?>, BiConsumer<EntryUI<?, ?, ?>, Price<?, ?, ?>>> process() {
            return (viewer, ui, callback) -> {
                Consumer<GenerationsPrice> processor = price -> callback.accept(ui, price);
                new GenerationsPriceCreatorMenu(viewer, processor).open();
            };
        }

        @Override
        public <U extends UI<?, ?, ?, ?>> Optional<PriceSelectorUI<U>> getSelector(Player viewer, Price<?, ?, ?> price, Consumer<Object> callback) {
            Preconditions.checkArgument(price instanceof GenerationsPrice, "Received invalid price option");

            PriceSelectorUI<U> selector = (PriceSelectorUI<U>) new GenerationsPriceSelector(viewer, ((GenerationsPrice) price).price, callback);
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
        public Deserializer<GenerationsPrice> getDeserializer() {
            return GenerationsPrice::deserialize;
        }
    }

}
