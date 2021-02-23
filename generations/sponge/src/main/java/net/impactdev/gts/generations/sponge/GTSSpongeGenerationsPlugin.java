package net.impactdev.gts.generations.sponge;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.events.extension.PlaceholderRegistryEvent;
import net.impactdev.gts.api.events.extension.PluginReloadEvent;
import net.impactdev.gts.api.extension.Extension;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.generations.sponge.config.GenerationsConfigKeys;
import net.impactdev.gts.generations.sponge.config.GenerationsLangConfigKeys;
import net.impactdev.gts.generations.sponge.config.mappings.GenerationsPriceControls;
import net.impactdev.gts.generations.sponge.entry.GenerationsEntry;
import net.impactdev.gts.generations.sponge.entry.GenerationsListingSearcher;
import net.impactdev.gts.generations.sponge.legacy.LegacyGenerationsPokemonDeserializer;
import net.impactdev.gts.generations.sponge.manager.GenerationsPokemonDataManager;
import net.impactdev.gts.generations.sponge.ui.GenerationsEntryMenu;
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
import org.slf4j.LoggerFactory;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GTSSpongeGenerationsPlugin implements Extension, ImpactorEventListener {

    private static GTSSpongeGenerationsPlugin instance;

    private Logger logger;
    private final PluginMetadata metadata = PluginMetadata.builder()
            .id("generations_extension")
            .name("GTS - Generations Extension")
            .version("@version@")
            .build();

    private GenerationsPokemonDataManager manager;

    private Path configDir;
    private Config extended;
    private Config lang;

    public GTSSpongeGenerationsPlugin() {
        instance = this;
    }

    @Override
    public void load(GTSService service, Path dataDir) {
        this.logger = new SpongeLogger(this, LoggerFactory.getLogger(this.getMetadata().getName()));
        this.logger.debug("Initializing Generations Extension...");

        this.configDir = dataDir;

        this.copyResource(Paths.get("generations.conf"), dataDir.resolve("generations"));
        this.extended = new SpongeConfig(new SpongeConfigAdapter(this, dataDir.resolve("generations").resolve("generations.conf").toFile()), new GenerationsConfigKeys());
        this.lang = new SpongeConfig(new SpongeConfigAdapter(this, dataDir.resolve("generations").resolve("lang").resolve(GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.LANGUAGE) + ".conf").toFile(), true), new GenerationsLangConfigKeys());

        service.getGTSComponentManager().registerEntryManager(GenerationsEntry.class, this.manager = new GenerationsPokemonDataManager());
        //service.getGTSComponentManager().registerPriceManager(ReforgedPrice.class, new ReforgedPrice.ReforgedPriceManager());
        service.addSearcher(new GenerationsListingSearcher());

        Impactor.getInstance().getEventBus().subscribe(this);
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
    public void unload() {

    }

    public static GTSSpongeGenerationsPlugin getInstance() {
        return instance;
    }

    public GenerationsPokemonDataManager getManager() {
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
//        GenerationsPlaceHolders placeholders = new GenerationsPlaceHolders();
//        placeholders.register(event.getManager());
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
