package net.impactdev.gts.reforged.sponge.ui.secondary;

import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import net.impactdev.gts.reforged.sponge.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.sponge.config.ReforgedLangConfigKeys;
import net.impactdev.gts.reforged.sponge.price.ReforgedPrice;
import net.impactdev.gts.reforged.sponge.ui.ReforgedPriceCreatorMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongePage;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ReforgedFormSelectionMenu {

    private EnumSpecies species;
    private ReforgedPriceCreatorMenu parent;

    private SpongePage<IEnumForm> display;

    public ReforgedFormSelectionMenu(Player viewer, EnumSpecies species, ReforgedPriceCreatorMenu parent) {
        this.species = species;
        this.parent = parent;

        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        this.display = SpongePage.builder()
                .title(service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.UI_FORM_SELECT_TITLE)))
                .viewer(viewer)
                .view(this.design())
                .previousPage(ItemTypes.ARROW, 47)
                .nextPage(ItemTypes.ARROW, 51)
                .offsets(1)
                .contentZone(new InventoryDimension(7, 3))
                .build();
        this.display.applier(this::get);
        this.display.define(this.species.getPossibleForms(false));
    }

    public void open() {
        this.display.open();
    }

    private SpongeLayout design() {
        return SpongeLayout.builder()
                .dimension(9, 5)
                .border()
                .dimension(9, 6)
                .slots(SpongeIcon.BORDER, 45, 53)
                .build();
    }

    private SpongeIcon get(IEnumForm form) {
        ItemStack display = ItemStack.builder()
                .from(ReforgedPrice.getPicture(this.species, form))
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, form.getLocalizedName()))
                .build();
        SpongeIcon icon = new SpongeIcon(display);
        icon.addListener(clickable -> {
            this.parent.setForm(form.getForm());
            this.parent.update();
            this.parent.open();
        });

        return icon;
    }
}
