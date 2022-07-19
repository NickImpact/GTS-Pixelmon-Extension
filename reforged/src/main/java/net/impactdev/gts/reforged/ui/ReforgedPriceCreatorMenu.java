package net.impactdev.gts.reforged.ui;

import com.google.common.collect.Lists;
import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonForms;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.reforged.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.config.ReforgedLangConfigKeys;
import net.impactdev.gts.reforged.price.ReforgedPrice;
import net.impactdev.gts.reforged.ui.secondary.ReforgedFormSelectionMenu;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.chat.ChatProcessor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class ReforgedPriceCreatorMenu {

    private final ImpactorUI display;

    private final PlatformPlayer viewer;
    private final Consumer<ReforgedPrice> callback;

    private Species species;
    private int level = -1;
    private String form = PixelmonForms.NONE;
    private boolean allowEggs = false;

    public ReforgedPriceCreatorMenu(PlatformPlayer viewer, Consumer<ReforgedPrice> callback) {
        this.viewer = viewer;
        this.callback = callback;

        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

        this.display = ImpactorUI.builder()
                .provider(Key.key("gts-reforged", "price-creator"))
                .title(service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.UI_PRICE_TITLE)))
                .layout(this.design())
                .build();
    }

    public void open() {
        this.display.open(this.viewer);
    }

    private Layout design() {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

        Layout.LayoutBuilder builder = Layout.builder();
        Icon<ItemStack> colored = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .itemType(ItemTypes.LIGHT_BLUE_STAINED_GLASS_PANE)
                    .add(Keys.CUSTOM_NAME, Component.empty())
                    .build()
                ))
                .build();

        builder.rows(ProvidedIcons.BORDER, 0, 3, 5)
                .slots(ProvidedIcons.BORDER, 9, 17, 18, 19, 20, 24, 25, 26, 37, 43)
                .slots(colored, 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);

        builder.slot(this.pokemon(), 13);
        builder.slot(this.speciesSelector(), 40);

        Icon<ItemStack> confirm = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .itemType(ItemTypes.RED_CONCRETE)
                    .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.AWAITING_SELECT_PRICE_TITLE)))
                    .add(Keys.LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.AWAITING_SELECT_PRICE_LORE)))
                    .build()
                ))
                .build();
        builder.slot(confirm, 44);

        Icon<ItemStack> back = Icon.builder(ItemStack.class)
                    .display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .itemType(ItemTypes.BARRIER)
                    .add(Keys.CUSTOM_NAME, service.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK)))
                    .build()
                ))
                .listener(context -> {
                    SpongeMainPageProvider.creator().viewer(this.viewer).build().open();
                    return false;
                })
                .build();
        builder.slot(back, 36);

        return builder.build();
    }

    private Icon<ItemStack> pokemon() {
        if(this.species == null) {
            ItemStack none = ItemStack.builder()
                    .itemType(ItemTypes.STONE_BUTTON)
                    .add(Keys.CUSTOM_NAME, Component.text("Undefined").color(NamedTextColor.RED))
                    .build();
            return Icon.builder(ItemStack.class).display(new DisplayProvider.Constant<>(none)).build();
        }

        ItemStack picture = ItemStack.builder()
                .from(ReforgedPrice.getPicture(this.species, this.species.getForm(this.form)))
                .add(Keys.CUSTOM_NAME, Component.text(this.species.getName()).color(NamedTextColor.YELLOW))
                .build();

        List<Component> lore = Lists.newArrayList();
        if(!this.form.equals(PixelmonForms.NONE)) {
            Component form = Component.text("Form: ").color(NamedTextColor.GRAY)
                    .append(Component.text(this.species.getForm(this.form).getName()).color(NamedTextColor.YELLOW));
            lore.add(form);
        }
        if(this.level > 0) {
            Component level = Component.text("Level: ").color(NamedTextColor.GRAY)
                    .append(Component.text(this.level).color(NamedTextColor.YELLOW));
            lore.add(level);
        }
        picture.offer(Keys.LORE, lore);

        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(picture))
                .build();
    }

    private Icon<ItemStack> speciesSelector() {
        ItemStack selector = ItemStack.builder()
                .itemType(this.resolve("pixelmon", "poke_ball"))
                .add(Keys.CUSTOM_NAME, this.translate(ReforgedLangConfigKeys.UI_PRICE_SPECIES_SELECT_TITLE))
                .add(Keys.LORE, this.translateList(ReforgedLangConfigKeys.UI_PRICE_SPECIES_SELECT_LORE))
                .build();
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(selector))
                .listener(context -> {
                    Impactor.getInstance().getRegistry()
                            .get(ChatProcessor.class)
                            .register(this.viewer.uuid(), input -> {
                                MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

                                Blacklist blacklist = Impactor.getInstance().getRegistry().get(Blacklist.class);
                                Optional<Species> species = PixelmonSpecies.get(input)
                                        .flatMap(RegistryValue::getValue)
                                        .filter(s -> !blacklist.isBlacklisted(Species.class, s.getName()));

                                if(species.isPresent()) {
                                    this.species = species.get();
                                    this.form = PixelmonForms.NONE;
                                    this.display.set(this.pokemon(), 13);
                                    this.display.set(this.formSelector(), 38);
                                    this.display.set(this.levelSelector(), 42);

                                    Icon<ItemStack> confirm = Icon.builder(ItemStack.class)
                                            .display(new DisplayProvider.Constant<>(ItemStack.builder().build()))
                                            .listener(ctx -> {
                                                this.display.close(this.viewer);
                                                this.callback.accept(new ReforgedPrice(new ReforgedPrice.PokemonPriceSpecs(this.species, this.form, this.level, this.allowEggs)));
                                                return false;
                                            })
                                            .build();
                                    this.display.set(confirm, 44);
                                    this.display.open(this.viewer);
                                } else {
                                    this.viewer.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_BLACKLISTED)));
                                    this.display.open(this.viewer);
                                }
                            });
                    return false;
                })
                .build();
    }

    private Icon<ItemStack> levelSelector() {
        ItemStack display = ItemStack.builder()
                .itemType(this.resolve("pixelmon", "rare_candy"))
                .add(Keys.CUSTOM_NAME, this.translate(ReforgedLangConfigKeys.UI_PRICE_LEVEL_SELECT_TITLE))
                .add(Keys.LORE, this.translateList(ReforgedLangConfigKeys.UI_PRICE_LEVEL_SELECT_LORE))
                .build();

        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(display))
                .listener(ctx -> {
                    Impactor.getInstance().getRegistry().get(ChatProcessor.class)
                            .register(this.viewer.uuid(), input -> {
                                try {
                                    this.level = Math.max(1, Math.min(PixelmonConfigProxy.getGeneral().getMaxLevel(), Integer.parseInt(input)));
                                    Impactor.getInstance().getScheduler().executeSync(() -> {
                                        this.display.set(this.pokemon(), 13);
                                        this.display.open(this.viewer);
                                    });
                                } catch (Exception ignored) {}
                            });
                    this.display.close(this.viewer);
                    return false;
                })
                .build();
    }

    public Icon<ItemStack> formSelector() {
        ItemStack display = ItemStack.builder()
                .itemType(this.resolve("pixelmon", "reassembly_unit"))
                .add(Keys.CUSTOM_NAME, this.translate(ReforgedLangConfigKeys.UI_PRICE_FORM_SELECT_TITLE))
                .add(Keys.LORE, this.translateList(ReforgedLangConfigKeys.UI_PRICE_FORM_SELECT_LORE))
                .build();
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(display))
                .listener(context -> {
                    new ReforgedFormSelectionMenu(this.viewer, this.species, this).open();
                    return false;
                })
                .build();
    }

    private ItemType resolve(String namespace, String value) {
        return Sponge.game().registry(RegistryTypes.ITEM_TYPE)
                .findEntry(ResourceKey.of(namespace, value))
                .map(RegistryEntry::value)
                .orElse(ItemTypes.BARRIER.get());
    }

    private Component translate(ConfigKey<String> key) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        return service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(key));
    }

    private List<Component> translateList(ConfigKey<List<String>> key) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        return service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(key));
    }

    public void setForm(String form) {
        this.form = form;
    }

    public void update() {
        this.display.set(this.pokemon(), 13);
    }
}
