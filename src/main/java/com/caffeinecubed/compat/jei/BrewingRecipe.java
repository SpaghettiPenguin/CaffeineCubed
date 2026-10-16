package com.caffeinecubed.compat.jei;

import com.caffeinecubed.coffee.Syrup;
import com.caffeinecubed.recipe.CoffeeBrewingRecipe;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * A cup of coffee as JEI's Coffee Brewing page shows it: a brewing recipe's grounds, as many as it takes, in every
 * quality and the cup each brews, Coffee from water or Ice Coffee from ice, with or without milk from a Milk Frother and
 * a syrup from a Syrup Compactor. The two lists line up, so JEI can cycle them together.
 *
 * @param id the brewing recipe's id, from its JSON file
 */
public record BrewingRecipe(ResourceLocation id, List<ItemStack> inputs, List<ItemStack> outputs, boolean iced, boolean milk, @Nullable Syrup syrup) {
    /**
     * A page for each brewing recipe, plain, then with milk; then the same again with each syrup; then all of those
     * again with ice.
     */
    public static List<BrewingRecipe> all(List<RecipeHolder<CoffeeBrewingRecipe>> brewing) {
        List<Syrup> syrups = new ArrayList<>();
        syrups.add(null);
        syrups.addAll(List.of(Syrup.values()));
        List<BrewingRecipe> recipes = new ArrayList<>();
        for (boolean iced : new boolean[]{false, true}) {
            for (Syrup syrup : syrups) {
                for (boolean milk : new boolean[]{false, true}) {
                    for (RecipeHolder<CoffeeBrewingRecipe> holder : brewing) {
                        CoffeeBrewingRecipe recipe = holder.value();
                        BeanRecipe cup = BeanRecipe.of(holder.id(), recipe.grounds(), stack -> recipe.brew(stack, iced, milk, syrup));
                        if (cup != null) {
                            List<ItemStack> perCup = cup.inputs().stream().map(stack -> stack.copyWithCount(recipe.count())).toList();
                            recipes.add(new BrewingRecipe(holder.id(), perCup, cup.outputs(), iced, milk, syrup));
                        }
                    }
                }
            }
        }
        return recipes;
    }
}
