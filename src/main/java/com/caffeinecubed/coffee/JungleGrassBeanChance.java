package com.caffeinecubed.coffee;

import com.caffeinecubed.CaffeineCubedConfig;
import com.caffeinecubed.registry.ModLootConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * Loot condition {@code caffeinecubed:jungle_grass_bean_chance}: passes as often as the config's
 * {@code jungleGrassBeanChance} says (1 in 32 unless changed), for the raw beans jungle grass drops.
 */
public final class JungleGrassBeanChance implements LootItemCondition {
    public static final JungleGrassBeanChance INSTANCE = new JungleGrassBeanChance();
    public static final MapCodec<JungleGrassBeanChance> CODEC = MapCodec.unit(INSTANCE);

    private JungleGrassBeanChance() {
    }

    @Override
    public LootItemConditionType getType() {
        return ModLootConditions.JUNGLE_GRASS_BEAN_CHANCE.get();
    }

    @Override
    public boolean test(LootContext context) {
        return context.getRandom().nextFloat() < CaffeineCubedConfig.get(CaffeineCubedConfig.JUNGLE_GRASS_BEAN_CHANCE);
    }
}
