package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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

    /**
     * Common ({@code c:}) item tags the mod adds to, named like NeoForge's own (see {@code Tags.Items}), so other mods'
     * recipes and machines can use the mod's items, and the mod's recipes can use theirs.
     */
    public static final class Items {
        /**
         * Raw coffee beans, as picked from a bush. Part of {@code c:crops}, like {@code c:crops/cocoa_bean}. The Coffee
         * Roaster roasts anything in it.
         */
        public static final TagKey<Item> COFFEE_BEAN_CROPS = common("crops/coffee_bean");
        /**
         * Every cup of coffee: Coffee, Ice Coffee, God's Coffee and God's Ice Coffee. Part of {@code c:drinks}.
         */
        public static final TagKey<Item> COFFEE_DRINKS = common("drinks/coffee");

        private Items() {
        }

        private static TagKey<Item> common(String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }
}
