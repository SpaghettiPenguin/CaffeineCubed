package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.recipe.CoffeeBrewingRecipe;
import com.caffeinecubed.recipe.GrindingRecipe;
import com.caffeinecubed.recipe.RoastingRecipe;
import com.caffeinecubed.recipe.SingleItemCoffeeRecipe;
import com.caffeinecubed.recipe.SiphonRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The mod's recipe types. Their recipes are JSON files in data/caffeinecubed/recipe, which data packs can change.
 */
public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, CaffeineCubed.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, CaffeineCubed.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<RoastingRecipe>> ROASTING = type("roasting");
    public static final DeferredHolder<RecipeType<?>, RecipeType<GrindingRecipe>> GRINDING = type("grinding");
    public static final DeferredHolder<RecipeType<?>, RecipeType<CoffeeBrewingRecipe>> BREWING = type("brewing");
    public static final DeferredHolder<RecipeType<?>, RecipeType<SiphonRecipe>> SIPHONING = type("siphoning");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RoastingRecipe>> ROASTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("roasting", () -> new SingleItemCoffeeRecipe.Serializer<>(RoastingRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrindingRecipe>> GRINDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("grinding", () -> new SingleItemCoffeeRecipe.Serializer<>(GrindingRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CoffeeBrewingRecipe>> BREWING_SERIALIZER =
            RECIPE_SERIALIZERS.register("brewing", CoffeeBrewingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SiphonRecipe>> SIPHONING_SERIALIZER =
            RECIPE_SERIALIZERS.register("siphoning", SiphonRecipe.Serializer::new);

    private ModRecipes() {
    }

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> type(String name) {
        return RECIPE_TYPES.register(name, () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, name)));
    }
}
