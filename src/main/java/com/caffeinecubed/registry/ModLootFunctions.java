package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.coffee.SetBeanQualityFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModLootFunctions {
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, CaffeineCubed.MODID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetBeanQualityFunction>> SET_BEAN_QUALITY =
            LOOT_FUNCTIONS.register("set_bean_quality", () -> new LootItemFunctionType<>(SetBeanQualityFunction.CODEC));

    private ModLootFunctions() {
    }
}
