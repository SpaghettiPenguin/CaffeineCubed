package com.caffeinecubed.recipe;

import com.caffeinecubed.coffee.BeanQuality;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * A recipe that turns one item into another, keeping its quality: roasting and grinding. In JSON:
 * <pre>
 * {"type": "caffeinecubed:roasting", "ingredient": {"item": "caffeinecubed:raw_coffee_beans"},
 *  "result": {"id": "caffeinecubed:light_roast_coffee_beans"}}
 * </pre>
 */
public abstract class SingleItemCoffeeRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient ingredient;
    private final ItemStack result;

    protected SingleItemCoffeeRecipe(Ingredient ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public ItemStack result() {
        return result;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    /**
     * What one of the input makes: the result, with the input's quality.
     */
    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return make(input.item());
    }

    public ItemStack make(ItemStack input) {
        return CoffeeRecipes.withQuality(result.copy(), BeanQuality.of(input));
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
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    // Made in machines and by hand, not in a crafting table, so it stays out of the recipe book
    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer<T extends SingleItemCoffeeRecipe> implements RecipeSerializer<T> {
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public Serializer(BiFunction<Ingredient, ItemStack, T> factory) {
            codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(SingleItemCoffeeRecipe::ingredient),
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(SingleItemCoffeeRecipe::result)
            ).apply(instance, factory::apply));
            streamCodec = StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, SingleItemCoffeeRecipe::ingredient,
                    ItemStack.STREAM_CODEC, SingleItemCoffeeRecipe::result,
                    factory::apply);
        }

        @Override
        public MapCodec<T> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return streamCodec;
        }
    }
}
