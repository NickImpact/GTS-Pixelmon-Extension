package net.impactdev.gts.reforged.sponge.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.ConfigKeyHolder;
import net.impactdev.impactor.api.configuration.keys.BaseConfigKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.stringKey;
import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.listKey;

public class ReforgedLangConfigKeys implements ConfigKeyHolder {

    public static final ConfigKey<String> POKEMON_TITLE = stringKey("listing.details.title", "&3{{pokemon:species}} {{pokemon:shiny_special:s}}&7| &bLvl {{pokemon:level}}");
    public static final ConfigKey<List<String>> POKEMON_DETAILS = listKey("listing.details.info", Lists.newArrayList(
            "&aGeneric Information:",
            "  &7Ability: &e{{pokemon:ability}}",
            "  &7Gender: {{pokemon:gender}}",
            "  &7Nature: &e{{pokemon:nature}}",
            "  &7Size: &e{{pokemon:size}}",
            "  &7Breed Status: {{pokemon:unbreedable}}",
            "",
            "&aStats:",
            "  &7EVs: &e{{pokemon:ev_hp}}&7/&e{{pokemon:ev_attack}}&7/&e{{pokemon:ev_defence}}&7/&e{{pokemon:ev_specialattack}}&7/&e{{pokemon:ev_specialdefence}}&7/&e{{pokemon:ev_speed}} &7(&b{{pokemon:ev_percentage}}&7)",
            "  &7IVs: &e{{pokemon:iv_hp}}&7/&e{{pokemon:iv_attack}}&7/&e{{pokemon:iv_defence}}&7/&e{{pokemon:iv_specialattack}}&7/&e{{pokemon:iv_specialdefence}}&7/&e{{pokemon:iv_speed}} &7(&b{{pokemon:iv_percentage}}&7)"
    ));

    private static final Map<String, ConfigKey<?>> KEYS;
    private static final int SIZE;

    static {
        Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
        Field[] values = ReforgedLangConfigKeys.class.getFields();
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
