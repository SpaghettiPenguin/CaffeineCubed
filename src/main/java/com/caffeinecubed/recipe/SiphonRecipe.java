package com.caffeinecubed.recipe;

import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Something the Triple Axis Siphon makes: {@code caffeinecubed:siphoning}. Up to three different ingredients, one in
 * each of its input slots in any order, some needed more than one at a time, drawn together into one result.
 * Ingredients that have a quality must all share it, and the result keeps it. In JSON:
 * <pre>
 * {"type": "caffeinecubed:siphoning",
 *  "ingredients": [{"ingredient": {"item": "caffeinecubed:rainbow_blend"}, "count": 3},
 *                  {"ingredient": {"item": "minecraft:water_bucket"}},
 *                  {"ingredient": {"item": "minecraft:enchanted_golden_apple"}}],
 *  "result": {"id": "caffeinecubed:gods_coffee"}}
 * </pre>
 */
public class SiphonRecipe implements Recipe<SiphonRecipe.Input> {
    public static final MapCodec<SiphonRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Part.CODEC.listOf().validate(parts -> parts.isEmpty() || parts.size() > 3
                    ? DataResult.error(() -> "A siphon recipe needs one to three ingredients, one for each input slot")
                    : DataResult.success(parts)).fieldOf("ingredients").forGetter(SiphonRecipe::parts),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(SiphonRecipe::result)
    ).apply(instance, SiphonRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SiphonRecipe> STREAM_CODEC = StreamCodec.composite(
            Part.STREAM_CODEC.apply(ByteBufCodecs.list()), SiphonRecipe::parts,
            ItemStack.STREAM_CODEC, SiphonRecipe::result,
            SiphonRecipe::new);

    private final List<Part> parts;
    private final ItemStack result;

    public SiphonRecipe(List<Part> parts, ItemStack result) {
        this.parts = List.copyOf(parts);
        this.result = result;
    }

    /**
     * One ingredient, and how many of it each use takes.
     */
    public record Part(Ingredient ingredient, int count) {
        public static final Codec<Part> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(Part::ingredient),
                Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(Part::count)
        ).apply(instance, Part::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Part> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, Part::ingredient,
                ByteBufCodecs.VAR_INT, Part::count,
                Part::new);

        public boolean is(ItemStack stack) {
            return ingredient.test(stack);
        }
    }

    /**
     * What's in the siphon's three input slots.
     */
    public record Input(List<ItemStack> stacks) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return stacks.get(index);
        }

        @Override
        public int size() {
            return stacks.size();
        }
    }

    public List<Part> parts() {
        return parts;
    }

    public ItemStack result() {
        return result;
    }

    /**
     * The recipe these stacks make, from the level's siphoning recipes: each stack is a different one of its
     * ingredients, with enough of it, and those that have a quality share it. Null if there is none.
     */
    @Nullable
    public static SiphonRecipe find(Level level, List<ItemStack> stacks) {
        return CoffeeRecipes.siphoning(level).stream().filter(recipe -> recipe.matches(stacks)).findFirst().orElse(null);
    }

    /**
     * What the siphon makes from these stacks, with their quality; nothing if they don't make anything.
     */
    public static ItemStack blend(Level level, List<ItemStack> stacks) {
        SiphonRecipe recipe = find(level, stacks);
        return recipe == null ? ItemStack.EMPTY : recipe.make(stacks);
    }

    /**
     * Whether this stack can join what is already in the other input slots: some recipe needs it along with all of
     * the ingredients there and has room for it, none of them is the same item, and if it has a quality, theirs is the
     * same. Anything left over that is no ingredient, like an empty bucket, doesn't count.
     */
    public static boolean fits(Level level, ItemStack stack, List<ItemStack> others) {
        List<SiphonRecipe> recipes = CoffeeRecipes.siphoning(level);
        List<ItemStack> present = others.stream().filter(other -> recipes.stream().anyMatch(recipe -> recipe.needs(other))).toList();
        if (present.stream().anyMatch(other -> other.is(stack.getItem())
                || hasQuality(stack) && hasQuality(other) && BeanQuality.of(other) != BeanQuality.of(stack))) {
            return false;
        }
        return recipes.stream().anyMatch(recipe -> recipe.parts.size() > present.size() && recipe.needs(stack)
                && present.stream().allMatch(recipe::needs));
    }

    /**
     * Whether any of the level's siphoning recipes uses this.
     */
    public static boolean isIngredient(Level level, ItemStack stack) {
        return CoffeeRecipes.siphoning(level).stream().anyMatch(recipe -> recipe.needs(stack));
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
     * Whether these stacks are exactly this recipe's ingredients, one of each in any order with the rest of the slots
     * empty, each with enough of it, and those with a quality share it.
     */
    public boolean matches(List<ItemStack> stacks) {
        if (stacks.stream().anyMatch(SiphonRecipe::hasQuality) && quality(stacks).isEmpty()) {
            return false;
        }
        List<Part> wanted = new ArrayList<>(parts);
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            Optional<Part> part = wanted.stream().filter(wantedPart -> wantedPart.is(stack)).findFirst();
            if (part.isEmpty() || stack.getCount() < part.get().count()) {
                return false;
            }
            wanted.remove(part.get());
        }
        return wanted.isEmpty();
    }

    /**
     * What these stacks make: the result, with the quality they share.
     */
    public ItemStack make(List<ItemStack> stacks) {
        ItemStack made = result.copy();
        quality(stacks).ifPresent(quality -> CoffeeRecipes.withQuality(made, quality));
        return made;
    }

    @Override
    public boolean matches(Input input, Level level) {
        return matches(input.stacks());
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return make(input.stacks());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        parts.forEach(part -> ingredients.add(part.ingredient()));
        return ingredients;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SIPHONING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.SIPHONING.get();
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

    public static class Serializer implements RecipeSerializer<SiphonRecipe> {
        @Override
        public MapCodec<SiphonRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SiphonRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
