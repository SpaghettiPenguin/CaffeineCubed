package com.caffeinecubed;

import com.caffeinecubed.client.CoffeeGrinderHandAnimation;
import com.caffeinecubed.client.CoffeeMachineScreen;
import com.caffeinecubed.client.CoffeeRoasterScreen;
import com.caffeinecubed.client.TripleAxisSiphonScreen;
import com.caffeinecubed.registry.ModItems;
import com.caffeinecubed.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CaffeineCubed.MODID, dist = Dist.CLIENT)
public class CaffeineCubedClient {
    public CaffeineCubedClient(IEventBus modEventBus, ModContainer container) {
        // The Config button in the mods list opens NeoForge's settings screen for the mod's config
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(CaffeineCubedClient::registerScreens);
        modEventBus.addListener(CaffeineCubedClient::registerClientExtensions);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.COFFEE_ROASTER.get(), CoffeeRoasterScreen::new);
        event.register(ModMenus.COFFEE_MACHINE.get(), CoffeeMachineScreen::new);
        event.register(ModMenus.TRIPLE_AXIS_SIPHON.get(), TripleAxisSiphonScreen::new);
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new CoffeeGrinderHandAnimation(), ModItems.COFFEE_GRINDER.get());
    }
}
