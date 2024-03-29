package net.impactdev.gts.generations.placeholders;

import com.google.common.collect.Lists;
import com.pixelmongenerations.common.battle.attacks.Attack;
import com.pixelmongenerations.common.battle.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmongenerations.common.entity.pixelmon.stats.EVsStore;
import com.pixelmongenerations.common.entity.pixelmon.stats.Gender;
import com.pixelmongenerations.common.entity.pixelmon.stats.IVStore;
import com.pixelmongenerations.common.entity.pixelmon.stats.StatsType;
import com.pixelmongenerations.common.entity.pixelmon.stats.extraStats.LakeTrioStats;
import com.pixelmongenerations.common.entity.pixelmon.stats.extraStats.MewStats;
import com.pixelmongenerations.core.config.PixelmonConfig;
import com.pixelmongenerations.core.enums.EnumSpecies;
import com.pixelmongenerations.core.storage.NbtKeys;
import net.impactdev.gts.generations.GTSSpongeGenerationsPlugin;
import net.impactdev.gts.generations.config.GenerationsLangConfigKeys;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.services.text.MessageService;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GenerationsPlaceholders {

    private static final DecimalFormat PERCENTAGE = new DecimalFormat("#0.##");

    public void register(GameRegistryEvent.Register<PlaceholderParser> event) {
        event.register(new PokemonPlaceholder(
                "species",
                "Pokemon's Species",
                pokemon -> Text.of(pokemon.getSpecies().getPokemonName()))
        );
        event.register(new PokemonPlaceholder(
                "level",
                "Pokemon's Level",
                pokemon -> Text.of(pokemon.level.getLevel())
        ));
        event.register(new PokemonPlaceholder(
                "form",
                "Pokemon's Form",
                pokemon -> {
                    return Optional.ofNullable(pokemon.getFormEnum())
                            .filter(form -> form.getForm() != 0)
                            .map(form -> (Text) Text.of(form.getProperName()))
                            .orElse(Text.of("N/A"));
                }
        ));
        event.register(new PokemonPlaceholder(
                "shiny_special",
                "A Preformatted Representation of Shiny State",
                pokemon -> {
                    if(pokemon.isShiny()) {
                        return Text.of(TextColors.GRAY, "(", TextColors.GOLD, "Shiny", TextColors.GRAY, ")");
                    }

                    return Text.EMPTY;
                }
        ));
        event.register(new PokemonPlaceholder(
                "shiny",
                "Pokemon Shiny State",
                pokemon -> Text.of(pokemon.isShiny())
        ));
        event.register(new PokemonPlaceholder(
                "ability",
                "Pokemon's Ability",
                pokemon -> {
                    MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
                    Function<String, String> replacer = in -> in.replaceAll("[\u00a7][r]", "")
                            .replaceAll("[\u00a7][k]", "")
                            .replaceAll("[%]ability[%]", pokemon.getAbility().getLocalizedName());
                    Config config = GTSSpongeGenerationsPlugin.getInstance().getMsgConfig();

                    if(pokemon.getAbilitySlot() == 2) {
                        return service.parse(replacer.apply(config.get(GenerationsLangConfigKeys.ABILITY_HIDDEN)));
                    } else {
                        return service.parse(replacer.apply(config.get(GenerationsLangConfigKeys.ABILITY)));
                    }
                }
        ));
        event.register(new PokemonPlaceholder(
                "gender",
                "Pokemon's Gender",
                pokemon -> {
                    Gender gender = pokemon.getGender();
                    TextColor color = gender == Gender.Male ? TextColors.AQUA :
                            gender == Gender.Female ? TextColors.LIGHT_PURPLE : TextColors.GRAY;

                    return Text.of(color, gender == Gender.None ? "No Gender" : pokemon.getGender().getProperName());
                }
        ));
        event.register(new PokemonPlaceholder(
                "nature",
                "Pokemon's Nature",
                pokemon -> {
                    Text result = Text.of(pokemon.getNature().getLocalizedName());
                    if(pokemon.getPseudoNature() != null && !pokemon.getNature().equals(pokemon.getPseudoNature())) {
                        result = Text.of(result, TextColors.GRAY, " (", TextColors.GOLD,
                                pokemon.getPseudoNature().getLocalizedName(), TextColors.GRAY, ")");
                    }

                    return result;
                }
        ));
        event.register(new PokemonPlaceholder(
                "size",
                "Pokemon's Size",
                pokemon -> Text.of(pokemon.getGrowth().getLocalizedName())
        ));

        for(String stat : Lists.newArrayList("ev", "iv")) {
            for (StatsType type : Lists.newArrayList(StatsType.HP, StatsType.Attack, StatsType.Defence, StatsType.SpecialAttack, StatsType.SpecialDefence, StatsType.Speed)) {
                event.register(new PokemonPlaceholder(
                        stat + "_" + type.name().toLowerCase(),
                        "A Pokemon's " + type.getLocalizedName() + " " + stat.toUpperCase() + " Stat",
                        pokemon -> {
                            if(stat.equals("ev")) {
                                return Text.of(pokemon.stats.EVs.get(type));
                            } else {
                                boolean hyper = pokemon.stats.isBottleCapIV(type);
                                Text result = Text.of(pokemon.stats.IVs.get(type));
                                if(hyper) {
                                    return Text.of(TextColors.AQUA, result);
                                }
                                return result;
                            }
                        }
                ));
            }
        }
        event.register(new PokemonPlaceholder(
                "ev_percentage",
                "A Pokemon's Percentage of Total EVs Gained",
                pokemon -> {
                    EVsStore evs = pokemon.stats.EVs;
                    double sum = 0;
                    sum += evs.Attack;
                    sum += evs.Defence;
                    sum += evs.HP;
                    sum += evs.SpecialAttack;
                    sum += evs.SpecialDefence;
                    sum += evs.Speed;

                    return Text.of(PERCENTAGE.format(sum / 510.0 * 100) + "%");
                }
        ));
        event.register(new PokemonPlaceholder(
                "iv_percentage",
                "A Pokemon's Percentage of Total IVs Gained",
                pokemon -> {
                    IVStore ivs = pokemon.stats.IVs;
                    double sum = 0;
                    for(int stat : ivs.getArray()) {
                        sum += stat;
                    }

                    return Text.of(PERCENTAGE.format(sum / 186.0 * 100) + "%");
                }
        ));
        event.register(new PokemonPlaceholder(
                "dynamax_level",
                "A Pokemon's Dynamax Level",
                pokemon -> Text.of(pokemon.level.getDynamaxLevel())
        ));
        event.register(new PokemonPlaceholder(
                "held_item",
                "A Pokemon's Held Item",
                pokemon -> {
                    ItemStack item = pokemon.heldItem;
                    if(item == ItemStack.EMPTY || item == null) {
                        return Text.EMPTY;
                    }

                    return Text.of(pokemon.heldItem.getDisplayName());
                }
        ));
        event.register(new PokemonPlaceholder(
                "texture",
                "A Pokemon's Custom Texture",
                pokemon -> Optional.ofNullable(pokemon.getCustomTexture())
                        .map(Text::of)
                        .orElse(Text.of("N/A"))
        ));
        event.register(new PokemonPlaceholder(
                "clones",
                "Number of Mew Clones",
                pokemon -> {
                    if(pokemon.getSpecies() == EnumSpecies.Mew) {
                        MewStats stats = (MewStats) pokemon.extraStats;

                        return Text.of(stats.numCloned);
                    }

                    return Text.EMPTY;
                }
        ));
        event.register(new PokemonPlaceholder(
                "enchantments",
                "Number of Lake Trio Enchantments",
                pokemon -> {
                    List<EnumSpecies> options = Lists.newArrayList(EnumSpecies.Azelf, EnumSpecies.Mesprit, EnumSpecies.Uxie);
                    if(options.contains(pokemon.getSpecies())) {
                        LakeTrioStats stats = (LakeTrioStats) pokemon.extraStats;

                        return Text.of(stats.numEnchanted);
                    }

                    return Text.EMPTY;
                }
        ));
        event.register(new PokemonPlaceholder(
                "hidden_power",
                "A Pokemon's Hidden Power",
                pokemon -> Text.of(HiddenPower.getHiddenPowerType(pokemon.stats.IVs))
        ));
        event.register(new PokemonPlaceholder(
                "egg-steps",
                "Amount of steps remaining for an egg",
                pokemon -> {
                    if(pokemon.isEgg) {
                        int total = (pokemon.baseStats.eggCycles + 1) * PixelmonConfig.stepsPerEggCycle;
                        int walked = pokemon.writeToNBT(new NBTTagCompound()).getInteger(NbtKeys.STEPS) + ((pokemon.baseStats.eggCycles - pokemon.eggCycles) * PixelmonConfig.stepsPerEggCycle);

                        return Text.of(walked, "/", total);
                    }

                    return Text.EMPTY;
                }
        ));
        for(int i = 0; i < 4; i++) {
            final int index = i;
            event.register(new PokemonPlaceholder(
                    "move" + (i + 1),
                    "Pokemon's Move at index: " + (i + 1),
                    pokemon -> {
                        Attack attack = pokemon.getMoveset().get(index);
                        if(attack != null) {
                            return Text.of(attack.getAttackBase().getLocalizedName());
                        } else {
                            return Text.EMPTY;
                        }
                    }
            ));
        }
        event.register(new PokemonPlaceholder(
                "can_gmax",
                "Pokemon G-Max Potential",
                pokemon -> Text.of(pokemon.hasGmaxFactor())
        ));
    }

}