package com.caffeinecubed.compat.jei;

import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.coffee.Syrup;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A cup of coffee as JEI's Coffee Brewing page shows it: three grounds of one roast in every quality and the cup each
 * brews, Coffee from water or Ice Coffee from ice, with or without milk from a Milk Frother and a syrup from a Syrup
 * Compactor. The two lists line up, so JEI can cycle them together.
 */
public record BrewingRecipe(Item grounds, List<ItemStack> inputs, List<ItemStack> outputs, boolean iced, boolean milk, @Nullable Syrup syrup) {
    /**
     * A recipe for each roast's grounds, plain, then with milk; then the same again with each syrup; then all of those
     * again with ice.
     */
    public static List<BrewingRecipe> all(List<Item> grounds) {
        List<Syrup> syrups = new ArrayList<>();
        syrups.add(null);
        syrups.addAll(List.of(Syrup.values()));
        List<BrewingRecipe> recipes = new ArrayList<>();
        for (boolean iced : new boolean[]{false, true}) {
            for (Syrup syrup : syrups) {
                for (boolean milk : new boolean[]{false, true}) {
                    for (BeanRecipe recipe : BeanRecipe.of(grounds, stack -> CoffeeMachineBlockEntity.brew(stack, iced, milk, syrup))) {
                        List<ItemStack> threeEach = recipe.inputs().stream()
                                .map(stack -> stack.copyWithCount(CoffeeMachineBlockEntity.GROUNDS_PER_CUP)).toList();
                        recipes.add(new BrewingRecipe(recipe.input(), threeEach, recipe.outputs(), iced, milk, syrup));
                    }
                }
            }
        }
        return recipes;
    }
}
