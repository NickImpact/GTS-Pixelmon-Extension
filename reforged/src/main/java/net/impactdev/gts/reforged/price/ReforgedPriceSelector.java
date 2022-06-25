package net.impactdev.gts.reforged.price;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.NbtKeys;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.items.SpriteItem;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.reforged.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.config.ReforgedLangConfigKeys;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static net.impactdev.gts.sponge.utils.Utilities.PARSER;
import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class ReforgedPriceSelector implements PriceManager.PriceSelectorUI {

    private final PlatformPlayer viewer;
    private final ReforgedPrice.PokemonPriceSpecs specs;
    private final ImpactorUI display;

    private final Consumer<Object> callback;

    private Pokemon selection;

    public ReforgedPriceSelector(PlatformPlayer viewer, ReforgedPrice.PokemonPriceSpecs specs, Consumer<Object> callback) {
        this.viewer = viewer;
        this.specs = specs;
        this.callback = callback;

        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

        this.display = ImpactorUI.builder()
                .provider(Key.key("gts-reforged", "price-selector"))
                .title(service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.UI_PRICE_SELECTOR_TITLE)))
                .layout(this.design())
                .build();
    }

    @Override
    public ImpactorUI getDisplay() {
        return this.display;
    }

    @Override
    public Consumer<Object> getCallback() {
        return this.callback;
    }

    private Layout design() {
        final MessageService PARSER = Utilities.PARSER;

        Layout.LayoutBuilder builder = Layout.builder();
        builder.size(3).border(ProvidedIcons.BORDER)
                .size(5).border(ProvidedIcons.BORDER)
                .size(6).border(ProvidedIcons.BORDER);

        builder.slots(this.border(ItemTypes.LIGHT_BLUE_STAINED_GLASS_PANE.get()), 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);
        builder.slots(ProvidedIcons.BORDER, 19, 20, 24, 25, 31, 46, 52);

        builder.slot(this.getSelected(), 13);

        AtomicInteger index = new AtomicInteger(28);
        Runnable increment = () -> {
            index.incrementAndGet();
            if(index.get() == 31) {
                index.getAndIncrement();
            }
        };
        for(Pokemon pokemon : StorageProxy.getParty(this.viewer.uuid()).getAll()) {
            if(pokemon == null) {
                increment.run();
                continue;
            }

            if(this.specs.matches(pokemon)) {
                builder.slot(this.createIconForPokemon(pokemon, true), index.get());
            } else {
                Icon<ItemStack> icon = Icon.builder(ItemStack.class)
                        .display(new DisplayProvider.Constant<>(ItemStack.builder()
                                .itemType(ItemTypes.BARRIER)
                                .add(Keys.CUSTOM_NAME, Component.text("Ineligible"))
                                .build()
                        ))
                        .build();
                builder.slot(icon, index.get());
            }
            increment.run();
        }

        Icon<ItemStack> confirm = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .itemType(ItemTypes.RED_CONCRETE)
                    .add(Keys.CUSTOM_NAME, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.AWAITING_SELECTION_TITLE)))
                    .add(Keys.LORE, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.AWAITING_SELECTION_LORE)))
                    .build())
                )
                .build();
        builder.slot(confirm, 51);

        Icon<ItemStack> back = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.BARRIER)
                        .add(Keys.CUSTOM_NAME, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK)))
                        .build())
                )
                .listener(context -> {
                    SpongeMainPageProvider.creator().viewer(this.viewer).build().open();
                    return false;
                })
                .build();
        builder.slot(back, 47);

        return builder.build();
    }

    private Icon<ItemStack> border(ItemType color) {
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(color)
                        .add(Keys.CUSTOM_NAME, Component.empty())
                        .build()
                ))
                .build();
    }

    private Icon<ItemStack> getSelected() {
        if(this.selection == null) {
            ItemStack none = ItemStack.builder()
                    .itemType(ItemTypes.STONE_BUTTON)
                    .add(Keys.CUSTOM_NAME, Component.text("Undefined").color(NamedTextColor.RED))
                    .build();
            return Icon.builder(ItemStack.class).display(new DisplayProvider.Constant<>(none)).build();
        } else {
            return this.createIconForPokemon(this.selection, false);
        }
    }

    private Icon<ItemStack> createIconForPokemon(Pokemon pokemon, boolean click) {
        PlaceholderSources sources = PlaceholderSources.builder()
                .append(Pokemon.class, () -> pokemon)
                .build();

        ItemStack item = ItemStack.builder()
                .fromItemStack(this.getPicture(pokemon))
                .add(Keys.CUSTOM_NAME, PARSER.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.POKEMON_TITLE), sources))
                .add(Keys.LORE, PARSER.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.POKEMON_DETAILS), sources))
                .build();
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(item))
                .listener(context -> {
                    if(click) {
                        this.selection = pokemon;
                        this.display.set(this.getSelected(), 13);

                        Icon<ItemStack> confirmer = Icon.builder(ItemStack.class)
                                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                                    .itemType(ItemTypes.GREEN_CONCRETE)
                                    .add(Keys.CUSTOM_NAME, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CONFIRM_SELECTION_TITLE)))
                                    .add(Keys.LORE, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CONFIRM_SELECTION_LORE)))
                                    .build())
                                )
                                .listener(ctx -> {
                                    this.display.close(this.viewer);
                                    this.callback.accept(this.selection.getPosition());
                                    return false;
                                })
                                .build();
                        this.display.set(confirmer, 51);
                    }

                    return false;
                })
                .build();
    }

    private ItemStack getPicture(Pokemon pokemon) {
        Calendar calendar = Calendar.getInstance();

        boolean aprilFools = (calendar.get(Calendar.MONTH) == Calendar.APRIL || calendar.get(Calendar.MONTH) == Calendar.JULY)
                && calendar.get(Calendar.DAY_OF_MONTH) == 1;

        if(pokemon.isEgg()) {
            net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(PixelmonItems.pixelmon_sprite);
            CompoundNBT nbt = new CompoundNBT();
            if(pokemon.getSpecies().is(PixelmonSpecies.MANAPHY, PixelmonSpecies.TOGEPI)) {
                nbt.putString(NbtKeys.SPRITE_NAME, String.format("pixelmon:sprites/eggs/%s1", pokemon.getSpecies().getName().toLowerCase()));
            } else {
                nbt.putString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
            }

            return (ItemStack) (Object) item;
        } else {
            return (ItemStack) (Object) (aprilFools ? SpriteItem.getPhoto(PokemonFactory.create(PixelmonSpecies.BIDOOF.getValueUnsafe())) : SpriteItem.getPhoto(pokemon));
        }
    }
}
