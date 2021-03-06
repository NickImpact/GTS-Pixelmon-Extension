package net.impactdev.gts.reforged.sponge.placeholders;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.entities.pixelmon.specs.UnbreedableFlag;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import net.impactdev.gts.reforged.sponge.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.sponge.config.ReforgedLangConfigKeys;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.services.text.MessageService;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.function.Function;

public class ReforgedPlaceholders {

    private static final DecimalFormat PERCENTAGE = new DecimalFormat("#0.##");

    public void register(GameRegistryEvent.Register<PlaceholderParser> event) {
        event.register(new PokemonPlaceholder(
                "species",
                "Pokemon's Species",
                pokemon -> Text.of(pokemon.getSpecies().getLocalizedName()))
        );
        event.register(new PokemonPlaceholder(
                "level",
                "Pokemon's Level",
                pokemon -> Text.of(pokemon.getLevel())
        ));
        event.register(new PokemonPlaceholder(
                "form",
                "Pokemon's Form",
                pokemon -> {
                    return Optional.ofNullable(pokemon.getFormEnum())
                            .filter(form -> form.getForm() != 0)
                            .map(form -> (Text) Text.of(form.getLocalizedName()))
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
                    Config config = GTSSpongeReforgedPlugin.getInstance().getMsgConfig();

                    if(pokemon.getAbilitySlot() == 2) {
                        return service.parse(replacer.apply(config.get(ReforgedLangConfigKeys.ABILITY_HIDDEN)));
                    } else {
                        return service.parse(replacer.apply(config.get(ReforgedLangConfigKeys.ABILITY)));
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

                    return Text.of(color, pokemon.getGender().getLocalizedName());
                }
        ));
        event.register(new PokemonPlaceholder(
                "nature",
                "Pokemon's Nature",
                pokemon -> {
                    Text result = Text.of(pokemon.getBaseNature().getLocalizedName());
                    if(pokemon.getMintNature() != null) {
                        result = Text.of(result, TextColors.GRAY, " (", TextColors.GOLD,
                                pokemon.getMintNature().getLocalizedName(), TextColors.GRAY, ")");
                    }

                    return result;
                }
        ));
        event.register(new PokemonPlaceholder(
                "size",
                "Pokemon's Size",
                pokemon -> Text.of(pokemon.getGrowth().getLocalizedName())
        ));
        event.register(new PokemonPlaceholder(
                "unbreedable",
                "Whether a Pokemon is Breedable or not",
                pokemon -> {
                    if(UnbreedableFlag.UNBREEDABLE.matches(pokemon)) {
                        return Text.of(TextColors.RED, "Unbreedable");
                    } else {
                        return Text.of(TextColors.GREEN, "Breedable");
                    }
                }
        ));

        for(String stat : Lists.newArrayList("ev", "iv")) {
            for (StatsType type : Lists.newArrayList(StatsType.HP, StatsType.Attack, StatsType.Defence, StatsType.SpecialAttack, StatsType.SpecialDefence, StatsType.Speed)) {
                event.register(new PokemonPlaceholder(
                        stat + "_" + type.name().toLowerCase(),
                        "A Pokemon's " + type.getLocalizedName() + " " + stat.toUpperCase() + " Stat",
                        pokemon -> {
                            if(stat.equals("ev")) {
                                return Text.of(pokemon.getStats().evs.get(type));
                            } else {
                                boolean hyper = pokemon.getStats().ivs.isHyperTrained(type);
                                Text result = Text.of(pokemon.getStats().ivs.get(type));
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
                    EVStore evs = pokemon.getEVs();
                    double sum = 0;
                    for(int stat : evs.getArray()) {
                        sum += stat;
                    }

                    return Text.of(PERCENTAGE.format(sum / 510.0 * 100) + "%");
                }
        ));
        event.register(new PokemonPlaceholder(
                "iv_percentage",
                "A Pokemon's Percentage of Total IVs Gained",
                pokemon -> {
                    IVStore ivs = pokemon.getIVs();
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
                pokemon -> Text.of(pokemon.getDynamaxLevel())
        ));
        event.register(new PokemonPlaceholder(
                "held_item",
                "A Pokemon's Held Item",
                pokemon -> {
                    ItemStack item = pokemon.getHeldItem();
                    if(item == ItemStack.EMPTY) {
                        return Text.EMPTY;
                    }

                    return Text.of(pokemon.getHeldItem().getDisplayName());
                }
        ));
        event.register(new PokemonPlaceholder(
                "texture",
                "A Pokemon's Custom Texture",
                pokemon -> Optional.ofNullable(pokemon.getCustomTexture())
                        .map(Text::of)
                        .orElse(Text.of("N/A"))
        ));
    }

}
