package com.caffeinecubed.compat.jei;

import com.caffeinecubed.block.entity.SiphonRecipe;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModDataComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * A Triple Axis Siphon recipe as JEI shows it: each of the three ingredients, as many as it takes, and the result.
 * Those that have a quality come in every quality, and their lists line up, so JEI can cycle them together and show
 * that the quality is kept; the others, like the water bucket, are just themselves.
 */
public record SiphoningRecipe(SiphonRecipe recipe, List<List<ItemStack>> inputs, List<ItemStack> outputs) {
    public static List<SiphoningRecipe> all() {
        List<SiphoningRecipe> recipes = new ArrayList<>();
        for (SiphonRecipe recipe : SiphonRecipe.ALL) {
            recipes.add(new SiphoningRecipe(recipe, recipe.parts().stream().map(part -> stacks(part.item(), part.count())).toList(),
                    stacks(recipe.result(), 1)));
        }
        return recipes;
    }

    private static List<ItemStack> stacks(ItemLike item, int count) {
        if (!item.asItem().components().has(ModDataComponents.BEAN_QUALITY.get())) {
            return List.of(new ItemStack(item, count));
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (BeanQuality quality : BeanQuality.values()) {
            ItemStack stack = new ItemStack(item, count);
            stack.set(ModDataComponents.BEAN_QUALITY.get(), quality);
            stacks.add(stack);
        }
        return stacks;
    }
}
