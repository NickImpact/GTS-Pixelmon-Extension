package net.impactdev.gts.reforged.ui;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.NbtKeys;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.items.SpriteItem;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.reforged.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.config.ReforgedLangConfigKeys;
import net.impactdev.gts.common.ui.Historical;
import net.impactdev.gts.reforged.entry.ChosenReforgedEntry;
import net.impactdev.gts.reforged.entry.ReforgedEntry;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.impactdev.pixelmonbridge.details.SpecKeys;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.math.vector.Vector2i;

import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static net.impactdev.gts.sponge.utils.Utilities.PARSER;
import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class ReforgedEntryMenu extends AbstractSpongeEntryUI<ChosenReforgedEntry> implements Historical<SpongeMainPageProvider> {

    public ReforgedEntryMenu(PlatformPlayer viewer) {
        super(viewer);
    }

    @Override
    protected ImpactorUI.UIBuilder modifyDisplayBuilder(ImpactorUI.UIBuilder builder) {
        return builder;
    }

    @Override
    protected Component getTitle() {
        return Component.text("Listing Creator - Pokemon");
    }

    @Override
    protected Vector2i getDimensions() {
        return new Vector2i(9, 6);
    }

    @Override
    protected Layout getDesign() {
        final MessageService PARSER = Utilities.PARSER;

        Layout.LayoutBuilder builder = Layout.builder();
        builder.size(3).border(ProvidedIcons.BORDER)
                .size(5).border(ProvidedIcons.BORDER)
                .size( 6);

        builder.slots(this.border(ItemTypes.RED_STAINED_GLASS_PANE.get()), 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);
        builder.slots(ProvidedIcons.BORDER, 19, 20, 24, 25, 31, 46, 52);

        builder.slot(this.createNoneChosenIcon(), 13);

        PlayerPartyStorage party = StorageProxy.getParty(this.viewer.uuid());
        party.retrieveAll();

        AtomicInteger index = new AtomicInteger(28);
        Runnable increment = () -> {
            index.incrementAndGet();
            if(index.get() == 31) {
                index.getAndIncrement();
            }
        };
        for(Pokemon pokemon : party.getAll()) {
            if(pokemon == null) {
                increment.run();
                continue;
            }

            Icon<ItemStack> icon = this.createIconForPokemon(ReforgedPokemon.from(pokemon), true);
            builder.slot(icon, index.get());
            increment.run();
        }

        Icon<ItemStack> back = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .itemType(ItemTypes.BARRIER)
                    .add(Keys.CUSTOM_NAME, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK)))
                    .build())
                )
                .listener(context -> {
                    this.getParent().ifPresent(parent -> parent.get().open());
                    return false;
                })
                .build();
        builder.slot(back, 45);

        builder.slot(this.createPriceIcon(), 47);
        builder.slot(GTSPlugin.instance().configuration().main().get(ConfigKeys.BINS_ENABLED) ? this.createBINIcon() : this.createAuctionIcon(), 49);
        builder.slot(this.createTimeIcon(), 51);
        builder.slot(this.generateWaitingIcon(false), 53);

        return builder.build();
    }

    @Override
    protected EntrySelection<? extends SpongeEntry<?>> getSelection() {
        return this.chosen;
    }

    @Override
    protected int getChosenSlot() {
        return 13;
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
    protected int getConfirmSlot() {
        return 53;
    }

    @Override
    protected double getMinimumMonetaryPrice(ChosenReforgedEntry chosen) {
        return new ReforgedEntry(chosen.getSelection()).getMin();
    }

    @Override
    public Icon<ItemStack> createChosenIcon() {
        return this.createIconForPokemon(this.chosen.getSelection(), false);
    }

    @Override
    public Optional<Supplier<SpongeMainPageProvider>> getParent() {
        return Optional.of(() -> SpongeMainPageProvider.creator().viewer(this.viewer).build());
    }

    private Icon<ItemStack> createIconForPokemon(ReforgedPokemon pokemon, boolean click) {
        Config mainLang = GTSPlugin.instance().configuration().language();
        MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);
        PlaceholderSources sources = PlaceholderSources.builder()
                .append(ReforgedPokemon.class, () -> pokemon)
                .build();

        ItemStack item = ItemStack.builder()
                .fromItemStack(this.getPicture(pokemon.getOrCreate()))
                .add(Keys.CUSTOM_NAME, PARSER.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.POKEMON_TITLE), sources))
                .add(Keys.LORE, PARSER.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig().get(ReforgedLangConfigKeys.POKEMON_DETAILS), sources))
                .build();
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(item))
                .listener(context -> {
                    if(click) {
                        Blacklist blacklist = Impactor.getInstance().getRegistry().get(Blacklist.class);
                        if(blacklist.isBlacklisted(Species.class, pokemon.get(SpecKeys.SPECIES).orElseThrow(() -> new RuntimeException("Pokemon data without species")))) {
                            this.viewer.sendMessage(parser.parse(mainLang.get(MsgConfigKeys.GENERAL_FEEDBACK_BLACKLISTED)));
                            this.viewer.playSound(Sound.sound(SoundTypes.BLOCK_ANVIL_LAND.get(), Sound.Source.MASTER, 1, 1));

                            return false;
                        }

                        this.setChosen(new ChosenReforgedEntry(pokemon));
                        this.getDisplay().set(this.createChosenIcon(), 13);
                        this.getDisplay().set(this.createPriceIcon(), this.getPriceSlot());
                        this.getDisplay().set(this.generateConfirmIcon(), 53);
                        this.style(true);
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
