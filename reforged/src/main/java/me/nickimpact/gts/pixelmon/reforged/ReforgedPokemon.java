package me.nickimpact.gts.pixelmon.reforged;

import com.nickimpact.impactor.api.json.Adapter;
import com.nickimpact.impactor.api.json.Registry;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.BonusStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.ExtraStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Pokerus;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import lombok.Getter;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.pixelmon.GTSPokemon;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.UUID;

@Getter
public class ReforgedPokemon implements GTSPokemon<Pokemon, EnumSpecies, EnumNature, EnumGrowth, Gender, EnumPokeballs, EnumSpecialTexture> {

	public static final ExtraStatAdapter ADAPTER = new ExtraStatAdapter(GTSService.getInstance().getRegistry().get(GTSPlugin.class));

	private UUID id;
	private EnumSpecies species;
	private int level;
	private int form;
	private boolean shiny;
	private String nickname;
	private String ability;
	private int abilitySlot;
	private EnumPokeballs pokeball;
	private EnumNature nature;
	private EnumGrowth growth;
	private Gender gender;
	private StatWrapper stats;
	private StatWrapper EVs;
	private StatWrapper IVs;
	private short status;
	private EnumSpecialTexture specialTexture;
	private String customTexture;
	private int health;
	private List<Integer> relearnableMoves;
	private boolean doesLevel;
	private EggData eggData;
	private ItemStack heldItem;
	private UUID originalTrainerID;
	private String originalTrainerName;
	private AttackWrapper[] moveset;

	/** Reforged Specific Fields */
	private List<String> specFlags;
	private Pokerus pokerus;
	private BonusStats bonus;
	private ExtraStats extras;

	private ReforgedPokemon(Pokemon pokemon) {

	}

	public static ReforgedPokemon from(Pokemon pokemon) {
		return new ReforgedPokemon(pokemon);
	}

	@Override
	public Pokemon construct() {
		Pokemon pokemon = Pixelmon.pokemonFactory.create(this.id);
		pokemon.setSpecies(this.species);


		return pokemon;
	}

	@Override
	public boolean doesLevel() {
		return this.doesLevel;
	}

	private static class ExtraStatAdapter extends Adapter<ExtraStats> {

		private Registry<ExtraStats> registry;

		ExtraStatAdapter(ImpactorPlugin plugin) {
			super(plugin);
			this.registry = new Registry<>(plugin);
		}

		@Override
		protected Registry<ExtraStats> getRegistry() {
			return this.registry;
		}
	}

}
