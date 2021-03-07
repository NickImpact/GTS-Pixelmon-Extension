package net.impactdev.gts.reforged.sponge.entry.description;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumPokerusType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.impactdev.gts.reforged.sponge.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.sponge.config.ReforgedLangConfigKeys;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.services.text.MessageService;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.function.Predicate;

import static com.pixelmonmod.pixelmon.enums.EnumSpecies.*;

public enum ContextualDetails {

    MewClones(ReforgedLangConfigKeys.MEW_CLONES, pokemon -> pokemon.getSpecies() == Mew),
    LakeTrioEnchants(ReforgedLangConfigKeys.LAKE_TRIO_ENCHANTS, pokemon -> {
        EnumSpecies species = pokemon.getSpecies();
        return species == Azelf || species == Mesprit || species == Uxie;
    }),
    EGG(ReforgedLangConfigKeys.EGG_INFO, Pokemon::isEgg),
    POKERUS(ReforgedLangConfigKeys.POKERUS, pokemon -> pokemon.getPokerus() != null && pokemon.getPokerus().type != EnumPokerusType.UNINFECTED),
    ;

    private ConfigKey<String> key;
    private Predicate<Pokemon> condition;

    ContextualDetails(ConfigKey<String> key, Predicate<Pokemon> condition) {
        this.key = key;
        this.condition = condition;
    }

    public static List<Text> receive(Pokemon pokemon) {
        List<Text> results = Lists.newArrayList();
        for(ContextualDetails details : ContextualDetails.values()) {
            if(details.condition.test(pokemon)) {
                results.add(translate(details.key, pokemon));
            }
        }

        if(results.size() > 0) {
            results.add(0, Text.EMPTY);
        }

        return results;
    }

    private static Text translate(ConfigKey<String> key, Pokemon pokemon) {
        MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Config config = GTSSpongeReforgedPlugin.getInstance().getMsgConfig();
        return service.parse(config.get(key), Lists.newArrayList(() -> pokemon));
    }
}
