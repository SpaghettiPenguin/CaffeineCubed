package com.caffeinecubed.compat.jei;

import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModDataComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * One step of making coffee as JEI shows it, such as roasting or grinding: some beans in every quality, and what each
 * one turns into. The two lists line up, so JEI can cycle them together and show that the quality is kept.
 */
public record BeanRecipe(Item input, List<ItemStack> inputs, List<ItemStack> outputs) {
    /**
     * A recipe for each of these items that the step turns into something.
     *
     * @param step makes one item from one input, or nothing if the input can't go through this step
     */
    public static List<BeanRecipe> of(List<? extends ItemLike> items, UnaryOperator<ItemStack> step) {
        List<BeanRecipe> recipes = new ArrayList<>();
        for (ItemLike item : items) {
            List<ItemStack> inputs = new ArrayList<>();
            for (BeanQuality quality : BeanQuality.values()) {
                ItemStack stack = new ItemStack(item);
                stack.set(ModDataComponents.BEAN_QUALITY.get(), quality);
                inputs.add(stack);
            }
            if (!step.apply(inputs.getFirst()).isEmpty()) {
                recipes.add(new BeanRecipe(item.asItem(), inputs, inputs.stream().map(step).toList()));
            }
        }
        return recipes;
    }
}
