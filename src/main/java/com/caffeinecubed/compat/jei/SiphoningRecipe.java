package com.caffeinecubed.compat.jei;

import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.recipe.CoffeeRecipes;
import com.caffeinecubed.recipe.SiphonRecipe;
import com.caffeinecubed.registry.ModDataComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * A Triple Axis Siphon recipe as JEI shows it: each of its ingredients, as many as it takes, and the result. An
 * ingredient that is one item with a quality comes in every quality, lined up with the result's, so JEI can cycle them
 * together and show that the quality is kept; the others, like the water bucket, are just themselves.
 *
 * @param id the recipe's id, from its JSON file
 * @param linked for each ingredient, whether it cycles through the qualities along with the result
 */
public record SiphoningRecipe(ResourceLocation id, List<List<ItemStack>> inputs, List<Boolean> linked, List<ItemStack> outputs) {
    public static List<SiphoningRecipe> all(List<RecipeHolder<SiphonRecipe>> siphoning) {
        List<SiphoningRecipe> recipes = new ArrayList<>();
        for (RecipeHolder<SiphonRecipe> holder : siphoning) {
            SiphonRecipe recipe = holder.value();
            List<List<ItemStack>> inputs = new ArrayList<>();
            List<Boolean> linked = new ArrayList<>();
            for (SiphonRecipe.Part part : recipe.parts()) {
                ItemStack[] items = part.ingredient().getItems();
                boolean quality = items.length == 1 && items[0].getItem().components().has(ModDataComponents.BEAN_QUALITY.get());
                inputs.add(BeanRecipe.everyQuality(part.ingredient()).stream().map(stack -> stack.copyWithCount(part.count())).toList());
                linked.add(quality);
            }
            if (inputs.stream().anyMatch(List::isEmpty)) {
                continue;
            }
            List<ItemStack> outputs = new ArrayList<>();
            if (linked.contains(true)) {
                for (BeanQuality quality : BeanQuality.values()) {
                    outputs.add(CoffeeRecipes.withQuality(recipe.result().copy(), quality));
                }
            } else {
                outputs.add(recipe.result().copy());
            }
            recipes.add(new SiphoningRecipe(holder.id(), inputs, linked, outputs));
        }
        return recipes;
    }
}
