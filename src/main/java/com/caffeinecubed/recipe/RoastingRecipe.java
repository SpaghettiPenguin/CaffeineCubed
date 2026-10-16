package com.caffeinecubed.recipe;

import com.caffeinecubed.registry.ModRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * What the Coffee Roaster turns an item into: {@code caffeinecubed:roasting}, one bean at a time, keeping its quality.
 */
public class RoastingRecipe extends SingleItemCoffeeRecipe {
    public RoastingRecipe(Ingredient ingredient, ItemStack result) {
        super(ingredient, result);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ROASTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ROASTING.get();
    }
}
