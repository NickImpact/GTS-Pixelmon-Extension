package net.impactdev.gts.reforged.config.mappings;

import com.google.common.collect.Maps;
import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;

import java.util.Map;
import java.util.Optional;

public class ReforgedPriceControls {

    private final Map<RegistryValue<Species>, Control> controls = Maps.newHashMap();

    public Optional<Control> get(RegistryValue<Species> species) {
        return Optional.ofNullable(this.controls.get(species));
    }

    public void createFor(RegistryValue<Species> species, double min, double max) {
        this.controls.put(species, new Control(min, max));
    }

    public static class Control {

        private final double min;
        private final double max;

        public Control(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public double getMin() {
            return this.min;
        }

        public double getMax() {
            return this.max;
        }

    }

}
