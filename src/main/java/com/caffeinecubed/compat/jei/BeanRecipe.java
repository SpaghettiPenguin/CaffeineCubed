package com.caffeinecubed.compat.jei;

import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModDataComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * One step of making coffee as JEI shows it, such as roasting or grinding: what a recipe takes, in every quality, and
 * what each one turns into. The two lists line up, so JEI can cycle them together and show that the quality is kept.
 *
 * @param id the recipe's id, from its JSON file
 */
public record BeanRecipe(ResourceLocation id, List<ItemStack> inputs, List<ItemStack> outputs) {
    /**
     * The recipe for every item this ingredient takes, or null if it takes none (an empty tag) or makes nothing.
     *
     * @param step makes one item from one input
     */
    @Nullable
    public static BeanRecipe of(ResourceLocation id, Ingredient ingredient, UnaryOperator<ItemStack> step) {
        List<ItemStack> inputs = everyQuality(ingredient);
        List<ItemStack> outputs = inputs.stream().map(step).toList();
        if (inputs.isEmpty() || outputs.stream().anyMatch(ItemStack::isEmpty)) {
            return null;
        }
        return new BeanRecipe(id, inputs, outputs);
    }

    /**
     * Each item the ingredient takes, in every quality if it has one.
     */
    public static List<ItemStack> everyQuality(Ingredient ingredient) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack item : ingredient.getItems()) {
            if (!item.getItem().components().has(ModDataComponents.BEAN_QUALITY.get())) {
                stacks.add(item.copy());
                continue;
            }
            for (BeanQuality quality : BeanQuality.values()) {
                ItemStack stack = item.copy();
                stack.set(ModDataComponents.BEAN_QUALITY.get(), quality);
                stacks.add(stack);
            }
        }
        return stacks;
    }
}
