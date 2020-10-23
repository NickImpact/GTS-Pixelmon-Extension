package net.impactdev.gts.reforged.sponge.ui;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.items.ItemPixelmonSprite;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.reforged.sponge.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.sponge.config.ReforgedLangConfigKeys;
import net.impactdev.gts.sponge.ui.Displayable;
import net.impactdev.gts.common.ui.Historical;
import net.impactdev.gts.reforged.sponge.entry.ReforgedEntry;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.ui.SpongeMainMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.pixelmonbridge.details.SpecKeys;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static net.impactdev.gts.sponge.utils.Utilities.PARSER;
import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class ReforgedEntryMenu extends AbstractSpongeEntryUI<ReforgedEntryMenu.Chosen> implements Historical<Displayable> {

    public ReforgedEntryMenu(Player viewer) {
        super(viewer);
    }

    @Override
    protected Text getTitle() {
        return Text.of("Listing Creator - Pokemon");
    }

    @Override
    protected InventoryDimension getDimensions() {
        return new InventoryDimension(9, 6);
    }

    @Override
    protected SpongeLayout getDesign() {
        final MessageService<Text> PARSER = Utilities.PARSER;

        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
        builder.dimension(9, 3).border()
                .dimension(9, 5).border()
                .dimension(9, 6);

        builder.slots(this.border(DyeColors.RED), 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);
        builder.slots(SpongeIcon.BORDER, 19, 20, 24, 25, 31, 46, 52);

        builder.slot(this.createNoneChosenIcon(), 13);

        AtomicInteger index = new AtomicInteger(28);
        Runnable increment = () -> {
            index.incrementAndGet();
            if(index.get() == 31) {
                index.getAndIncrement();
            }
        };
        for(Pokemon pokemon : Pixelmon.storageManager.getParty(this.viewer.getUniqueId()).getAll()) {
            if(pokemon == null) {
                increment.run();
                continue;
            }

            SpongeIcon icon = this.createIconForPokemon(ReforgedPokemon.from(pokemon), true);
            builder.slot(icon, index.get());
            increment.run();
        }

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(() -> this.viewer)))
                .build()
        );
        back.addListener(clickable -> {
            this.getParent().ifPresent(parent -> parent.get().open(this.viewer));
        });
        builder.slot(back, 45);

        builder.slot(this.createPriceIcon(), 47);
        builder.slot(this.createBINIcon(), 49);
        builder.slot(this.createTimeIcon(), 51);
        builder.slot(this.generateWaitingIcon(false), 53);

        return builder.build();
    }

    @Override
    protected EntrySelection<? extends SpongeEntry<?>> getSelection() {
        return this.chosen;
    }

    @Override
    protected int getPriceSlot() {
        return 47;
    }

    @Override
    protected int getSelectionTypeSlot() {
        return 49;
    }

    @Override
    protected int getTimeSlot() {
        return 51;
    }

    @Override
    public SpongeIcon createChosenIcon() {
        return this.createIconForPokemon(this.chosen.selection, false);
    }

    @Override
    public Optional<Supplier<Displayable>> getParent() {
        return Optional.of(() -> new SpongeMainMenu(this.viewer));
    }

    @Getter
    @RequiredArgsConstructor
    protected static class Chosen implements EntrySelection<ReforgedEntry> {
        private final ReforgedPokemon selection;

        @Override
        public ReforgedEntry createFromSelection() {
            return new ReforgedEntry(this.selection);
        }
    }

    private SpongeIcon createIconForPokemon(ReforgedPokemon pokemon, boolean click) {
        ItemStack item = ItemStack.builder()
                .fromItemStack(this.getPicture(pokemon.getOrCreate()))
                .add(Keys.DISPLAY_NAME, PARSER.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.POKEMON_TITLE), Lists.newArrayList(() -> pokemon)))
                .add(Keys.ITEM_LORE, PARSER.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.POKEMON_DETAILS), Lists.newArrayList(() -> pokemon)))
                .build();
        SpongeIcon icon = new SpongeIcon(item);
        if(click) {
            icon.addListener(clickable -> {
                Blacklist blacklist = Impactor.getInstance().getRegistry().get(Blacklist.class);
                if(blacklist.isBlacklisted(EnumSpecies.class, pokemon.get(SpecKeys.SPECIES).orElseThrow(() -> new RuntimeException("Pokemon data withut species")))) {
                    this.viewer.sendMessage(Text.of(TextColors.RED, "Blacklisted"));
                    this.viewer.playSound(SoundTypes.BLOCK_ANVIL_LAND, this.viewer.getPosition(), 1, 1);
                    return;
                }

                this.chosen = new Chosen(pokemon);
                this.getDisplay().setSlot(13, this.createChosenIcon());
                this.getDisplay().setSlot(53, this.generateConfirmIcon());
                this.style(true);
            });
        }
        return icon;
    }

    private ItemStack getPicture(Pokemon pokemon) {
        Calendar calendar = Calendar.getInstance();

        boolean aprilFools = false;
        if(calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            aprilFools = true;
        }

        if(pokemon.isEgg()) {
            net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
            NBTTagCompound nbt = new NBTTagCompound();
            switch (pokemon.getSpecies()) {
                case Manaphy:
                case Togepi:
                    nbt.setString(NbtKeys.SPRITE_NAME,
                            String.format("pixelmon:sprites/eggs/%s1", pokemon.getSpecies().name.toLowerCase()));
                    break;
                default:
                    nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
                    break;
            }
            item.setTagCompound(nbt);
            return (ItemStack) (Object) item;
        } else {
            return (ItemStack) (Object) (aprilFools ? ItemPixelmonSprite.getPhoto(Pixelmon.pokemonFactory.create(EnumSpecies.Bidoof)) : ItemPixelmonSprite.getPhoto(pokemon));
        }
    }

}
