package net.impactdev.gts.reforged.entry;

import com.google.common.collect.Lists;
import com.pixelmonmod.api.Flags;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.registries.PixelmonPalettes;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.NbtKeys;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.items.SpriteItem;
import net.impactdev.gts.api.listings.prices.PriceControlled;
import net.impactdev.gts.api.util.TriFunction;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.reforged.config.ReforgedConfigKeys;
import net.impactdev.gts.reforged.config.ReforgedLangConfigKeys;
import net.impactdev.gts.reforged.config.mappings.ReforgedPriceControls;
import net.impactdev.gts.reforged.converter.JObjectConverter;
import net.impactdev.gts.reforged.entry.description.ContextualDetails;
import net.impactdev.gts.sponge.listings.makeup.SpongeDisplay;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.services.text.MessageService;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.reforged.GTSSpongeReforgedPlugin;
import net.impactdev.pixelmonbridge.details.SpecKeys;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@GTSKeyMarker({"pokemon", "reforged-pokemon"})
public class ReforgedEntry extends SpongeEntry<ReforgedPokemon> implements PriceControlled {

    public ReforgedPokemon pokemon;
    private transient Display<ItemStack> display;

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
        final MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);
        TextComponent.Builder builder = Component.text();
        if (this.pokemon.getOrCreate().isShiny()) {
             builder.append(parser.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.POKEMON_SHINY_DETAILS_LABEL) + " "));
        }
        return builder.append( this.getName()).build();
    }

    @Override
    public Display<ItemStack> getDisplay(UUID viewer) {
        if(this.display == null) {
            final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

            List<Component> lore = Lists.newArrayList();
            PlaceholderSources sources = PlaceholderSources.builder()
                    .append(ReforgedPokemon.class, () -> this.pokemon)
                    .build();

            lore.addAll(service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.POKEMON_DETAILS), sources));
            lore.addAll(ContextualDetails.receive(this.getOrCreateElement().getOrCreate()));

            ItemStack rep = ItemStack.builder()
                    .from(this.getPicture(this.pokemon.getOrCreate()))
                    .add(Keys.CUSTOM_NAME, service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig()
                            .get(ReforgedLangConfigKeys.POKEMON_TITLE), sources)
                    )
                    .add(Keys.LORE, lore)
                    .build();
            this.display = new SpongeDisplay(rep);
        }

        return this.display;
    }

    @Override
    public boolean give(UUID receiver) {
        if(Sponge.server().player(receiver).isPresent()) {
            PlayerPartyStorage storage = StorageProxy.getParty(receiver);
            if(storage.inTemporaryMode()) {
                return false;
            }

            if (!storage.hasSpace()) {
                PCStorage pc = StorageProxy.getPCForPlayer(receiver);
                if (!pc.hasSpace()) {
                    return false;
                }
            }

            return storage.add(this.pokemon.getOrCreate());
        }

        return false;
    }

    @Override
    public boolean take(UUID depositor) {
        Optional<ServerPlayer> user = Sponge.server().player(depositor);
        Config mainLang = GTSPlugin.instance().configuration().language();
        Config reforgedLang = GTSSpongeReforgedPlugin.getInstance().getMsgConfig();

        MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);

        PlayerPartyStorage party = StorageProxy.getParty(depositor);
        if(party.inTemporaryMode()) {
            return false;
        }

        if(BattleRegistry.getBattle(party.getPlayer()) != null) {
            user.ifPresent(player -> player.sendMessage(parser.parse(reforgedLang.get(ReforgedLangConfigKeys.ERROR_IN_BATTLE))));
            return false;
        }

        // Attempt to handle a case where certain UTF-8 character encodings cause invisible nicknames/database issues
        Optional<String> nickname = this.pokemon.get(SpecKeys.NICKNAME);
        if(nickname.isPresent()) {
            byte[] bytes = nickname.get().getBytes(StandardCharsets.UTF_8);
        }
        
        if (this.pokemon.getOrCreate().isEgg()) {
             if (!GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(ReforgedConfigKeys.ALLOW_EGG_BASE)) {
                 user.ifPresent( player -> player.sendMessage(parser.parse(reforgedLang.get(ReforgedLangConfigKeys.ERROR_ISEGG))));
                 return false;
             }
         }

        if(this.getOrCreateElement().getOrCreate().hasFlag(Flags.UNTRADEABLE)) {
            user.ifPresent(player -> player.sendMessage(parser.parse(reforgedLang.get(ReforgedLangConfigKeys.ERROR_UNTRADEABLE))));
            return false;
        }

        boolean blacklisted = Impactor.getInstance().getRegistry()
                .get(Blacklist.class)
                .isBlacklisted(Species.class, this.pokemon.getOrCreate().getSpecies().getName());
        if(blacklisted) {
            user.ifPresent(player -> player.sendMessage(parser.parse(mainLang.get(MsgConfigKeys.GENERAL_FEEDBACK_BLACKLISTED))));
            return false;
        }

        // Check party size. Ensure we aren't less than 1 because who knows whether Reforged or another plugin
        // will break something
        if(party.getTeam().size() <= 1 && !this.getOrCreateElement().get(SpecKeys.EGG_INFO).isPresent()) {
            user.ifPresent(player -> player.sendMessage(parser.parse(reforgedLang.get(ReforgedLangConfigKeys.ERROR_LAST_ABLE_MEMBER))));
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
        url.append(this.pokemon.getOrCreate().getSpecies().getName().toLowerCase());
        url.append(".gif");
        return Optional.of(url.toString());
    }

    @Override
    public List<String> getDetails() {
        MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);
        Config reforgedLang = GTSSpongeReforgedPlugin.getInstance().getMsgConfig();
        PlaceholderSources sources = PlaceholderSources.builder()
                .append(ReforgedPokemon.class, this::getOrCreateElement)
                .build();

        return parser.parse(reforgedLang.get(ReforgedLangConfigKeys.DISCORD_DETAILS), sources)
                .stream()
                .map(PlainTextComponentSerializer.plainText()::serialize)
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

        boolean aprilFools = (calendar.get(Calendar.MONTH) == Calendar.APRIL
                || calendar.get(Calendar.MONTH) == Calendar.JULY)
                && calendar.get(Calendar.DAY_OF_MONTH) == 1;

        if(pokemon.isEgg()) {
            net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(PixelmonItems.pixelmon_sprite);
            CompoundNBT nbt = new CompoundNBT();
            if(pokemon.getSpecies().is(PixelmonSpecies.MANAPHY, PixelmonSpecies.TOGEPI)) {
                nbt.putString(NbtKeys.SPRITE_NAME, String.format("pixelmon:sprites/eggs/%s1", pokemon.getSpecies().getName().toLowerCase()));
            } else {
                nbt.putString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
            }

            return (ItemStack) (Object) item;
        } else {
            return (ItemStack) (Object) (aprilFools ? SpriteItem.getPhoto(PokemonFactory.create(PixelmonSpecies.BIDOOF.getValueUnsafe())) : SpriteItem.getPhoto(pokemon));
        }
    }

    @Override
    public double getMin() {
        Optional<ReforgedPriceControls.Control> control = GTSSpongeReforgedPlugin.getInstance().getConfiguration()
                .get(ReforgedConfigKeys.PRICE_CONTROLS)
                .get(this.getOrCreateElement().getOrCreate().getSpecies().getRegistryValue());

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
                GTSPlugin.instance().configuration().main().get(ConfigKeys.LISTINGS_MIN_PRICE),
                calculated
        ));
    }

    @Override
    public double getMax() {
        Optional<ReforgedPriceControls.Control> control = GTSSpongeReforgedPlugin.getInstance().getConfiguration()
                .get(ReforgedConfigKeys.PRICE_CONTROLS)
                .get(this.getOrCreateElement().getOrCreate().getSpecies().getRegistryValue());

        return Math.max(1, Math.min(
                GTSPlugin.instance().configuration().main().get(ConfigKeys.LISTINGS_MAX_PRICE),
                control.map(ReforgedPriceControls.Control::getMax).orElse(Double.MAX_VALUE)
        ));
    }

    private enum MinimumPriceCalculator {
        LEGENDARY(ReforgedConfigKeys.MIN_PRICING_LEGEND_ENABLED, ReforgedConfigKeys.MIN_PRICING_LEGEND_PRICE, (pokemon, key, current) -> {
            if(PixelmonSpecies.getLegendaries().contains(pokemon.getSpecies().getDex())) {
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
        CUSTOM_TEXTURE(ReforgedConfigKeys.MIN_PRICING_TEXTURE_ENABLED, ReforgedConfigKeys.MIN_PRICING_TEXTURE_PRICE, (pokemon, key, current) -> {
            if(!pokemon.getPalette().is(PixelmonPalettes.NONE)) {
                return GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(key) + current;
            }

            return current;
        }),
        HIDDEN_ABILITY(ReforgedConfigKeys.MIN_PRICING_HA_ENABLED, ReforgedConfigKeys.MIN_PRICING_HA_PRICE, (pokemon, key, current) -> {
            if(pokemon.getForm().getAbilities().isHiddenAbility(pokemon.getAbility())) {
                return GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(key) + current;
            }

            return current;
        }),
        IV(ReforgedConfigKeys.MIN_PRICING_IVS_ENABLED, ReforgedConfigKeys.MIN_PRICING_IVS_PRICE, (pokemon, key, current) -> {
            int required = GTSSpongeReforgedPlugin.getInstance().getConfiguration().get(ReforgedConfigKeys.MIN_PRICING_IVS_REQUIRE);
            for(int iv : pokemon.getStats().getIVs().getArray()) {
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
