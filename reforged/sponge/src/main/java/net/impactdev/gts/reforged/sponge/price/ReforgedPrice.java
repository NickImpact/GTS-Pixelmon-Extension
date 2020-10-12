package net.impactdev.gts.reforged.sponge.price;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumNoForm;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import com.pixelmonmod.pixelmon.items.ItemPixelmonSprite;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.reforged.sponge.GTSSpongeReforgedPlugin;
import net.impactdev.gts.reforged.sponge.config.ReforgedLangConfigKeys;
import net.impactdev.gts.sponge.listings.makeup.SpongeDisplay;
import net.impactdev.gts.sponge.pricing.SpongePrice;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.pixelmonbridge.details.SpecKeys;
import net.impactdev.pixelmonbridge.details.components.Level;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;
import net.kyori.text.TextComponent;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class ReforgedPrice implements SpongePrice<ReforgedPrice.PokemonPriceSpecs, StoragePosition> {

    private PokemonPriceSpecs price;

    /** The pokemon the payer pays to buy the represented Listing */
    private ReforgedPokemon out;

    public ReforgedPrice(PokemonPriceSpecs price) {
        this.price = price;
    }

    @Override
    public PokemonPriceSpecs getPrice() {
        return this.price;
    }

    @Override
    public TextComponent getText() {
        TextComponent.Builder formComponent = TextComponent.builder();

        IEnumForm form = this.price.getSpecies().getFormEnum(this.price.getForm());
        if(!form.equals(EnumNoForm.NoForm)) {
            formComponent.append(form.getLocalizedName()).append(TextComponent.space());
        }

        return TextComponent.builder()
                .append("Level " + this.price.getLevel())
                .append(TextComponent.space())
                .append(formComponent.build())
                .append(this.price.getSpecies().getPokemonName())
                .build();
    }

    @Override
    public Display<ItemStack> getDisplay() {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        final ReforgedPokemon pokemon = new ReforgedPokemon();
        pokemon.offer(SpecKeys.SPECIES, this.price.getSpecies().getPokemonName());
        pokemon.offer(SpecKeys.LEVEL, new Level(this.price.getLevel(), 0, false));
        pokemon.offer(SpecKeys.FORM, this.price.getForm());

        List<Text> lore = Lists.newArrayList();
        // TODO - Add lore

        ItemStack rep = ItemStack.builder()
                .from(this.getPicture(this.price.getSpecies()))
                .add(Keys.DISPLAY_NAME, service.parse(GTSSpongeReforgedPlugin.getInstance().getMsgConfig()
                        .get(ReforgedLangConfigKeys.POKEMON_TITLE), Lists.newArrayList(() -> pokemon))
                )
                .add(Keys.ITEM_LORE, lore)
                .build();

        return new SpongeDisplay(rep);
    }

    @Override
    public boolean canPay(UUID payer) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(payer);
        for(Pokemon pokemon : storage.getTeam()) {
            if(this.price.matches(pokemon)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void pay(UUID payer, StoragePosition source) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(payer);
        Pokemon pokemon = storage.get(source);
        this.out = ReforgedPokemon.from(pokemon);
        storage.set(pokemon.getPosition(), null);
    }

    @Override
    public boolean reward(UUID recipient) {
        // TODO - Consider how we handle this with cross server
        // Perhaps we write it as a secondary field to the JSON of this price, and that will be carried
        // across when populated, indicating a user has paid for the listing
        return false;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        return new JObject()
                .add("price", new JObject()
                        .add("species", this.price.getSpecies().getPokemonName())
                        .add("form", this.price.getForm())
                        .add("level", this.price.getLevel())
                )
                .add("version", this.getVersion());
    }

    public static class PokemonPriceSpecs {

        private EnumSpecies species;
        private int form;
        private int level;
        private boolean allowEggs;

        public PokemonPriceSpecs(EnumSpecies species, int form, int level, boolean allowEggs) {
            this.species = species;
            this.form = form;
            this.level = level;
            this.allowEggs = allowEggs;
        }

        public EnumSpecies getSpecies() {
            return this.species;
        }

        public int getForm() {
            return this.form;
        }

        public int getLevel() {
            return this.level;
        }

        public boolean doesAllowEggs() {
            return this.allowEggs;
        }

        public boolean matches(Pokemon pokemon) {
            if(!this.allowEggs) {
                if(pokemon.isEgg()) {
                    return false;
                }
            }

            return pokemon.getSpecies().equals(this.species) &&
                    pokemon.getForm() == this.form &&
                    pokemon.getLevel() == this.level;
        }

    }

    private ItemStack getPicture(EnumSpecies species) {
        Calendar calendar = Calendar.getInstance();

        boolean aprilFools = false;
        if(calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            aprilFools = true;
        }

        return (ItemStack) (Object) (ItemPixelmonSprite.getPhoto(Pixelmon.pokemonFactory.create(aprilFools ? EnumSpecies.Bidoof : species)));
    }
}
