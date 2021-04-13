package net.impactdev.gts.generations.ui.secondary;

import com.pixelmongenerations.core.enums.EnumSpecies;
import com.pixelmongenerations.core.enums.forms.IEnumForm;
import net.impactdev.gts.generations.GTSSpongeGenerationsPlugin;
import net.impactdev.gts.generations.config.GenerationsLangConfigKeys;
import net.impactdev.gts.generations.price.GenerationsPrice;
import net.impactdev.gts.generations.ui.GenerationsPriceCreatorMenu;
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

import java.util.stream.Collectors;

public class GenerationsFormSelectionMenu
{
    private Player viewer;

    private EnumSpecies species;
    private GenerationsPriceCreatorMenu parent;

    private SpongePage<IEnumForm> display;

    public GenerationsFormSelectionMenu(Player viewer, EnumSpecies species, GenerationsPriceCreatorMenu parent) {
        this.viewer = viewer;
        this.species = species;
        this.parent = parent;

        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        this.display = SpongePage.builder()
                .title(service.parse(GTSSpongeGenerationsPlugin.getInstance().getMsgConfig()
                        .get(GenerationsLangConfigKeys.UI_FORM_SELECT_TITLE)))
                .viewer(viewer)
                .view(this.design())
                .previousPage(ItemTypes.ARROW, 47)
                .nextPage(ItemTypes.ARROW, 51)
                .offsets(1)
                .contentZone(new InventoryDimension(7, 3))
                .build();
        this.display.applier(this::get);
        this.display.define(this.species.getAllForms().stream()
                .map(formInt -> this.species.getFormEnum(formInt))
                .collect(Collectors.toList()));
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
                .from(GenerationsPrice.getPicture(this.species, form))
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, form.getProperName()))
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
