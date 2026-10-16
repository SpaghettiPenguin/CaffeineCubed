package com.caffeinecubed.recipe;

import com.caffeinecubed.registry.ModRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * What the Coffee Grinder and the Grinder Attachment turn an item into: {@code caffeinecubed:grinding}, one bean at a
 * time, keeping its quality.
 */
public class GrindingRecipe extends SingleItemCoffeeRecipe {
    public GrindingRecipe(Ingredient ingredient, ItemStack result) {
        super(ingredient, result);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.GRINDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.GRINDING.get();
    }
}
