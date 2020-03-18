package me.nickimpact.gts.pixelmon;

import lombok.Builder;
import lombok.Getter;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * This wrapper is meant to handle the storage of a pokemon via any means. With Reforged and Generations, serializing
 * a pokemon is nearly impossible due to the nature of the class structure (aka recursive or unserializable data structures).
 *
 * <p>With this class, we simply deserialize the pokemon ourselves, providing types that will permit the storage
 * of pokemon safely via desirable means. So if a user were to use GSON, you can successfully serialize a pokemon
 * converted into this type.</p>
 *
 * @param <Pokemon> The actual implementation of a pokemon. This is what either reforged or generations uses to handle the
 *                 data storage of the pokemon
 * @param <Species> The species of the pokemon
 * @param <Nature> The nature of the pokemon
 * @param <Growth> The growth of the pokemon
 * @param <Gender> The gender of the pokemon
 * @param <Pokeball> The pokeball the pokemon was captured in / inherited
 * @param <SpecialTexture> The special texture set on this pokemon
 */
public interface GTSPokemon<Pokemon, Species, Nature, Growth, Gender, Pokeball, SpecialTexture> {

	Pokemon construct();

	Species getSpecies();
	int getLevel();
	int getForm();
	boolean isShiny();

	String getNickname();

	String getAbility();
	int getAbilitySlot();

	Pokeball getPokeball();
	Nature getNature();
	Growth getGrowth();
	Gender getGender();

	StatWrapper getStats();
	StatWrapper getEVs();
	StatWrapper getIVs();

	short getStatus();

	SpecialTexture getSpecialTexture();
	String getCustomTexture();

	int getHealth();
	List<Integer> getRelearnableMoves();

	boolean doesLevel();
	EggData getEggData();

	ItemStack getHeldItem();

	UUID getOriginalTrainerID();
	String getOriginalTrainerName();

	AttackWrapper[] getMoveset();

	@Getter
	@Builder
	class StatWrapper {

		private int hp;
		private int attack;
		private int defence;
		private int spatk;
		private int spdef;
		private int speed;

	}

	@Getter
	@Builder
	class EggData {

		private Integer eggCycles;
		private Integer eggSteps;

	}

	@Getter
	@Builder
	class AttackWrapper {

		private int moveID;
		private int pp;
		private int ppLevel;

	}


}
