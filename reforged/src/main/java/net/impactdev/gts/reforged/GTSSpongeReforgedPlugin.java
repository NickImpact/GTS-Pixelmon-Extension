package net.impactdev.gts.reforged;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.impactdev.gts.api.commands.GTSCommandExecutor;
import net.impactdev.gts.api.environment.Environment;
import net.impactdev.gts.api.events.extension.PluginReloadEvent;
import net.impactdev.gts.api.extension.Extension;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.reforged.config.ReforgedConfigKeys;
import net.impactdev.gts.reforged.config.ReforgedLangConfigKeys;
import net.impactdev.gts.reforged.entry.ReforgedEntry;
import net.impactdev.gts.reforged.entry.ReforgedListingSearcher;
import net.impactdev.gts.reforged.legacy.LegacyReforgedPokemonDeserializer;
import net.impactdev.gts.reforged.manager.ReforgedPokemonDataManager;
import net.impactdev.gts.reforged.placeholders.ReforgedPlaceholders;
import net.impactdev.gts.reforged.price.ReforgedPrice;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.dependencies.Dependency;
import net.impactdev.impactor.api.event.annotations.Subscribe;
import net.impactdev.impactor.api.event.listener.ImpactorEventListener;
import net.impactdev.impactor.api.logging.Log4jLogger;
import net.impactdev.impactor.api.logging.PluginLogger;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.events.extension.PlaceholderRegistryEvent;
import net.minecraft.nbt.CompoundNBT;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GTSSpongeReforgedPlugin implements Extension, ImpactorEventListener {

    private static GTSSpongeReforgedPlugin instance;

    private PluginLogger logger;
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
        this.logger = new Log4jLogger(LogManager.getLogger(this.metadata().name()));
        this.logger.debug("Initializing Reforged Extension...");

        this.configDir = dataDir;

        this.copyResource(Paths.get("reforged.conf"), dataDir.resolve("reforged"));
        this.extended = Config.builder()
                .path(dataDir.resolve("reforged").resolve("reforged.conf"))
                .provider(ReforgedConfigKeys.class)
                .build();
        this.lang = Config.builder()
                .path(dataDir.resolve("reforged").resolve("lang").resolve(GTSPlugin.instance().configuration().main().get(ConfigKeys.LANGUAGE) + ".conf"))
                .provider(ReforgedLangConfigKeys.class)
                .supply(true)
                .build();

        service.getGTSComponentManager().registerLegacyEntryDeserializer("pokemon", new LegacyReforgedPokemonDeserializer());
        service.getGTSComponentManager().registerEntryManager(ReforgedEntry.class, this.manager = new ReforgedPokemonDataManager());
        service.getGTSComponentManager().registerPriceManager(ReforgedPrice.class, new ReforgedPrice.ReforgedPriceManager());
        service.addSearcher(new ReforgedListingSearcher());

        Impactor.getInstance().getEventBus().subscribe(this.metadata(), this);

//        Pattern pattern = Pattern.compile("t[mr](8_)?(\\d{1,3})");
//        GTSService.getInstance().getDataTranslatorManager().register(CompoundNBT.class, in -> {
//            String id = in.getString("ItemType");
//            if(pattern.matcher(id).find()) {
//                ItemStack sponge = (ItemStack) (Object) RemapHandler.findNewTMFor(id, in.getInteger("Count"));
//                return Optional.ofNullable(sponge).map(item -> NBTTranslator.getInstance().translate(item.toContainer()));
//            }
//
//            return Optional.empty();
//        });
    }

    @Override
    public void enable(GTSService service) {
        this.logger.debug("Enabling...");
        GTSService.getInstance().getDataTranslatorManager().register(CompoundNBT.class, nbt -> {
            final String id = nbt.getString("ItemType");
            switch (id) {
                case "pixelmon:up-grade":
                    nbt.putString("ItemType", PixelmonItems.up_grade.delegate.name().toString());
                    return Optional.of(nbt);
                case "pixelmon:jade_shard":
                    nbt.putString("ItemType", PixelmonItems.delta_shard.delegate.name().toString());
                    return Optional.of(nbt);
            }

            return Optional.empty();
        });
    }

    @Override
    public List<Dependency> getRequiredDependencies() {
        return Lists.newArrayList();
    }

    @Override
    public Set<GTSCommandExecutor<?, ?, ?>> getExecutors() {
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

    public Config getMsgConfig() {
        return this.lang;
    }

    @Subscribe
    public void onPlaceholderRegistrationEvent(PlaceholderRegistryEvent event) {
        ReforgedPlaceholders placeholders = new ReforgedPlaceholders();
        placeholders.register(event.getPopulator());
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

    @Override
    public PluginMetadata metadata() {
        return this.metadata;
    }

    @Override
    public PluginLogger logger() {
        return this.logger;
    }

    @Override
    public void construct() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
