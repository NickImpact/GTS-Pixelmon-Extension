package net.impactdev.gts.reforged.sponge.ui;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.impactdev.gts.reforged.sponge.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.sponge.config.ReforgedLangConfigKeys;
import net.impactdev.gts.reforged.sponge.price.ReforgedPrice;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.gui.signs.SignQuery;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ReforgedPriceCreatorMenu {

    private final SpongeUI display;

    private final Player viewer;
    private final Consumer<ReforgedPrice> callback;

    private EnumSpecies species;
    private int level = -1;
    private byte form = -1;
    private boolean allowEggs = false;

    public ReforgedPriceCreatorMenu(Player viewer, Consumer<ReforgedPrice> callback) {
        this.viewer = viewer;
        this.callback = callback;

        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        this.display = SpongeUI.builder()
                .title(service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.UI_PRICE_TITLE)))
                .dimension(InventoryDimension.of(9, 5))
                .build()
                .define(this.design());
    }

    public void open() {
        this.display.open(this.viewer);
    }

    private SpongeLayout design() {
        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
        SpongeIcon colored = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR, DyeColors.LIGHT_BLUE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .build()
        );

        builder.rows(SpongeIcon.BORDER, 0, 3, 5)
                .slots(SpongeIcon.BORDER, 9, 17, 18, 19, 20, 24, 25, 26, 37)
                .slots(colored, 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);

        builder.slot(this.pokemon(), 13);
        builder.slot(this.speciesSelector(), 40);

        SpongeIcon confirm = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Confirm"))
                .build()
        );
        confirm.addListener(clickable -> {
            this.display.close(this.viewer);
            this.callback.accept(new ReforgedPrice(new ReforgedPrice.PokemonPriceSpecs(this.species, this.form, this.level, this.allowEggs)));
        });
        builder.slot(confirm, 44);

        return builder.build();
    }

    private SpongeIcon pokemon() {
        if(this.species == null) {
            ItemStack none = ItemStack.builder()
                    .itemType(ItemTypes.STONE_BUTTON)
                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Undefined"))
                    .build();
            return new SpongeIcon(none);
        }

        ItemStack picture = ItemStack.builder()
                .from(ReforgedPrice.getPicture(this.species, this.species.getFormEnum(this.form)))
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, this.species.getLocalizedName()))
                .build();

        List<Text> lore = Lists.newArrayList();
        if(this.form > 0) {
            lore.add(Text.of(TextColors.GRAY, "Form: ", TextColors.YELLOW, this.species.getFormEnum(this.form).getLocalizedName()));
        }
        if(this.level > 0) {
            lore.add(Text.of(TextColors.GRAY, "Level: ", TextColors.YELLOW, this.level));
        }
        picture.offer(Keys.ITEM_LORE, lore);

        SpongeIcon icon = new SpongeIcon(picture);
        icon.addListener(clickable -> {

        });
        return icon;
    }

    private SpongeIcon speciesSelector() {
        ItemStack selector = ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:poke_ball").get())
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Select Species"))
                .build();
        SpongeIcon icon = new SpongeIcon(selector);
        icon.addListener(clickable -> {
            SignQuery<Text, Player> query = SignQuery.<Text, Player>builder()
                    .position(new Vector3d(0, 1, 0))
                    .text(Lists.newArrayList(
                            Text.EMPTY,
                            Text.of("----------------"),
                            Text.of("Enter the Species"),
                            Text.of("you desire above")
                    ))
                    .reopenOnFailure(false)
                    .response(submission -> {
                        Optional<EnumSpecies> species = EnumSpecies.getFromName(submission.get(0));
                        if(species.isPresent()) {
                            this.species = species.get();

                            Impactor.getInstance().getScheduler().executeSync(() -> {
                                this.display.setSlot(13, this.pokemon());
                                this.display.open(this.viewer);
                            });
                            return true;
                        }

                        this.display.open(this.viewer);
                        return false;
                    })
                    .build();
            this.viewer.closeInventory();
            query.sendTo(this.viewer);
        });
        return icon;
    }

}
