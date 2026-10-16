package com.caffeinecubed.block.entity;

import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * Something the Triple Axis Siphon makes: three different ingredients, one in each of its input slots in any order,
 * some needed more than one at a time, drawn together into one result. Ingredients that have a quality must all share
 * it, and the result keeps it.
 */
public record SiphonRecipe(List<Part> parts, ItemLike result) {
    public static final List<SiphonRecipe> ALL = List.of(
            // Rainbow Blend: one of each roast's grounds
            new SiphonRecipe(List.of(new Part(ModItems.LIGHT_ROAST_COFFEE_GROUNDS, 1), new Part(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS, 1),
                    new Part(ModItems.DARK_ROAST_COFFEE_GROUNDS, 1)), ModItems.RAINBOW_BLEND),
            // God's Coffee: three Rainbow Blends, a bucket of water and an enchanted golden apple
            new SiphonRecipe(List.of(new Part(ModItems.RAINBOW_BLEND, 3), new Part(Items.WATER_BUCKET, 1),
                    new Part(Items.ENCHANTED_GOLDEN_APPLE, 1)), ModItems.GODS_COFFEE),
            // God's Ice Coffee: the same with a block of ice instead of the water
            new SiphonRecipe(List.of(new Part(ModItems.RAINBOW_BLEND, 3), new Part(Items.ICE, 1),
                    new Part(Items.ENCHANTED_GOLDEN_APPLE, 1)), ModItems.GODS_ICE_COFFEE));

    /**
     * One ingredient, and how many of it each use takes.
     */
    public record Part(ItemLike item, int count) {
        public boolean is(ItemStack stack) {
            return stack.is(item.asItem());
        }
    }

    /**
     * The recipe these three stacks make: each is a different one of its ingredients, with enough of it, and those that
     * have a quality share it. Null if there is none.
     */
    @Nullable
    public static SiphonRecipe find(List<ItemStack> stacks) {
        if (quality(stacks).isEmpty() && stacks.stream().anyMatch(SiphonRecipe::hasQuality)) {
            return null;
        }
        return ALL.stream().filter(recipe -> recipe.matches(stacks)).findFirst().orElse(null);
    }

    /**
     * What the siphon makes from these three stacks, with their quality; nothing if they don't make anything.
     */
    public static ItemStack blend(List<ItemStack> stacks) {
        SiphonRecipe recipe = find(stacks);
        if (recipe == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(recipe.result());
        quality(stacks).ifPresent(quality -> result.set(ModDataComponents.BEAN_QUALITY.get(), quality));
        return result;
    }

    /**
     * Whether this stack can join what is already in the other input slots: some recipe needs it along with all of
     * the ingredients there, none of them is the same item, and if it has a quality, theirs is the same. Anything left
     * over that is no ingredient, like an empty bucket, doesn't count.
     */
    public static boolean fits(ItemStack stack, List<ItemStack> others) {
        List<ItemStack> present = others.stream().filter(SiphonRecipe::isIngredient).toList();
        if (present.stream().anyMatch(other -> other.is(stack.getItem())
                || hasQuality(stack) && hasQuality(other) && BeanQuality.of(other) != BeanQuality.of(stack))) {
            return false;
        }
        return ALL.stream().anyMatch(recipe -> recipe.needs(stack) && present.stream().allMatch(recipe::needs));
    }

    /**
     * Whether any recipe uses this.
     */
    public static boolean isIngredient(ItemStack stack) {
        return ALL.stream().anyMatch(recipe -> recipe.needs(stack));
    }

    public boolean needs(ItemStack stack) {
        return part(stack).isPresent();
    }

    /**
     * The ingredient this stack is, if this recipe uses it.
     */
    public Optional<Part> part(ItemStack stack) {
        return parts.stream().filter(part -> part.is(stack)).findFirst();
    }

    /**
     * Whether these stacks are exactly this recipe's ingredients, one of each in any order, each with enough of it.
     */
    public boolean matches(List<ItemStack> stacks) {
        List<Part> wanted = new ArrayList<>(parts);
        for (ItemStack stack : stacks) {
            Optional<Part> part = wanted.stream().filter(wantedPart -> wantedPart.is(stack)).findFirst();
            if (part.isEmpty() || stack.getCount() < part.get().count()) {
                return false;
            }
            wanted.remove(part.get());
        }
        return wanted.isEmpty();
    }

    private static boolean hasQuality(ItemStack stack) {
        return stack.has(ModDataComponents.BEAN_QUALITY.get());
    }

    /**
     * The quality the stacks that have one share; empty if none has one, or they differ.
     */
    private static Optional<BeanQuality> quality(List<ItemStack> stacks) {
        List<BeanQuality> qualities = stacks.stream().filter(SiphonRecipe::hasQuality).map(BeanQuality::of).distinct().toList();
        return qualities.size() == 1 ? Optional.of(qualities.getFirst()) : Optional.empty();
    }
}
