package com.caffeinecubed;

import com.caffeinecubed.registry.ModBlockEntities;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModCapabilities;
import com.caffeinecubed.registry.ModCreativeTabs;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import com.caffeinecubed.registry.ModLootFunctions;
import com.caffeinecubed.registry.ModMenus;
import com.caffeinecubed.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CaffeineCubed.MODID)
public class CaffeineCubed {
    public static final String MODID = "caffeinecubed";

    public CaffeineCubed(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModLootFunctions.LOOT_FUNCTIONS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        modEventBus.addListener(ModCapabilities::register);

        // The beans and the plant were first called Coffee Beans and Coffee Plant. These aliases let worlds saved
        // back then keep them under the new names.
        ModItems.ITEMS.addAlias(id("coffee_beans"), id("raw_coffee_beans"));
        ModBlocks.BLOCKS.addAlias(id("coffee_plant"), id("coffee_bush"));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
