package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.coffee.JungleGrassBeanChance;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModLootConditions {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, CaffeineCubed.MODID);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> JUNGLE_GRASS_BEAN_CHANCE =
            LOOT_CONDITIONS.register("jungle_grass_bean_chance", () -> new LootItemConditionType(JungleGrassBeanChance.CODEC));

    private ModLootConditions() {
    }
}
