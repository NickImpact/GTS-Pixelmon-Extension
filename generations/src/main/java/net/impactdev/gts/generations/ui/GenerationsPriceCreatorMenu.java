package net.impactdev.gts.generations.ui;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import com.pixelmongenerations.core.config.PixelmonConfig;
import com.pixelmongenerations.core.enums.EnumSpecies;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.generations.GTSSpongeGenerationsPlugin;
import net.impactdev.gts.generations.config.GenerationsLangConfigKeys;
import net.impactdev.gts.generations.price.GenerationsPrice;
import net.impactdev.gts.generations.ui.secondary.GenerationsFormSelectionMenu;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
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

import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class GenerationsPriceCreatorMenu
{

    private final SpongeUI display;

    private final Player viewer;
    private final Consumer<GenerationsPrice> callback;

    private EnumSpecies species;
    private int level = -1;
    private byte form = -1;
    private boolean allowEggs = false;

    public GenerationsPriceCreatorMenu(Player viewer, Consumer<GenerationsPrice> callback) {
        this.viewer = viewer;
        this.callback = callback;

        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        this.display = SpongeUI.builder()
                .title(service.parse(GTSSpongeGenerationsPlugin.getInstance().getMsgConfig()
                        .get(GenerationsLangConfigKeys.UI_PRICE_TITLE)))
                .dimension(InventoryDimension.of(9, 5))
                .build()
                .define(this.design());
    }

    public void open() {
        this.display.open(this.viewer);
    }

    private SpongeLayout design() {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
        SpongeIcon colored = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR, DyeColors.LIGHT_BLUE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .build()
        );

        builder.rows(SpongeIcon.BORDER, 0, 3, 5)
                .slots(SpongeIcon.BORDER, 9, 17, 18, 19, 20, 24, 25, 26, 37, 43)
                .slots(colored, 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);

        builder.slot(this.pokemon(), 13);
        builder.slot(this.speciesSelector(), 40);

        SpongeIcon confirm = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.AWAITING_SELECT_PRICE_TITLE)))
                .add(Keys.ITEM_LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.AWAITING_SELECT_PRICE_LORE)))
                .build()
        );
        builder.slot(confirm, 44);

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, service.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(() -> this.viewer)))
                .build()
        );
        back.addListener(clickable -> {
            SpongeMainPageProvider.creator().viewer(this.viewer).build().open();
        });
        builder.slot(back, 36);

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
                .from(GenerationsPrice.getPicture(this.species, this.species.getFormEnum(this.form)))
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, this.species.getPokemonName()))
                .build();

        List<Text> lore = Lists.newArrayList();
        if(this.form > 0) {
            lore.add(Text.of(TextColors.GRAY, "Form: ", TextColors.YELLOW, this.species.getFormEnum(this.form).getProperName()));
        }
        if(this.level > 0) {
            lore.add(Text.of(TextColors.GRAY, "Level: ", TextColors.YELLOW, this.level));
        }
        picture.offer(Keys.ITEM_LORE, lore);

        return new SpongeIcon(picture);
    }

    private SpongeIcon speciesSelector() {
        ItemStack selector = ItemStack.builder()
                .itemType(this.resolve("pixelmon:poke_ball"))
                .add(Keys.DISPLAY_NAME, this.translate(GenerationsLangConfigKeys.UI_PRICE_SPECIES_SELECT_TITLE))
                .add(Keys.ITEM_LORE, this.translateList(GenerationsLangConfigKeys.UI_PRICE_SPECIES_SELECT_LORE))
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
                            if(!Impactor.getInstance().getRegistry().get(Blacklist.class).isBlacklisted(EnumSpecies.class, species.get().name)) {
                                this.species = species.get();

                                Impactor.getInstance().getScheduler().executeSync(() -> {
                                    this.form = -1;

                                    this.display.setSlot(13, this.pokemon());

                                    this.display.setSlot(38, this.formSelector());
                                    this.display.setSlot(42, this.levelSelector());

                                    final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
                                    SpongeIcon confirm = new SpongeIcon(ItemStack.builder()
                                            .itemType(ItemTypes.CONCRETE)
                                            .add(Keys.DYE_COLOR, DyeColors.LIME)
                                            .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CONFIRM_SELECT_PRICE_TITLE)))
                                            .add(Keys.ITEM_LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CONFIRM_SELECT_PRICE_LORE)))
                                            .build()
                                    );
                                    confirm.addListener(c -> {
                                        this.display.close(this.viewer);
                                        this.callback.accept(new GenerationsPrice(new GenerationsPrice.PokemonPriceSpecs(this.species, this.form, this.level, this.allowEggs)));
                                    });
                                    this.display.setSlot(44, confirm);

                                    this.display.open(this.viewer);
                                });
                            } else {
                                final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
                                this.viewer.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_BLACKLISTED)));

                                this.display.open(this.viewer);
                            }
                            return true;
                        }

                        this.display.open(this.viewer);
                        return false;
                    })
                    .build();
            this.display.close(this.viewer);
            query.sendTo(this.viewer);
        });
        return icon;
    }

    private SpongeIcon levelSelector() {
        ItemStack display = ItemStack.builder()
                .itemType(this.resolve("pixelmon:rare_candy"))
                .add(Keys.DISPLAY_NAME, this.translate(GenerationsLangConfigKeys.UI_PRICE_LEVEL_SELECT_TITLE))
                .add(Keys.ITEM_LORE, this.translateList(GenerationsLangConfigKeys.UI_PRICE_LEVEL_SELECT_LORE))
                .build();

        SpongeIcon icon = new SpongeIcon(display);
        icon.addListener(clickable -> {
            SignQuery<Text, Player> query = SignQuery.<Text, Player>builder()
                    .position(new Vector3d(0, 1, 0))
                    .text(Lists.newArrayList(
                            Text.EMPTY,
                            Text.of("----------------"),
                            Text.of("Enter the level"),
                            Text.of("you desire above")
                    ))
                    .reopenOnFailure(false)
                    .response(submission -> {
                        try {
                            this.level = Math.max(1, Math.min(PixelmonConfig.maxLevel, Integer.parseInt(submission.get(0))));
                            Impactor.getInstance().getScheduler().executeSync(() -> {
                                this.display.setSlot(13, this.pokemon());
                                this.display.open(this.viewer);
                            });
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .build();
            this.display.close(this.viewer);
            query.sendTo(this.viewer);
        });

        return icon;
    }

    public SpongeIcon formSelector() {
        ItemStack display = ItemStack.builder()
                .itemType(this.resolve("pixelmon:lucarionite"))
                .add(Keys.DISPLAY_NAME, this.translate(GenerationsLangConfigKeys.UI_PRICE_FORM_SELECT_TITLE))
                .add(Keys.ITEM_LORE, this.translateList(GenerationsLangConfigKeys.UI_PRICE_FORM_SELECT_LORE))
                .build();
        SpongeIcon icon = new SpongeIcon(display);
        icon.addListener(clickable -> new GenerationsFormSelectionMenu(this.viewer, this.species, this).open());
        return icon;
    }

    private ItemType resolve(String id) {
        return Sponge.getRegistry().getType(ItemType.class, id).orElse(ItemTypes.BARRIER);
    }

    private Text translate(ConfigKey<String> key) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        return service.parse(GTSSpongeGenerationsPlugin.getInstance().getMsgConfig().get(key));
    }

    private List<Text> translateList(ConfigKey<List<String>> key) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        return service.parse(GTSSpongeGenerationsPlugin.getInstance().getMsgConfig().get(key));
    }

    public void setForm(byte form) {
        this.form = form;
    }

    public void update() {
        this.display.setSlot(13, this.pokemon());
    }
}

