package net.impactdev.gts.reforged.placeholders;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.pixelmonmod.api.Flags;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.gender.Gender;
import com.pixelmonmod.pixelmon.api.pokemon.species.palette.PaletteProperties;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.EVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.api.pokemon.stats.extraStats.MewStats;
import com.pixelmonmod.pixelmon.api.registries.PixelmonForms;
import com.pixelmonmod.pixelmon.api.registries.PixelmonPalettes;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import net.impactdev.gts.common.adventure.processors.gradients.NumberBasedGradientProcessor;
import net.impactdev.gts.reforged.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.config.ReforgedLangConfigKeys;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.placeholder.PlaceholderParser;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import static com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies.AZELF;
import static com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies.MESPRIT;
import static com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies.MEW;
import static com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies.UXIE;
import static net.kyori.adventure.text.Component.text;

public class ReforgedPlaceholders {

    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#0.##");
    private static final NumberBasedGradientProcessor<Integer> IVS = NumberBasedGradientProcessor.<Integer>builder()
            .type(new TypeToken<Integer>() {})
            .min(0)
            .max(31)
            .translator(Component::text)
            .factor(x -> x.floatValue() * 0.1f / 3)
            .colors(NamedTextColor.RED, NamedTextColor.YELLOW, NamedTextColor.GREEN)
            .build();
    private static final NumberBasedGradientProcessor<Double> PERCENTAGE = NumberBasedGradientProcessor.<Integer>builder()
            .type(new TypeToken<Double>() {})
            .min(0)
            .max(100)
            .translator(value -> text(PERCENTAGE_FORMAT.format(value)).append(text("%")))
            .factor(x -> x.floatValue() * 0.1f / 10)
            .colors(NamedTextColor.RED, NamedTextColor.YELLOW, NamedTextColor.GREEN)
            .build();

    public void register(Object populator) {
        RegisterRegistryValueEvent.RegistryStep<PlaceholderParser> registry = (RegisterRegistryValueEvent.RegistryStep<PlaceholderParser>) populator;

        this.register(registry, "species", pokemon -> text(pokemon.getSpecies().getLocalizedName()));
        this.register(registry, "level", pokemon -> text(pokemon.getPokemonLevel()));
        this.register(registry, "form", pokemon -> Optional.ofNullable(pokemon.getForm())
                .filter(form -> !form.getName().equals(PixelmonForms.NONE))
                .map(form -> text(form.getLocalizedName()))
                .orElse(text("N/A"))
        );
        this.register(registry, "palette", pokemon -> Optional.ofNullable(pokemon.getPalette())
                .filter(palette -> !palette.is(PixelmonPalettes.NONE))
                .map(palette -> text(palette.getLocalizedName()))
                .orElse(text("N/A"))
        );
        this.register(registry, "shiny", pokemon -> text(pokemon.isShiny()));
        this.register(registry, "ability", pokemon -> {
            MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
            Function<String, String> replacer = in -> in.replaceAll("§r", "")
                    .replaceAll("§k", "")
                    .replaceAll("%ability%", pokemon.getAbility().getLocalizedName());
            Config config = GTSSpongeReforgedPlugin.getInstance().getMsgConfig();

            boolean hidden = pokemon.getForm().getAbilities().isHiddenAbility(pokemon.getAbility());
            if(hidden) {
                return service.parse(replacer.apply(config.get(ReforgedLangConfigKeys.ABILITY_HIDDEN)));
            } else {
                return service.parse(replacer.apply(config.get(ReforgedLangConfigKeys.ABILITY)));
            }
        });
        this.register(registry, "gender", pokemon -> {
            Gender gender = pokemon.getGender();
            TextColor color = gender == Gender.MALE ? NamedTextColor.AQUA :
                    gender == Gender.FEMALE ? NamedTextColor.LIGHT_PURPLE : NamedTextColor.GRAY;

            return text(pokemon.getGender().getLocalizedName()).color(color);
        });
        this.register(registry, "nature", pokemon -> {
            Component result = text(pokemon.getBaseNature().getLocalizedName());
            if(pokemon.getMintNature() != null) {
                result = result.append(text("(").color(NamedTextColor.GRAY))
                        .append(text(pokemon.getMintNature().getLocalizedName()).color(NamedTextColor.GOLD))
                        .append(text(")").color(NamedTextColor.GRAY));
            }

            return result;
        });
        this.register(registry, "size", pokemon -> text(pokemon.getGrowth().getLocalizedName()));
        this.register(registry, "unbreedable", pokemon -> {
            if(pokemon.hasFlag(Flags.UNBREEDABLE)) {
                return text("Unbreedable").color(NamedTextColor.RED);
            } else {
                return text("Breedable").color(NamedTextColor.GREEN);
            }
        });
        for(String stat : Lists.newArrayList("ev", "iv")) {
            for(BattleStatsType type : BattleStatsType.getStatValues()) {
                this.register(registry, stat + "_" + StatNameTranslator.key(type), pokemon -> {
                    switch (stat) {
                        case "ev":
                            return text(pokemon.getEVs().getStat(type));
                        case "iv":
                            boolean hyper = pokemon.getIVs().isHyperTrained(type);
                            int value = pokemon.getIVs().getStat(type);
                            if(hyper) {
                                return text(value).color(NamedTextColor.GOLD);
                            } else {
                                return IVS.process(value);
                            }
                        default:
                            return text("-1");
                    }
                });
            }
        }
        this.register(registry, "ev_percentage", pokemon -> {
            EVStore evs = pokemon.getEVs();
            return PERCENTAGE.process((double) evs.getTotal() / EVStore.MAX_TOTAL_EVS * 100);
        });
        this.register(registry, "iv_percentage", pokemon -> {
            IVStore ivs = pokemon.getIVs();
            return PERCENTAGE.process((double) ivs.getTotal() / (IVStore.MAX_IVS * 6) * 100);
        });
        this.register(registry, "dynamax_level", pokemon -> text(pokemon.getDynamaxLevel()));
        this.register(registry, "held_item", pokemon -> {
            ItemStack held = pokemon.getHeldItem();
            if(held.isEmpty()) {
                return Component.empty();
            } else {
                return text(pokemon.getHeldItemAsItemHeld().getLocalizedName());
            }
        });
        this.register(registry, "texture", pokemon -> {
            PaletteProperties texture = pokemon.getPalette();
            if(texture.is(PixelmonPalettes.NONE)) {
                return Component.empty();
            }

            String name = texture.getLocalizedName();
            return text(String.valueOf(name.charAt(0)).toUpperCase())
                    .append(text(name.substring(1)));
        });
        this.register(registry, "clones", pokemon -> {
            if(pokemon.getSpecies().is(MEW)) {
                MewStats stats = (MewStats) pokemon.getExtraStats();
                return text(stats.numCloned);
            }

            return text("N/A");
        });
        this.register(registry,"enchantments", pokemon -> {
            if(pokemon.getSpecies().is(AZELF, MESPRIT, UXIE)) {
                LakeTrioStats stats = (LakeTrioStats) pokemon.getExtraStats();
                return text(stats.numEnchanted);
            }

            return text("N/A");
        });
        this.register(registry, "hidden_power", pokemon -> {
            IVStore ivs = pokemon.getIVs();
            int a = ivs.getStat(BattleStatsType.HP) % 2;
            int b = ivs.getStat(BattleStatsType.ATTACK) % 2;
            int c = ivs.getStat(BattleStatsType.DEFENSE) % 2;
            int d = ivs.getStat(BattleStatsType.SPEED) % 2;
            int e = ivs.getStat(BattleStatsType.SPECIAL_ATTACK) % 2;
            int f = ivs.getStat(BattleStatsType.SPECIAL_DEFENSE) % 2;
            double fedbca = (double) (32 * f + 16 * e + 8 * d + 4 * c + 2 * b + a);
            int type = (int) Math.floor(fedbca * 15.0 / 63.0);
            Element element = null;
            if (type == 0) {
                element = Element.FIGHTING;
            } else if (type == 1) {
                element = Element.FLYING;
            } else if (type == 2) {
                element = Element.POISON;
            } else if (type == 3) {
                element = Element.GROUND;
            } else if (type == 4) {
                element = Element.ROCK;
            } else if (type == 5) {
                element = Element.BUG;
            } else if (type == 6) {
                element = Element.GHOST;
            } else if (type == 7) {
                element = Element.STEEL;
            } else if (type == 8) {
                element = Element.FIRE;
            } else if (type == 9) {
                element = Element.WATER;
            } else if (type == 10) {
                element = Element.GRASS;
            } else if (type == 11) {
                element = Element.ELECTRIC;
            } else if (type == 12) {
                element = Element.PSYCHIC;
            } else if (type == 13) {
                element = Element.ICE;
            } else if (type == 14) {
                element = Element.DRAGON;
            } else {
                element = Element.DARK;
            }

            return text(element.getLocalizedName());
        });
        this.register(registry, "egg-steps", pokemon -> {
            if(pokemon.isEgg()) {
                int total = (pokemon.getEggCycles() + 1) * PixelmonConfigProxy.getBreeding().getStepsPerEggCycle();
                int walked = pokemon.getEggSteps() + ((pokemon.getForm().getEggCycles() - pokemon.getEggCycles()) * PixelmonConfigProxy.getBreeding().getStepsPerEggCycle());

                return text(walked).append(text("/")).append(text(total));
            }

            return text("N/A");
        });
        for(int i = 0; i < 4; i++) {
            final int index = i;
            this.register(registry, "move" + (i + 1), pokemon -> {
                Attack attack = pokemon.getMoveset().get(index);
                if(attack != null) {
                    return text(attack.getActualMove().getLocalizedName());
                }

                return text("---");
            });
        }
        this.register(registry, "can-gmax", pokemon -> text(pokemon.hasGigantamaxFactor()));
    }

    private void register(RegisterRegistryValueEvent.RegistryStep<PlaceholderParser> registry, String key, Function<Pokemon, Component> translator) {
        registry.register(this.key(key), new PokemonPlaceholder(translator));
    }

    private ResourceKey key(String value) {
        return ResourceKey.of("gts-reforged", value);
    }

    private enum StatNameTranslator {
        HP(BattleStatsType.HP, "hp"),
        ATTACK(BattleStatsType.ATTACK, "attack"),
        DEFENCE(BattleStatsType.DEFENSE, "defence"),
        SPECIAL_ATTACK(BattleStatsType.SPECIAL_ATTACK, "specialattack"),
        SPECIAL_DEFENCE(BattleStatsType.SPECIAL_DEFENSE, "specialdefence"),
        SPEED(BattleStatsType.SPEED, "speed");

        private final BattleStatsType type;
        private final String key;

        StatNameTranslator(final BattleStatsType type, final String key) {
            this.type = type;
            this.key = key;
        }

        public static String key(BattleStatsType type) {
            return Arrays.stream(values()).filter(x -> x.type.equals(type))
                    .map(x -> x.key)
                    .findAny()
                    .orElseThrow(IllegalArgumentException::new);
        }
    }

}
