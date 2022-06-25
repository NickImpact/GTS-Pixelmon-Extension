package net.impactdev.gts.reforged.entry.description;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokerusStrain;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import net.impactdev.gts.reforged.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.config.ReforgedLangConfigKeys;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.function.Predicate;

import static com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies.AZELF;
import static com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies.MESPRIT;
import static com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies.MEW;
import static com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies.UXIE;

public enum ContextualDetails {

    MewClones(ReforgedLangConfigKeys.MEW_CLONES, pokemon -> pokemon.getSpecies().is(MEW)),
    LakeTrioEnchants(ReforgedLangConfigKeys.LAKE_TRIO_ENCHANTS, pokemon -> pokemon.getSpecies().is(AZELF, MESPRIT, UXIE)),
    EGG(ReforgedLangConfigKeys.EGG_INFO, Pokemon::isEgg),
    POKERUS(ReforgedLangConfigKeys.POKERUS, pokemon -> pokemon.getPokerus() != null && pokemon.getPokerus().type != PokerusStrain.UNINFECTED),
    GIGANTAMAX(ReforgedLangConfigKeys.GIGAMAX, Pokemon::canGigantamax)
    ;

    private final ConfigKey<String> key;
    private final Predicate<Pokemon> condition;

    ContextualDetails(ConfigKey<String> key, Predicate<Pokemon> condition) {
        this.key = key;
        this.condition = condition;
    }

    public static List<Component> receive(Pokemon pokemon) {
        List<Component> results = Lists.newArrayList();
        for(ContextualDetails details : ContextualDetails.values()) {
            if(details.condition.test(pokemon)) {
                results.add(translate(details.key, pokemon));
            }
        }

        if(results.size() > 0) {
            results.add(0, Component.empty());
        }

        return results;
    }

    private static Component translate(ConfigKey<String> key, Pokemon pokemon) {
        MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Config config = GTSSpongeReforgedPlugin.getInstance().getMsgConfig();

        return service.parse(config.get(key), PlaceholderSources.builder().append(Pokemon.class, () -> pokemon).build());
    }
}
