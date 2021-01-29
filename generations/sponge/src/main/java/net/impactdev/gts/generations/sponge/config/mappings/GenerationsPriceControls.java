package net.impactdev.gts.generations.sponge.config.mappings;

import com.google.common.collect.Maps;
import com.pixelmongenerations.core.enums.EnumSpecies;

import java.util.Map;
import java.util.Optional;

public class GenerationsPriceControls {

    private final Map<EnumSpecies, Control> controls = Maps.newHashMap();

    public Optional<Control> get(EnumSpecies species) {
        return Optional.ofNullable(this.controls.get(species));
    }

    public void createFor(EnumSpecies species, double min, double max) {
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
