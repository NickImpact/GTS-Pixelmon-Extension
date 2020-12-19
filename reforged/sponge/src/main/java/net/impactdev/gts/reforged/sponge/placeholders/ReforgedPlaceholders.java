package net.impactdev.gts.reforged.sponge.placeholders;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.entities.pixelmon.specs.UnbreedableFlag;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.text.DecimalFormat;
import java.util.Optional;

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
                "ability",
                "Pokemon's Ability",
                pokemon -> Text.of(pokemon.getAbility().getLocalizedName()
                        .replaceAll("[\u00a7][r]", "")
                        .replaceAll("[\u00a7][k].", "")
                )
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
                                return Text.of(pokemon.getStats().ivs.get(type));
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
    }

}
