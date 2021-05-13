package net.impactdev.gts.generations.config;

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

import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.listKey;
import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.stringKey;

public class GenerationsLangConfigKeys implements ConfigKeyHolder {

    public static final ConfigKey<String> POKEMON_TITLE = stringKey("listing.details.title", "&3{{gts-generations:species}} {{gts-generations:shiny_special:s}}&7| &bLvl {{gts-generations:level}}");
    public static final ConfigKey<List<String>> POKEMON_DETAILS = listKey("listing.details.info", Lists.newArrayList(
            "&aGeneric Information:",
            "  &7Form: &e{{gts-generations:form}}",
            "  &7Ability: &e{{gts-generations:ability}}",
            "  &7Gender: {{gts-generations:gender}}",
            "  &7Nature: &e{{gts-generations:nature}}",
            "  &7Size: &e{{gts-generations:size}}",
            "",
            "&aStats:",
            "  &7EVs: &e{{gts-generations:ev_hp}}&7/&e{{gts-generations:ev_attack}}&7/&e{{gts-generations:ev_defence}}&7/&e{{gts-generations:ev_specialattack}}&7/&e{{gts-generations:ev_specialdefence}}&7/&e{{gts-generations:ev_speed}} &7(&b{{gts-generations:ev_percentage}}&7)",
            "  &7IVs: &e{{gts-generations:iv_hp}}&7/&e{{gts-generations:iv_attack}}&7/&e{{gts-generations:iv_defence}}&7/&e{{gts-generations:iv_specialattack}}&7/&e{{gts-generations:iv_specialdefence}}&7/&e{{gts-generations:iv_speed}} &7(&b{{gts-generations:iv_percentage}}&7)"
    ));

    public static final ConfigKey<List<String>> DISCORD_DETAILS = listKey("listing.details.discord", Lists.newArrayList(
            "Level: {{gts-generations:level}}",
            "Form: {{gts-generations:form}}",
            "Shiny: {{gts-generations:shiny}}",
            "",
            "Ability: {{gts-generations:ability}}",
            "Gender: {{gts-generations:gender}}",
            "Nature: {{gts-generations:nature}}",
            "Held Item: {{gts-generations:held_item}}",
            "",
            "EVs: {{gts-generations:ev_hp}}/{{gts-generations:ev_attack}}/{{gts-generations:ev_defence}}/{{gts-generations:ev_specialattack}}/{{gts-generations:ev_specialdefence}}/{{gts-generations:ev_speed}} ({{gts-generations:ev_percentage}})",
            "EVs: {{gts-generations:iv_hp}}/{{gts-generations:iv_attack}}/{{gts-generations:iv_defence}}/{{gts-generations:iv_specialattack}}/{{gts-generations:iv_specialdefence}}/{{gts-generations:iv_speed}} ({{gts-generations:iv_percentage}})"
    ));

    public static final ConfigKey<String> ABILITY = stringKey("ability.normal", "%ability%");
    public static final ConfigKey<String> ABILITY_HIDDEN = stringKey("ability.hidden", "%ability% &7(&6HA&7)");

    public static final ConfigKey<String> MEW_CLONES = stringKey("contextual.extras.mew-clones", "&7Cloned &e{{gts-reforged:clones}} &7times");
    public static final ConfigKey<String> LAKE_TRIO_ENCHANTS = stringKey("contextual.extras.lake-trio-enchants", "&7Enchanted &e{{gts-reforged:enchantments}} &7times");
    public static final ConfigKey<String> EGG_INFO = stringKey("contextual.egg-info", "&7Hatch Progress: &e{{gts-reforged:egg-steps}}");
    public static final ConfigKey<String> POKERUS = stringKey("contextual.pokerus", "&dInflicted with Pokerus");
    public static final ConfigKey<String> GIGAMAX = stringKey("contextual.gigamax", "&eGigantamax Capable");

    //------------------------------------------------------------------------------------------------------------------
    //
    //  UI Based Language Options
    //
    //------------------------------------------------------------------------------------------------------------------

    public static ConfigKey<String> UI_PRICE_TITLE = stringKey("ui.menu.pricing.title", "&cGTS &7\u00bb &3Specify your price");
    public static ConfigKey<String> UI_PRICE_SELECTOR_TITLE = stringKey("ui.menu.price-selector.title", "&cGTS &7\u00bb Select your Payment");

    public static ConfigKey<String> UI_FORM_SELECT_TITLE = stringKey("ui.ui.menu.price-selector.forms.title", "&cGTS &7\u00bb &3Select a Form");

    public static ConfigKey<String> UI_PRICE_SPECIES_SELECT_TITLE = stringKey("ui.menu.price-selector.icons.species.title", "&aSelect Species");
    public static ConfigKey<List<String>> UI_PRICE_SPECIES_SELECT_LORE = listKey("ui.menu.price-selector.icons.species.lore", Lists.newArrayList(
            "&7Sets the species of the",
            "&7pokemon you are asking to",
            "&7receive!",
            "",
            "&eClick to set pokemon species!"
    ));

    public static ConfigKey<String> UI_PRICE_LEVEL_SELECT_TITLE = stringKey("ui.menu.price-selector.icons.level.title", "&aSelect Level");
    public static ConfigKey<List<String>> UI_PRICE_LEVEL_SELECT_LORE = listKey("ui.menu.price-selector.icons.level.lore", Lists.newArrayList(
            "&7Sets the minimum level of",
            "&7the pokemon you are asking",
            "&7to receive!",
            "",
            "&eClick to set minimum level!"
    ));

    public static ConfigKey<String> UI_PRICE_FORM_SELECT_TITLE = stringKey("ui.menu.price-selector.icons.form.title", "&aSelect Form");
    public static ConfigKey<List<String>> UI_PRICE_FORM_SELECT_LORE = listKey("ui.menu.price-selector.icons.form.lore", Lists.newArrayList(
            "&7Sets the form of the",
            "&7pokemon you are asking to",
            "&7receive!",
            "",
            "&eClick to set requested form!"
    ));

    public static final ConfigKey<String> ERROR_UNTRADEABLE = stringKey("general.errors.untradeable", "{{gts:error}} That pokemon is marked as &cuntradeable&7, and cannot be sold...");
    public static final ConfigKey<String> ERROR_IN_BATTLE = stringKey("general.errors.in-battle", "{{gts:error}} You are in battle, so you can't sell any pokemon currently...");
    public static final ConfigKey<String> ERROR_LAST_ABLE_MEMBER = stringKey("general.errors.last-able-member", "{{gts:error}} You can't sell your last non-egg member!");

    private static final Map<String, ConfigKey<?>> KEYS;
    private static final int SIZE;

    static {
        Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
        Field[] values = GenerationsLangConfigKeys.class.getFields();
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
