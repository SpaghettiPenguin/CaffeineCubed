package com.caffeinecubed.coffee;

import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModLootFunctions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

/**
 * Loot function {@code caffeinecubed:set_bean_quality}: gives the dropped beans the {@link BeanQuality} of the biome
 * they dropped in.
 */
public class SetBeanQualityFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetBeanQualityFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance).apply(instance, SetBeanQualityFunction::new));

    public SetBeanQualityFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType<SetBeanQualityFunction> getType() {
        return ModLootFunctions.SET_BEAN_QUALITY.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        if (origin != null) {
            stack.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.at(context.getLevel(), BlockPos.containing(origin)));
        }
        return stack;
    }
}
