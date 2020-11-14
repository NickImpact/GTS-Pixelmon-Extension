package net.impactdev.gts.reforged.sponge.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.impactdev.gts.reforged.sponge.config.mappings.ReforgedPriceControls;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.ConfigKeyHolder;
import net.impactdev.impactor.api.configuration.keys.BaseConfigKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.*;

public class ReforgedConfigKeys implements ConfigKeyHolder {

    public static final ConfigKey<ReforgedPriceControls> PRICE_CONTROLS = customKey(adapter -> {
        ReforgedPriceControls controller = new ReforgedPriceControls();
        for(String key : adapter.getKeys("price-controls.overrides", Lists.newArrayList())) {
            Optional<EnumSpecies> species = EnumSpecies.getFromName(key);
            if(species.isPresent()) {
                double min = adapter.getDouble("price-controls.overrides" + key + ".min", -1);
                double max = adapter.getDouble("price-controls.overrides" + key + ".max", -1);

                controller.createFor(species.get(), min, max);
            }
        }
        return controller;
    });

    public static final ConfigKey<Boolean> MIN_PRICING_IVS_ENABLED = booleanKey("price-controls.minimum.ivs.enabled", true);
    public static final ConfigKey<Integer> MIN_PRICING_IVS_REQUIRE = intKey("price-controls.minimum.ivs.required-to-apply", 31);
    public static final ConfigKey<Double> MIN_PRICING_IVS_PRICE = doubleKey("price-controls.minimum.ivs.price-per-match", 5000);

    public static final ConfigKey<Boolean> MIN_PRICING_HA_ENABLED = booleanKey("price-controls.minimum.ha.enabled", true);
    public static final ConfigKey<Double> MIN_PRICING_HA_PRICE = doubleKey("price-controls.minimum.ha.price", 5000);

    public static final ConfigKey<Boolean> MIN_PRICING_LEGEND_ENABLED = booleanKey("price-controls.minimum.legend.enabled", true);
    public static final ConfigKey<Double> MIN_PRICING_LEGEND_PRICE = doubleKey("price-controls.minimum.legend.price", 5000);

    public static final ConfigKey<Boolean> MIN_PRICING_SHINY_ENABLED = booleanKey("price-controls.minimum.shiny.enabled", true);
    public static final ConfigKey<Double> MIN_PRICING_SHINY_PRICE = doubleKey("price-controls.minimum.shiny.price", 5000);

    private static final Map<String, ConfigKey<?>> KEYS;
    private static final int SIZE;

    static {
        Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
        Field[] values = ReforgedConfigKeys.class.getFields();
        int i = 0;

        for (Field f : values) {
            // ignore non-static fields
            if (!Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            // ignore fields that aren't configkeys
            if (!ConfigKey.class.equals(f.getType())) {
                continue;
            }

            try {
                // get the key instance
                BaseConfigKey<?> key = (BaseConfigKey<?>) f.get(null);
                // set the ordinal value of the key.
                key.ordinal = i++;
                // add the key to the return map
                keys.put(f.getName(), key);
            } catch (Exception e) {
                throw new RuntimeException("Exception processing field: " + f, e);
            }
        }

        KEYS = ImmutableMap.copyOf(keys);
        SIZE = i;
    }

    @Override
    public Map<String, ConfigKey<?>> getKeys() {
        return KEYS;
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}
