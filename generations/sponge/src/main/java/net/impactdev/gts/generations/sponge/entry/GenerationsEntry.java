package net.impactdev.gts.generations.sponge.entry;

import com.google.common.collect.Lists;
import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.common.item.ItemPixelmonSprite;
import com.pixelmongenerations.core.Pixelmon;
import com.pixelmongenerations.core.config.PixelmonEntityList;
import com.pixelmongenerations.core.config.PixelmonItems;
import com.pixelmongenerations.core.enums.EnumSpecies;
import com.pixelmongenerations.core.storage.NbtKeys;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.prices.PriceControlled;
import net.impactdev.gts.generations.sponge.GTSSpongeGenerationsPlugin;
import net.impactdev.gts.generations.sponge.config.GenerationsLangConfigKeys;
import net.impactdev.gts.generations.sponge.converter.JObjectConverter;
import net.impactdev.gts.sponge.listings.makeup.SpongeDisplay;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.pixelmonbridge.generations.GenerationsPokemon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GenerationsEntry extends SpongeEntry<GenerationsPokemon> implements PriceControlled {

    private GenerationsPokemon pokemon;

    public GenerationsEntry(GenerationsPokemon pokemon) {
        this.pokemon = pokemon;
    }

    @Override
    public GenerationsPokemon getOrCreateElement() {
        return this.pokemon;
    }

    @Override
    public TextComponent getName() {
        return Component.text(this.pokemon.getOrCreate().getSpecies().getPokemonName());
    }

    @Override
    public TextComponent getDescription() {
        return this.getName();
    }

    @Override
    public Display<ItemStack> getDisplay(UUID viewer, Listing listing) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        List<Text> lore = Lists.newArrayList();
        lore.addAll(service.parse(GTSSpongeGenerationsPlugin.getInstance().getMsgConfig().get(GenerationsLangConfigKeys.POKEMON_DETAILS), Lists.newArrayList(() -> this.pokemon)));

        ItemStack rep = ItemStack.builder()
                .from(this.getPicture(this.pokemon.getOrCreate()))
                .add(Keys.DISPLAY_NAME, service.parse(GTSSpongeGenerationsPlugin.getInstance().getMsgConfig()
                        .get(GenerationsLangConfigKeys.POKEMON_TITLE), Lists.newArrayList(() -> this.pokemon))
                )
                .add(Keys.ITEM_LORE, lore)
                .build();

        return new SpongeDisplay(rep);
    }

    @Override
    public boolean give(UUID receiver) {
        return false;
    }

    @Override
    public boolean take(UUID depositor) {
        return false;
    }

    @Override
    public Optional<String> getThumbnailURL() {
        return Optional.empty();
    }

    @Override
    public List<String> getDetails() {
        return null;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        // Since the custom serializer for the Cross Pixelmon Library uses it's own version of
        // JObject, as to not rely on Impactor, we need to convert between the two objects
        return new JObject()
                .add("pokemon", JObjectConverter.convert(GTSSpongeGenerationsPlugin.getInstance()
                        .getManager()
                        .getInternalManager()
                        .serialize(this.pokemon)
                ))
                .add("version", this.getVersion());
    }

    @Override
    public double getMin() {
        return 0;
    }

    @Override
    public double getMax() {
        return 0;
    }

    private ItemStack getPicture(EntityPixelmon pokemon) {
        Calendar calendar = Calendar.getInstance();

        boolean aprilFools = false;
        if(calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            aprilFools = true;
        }

        if(pokemon.isEgg) {
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
            World world = (World) (Object) Sponge.getServer().getWorlds().iterator().next();
            return (ItemStack) (Object) (aprilFools ? ItemPixelmonSprite.getPhoto((EntityPixelmon) PixelmonEntityList.createEntityByName(EnumSpecies.Bidoof.name, world)) : ItemPixelmonSprite.getPhoto(pokemon));
        }
    }

}
