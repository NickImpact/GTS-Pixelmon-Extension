package net.impactdev.gts.reforged.config;

import com.google.common.collect.Lists;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.loader.KeyProvider;

import java.util.List;

import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.stringKey;
import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.listKey;

@KeyProvider
public final class ReforgedLangConfigKeys {

    public static final ConfigKey<String> POKEMON_TITLE = stringKey("listing.details.title", "&3{{gts-reforged:species}} {{gts-reforged:shiny_special:s}}&7| &bLvl {{gts-reforged:level}}");
    public static final ConfigKey<List<String>> POKEMON_DETAILS = listKey("listing.details.info", Lists.newArrayList(
            "&aGeneric Information:",
            "  &7Form: &e{{gts-reforged:form}}",
            "  &7Palette: &e{{gts-reforged:palette}}",
            "  &7Ability: &e{{gts-reforged:ability}}",
            "  &7Gender: {{gts-reforged:gender}}",
            "  &7Nature: &e{{gts-reforged:nature}}",
            "  &7Size: &e{{gts-reforged:size}}",
            "  &7Breed Status: {{gts-reforged:unbreedable}}",
            "",
            "&aStats:",
            "  &7EVs: &e{{gts-reforged:ev_hp}}&7/&e{{gts-reforged:ev_attack}}&7/&e{{gts-reforged:ev_defence}}&7/&e{{gts-reforged:ev_specialattack}}&7/&e{{gts-reforged:ev_specialdefence}}&7/&e{{gts-reforged:ev_speed}} &7(&b{{gts-reforged:ev_percentage}}&7)",
            "  &7IVs: &e{{gts-reforged:iv_hp}}&7/&e{{gts-reforged:iv_attack}}&7/&e{{gts-reforged:iv_defence}}&7/&e{{gts-reforged:iv_specialattack}}&7/&e{{gts-reforged:iv_specialdefence}}&7/&e{{gts-reforged:iv_speed}} &7(&b{{gts-reforged:iv_percentage}}&7)"
    ));

    public static final ConfigKey<List<String>> DISCORD_DETAILS = listKey("listing.details.discord", Lists.newArrayList(
            "Level: {{gts-reforged:level}}",
            "Form: {{gts-reforged:form}}",
            "Palette: {{gts-reforged:palette}}",
            "Shiny: {{gts-reforged:shiny}}",
            "",
            "Ability: {{gts-reforged:ability}}",
            "Gender: {{gts-reforged:gender}}",
            "Nature: {{gts-reforged:nature}}",
            "",
            "EVs: {{gts-reforged:ev_hp}}/{{gts-reforged:ev_attack}}/{{gts-reforged:ev_defence}}/{{gts-reforged:ev_specialattack}}/{{gts-reforged:ev_specialdefence}}/{{gts-reforged:ev_speed}} ({{gts-reforged:ev_percentage}})",
            "IVs: {{gts-reforged:iv_hp}}/{{gts-reforged:iv_attack}}/{{gts-reforged:iv_defence}}/{{gts-reforged:iv_specialattack}}/{{gts-reforged:iv_specialdefence}}/{{gts-reforged:iv_speed}} ({{gts-reforged:iv_percentage}})"
    ));

    public static final ConfigKey<String> ABILITY = stringKey("ability.normal", "%ability%");
    public static final ConfigKey<String> ABILITY_HIDDEN = stringKey("ability.hidden", "%ability% &7(&6HA&7)");

    public static final ConfigKey<String> MEW_CLONES = stringKey("contextual.extras.mew-clones", "&7Cloned &e{{gts-reforged:clones}} &7times");
    public static final ConfigKey<String> LAKE_TRIO_ENCHANTS = stringKey("contextual.extras.lake-trio-enchants", "&7Enchanted &e{{gts-reforged:enchantments}} &7times");
    public static final ConfigKey<String> EGG_INFO = stringKey("contextual.egg-info", "&7Hatch Progress: &e{{gts-reforged:egg-steps}}");
    public static final ConfigKey<String> POKERUS = stringKey("contextual.pokerus", "&dInflicted with Pokerus");
    public static final ConfigKey<String> GIGAMAX = stringKey("contextual.gigamax", "&aGigantamax Capable");

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

    public static ConfigKey<String> POKEMON_SHINY_DETAILS_LABEL = stringKey("general.details.shiny", "&6Shiny");
    public static final ConfigKey<String> ERROR_UNTRADEABLE = stringKey("general.errors.untradeable", "{{gts:error}} That pokemon is marked as &cuntradeable&7, and cannot be sold...");
    public static final ConfigKey<String> ERROR_IN_BATTLE = stringKey("general.errors.in-battle", "{{gts:error}} You are in battle, so you can't sell any pokemon currently...");
    public static final ConfigKey<String> ERROR_ISEGG = stringKey("general.errors.isegg", "{{gts:error}} &cEggs&7 cannot be sold at this time...");
    public static final ConfigKey<String> ERROR_LAST_ABLE_MEMBER = stringKey("general.errors.last-able-member", "{{gts:error}} You can't sell your last non-egg member!");

}
