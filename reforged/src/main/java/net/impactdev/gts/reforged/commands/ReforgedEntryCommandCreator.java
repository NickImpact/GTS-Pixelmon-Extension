package net.impactdev.gts.reforged.commands;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.gts.common.plugin.permissions.GTSPermissions;
import net.impactdev.gts.reforged.entry.ChosenReforgedEntry;
import net.impactdev.gts.reforged.ui.ReforgedEntryMenu;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

@Alias("pokemon")
@Permission(GTSPermissions.DEFAULT)
public class ReforgedEntryCommandCreator implements CommandGenerator.EntryGenerator<ChosenReforgedEntry> {

    @Override
    public String[] getAliases() {
        return this.getClass().getAnnotation(Alias.class).value();
    }

    @Override
    public ChosenReforgedEntry create(UUID source, Queue<String> args, Context context) throws Exception {
        if(args.isEmpty()) {
            context.redirected();
            ServerPlayer player = Sponge.server().player(source).orElseThrow(() -> new IllegalStateException("Missing reference to your player, this is weird"));
            PlatformPlayer platform = PlatformPlayer.from(player);

            new ReforgedEntryMenu(platform).open(platform);
            return null;
        }

        int slot = this.require(args, Integer::parseInt);
        if(slot > 6 || slot < 1) {
            throw new IllegalArgumentException("Invalid index position, must be 1-6");
        }

        PlayerPartyStorage storage = StorageProxy.getParty(source);
        Optional<Pokemon> target = Optional.ofNullable(storage.get(slot - 1));
        if(!target.isPresent()) {
            throw new IllegalArgumentException("Target slot is empty!");
        }

        if(storage.getTeam().size() == 1 && !target.filter(Pokemon::isEgg).isPresent()) {
            throw new IllegalArgumentException("Cannot sell your last pokemon in your party!");
        }

        return new ChosenReforgedEntry(ReforgedPokemon.from(target.get()));
    }
}
