package com.caffeinecubed.gametest;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.Roast;
import com.caffeinecubed.item.CupOfCoffeeItem;
import com.caffeinecubed.recipe.CoffeeBrewingRecipe;
import com.caffeinecubed.recipe.SiphonRecipe;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import com.caffeinecubed.registry.ModRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The roasting, grinding, brewing and siphoning recipes are data: the mod's own load from its JSON files, the format
 * reads as the README says, they reach players' games intact, and a data pack that changes them changes what the
 * machines do. A test that changes the recipes puts them back before it ends; it all happens within one tick, so no
 * other test sees it.
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class DataPackRecipeTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(1, 1, 1);

    private DataPackRecipeTests() {
    }

    @GameTest(template = EMPTY)
    public static void theModsOwnRecipesAllLoad(GameTestHelper helper) {
        RecipeManager recipes = helper.getLevel().getRecipeManager();
        expectIds(helper, recipes, ModRecipes.ROASTING.get(), "roasting/light_roast_coffee_beans", "roasting/medium_roast_coffee_beans",
                "roasting/dark_roast_coffee_beans");
        expectIds(helper, recipes, ModRecipes.GRINDING.get(), "grinding/light_roast_coffee_grounds", "grinding/medium_roast_coffee_grounds",
                "grinding/dark_roast_coffee_grounds");
        expectIds(helper, recipes, ModRecipes.BREWING.get(), "brewing/light_roast_coffee", "brewing/medium_roast_coffee",
                "brewing/dark_roast_coffee");
        expectIds(helper, recipes, ModRecipes.SIPHONING.get(), "siphoning/rainbow_blend", "siphoning/gods_coffee", "siphoning/gods_ice_coffee");
        for (Roast roast : Roast.values()) {
            String name = roast.getSerializedName() + "_roast_coffee";
            CoffeeBrewingRecipe brewing = (CoffeeBrewingRecipe) recipes.byKey(id("brewing/" + name)).orElseThrow().value();
            helper.assertTrue(brewing.roast() == roast && brewing.count() == 3, name + " should take 3 grounds and be " + roast
                    + " roast, but takes " + brewing.count() + " and is " + brewing.roast());
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theJsonFormatReadsAsDocumented(GameTestHelper helper) {
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
        // Left out, a brewing recipe takes 3 grounds, and a siphon ingredient 1
        CoffeeBrewingRecipe brewing = (CoffeeBrewingRecipe) parse(ops, """
                {"type": "caffeinecubed:brewing", "grounds": {"item": "caffeinecubed:dark_roast_coffee_grounds"}, "roast": "dark"}
                """).getOrThrow();
        helper.assertTrue(brewing.count() == 3, "A brewing recipe should take 3 grounds unless it says, not " + brewing.count());
        SiphonRecipe siphon = (SiphonRecipe) parse(ops, """
                {"type": "caffeinecubed:siphoning",
                 "ingredients": [{"ingredient": {"item": "minecraft:sugar"}}, {"ingredient": {"tag": "minecraft:flowers"}, "count": 2}],
                 "result": {"id": "caffeinecubed:rainbow_blend", "count": 2}}
                """).getOrThrow();
        helper.assertTrue(siphon.parts().size() == 2 && siphon.parts().get(0).count() == 1 && siphon.parts().get(1).count() == 2
                && siphon.result().is(ModItems.RAINBOW_BLEND.get()) && siphon.result().getCount() == 2, "The siphon recipe read wrong: " + siphon.parts());

        // Mistakes are turned away, so a broken data pack says what is wrong instead of breaking a machine
        helper.assertTrue(parse(ops, """
                {"type": "caffeinecubed:brewing", "grounds": {"item": "caffeinecubed:dark_roast_coffee_grounds"}, "roast": "burnt"}
                """).isError(), "There is no burnt roast");
        helper.assertTrue(parse(ops, """
                {"type": "caffeinecubed:siphoning", "ingredients": [{"ingredient": {"item": "minecraft:sugar"}},
                 {"ingredient": {"item": "minecraft:apple"}}, {"ingredient": {"item": "minecraft:egg"}},
                 {"ingredient": {"item": "minecraft:wheat"}}], "result": {"id": "caffeinecubed:rainbow_blend"}}
                """).isError(), "The siphon has only three input slots");
        helper.assertTrue(parse(ops, """
                {"type": "caffeinecubed:roasting", "ingredient": {"item": "caffeinecubed:raw_coffee_beans"}}
                """).isError(), "A roasting recipe needs a result");

        // A data pack takes one of the mod's recipes out by putting this in its place, as with any recipe
        helper.assertTrue(Recipe.CONDITIONAL_CODEC.parse(ops, JsonParser.parseString("""
                {"neoforge:conditions": [{"type": "neoforge:false"}]}
                """)).getOrThrow().isEmpty(), "A recipe whose condition is false shouldn't load");
        helper.succeed();
    }

    /**
     * Players' games get the server's recipes over the network (for JEI and for the slots in the machines' menus), so
     * each one has to come out the other end the same. As with every recipe, a tag arrives as the items in it.
     */
    @GameTest(template = EMPTY)
    public static void recipesReachPlayersUnchanged(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, level.registryAccess());
        List<RecipeType<?>> types = List.of(ModRecipes.ROASTING.get(), ModRecipes.GRINDING.get(), ModRecipes.BREWING.get(), ModRecipes.SIPHONING.get());
        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            if (!types.contains(holder.value().getType())) {
                continue;
            }
            Recipe<?> sent = sendAndReceive(level, holder.value());
            JsonElement before = tagsAsItems(Recipe.CODEC.encodeStart(ops, holder.value()).getOrThrow());
            JsonElement after = Recipe.CODEC.encodeStart(ops, sent).getOrThrow();
            helper.assertTrue(before.equals(after), holder.id() + " changed on the way: " + before + " became " + after);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aDataPackCanChangeWhatTheMachinesDo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RecipeManager manager = level.getRecipeManager();
        List<RecipeHolder<?>> original = new ArrayList<>(manager.getRecipes());
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, level.registryAccess());
        try {
            List<RecipeHolder<?>> changed = new ArrayList<>(original);
            // Medium beans can't be roasted any darker, cocoa beans roast into dark roast beans, dark grounds brew a
            // cup one at a time, and the siphon blends light grounds with sugar
            changed.removeIf(holder -> holder.id().equals(id("roasting/dark_roast_coffee_beans")) || holder.id().equals(id("brewing/dark_roast_coffee")));
            changed.add(holder(ops, "roasting/dark_roast_coffee_beans_from_cocoa", """
                    {"type": "caffeinecubed:roasting", "ingredient": {"item": "minecraft:cocoa_beans"},
                     "result": {"id": "caffeinecubed:dark_roast_coffee_beans"}}
                    """));
            changed.add(holder(ops, "brewing/dark_roast_coffee", """
                    {"type": "caffeinecubed:brewing", "grounds": {"item": "caffeinecubed:dark_roast_coffee_grounds"}, "count": 1, "roast": "dark"}
                    """));
            changed.add(holder(ops, "siphoning/sweet_blend", """
                    {"type": "caffeinecubed:siphoning",
                     "ingredients": [{"ingredient": {"item": "caffeinecubed:light_roast_coffee_grounds"}}, {"ingredient": {"item": "minecraft:sugar"}}],
                     "result": {"id": "caffeinecubed:rainbow_blend"}}
                    """));
            manager.replaceRecipes(changed);

            helper.assertTrue(CoffeeRoasterBlockEntity.roast(level, beans(ModItems.MEDIUM_ROAST_COFFEE_BEANS.get(), BeanQuality.GOOD, 1)).isEmpty(),
                    "Without its recipe, medium beans shouldn't roast any more");
            ItemStack fromCocoa = CoffeeRoasterBlockEntity.roast(level, new ItemStack(Items.COCOA_BEANS));
            helper.assertTrue(fromCocoa.is(ModItems.DARK_ROAST_COFFEE_BEANS.get()), "Cocoa beans should roast into dark roast beans, not " + fromCocoa);

            helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get());
            CoffeeMachineBlockEntity machine = (CoffeeMachineBlockEntity) helper.getBlockEntity(MACHINE);
            machine.setItem(CoffeeMachineBlockEntity.WATER, new ItemStack(Items.WATER_BUCKET));
            machine.setItem(CoffeeMachineBlockEntity.FUEL, new ItemStack(Items.COAL));
            machine.setItem(CoffeeMachineBlockEntity.GROUNDS, beans(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 1));
            BlockPos at = helper.absolutePos(MACHINE);
            for (int tick = 0; tick < CoffeeMachineBlockEntity.brewingTime(); tick++) {
                CoffeeMachineBlockEntity.serverTick(level, at, level.getBlockState(at), machine);
            }
            ItemStack cup = machine.getItem(CoffeeMachineBlockEntity.OUTPUT);
            helper.assertTrue(cup.is(ModItems.COFFEE.get()) && CupOfCoffeeItem.getRoast(cup) == Roast.DARK && BeanQuality.of(cup) == BeanQuality.GREAT,
                    "One great dark ground should brew a cup of great dark roast coffee, but made " + cup);

            helper.setBlock(MACHINE, ModBlocks.TRIPLE_AXIS_SIPHON.get());
            TripleAxisSiphonBlockEntity siphon = (TripleAxisSiphonBlockEntity) helper.getBlockEntity(MACHINE);
            helper.assertTrue(siphon.canPlaceItem(TripleAxisSiphonBlockEntity.TOP, new ItemStack(Items.SUGAR)), "Sugar should go in the siphon now");
            siphon.setItem(TripleAxisSiphonBlockEntity.LEFT, beans(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GOOD, 1));
            siphon.setItem(TripleAxisSiphonBlockEntity.TOP, new ItemStack(Items.SUGAR));
            for (int tick = 0; tick < TripleAxisSiphonBlockEntity.siphoningTime(); tick++) {
                TripleAxisSiphonBlockEntity.serverTick(level, at, level.getBlockState(at), siphon);
            }
            ItemStack blend = siphon.getItem(TripleAxisSiphonBlockEntity.OUTPUT);
            helper.assertTrue(blend.is(ModItems.RAINBOW_BLEND.get()) && BeanQuality.of(blend) == BeanQuality.GOOD,
                    "Good light grounds and sugar should make a good Rainbow Blend, but made " + blend);
        } finally {
            manager.replaceRecipes(original);
        }
        helper.assertTrue(CoffeeRoasterBlockEntity.roast(level, new ItemStack(ModItems.MEDIUM_ROAST_COFFEE_BEANS.get())).is(ModItems.DARK_ROAST_COFFEE_BEANS.get()),
                "The mod's own recipes should be back");
        helper.succeed();
    }

    /**
     * The recipe's JSON with each tag ingredient written out as the items in it, the way it reads after the trip.
     */
    private static JsonElement tagsAsItems(JsonElement json) {
        if (json instanceof JsonArray array) {
            JsonArray copy = new JsonArray();
            array.forEach(element -> copy.add(tagsAsItems(element)));
            return copy;
        }
        if (!(json instanceof JsonObject object)) {
            return json;
        }
        if (object.size() == 1 && object.has("tag")) {
            JsonArray items = new JsonArray();
            for (ItemStack stack : Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse(object.get("tag").getAsString()))).getItems()) {
                JsonObject item = new JsonObject();
                item.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                items.add(item);
            }
            return items.size() == 1 ? items.get(0) : items;
        }
        JsonObject copy = new JsonObject();
        object.entrySet().forEach(entry -> copy.add(entry.getKey(), tagsAsItems(entry.getValue())));
        return copy;
    }

    private static void expectIds(GameTestHelper helper, RecipeManager recipes, RecipeType<?> type, String... names) {
        Set<ResourceLocation> found = recipes.getRecipes().stream().filter(holder -> holder.value().getType() == type)
                .map(RecipeHolder::id).collect(Collectors.toSet());
        Set<ResourceLocation> expected = Arrays.stream(names).map(DataPackRecipeTests::id).collect(Collectors.toSet());
        helper.assertTrue(found.equals(expected), type + " recipes should be " + expected + " but are " + found);
    }

    private static DataResult<Recipe<?>> parse(DynamicOps<JsonElement> ops, String json) {
        return Recipe.CODEC.parse(ops, JsonParser.parseString(json));
    }

    private static RecipeHolder<?> holder(DynamicOps<JsonElement> ops, String name, String json) {
        return new RecipeHolder<>(id(name), parse(ops, json).getOrThrow());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> T sendAndReceive(ServerLevel level, T recipe) {
        RecipeSerializer<T> serializer = (RecipeSerializer<T>) recipe.getSerializer();
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        try {
            serializer.streamCodec().encode(buffer, recipe);
            return serializer.streamCodec().decode(buffer);
        } finally {
            buffer.release();
        }
    }

    private static ItemStack beans(Item item, BeanQuality quality, int count) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        return stack;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, path);
    }
}
