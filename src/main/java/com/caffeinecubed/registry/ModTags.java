package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class ModTags {
    private ModTags() {
    }

    /**
     * Which biomes grow which quality of coffee. Biomes in none of these grow decent coffee.
     */
    public static final class Biomes {
        /**
         * Cold biomes: taigas, snowy and icy biomes, cold mountains and the End.
         */
        public static final TagKey<Biome> GROWS_POOR_COFFEE = tag("grows_poor_coffee");
        public static final TagKey<Biome> GROWS_GOOD_COFFEE = tag("grows_good_coffee");
        public static final TagKey<Biome> GROWS_GREAT_COFFEE = tag("grows_great_coffee");

        private Biomes() {
        }

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, name));
        }
    }
}
