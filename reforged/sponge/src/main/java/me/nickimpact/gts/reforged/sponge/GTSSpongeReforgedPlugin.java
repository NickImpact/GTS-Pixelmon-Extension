package me.nickimpact.gts.reforged.sponge;

import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.plugin.PluginMetadata;
import com.nickimpact.impactor.sponge.logging.SpongeLogger;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.extension.Extension;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 *
 * For entry type displays with discord:
 * https://projectpokemon.org/images/normal-sprite/bulbasaur.gif
 * https://projectpokemon.org/images/shiny-sprite/bulbasaur.gif
 */
public class GTSSpongeReforgedPlugin implements Extension {

    private Logger logger;
    private PluginMetadata metadata = PluginMetadata.builder()
            .id("reforged_extension")
            .name("GTS - Reforged Extension")
            .version("6.0.0")
            .build();

    @Override
    public void load(GTSService service) {
        this.logger = new SpongeLogger(this, LoggerFactory.getLogger(this.getMetadata().getName()));
        this.logger.debug("Testing");
    }

    @Override
    public void unload() {

    }

    @Override
    public Path getConfigDir() {
        return null;
    }

    @Override
    public Config getConfiguration() {
        return null;
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
        return null;
    }
}
