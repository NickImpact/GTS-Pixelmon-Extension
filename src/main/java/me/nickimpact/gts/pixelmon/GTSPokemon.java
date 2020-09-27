package me.nickimpact.gts.pixelmon;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import me.nickimpact.gts.pixelmon.data.EggData;
import me.nickimpact.gts.pixelmon.data.LevelData;
import me.nickimpact.gts.pixelmon.data.TrainerData;

import java.util.Optional;
import java.util.UUID;

/**
 * This wrapper is meant to handle the storage of a pokemon via any means. With Reforged and Generations, serializing
 * a pokemon is nearly impossible due to the nature of the class structure (aka recursive or unserializable data structures).
 *
 * <p>With this class, we simply deserialize the pokemon ourselves, providing types that will permit the storage
 * of pokemon safely via desirable means. So if a user were to use GSON, you can successfully serialize a pokemon
 * converted into this type.</p>
 *
 * <p>Due to the differences between Pokemon mods, this interface will only implement the typical details
 * that are object independent.</p>
 */
public interface GTSPokemon<T> extends JsonSerializer<GTSPokemon<T>>, JsonDeserializer<GTSPokemon<T>> {

	T construct();

	UUID getID();

	LevelData getLevelData();

	int getFormID();

	boolean isShiny();

	String getAbility();

	int getAbilitySlot();

	String getNature();

	String getGender();

	Optional<EggData> getEggData();

	boolean doesLevel();

	TrainerData getTrainerData();

	String getNickname();

}
