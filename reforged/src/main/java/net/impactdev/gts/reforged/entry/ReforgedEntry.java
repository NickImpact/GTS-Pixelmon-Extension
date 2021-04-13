package net.impactdev.gts.reforged.entry;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.MiniorStats;
import net.impactdev.gts.api.listings.prices.PriceControlled;
import net.impactdev.gts.api.util.TriFunction;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.reforged.config.ReforgedConfigKeys;
import net.impactdev.gts.reforged.config.mappings.ReforgedPriceControls;
import net.impactdev.gts.reforged.entry.description.ContextualDetails;
import net.impactdev.gts.reforged.flags.ReforgedSpecFlags;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.services.text.MessageService;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.items.ItemPixelmonSprite;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.reforged.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.config.ReforgedLangConfigKeys;
import net.impactdev.gts.reforged.converter.JObjectConverter;
import net.impactdev.gts.sponge.listings.makeup.SpongeDisplay;
import net.impactdev.pixelmonbridge.details.SpecKeys;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@GTSKeyMarker({"pokemon", "reforged-pokemon"})
public class ReforgedEntry extends SpongeEntry<ReforgedPokemon> implements PriceControlled {

    public ReforgedPokemon pokemon;

    public ReforgedEntry(ReforgedPokemon pokemon) {
        this.pokemon = pokemon;
    }

    @Override
    public ReforgedPokemon getOrCreateElement() {
        return this.pokemon;
    }

    @Override
    public TextComponent getName() {
        return Component.text(this.pokemon.getOrCreate().getSpecies().getLocalizedName());
    }

    @Override
    public TextComponent getDescription() {
        return this.getName();
    }

    @Override
    public Display<ItemStack> getDisplay(UUID viewer, Listing listing) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        List<Text> lore = Lists.newArrayList();
        lore.addAll(service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.POKEMON_DETAILS), Lists.newArrayList(() -> this.pokemon)));
        lore.addAll(ContextualDetails.receive(this.getOrCreateElement().getOrCreate()));

        ItemStack rep = ItemStack.builder()
                .from(this.getPicture(this.pokemon.getOrCreate()))
                .add(Keys.DISPLAY_NAME, service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig()
                        .get(ReforgedLangConfigKeys.POKEMON_TITLE), Lists.newArrayList(() -> this.pokemon))
                )
                .add(Keys.ITEM_LORE, lore)
                .build();

        return new SpongeDisplay(rep);
    }

    @Override
    public boolean give(UUID receiver) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(receiver);
        if(!storage.hasSpace()) {
            PCStorage pc = Pixelmon.storageManager.getPCForPlayer(receiver);
            if(!pc.hasSpace()) {
                return false;
            }
        }

        return storage.add(this.pokemon.getOrCreate());
    }

    @Override
    public boolean take(UUID depositor) {
        Optional<Player> user = Sponge.getServer().getPlayer(depositor);
        Config mainLang = GTSPlugin.getInstance().getMsgConfig();
        Config reforgedLang = GTSSpongeReforgedPlugin.getInstance().getMsgConfig();

        MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);

        PlayerPartyStorage party = Pixelmon.storageManager.getParty(depositor);
        if(BattleRegistry.getBattle(party.getPlayer()) != null) {
            user.ifPresent(player -> player.sendMessages(parser.parse(reforgedLang.get(ReforgedLangConfigKeys.ERROR_IN_BATTLE))));
            return false;
        }

        if(ReforgedSpecFlags.UNTRADABLE.matches(this.getOrCreateElement().getOrCreate())) {
            user.ifPresent(player -> player.sendMessages(parser.parse(reforgedLang.get(ReforgedLangConfigKeys.ERROR_UNTRADEABLE))));
            return false;
        }

        boolean blacklisted = Impactor.getInstance().getRegistry()
                .get(Blacklist.class)
                .isBlacklisted(EnumSpecies.class, this.pokemon.getOrCreate().getSpecies().name);
        if(blacklisted) {
            user.ifPresent(player -> player.sendMessages(parser.parse(mainLang.get(MsgConfigKeys.GENERAL_FEEDBACK_BLACKLISTED))));
            return false;
        }

        // Check party size. Ensure we aren't less than 1 because who knows whether Reforged or another plugin
        // will break something
        if(party.getTeam().size() <= 1) {
            user.ifPresent(player -> player.sendMessages(parser.parse(reforgedLang.get(ReforgedLangConfigKeys.ERROR_LAST_ABLE_MEMBER))));
            return false;
        }

        party.retrieveAll();
        party.set(party.getPosition(this.pokemon.getOrCreate()), null);
        return true;
    }

    @Override
    public Optional<String> getThumbnailURL() {
        StringBuilder url = new StringBuilder("https://projectpokemon.org/images/");
        if(this.pokemon.get(SpecKeys.SHINY).orElse(false)) {
            url.append("shiny");
        } else {
            url.append("normal");
        }

        url.append("-sprite/");
        url.append(this.pokemon.getOrCreate().getSpecies().name.toLowerCase());
        url.append(".gif");
        return Optional.of(url.toString());
    }

    @Override
    public List<String> getDetails() {
        MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);
        Config reforgedLang = GTSSpongeReforgedPlugin.getInstance().getMsgConfig();

        return parser.parse(reforgedLang.get(ReforgedLangConfigKeys.DISCORD_DETAILS), Lists.newArrayList(this::getOrCreateElement))
                .stream()
                .map(Text::toPlain)
                .collect(Collectors.toList());
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        // Since the custom serializer for the Cross Pixelmon Library uses it's own version of
        // JObject, as to not rely on Impactor, we need to convert between the two objects
        return new JObject()
                .add("pokemon", JObjectConverter.convert(GTSSpongeReforgedPlugin.getInstance()
                        .getManager()
                        .getInternalManager()
                        .serialize(this.pokemon)
                ))
                .add("version", this.getVersion());
    }

    private ItemStack getPicture(Pokemon pokemon) {
        Calendar calendar = Calendar.getInstance();

        boolean aprilFools = false;
        if(calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            aprilFools = true;
        }

        if(pokemon.isEgg()) {
            net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
            NBTTagCompound nbt = new NBTTagCompound();
            switch (pokemon.getSpecies()) {
                case Manaphy:
                case Togepi:
                    nbt.setString(NbtKeys.SPRITE_NAME,
                            String.format("pixelmon:sprites/eggs/%s1", pokemon.getSpecies().name.toLowerCase()));
                    break;
                default:
                    nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
                    break;
            }
            item.setTagCompound(nbt);
            return (ItemStack) (Object) item;
        } else {
            if(pokemon.getSpecies() == EnumSpecies.Minior) {
                byte color = ((MiniorStats) pokemon.getExtraStats()).color;

                Pokemon result = Pixelmon.pokemonFactory.create(PokemonSpec.from("minior", "f:" + (color + 1)));
                return (ItemStack) (Object) (aprilFools ? ItemPixelmonSprite.getPhoto(Pixelmon.pokemonFactory.create(EnumSpecies.Bidoof)) : ItemPixelmonSprite.getPhoto(result));
            }

            return (ItemStack) (Object) (aprilFools ? ItemPixelmonSprite.getPhoto(Pixelmon.pokemonFactory.create(EnumSpecies.Bidoof)) : ItemPixelmonSprite.getPhoto(pokemon));
        }
    }

    @Override
    public double getMin() {
        Optional<ReforgedPriceControls.Control> control = GTSSpongeReforgedPlugin.getInstance().getConfiguration()
                .get(ReforgedConfigKeys.PRICE_CONTROLS)
                .get(this.getOrCreateElement().getOrCreate().getSpecies());

        double calculated = control.map(ReforgedPriceControls.Control::getMin)
                .orElseGet(() -> {
                    if(GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(ReforgedConfigKeys.MIN_PRICING_USE_CUSTOM_BASE)) {
                        return GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(ReforgedConfigKeys.MIN_PRICING_CUSTOM_BASE);
                    }

                    return 0.0;
                });

        for(MinimumPriceCalculator calculator : MinimumPriceCalculator.values()) {
            calculated = calculator.calculate(this.getOrCreateElement().getOrCreate(), calculated);
        }

        return Math.max(1, Math.max(
                GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.LISTINGS_MIN_PRICE),
                calculated
        ));
    }

    @Override
    public double getMax() {
        Optional<ReforgedPriceControls.Control> control = GTSSpongeReforgedPlugin.getInstance().getConfiguration()
                .get(ReforgedConfigKeys.PRICE_CONTROLS)
                .get(this.getOrCreateElement().getOrCreate().getSpecies());

        return Math.max(1, Math.min(
                GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.LISTINGS_MAX_PRICE),
                control.map(ReforgedPriceControls.Control::getMax).orElse(Double.MAX_VALUE)
        ));
    }

    private enum MinimumPriceCalculator {
        LEGENDARY(ReforgedConfigKeys.MIN_PRICING_LEGEND_ENABLED, ReforgedConfigKeys.MIN_PRICING_LEGEND_PRICE, (pokemon, key, current) -> {
            if(EnumSpecies.legendaries.contains(pokemon.getSpecies().getPokemonName())) {
                return GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(key) + current;
            }

            return current;
        }),
        SHINY(ReforgedConfigKeys.MIN_PRICING_SHINY_ENABLED, ReforgedConfigKeys.MIN_PRICING_SHINY_PRICE, (pokemon, key, current) -> {
            if(pokemon.isShiny()) {
                return GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(key) + current;
            }

            return current;
        }),
        HIDDEN_ABILITY(ReforgedConfigKeys.MIN_PRICING_HA_ENABLED, ReforgedConfigKeys.MIN_PRICING_HA_PRICE, (pokemon, key, current) -> {
            if(pokemon.getAbilitySlot() == 2) {
                return GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(key) + current;
            }

            return current;
        }),
        IV(ReforgedConfigKeys.MIN_PRICING_IVS_ENABLED, ReforgedConfigKeys.MIN_PRICING_IVS_PRICE, (pokemon, key, current) -> {
            int required = GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(ReforgedConfigKeys.MIN_PRICING_IVS_REQUIRE);
            for(int iv : pokemon.getStats().ivs.getArray()) {
                if(iv >= required) {
                    current += GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(key);
                }
            }

            return current;
        }),;

        private final ConfigKey<Boolean> enableKey;
        private final ConfigKey<Double> priceKey;
        private final TriFunction<Pokemon, ConfigKey<Double>, Double, Double> priceApplier;

        MinimumPriceCalculator(ConfigKey<Boolean> enableKey, ConfigKey<Double> priceKey, TriFunction<Pokemon, ConfigKey<Double>, Double, Double> priceApplier) {
            this.enableKey = enableKey;
            this.priceKey = priceKey;
            this.priceApplier = priceApplier;
        }

        public double calculate(Pokemon source, double current) {
            if(GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(this.enableKey)) {
                return this.priceApplier.apply(source, this.priceKey, current);
            }

            return current;
        }
    }
}
