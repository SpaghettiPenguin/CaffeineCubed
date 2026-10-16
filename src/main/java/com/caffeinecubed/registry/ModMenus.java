package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.menu.CoffeeMachineMenu;
import com.caffeinecubed.menu.CoffeeRoasterMenu;
import com.caffeinecubed.menu.TripleAxisSiphonMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CaffeineCubed.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<CoffeeRoasterMenu>> COFFEE_ROASTER =
            MENUS.register("coffee_roaster", () -> new MenuType<>(CoffeeRoasterMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<CoffeeMachineMenu>> COFFEE_MACHINE =
            MENUS.register("coffee_machine", () -> new MenuType<>(CoffeeMachineMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<TripleAxisSiphonMenu>> TRIPLE_AXIS_SIPHON =
            MENUS.register("triple_axis_siphon", () -> new MenuType<>(TripleAxisSiphonMenu::new, FeatureFlags.VANILLA_SET));

    private ModMenus() {
    }
}
