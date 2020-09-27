package me.nickimpact.gts.pixelmon.reforged;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import me.nickimpact.gts.pixelmon.GTSPokemon;
import me.nickimpact.gts.pixelmon.data.EggData;
import me.nickimpact.gts.pixelmon.data.LevelData;
import me.nickimpact.gts.pixelmon.data.TrainerData;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;

public class ReforgedPokemon implements GTSPokemon<Pokemon> {

    private transient Pokemon delegate;

    @Override
    public Pokemon construct() {
        return null;
    }

    @Override
    public UUID getID() {
        return null;
    }

    @Override
    public LevelData getLevelData() {
        return null;
    }

    @Override
    public int getFormID() {
        return 0;
    }

    @Override
    public boolean isShiny() {
        return false;
    }

    @Override
    public String getAbility() {
        return null;
    }

    @Override
    public int getAbilitySlot() {
        return 0;
    }

    @Override
    public String getNature() {
        return null;
    }

    @Override
    public String getGender() {
        return null;
    }

    @Override
    public Optional<EggData> getEggData() {
        return Optional.empty();
    }

    @Override
    public boolean doesLevel() {
        return false;
    }

    @Override
    public TrainerData getTrainerData() {
        return null;
    }

    @Override
    public String getNickname() {
        return null;
    }

    @Override
    public GTSPokemon<Pokemon> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return null;
    }

    @Override
    public JsonElement serialize(GTSPokemon<Pokemon> pokemonGTSPokemon, Type type, JsonSerializationContext jsonSerializationContext) {
        return null;
    }
}
