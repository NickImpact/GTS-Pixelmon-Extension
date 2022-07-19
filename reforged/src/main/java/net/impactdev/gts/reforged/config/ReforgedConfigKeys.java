package net.impactdev.gts.reforged.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import net.impactdev.gts.reforged.config.mappings.ReforgedPriceControls;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.keys.BaseConfigKey;
import net.impactdev.impactor.api.configuration.loader.KeyProvider;

import java.util.Optional;

import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.*;

@KeyProvider
public class ReforgedConfigKeys  {

    public static final ConfigKey<ReforgedPriceControls> PRICE_CONTROLS = customKey(adapter -> {
        ReforgedPriceControls controller = new ReforgedPriceControls();
        for(String key : adapter.getKeys("price-controls.overrides", Lists.newArrayList())) {
            Optional<RegistryValue<Species>> species = PixelmonSpecies.get(key);
            if(species.isPresent()) {
                double min = adapter.getDouble("price-controls.overrides." + key + ".min", -1);
                double max = adapter.getDouble("price-controls.overrides." + key + ".max", -1);

                controller.createFor(species.get(), min, max);
            }
        }
        return controller;
    });
    
    public static final ConfigKey<Boolean> ALLOW_EGG_BASE = booleanKey("listing-control.allow-eggs", true);

    public static final ConfigKey<Boolean> MIN_PRICING_USE_CUSTOM_BASE = booleanKey("price-controls.minimum.use-custom-minimum", false);
    public static final ConfigKey<Double> MIN_PRICING_CUSTOM_BASE = doubleKey("price-controls.minimum.custom-minimum", 2500);

    public static final ConfigKey<Boolean> MIN_PRICING_IVS_ENABLED = booleanKey("price-controls.minimum.ivs.enabled", true);
    public static final ConfigKey<Integer> MIN_PRICING_IVS_REQUIRE = intKey("price-controls.minimum.ivs.required-to-apply", 31);
    public static final ConfigKey<Double> MIN_PRICING_IVS_PRICE = doubleKey("price-controls.minimum.ivs.price-per-match", 5000);

    public static final ConfigKey<Boolean> MIN_PRICING_HA_ENABLED = booleanKey("price-controls.minimum.ha.enabled", true);
    public static final ConfigKey<Double> MIN_PRICING_HA_PRICE = doubleKey("price-controls.minimum.ha.price", 5000);

    public static final ConfigKey<Boolean> MIN_PRICING_LEGEND_ENABLED = booleanKey("price-controls.minimum.legendary.enabled", true);
    public static final ConfigKey<Double> MIN_PRICING_LEGEND_PRICE = doubleKey("price-controls.minimum.legendary.price", 5000);

    public static final ConfigKey<Boolean> MIN_PRICING_SHINY_ENABLED = booleanKey("price-controls.minimum.shiny.enabled", true);
    public static final ConfigKey<Double> MIN_PRICING_SHINY_PRICE = doubleKey("price-controls.minimum.shiny.price", 5000);

    public static final ConfigKey<Boolean> MIN_PRICING_TEXTURE_ENABLED = booleanKey("price-controls.minimum.texture.enabled", true);
    public static final ConfigKey<Double> MIN_PRICING_TEXTURE_PRICE = doubleKey("price-controls.minimum.texture.price", 5000);

}
