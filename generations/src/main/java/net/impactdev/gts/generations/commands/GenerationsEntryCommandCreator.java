package net.impactdev.gts.generations.commands;

import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.core.storage.PixelmonStorage;
import com.pixelmongenerations.core.storage.PlayerStorage;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.generations.entry.ChosenGenerationsEntry;
import net.impactdev.pixelmonbridge.generations.GenerationsPokemon;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public class GenerationsEntryCommandCreator implements CommandGenerator.EntryGenerator<ChosenGenerationsEntry> {

    @Override
    public String[] getAliases() {
        return new String[] {
                "pokemon",
                "poke"
        };
    }

    @Override
    public ChosenGenerationsEntry create(UUID source, Queue<String> args, Context context) throws Exception {
        if(args.isEmpty()) {
            throw new IllegalStateException("No information provided for pokemon selection");
        }

        int slot = this.require(args, Integer::parseInt);
        if(slot > 6 || slot < 1) {
            throw new IllegalArgumentException("Invalid index position, must be 1-6");
        }
        PlayerStorage storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(source)
                .orElseThrow(() -> new Exception("Unable to locate your party, this is problematic!"));
        Optional<NBTTagCompound> target = Optional.ofNullable(storage.partyPokemon[slot - 1]);
        if(!target.isPresent()) {
            throw new IllegalArgumentException("Target slot is empty!");
        }

        EntityPixelmon pokemon = new EntityPixelmon(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
        pokemon.readFromNBT(target.get());
        pokemon.update();

        return new ChosenGenerationsEntry(GenerationsPokemon.from(pokemon));
    }
}
