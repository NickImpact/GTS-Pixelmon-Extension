package net.impactdev.gts.generations.entry.description;

import com.google.common.collect.Lists;
import com.pixelmongenerations.common.entity.pixelmon.Entity3HasStats;
import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.core.enums.EnumSpecies;
import net.impactdev.gts.generations.GTSSpongeGenerationsPlugin;
import net.impactdev.gts.generations.config.GenerationsLangConfigKeys;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.services.text.MessageService;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.function.Predicate;

import static com.pixelmongenerations.core.enums.EnumSpecies.*;

public enum ContextualDetails {

    MewClones(GenerationsLangConfigKeys.MEW_CLONES, pokemon -> pokemon.getSpecies() == Mew),
    LakeTrioEnchants(GenerationsLangConfigKeys.LAKE_TRIO_ENCHANTS, pokemon -> {
        EnumSpecies species = pokemon.getSpecies();
        return species == Azelf || species == Mesprit || species == Uxie;
    }),
    EGG(GenerationsLangConfigKeys.EGG_INFO, pokemon -> pokemon.isEgg),
    POKERUS(GenerationsLangConfigKeys.POKERUS, pokemon -> pokemon.getPokeRus() == 1),
    GIGANTAMAX(GenerationsLangConfigKeys.GIGAMAX, Entity3HasStats::hasGmaxFactor)
    ;

    private ConfigKey<String> key;
    private Predicate<EntityPixelmon> condition;

    ContextualDetails(ConfigKey<String> key, Predicate<EntityPixelmon> condition) {
        this.key = key;
        this.condition = condition;
    }

    public static List<Text> receive(EntityPixelmon pokemon) {
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

    private static Text translate(ConfigKey<String> key, EntityPixelmon pokemon) {
        MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Config config = GTSSpongeGenerationsPlugin.getInstance().getMsgConfig();
        return service.parse(config.get(key), Lists.newArrayList(() -> pokemon));
    }
}
