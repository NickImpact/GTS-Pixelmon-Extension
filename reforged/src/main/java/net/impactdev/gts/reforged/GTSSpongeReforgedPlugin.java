package net.impactdev.gts.reforged;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.config.RemapHandler;
import net.impactdev.gts.api.commands.GTSCommandExecutor;
import net.impactdev.gts.api.environment.Environment;
import net.impactdev.gts.api.events.extension.PluginReloadEvent;
import net.impactdev.gts.reforged.entry.ReforgedEntry;
import net.impactdev.gts.reforged.entry.ReforgedListingSearcher;
import net.impactdev.gts.reforged.price.ReforgedPrice;
import net.impactdev.gts.sponge.data.NBTTranslator;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.dependencies.Dependency;
import net.impactdev.impactor.api.event.annotations.Subscribe;
import net.impactdev.impactor.api.event.listener.ImpactorEventListener;
import net.impactdev.impactor.api.logging.Logger;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import net.impactdev.impactor.sponge.configuration.SpongeConfig;
import net.impactdev.impactor.sponge.configuration.SpongeConfigAdapter;
import net.impactdev.impactor.sponge.logging.SpongeLogger;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.events.extension.PlaceholderRegistryEvent;
import net.impactdev.gts.api.extension.Extension;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.reforged.config.ReforgedConfigKeys;
import net.impactdev.gts.reforged.config.ReforgedLangConfigKeys;
import net.impactdev.gts.reforged.legacy.LegacyReforgedPokemonDeserializer;
import net.impactdev.gts.reforged.manager.ReforgedPokemonDataManager;
import net.impactdev.gts.reforged.placeholders.ReforgedPlaceholders;
import net.minecraft.nbt.NBTTagCompound;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * For entry type displays with discord:
 * https://projectpokemon.org/images/normal-sprite/bulbasaur.gif
 * https://projectpokemon.org/images/shiny-sprite/bulbasaur.gif
 */
public class GTSSpongeReforgedPlugin implements Extension, ImpactorEventListener {

    private static GTSSpongeReforgedPlugin instance;

    private Logger logger;
    private final PluginMetadata metadata = PluginMetadata.builder()
            .id("reforged_extension")
            .name("GTS - Reforged Extension")
            .version("@version@")
            .build();

    private ReforgedPokemonDataManager manager;

    private Path configDir;
    private Config extended;
    private Config lang;

    public GTSSpongeReforgedPlugin() {
        instance = this;
    }

    @Override
    public void load(GTSService service, Path dataDir) {
        this.logger = new SpongeLogger(this, LoggerFactory.getLogger(this.getMetadata().getName()));
        this.logger.debug("Initializing Reforged Extension...");

        this.configDir = dataDir;

        this.copyResource(Paths.get("reforged.conf"), dataDir.resolve("reforged"));
        this.extended = new SpongeConfig(new SpongeConfigAdapter(this, dataDir.resolve("reforged").resolve("reforged.conf").toFile()), new ReforgedConfigKeys());

        this.lang = new SpongeConfig(new SpongeConfigAdapter(this, dataDir.resolve("reforged").resolve("lang").resolve(GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.LANGUAGE) + ".conf").toFile(), true), new ReforgedLangConfigKeys());

        service.getGTSComponentManager().registerLegacyEntryDeserializer("pokemon", new LegacyReforgedPokemonDeserializer());
        service.getGTSComponentManager().registerEntryManager(ReforgedEntry.class, this.manager = new ReforgedPokemonDataManager());
        service.getGTSComponentManager().registerPriceManager(ReforgedPrice.class, new ReforgedPrice.ReforgedPriceManager());
        service.addSearcher(new ReforgedListingSearcher());

        Impactor.getInstance().getEventBus().subscribe(this);

        Pattern pattern = Pattern.compile("t[mr](8_)?([0-9]{1,3})");
        GTSService.getInstance().getDataTranslatorManager().register(NBTTagCompound.class, in -> {
            String id = in.getString("ItemType");
            if(pattern.matcher(id).find()) {
                ItemStack sponge = (ItemStack) (Object) RemapHandler.findNewTMFor(id, in.getInteger("Count"));
                return Optional.ofNullable(sponge).map(item -> NBTTranslator.getInstance().translate(item.toContainer()));
            }

            return Optional.empty();
        });
    }

    @Override
    public void enable(GTSService service) {
        this.logger.debug("Enabling...");
    }

    @Override
    public List<Dependency> getRequiredDependencies() {
        return Lists.newArrayList();
    }

    @Override
    public Set<GTSCommandExecutor<?, ?>> getExecutors() {
        return Collections.EMPTY_SET;
    }

    @Override
    public void getExtendedEnvironmentInformation(Environment environment) {
        environment.append("Pixelmon Version", Pixelmon.getVersion());
    }

    @Override
    public void unload() {

    }

    public static GTSSpongeReforgedPlugin getInstance() {
        return instance;
    }

    public ReforgedPokemonDataManager getManager() {
        return this.manager;
    }

    @Override
    public Path getConfigDir() {
        return this.configDir;
    }

    @Override
    public Config getConfiguration() {
        return this.extended;
    }

    @Override
    public PluginMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    public Logger getPluginLogger() {
        return this.logger;
    }

    @Override
    public Config getMsgConfig() {
        return this.lang;
    }

    @Subscribe
    public void onPlaceholderRegistrationEvent(PlaceholderRegistryEvent<GameRegistryEvent.Register<PlaceholderParser>> event) {
        ReforgedPlaceholders placeholders = new ReforgedPlaceholders();
        placeholders.register(event.getManager());
    }

    @Subscribe
    public void onReloadEvent(PluginReloadEvent event) {
        this.extended.reload();
        this.lang.reload();
    }

    private void copyResource(Path path, Path destination) {
        if(!Files.exists(destination.resolve(path))) {
            try (InputStream resource = this.getClass().getClassLoader().getResourceAsStream(path.toString())) {
                Files.createDirectories(destination.resolve(path).getParent());
                Files.copy(resource, destination.resolve(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
