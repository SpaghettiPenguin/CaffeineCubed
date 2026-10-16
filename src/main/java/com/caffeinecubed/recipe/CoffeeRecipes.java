package com.caffeinecubed.recipe;

import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModRecipes;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Finds the mod's recipes, which are data: the JSON files in data/caffeinecubed/recipe (roasting, grinding, brewing and
 * siphoning), which data packs can add to, change or remove. Every lookup needs a level, since that's where a world's
 * recipes live; players' games get the server's recipes when they join.
 */
public final class CoffeeRecipes {
    private CoffeeRecipes() {
    }

    public static Optional<RoastingRecipe> roasting(Level level, ItemStack beans) {
        return beans.isEmpty() ? Optional.empty() : level.getRecipeManager()
                .getRecipeFor(ModRecipes.ROASTING.get(), new SingleRecipeInput(beans), level).map(RecipeHolder::value);
    }

    public static Optional<GrindingRecipe> grinding(Level level, ItemStack beans) {
        return beans.isEmpty() ? Optional.empty() : level.getRecipeManager()
                .getRecipeFor(ModRecipes.GRINDING.get(), new SingleRecipeInput(beans), level).map(RecipeHolder::value);
    }

    public static Optional<CoffeeBrewingRecipe> brewing(Level level, ItemStack grounds) {
        return grounds.isEmpty() ? Optional.empty() : level.getRecipeManager()
                .getRecipeFor(ModRecipes.BREWING.get(), new SingleRecipeInput(grounds), level).map(RecipeHolder::value);
    }

    public static List<SiphonRecipe> siphoning(Level level) {
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.SIPHONING.get()).stream().map(RecipeHolder::value).toList();
    }

    /**
     * The result with the given quality, if it is something that has one (beans, grounds, coffee and so on).
     */
    public static ItemStack withQuality(ItemStack result, BeanQuality quality) {
        if (result.getItem().components().has(ModDataComponents.BEAN_QUALITY.get())) {
            result.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        }
        return result;
    }
}
