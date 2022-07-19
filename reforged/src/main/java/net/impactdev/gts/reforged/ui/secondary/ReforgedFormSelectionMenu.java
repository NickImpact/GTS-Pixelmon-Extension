package net.impactdev.gts.reforged.ui.secondary;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import net.impactdev.gts.reforged.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.config.ReforgedLangConfigKeys;
import net.impactdev.gts.reforged.price.ReforgedPrice;
import net.impactdev.gts.reforged.ui.ReforgedPriceCreatorMenu;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.impactdev.impactor.api.ui.containers.pagination.Pagination;
import net.impactdev.impactor.api.ui.containers.pagination.updaters.PageUpdater;
import net.impactdev.impactor.api.ui.containers.pagination.updaters.PageUpdaterType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.math.vector.Vector2i;

import java.util.Collection;
import java.util.List;

public class ReforgedFormSelectionMenu {

    private final Species species;
    private final ReforgedPriceCreatorMenu parent;

    private final Pagination display;

    public ReforgedFormSelectionMenu(PlatformPlayer viewer, Species species, ReforgedPriceCreatorMenu parent) {
        this.species = species;
        this.parent = parent;

        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        this.display = Pagination.builder()
                .provider(Key.key("gts-reforged", "form-selection"))
                .title(service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.UI_FORM_SELECT_TITLE)))
                .viewer(viewer)
                .layout(this.design())
                .updater(PageUpdater.builder()
                        .type(PageUpdaterType.PREVIOUS)
                        .slot(47)
                        .provider(target -> ItemStack.builder()
                                .itemType(ItemTypes.SPECTRAL_ARROW)
                                .add(Keys.CUSTOM_NAME, MiniMessage.miniMessage().deserialize("<gradient:red:gold>Previous Page (" + target + ")</gradient>"))
                                .build()
                        )
                        .build()
                )
                .updater(PageUpdater.builder()
                        .type(PageUpdaterType.NEXT)
                        .slot(53)
                        .provider(target -> ItemStack.builder()
                                .itemType(ItemTypes.SPECTRAL_ARROW)
                                .add(Keys.CUSTOM_NAME, MiniMessage.miniMessage().deserialize("<gradient:green:blue>Next Page (" + target + ")</gradient>"))
                                .build()
                        )
                        .build()
                )
                .zone(new Vector2i(7, 3), Vector2i.ONE)
                .synchronous(Stats.class)
                .contents(this.translate(this.species.getForms()))
                .build();
    }

    public void open() {
        this.display.open();
    }

    private Layout design() {
        return Layout.builder()
                .size(5)
                .border(ProvidedIcons.BORDER)
                .size(6)
                .slots(ProvidedIcons.BORDER, 45, 53)
                .build();
    }

    private List<Icon.Binding<?, Stats>> translate(Collection<Stats> forms) {
        List<Icon.Binding<?, Stats>> result = Lists.newArrayList();
        for(Stats form : forms) {
            ItemStack display = ItemStack.builder()
                    .from(ReforgedPrice.getPicture(this.species, form))
                    .add(Keys.CUSTOM_NAME, Component.text(form.getLocalizedName()).color(NamedTextColor.GREEN))
                    .build();

            result.add(Icon.builder(ItemStack.class)
                    .display(new DisplayProvider.Constant<>(display))
                    .listener(context -> {
                        this.parent.setForm(form.getName());
                        this.parent.update();
                        this.parent.open();
                        return false;
                    })
                    .build(() -> form));
        }

        return result;
    }
}
