package com.caffeinecubed.recipe;

import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.Roast;
import com.caffeinecubed.coffee.Syrup;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import com.caffeinecubed.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Which grounds the Coffee Machine brews, how many make a cup and what roast the cup is, which decides its roast
 * effect: {@code caffeinecubed:brewing}. In JSON:
 * <pre>
 * {"type": "caffeinecubed:brewing", "grounds": {"item": "caffeinecubed:light_roast_coffee_grounds"}, "count": 3,
 *  "roast": "light"}
 * </pre>
 * The cup is Coffee, or Ice Coffee when there's ice in the water slot, with the grounds' quality, and milk or a syrup
 * if the machine's attachments add them.
 */
public class CoffeeBrewingRecipe implements Recipe<SingleRecipeInput> {
    public static final int DEFAULT_COUNT = 3;
    public static final MapCodec<CoffeeBrewingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("grounds").forGetter(CoffeeBrewingRecipe::grounds),
            Codec.intRange(1, 64).optionalFieldOf("count", DEFAULT_COUNT).forGetter(CoffeeBrewingRecipe::count),
            Roast.CODEC.fieldOf("roast").forGetter(CoffeeBrewingRecipe::roast)
    ).apply(instance, CoffeeBrewingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CoffeeBrewingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CoffeeBrewingRecipe::grounds,
            ByteBufCodecs.VAR_INT, CoffeeBrewingRecipe::count,
            Roast.STREAM_CODEC, CoffeeBrewingRecipe::roast,
            CoffeeBrewingRecipe::new);

    private final Ingredient grounds;
    private final int count;
    private final Roast roast;

    public CoffeeBrewingRecipe(Ingredient grounds, int count, Roast roast) {
        this.grounds = grounds;
        this.count = count;
        this.roast = roast;
    }

    public Ingredient grounds() {
        return grounds;
    }

    /**
     * How many grounds a cup takes.
     */
    public int count() {
        return count;
    }

    public Roast roast() {
        return roast;
    }

    /**
     * The cup these grounds brew: Coffee or (with ice) Ice Coffee, of this recipe's roast and the grounds' quality, with
     * or without milk and a syrup.
     */
    public ItemStack brew(ItemStack groundsStack, boolean iced, boolean milk, @Nullable Syrup syrup) {
        ItemStack coffee = new ItemStack(iced ? ModItems.ICE_COFFEE.get() : ModItems.COFFEE.get());
        coffee.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.of(groundsStack));
        coffee.set(ModDataComponents.ROAST.get(), roast);
        if (milk) {
            coffee.set(ModDataComponents.MILK.get(), Unit.INSTANCE);
        }
        if (syrup != null) {
            coffee.set(ModDataComponents.SYRUP.get(), syrup);
        }
        return coffee;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return grounds.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return brew(input.item(), false, false, null);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        ItemStack coffee = new ItemStack(ModItems.COFFEE.get());
        coffee.set(ModDataComponents.ROAST.get(), roast);
        return coffee;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, grounds);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BREWING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.BREWING.get();
    }

    public static class Serializer implements RecipeSerializer<CoffeeBrewingRecipe> {
        @Override
        public MapCodec<CoffeeBrewingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CoffeeBrewingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
