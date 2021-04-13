package net.impactdev.gts.generations.price;

import com.google.common.collect.Lists;
import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.common.item.ItemPixelmonSprite;
import com.pixelmongenerations.core.storage.PixelmonStorage;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.generations.GTSSpongeGenerationsPlugin;
import net.impactdev.gts.generations.config.GenerationsLangConfigKeys;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static net.impactdev.gts.sponge.utils.Utilities.PARSER;
import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class GenerationsPriceSelector implements PriceManager.PriceSelectorUI<SpongeUI> {

    private final Player viewer;
    private final GenerationsPrice.PokemonPriceSpecs specs;
    private final SpongeUI display;

    private Consumer<Object> callback;

    private EntityPixelmon selection;

    public GenerationsPriceSelector(Player viewer, GenerationsPrice.PokemonPriceSpecs specs, Consumer<Object> callback) {
        this.viewer = viewer;
        this.specs = specs;
        this.callback = callback;

        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        this.display = SpongeUI.builder()
                .title(service.parse(GTSSpongeGenerationsPlugin.getInstance().getMsgConfig()
                        .get(GenerationsLangConfigKeys.UI_PRICE_SELECTOR_TITLE)))
                .dimension(InventoryDimension.of(9, 6))
                .build()
                .define(this.design());
    }

    @Override
    public SpongeUI getDisplay() {
        return this.display;
    }

    @Override
    public Consumer<Object> getCallback() {
        return this.callback;
    }

    private SpongeLayout design() {
        final MessageService<Text> PARSER = Utilities.PARSER;

        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
        builder.dimension(9, 3).border()
                .dimension(9, 5).border()
                .dimension(9, 6).border();

        builder.slots(this.border(DyeColors.LIGHT_BLUE), 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);
        builder.slots(SpongeIcon.BORDER, 19, 20, 24, 25, 31, 46, 52);

        builder.slot(this.getSelected(), 13);

        AtomicInteger index = new AtomicInteger(28);
        Runnable increment = () -> {
            index.incrementAndGet();
            if(index.get() == 31) {
                index.getAndIncrement();
            }
        };
        for(NBTTagCompound nbt : PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(this.viewer.getUniqueId()).get().partyPokemon) {
            if(nbt == null)
                continue;

            EntityPixelmon pokemon = new EntityPixelmon(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
            pokemon.readFromNBT(nbt);

            if(this.specs.matches(pokemon)) {
                SpongeIcon icon = this.createIconForPokemon(pokemon, true);
                builder.slot(icon, index.get());
            } else {
                builder.slot(
                        new SpongeIcon(ItemStack.builder()
                                .itemType(ItemTypes.BARRIER)
                                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Ineligible"))
                                .build()
                        ),
                        index.get()
                );
            }
            increment.run();
        }

        SpongeIcon confirm = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.DISPLAY_NAME, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.AWAITING_SELECTION_TITLE)))
                .add(Keys.ITEM_LORE, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.AWAITING_SELECTION_LORE)))
                .build());
        builder.slot(confirm, 51);

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(() -> this.viewer)))
                .build()
        );
        back.addListener(clickable -> {
            SpongeMainPageProvider.creator().viewer(this.viewer).build().open();
        });
        builder.slot(back, 47);

        return builder.build();
    }

    private SpongeIcon border(DyeColor color) {
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .add(Keys.DYE_COLOR, color)
                .build());
    }

    private SpongeIcon getSelected() {
        if(this.selection == null) {
            ItemStack none = ItemStack.builder()
                    .itemType(ItemTypes.STONE_BUTTON)
                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Undefined"))
                    .build();
            return new SpongeIcon(none);
        } else {
            return this.createIconForPokemon(this.selection, false);
        }
    }

    private SpongeIcon createIconForPokemon(EntityPixelmon pokemon, boolean click)
    {
        net.minecraft.item.ItemStack forgeItem = ItemPixelmonSprite.getPhoto(pokemon);
        ItemStack item = ItemStack.builder()
                .fromItemStack((ItemStack) (Object) forgeItem)
                .add(Keys.DISPLAY_NAME, PARSER.parse(GTSSpongeGenerationsPlugin.getInstance().getMsgConfig().get(GenerationsLangConfigKeys.POKEMON_TITLE), Lists.newArrayList(() -> pokemon)))
                .add(Keys.ITEM_LORE, PARSER.parse(GTSSpongeGenerationsPlugin.getInstance().getMsgConfig().get(GenerationsLangConfigKeys.POKEMON_DETAILS), Lists.newArrayList(() -> pokemon)))
                .build();
        SpongeIcon icon = new SpongeIcon(item);
        if(click) {
            icon.addListener(clickable -> {
                this.selection = pokemon;
                this.display.setSlot(13, this.getSelected());

                SpongeIcon confirmer = new SpongeIcon(ItemStack.builder()
                        .itemType(ItemTypes.CONCRETE)
                        .add(Keys.DYE_COLOR, DyeColors.GREEN)
                        .add(Keys.DISPLAY_NAME, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CONFIRM_SELECTION_TITLE)))
                        .add(Keys.ITEM_LORE, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CONFIRM_SELECTION_LORE)))
                        .build());

                confirmer.addListener(c -> {
                    this.display.close(this.viewer);
                    this.callback.accept(this.selection);
                });

                this.display.setSlot(51, confirmer);
            });
        }
        return icon;
    }
}
