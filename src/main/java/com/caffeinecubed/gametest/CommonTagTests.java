package com.caffeinecubed.gametest;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
import com.caffeinecubed.recipe.CoffeeRecipes;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModItems;
import com.caffeinecubed.registry.ModTags;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The mod's items are in the common ({@code c:}) tags other mods look for, and its recipes and machines take what's in
 * the common tags they use, so other mods' equivalents work, while plain vanilla items still do exactly what they did.
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class CommonTagTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(1, 1, 1);

    private CommonTagTests() {
    }

    @GameTest(template = EMPTY)
    public static void rawBeansAreACropLikeCocoaBeans(GameTestHelper helper) {
        ItemStack beans = new ItemStack(ModItems.RAW_COFFEE_BEANS.get());
        helper.assertTrue(beans.is(ModTags.Items.COFFEE_BEAN_CROPS) && beans.is(Tags.Items.CROPS), "Raw coffee beans should be in c:crops/coffee_bean and c:crops");
        for (ItemLike processed : List.of(ModItems.LIGHT_ROAST_COFFEE_BEANS, ModItems.DARK_ROAST_COFFEE_GROUNDS, ModItems.RAINBOW_BLEND)) {
            helper.assertFalse(new ItemStack(processed).is(Tags.Items.CROPS), processed + " is made, not grown, so it isn't a crop");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void everyCoffeeIsADrink(GameTestHelper helper) {
        List<Item> cups = List.of(ModItems.COFFEE.get(), ModItems.ICE_COFFEE.get(), ModItems.GODS_COFFEE.get(), ModItems.GODS_ICE_COFFEE.get());
        for (Item cup : cups) {
            ItemStack stack = new ItemStack(cup);
            helper.assertTrue(stack.is(ModTags.Items.COFFEE_DRINKS) && stack.is(Tags.Items.DRINKS), cup + " should be in c:drinks/coffee and c:drinks");
        }
        List<Item> drinks = ModItems.ITEMS.getEntries().stream().<Item>map(entry -> entry.get()).filter(item -> new ItemStack(item).is(Tags.Items.DRINKS)).toList();
        helper.assertTrue(drinks.size() == cups.size() && drinks.containsAll(cups), "Only the cups should be drinks, not " + drinks);
        helper.succeed();
    }

    /**
     * Each crafting recipe's ingredients now come from common tags where there is one for the same thing; whatever it
     * took before must still fit.
     */
    @GameTest(template = EMPTY)
    public static void craftingRecipesStillTakeTheVanillaItems(GameTestHelper helper) {
        Map<String, List<Item>> recipes = Map.of(
                "coffee_grinder", List.of(Items.STICK, Items.FLINT, Items.IRON_INGOT),
                "coffee_machine", List.of(Items.IRON_INGOT, Items.GLASS_PANE, Items.PAPER, Items.FURNACE),
                "coffee_roaster", List.of(Items.HOPPER, Items.FURNACE, Items.IRON_INGOT),
                "grinder_attachment", List.of(Items.HOPPER, ModItems.COFFEE_GRINDER.get(), Items.IRON_INGOT),
                "milk_frother", List.of(Items.IRON_INGOT, Items.GLASS_PANE, Items.MILK_BUCKET),
                "syrup_compactor", List.of(Items.PISTON, Items.GLASS_BOTTLE, Items.IRON_INGOT),
                "triple_axis_siphon", List.of(Items.GOLD_INGOT, Items.NETHER_STAR, Items.QUARTZ_BLOCK),
                "guide_book", List.of(Items.BOOK, ModItems.RAW_COFFEE_BEANS.get()));
        for (Map.Entry<String, List<Item>> entry : recipes.entrySet()) {
            Recipe<?> recipe = helper.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, entry.getKey()))
                    .orElseThrow().value();
            List<Ingredient> ingredients = recipe.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).toList();
            for (Item item : entry.getValue()) {
                helper.assertTrue(ingredients.stream().anyMatch(ingredient -> ingredient.test(new ItemStack(item))),
                        entry.getKey() + " should still take " + item);
            }
            for (Ingredient ingredient : ingredients) {
                helper.assertTrue(entry.getValue().stream().anyMatch(item -> ingredient.test(new ItemStack(item))),
                        entry.getKey() + " has an ingredient that takes none of the items it used to: " + List.of(ingredient.getItems()));
            }
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void machinesTakeEverythingInTheCommonTagsTheyUse(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (Item beans : items(helper, ModTags.Items.COFFEE_BEAN_CROPS)) {
            helper.assertTrue(CoffeeRoasterBlockEntity.isRoastable(level, new ItemStack(beans)), "The roaster should roast " + beans);
        }
        for (Item water : items(helper, Tags.Items.BUCKETS_WATER)) {
            helper.assertTrue(CoffeeMachineBlockEntity.isWaterOrIce(new ItemStack(water)), "The machine should brew with " + water);
            helper.assertTrue(CoffeeRecipes.siphoning(level).stream().anyMatch(recipe -> recipe.needs(new ItemStack(water))),
                    "God's Coffee should take " + water);
        }
        for (Item milk : items(helper, Tags.Items.BUCKETS_MILK)) {
            helper.assertTrue(CoffeeMachineBlockEntity.isMilk(new ItemStack(milk)), "The Milk Frother should take " + milk);
        }
        helper.succeed();
    }

    /**
     * Used water and milk buckets still come back empty, and a hopper underneath still takes them out.
     */
    @GameTest(template = EMPTY)
    public static void usedBucketsComeBackEmptyAndCanBeTakenOut(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get());
        helper.setBlock(MACHINE.west(), ModBlocks.MILK_FROTHER.get());
        CoffeeMachineBlockEntity machine = (CoffeeMachineBlockEntity) helper.getBlockEntity(MACHINE);
        machine.setItem(CoffeeMachineBlockEntity.WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(CoffeeMachineBlockEntity.MILK, new ItemStack(Items.MILK_BUCKET));
        machine.setItem(CoffeeMachineBlockEntity.FUEL, new ItemStack(Items.COAL));
        ItemStack grounds = new ItemStack(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), 3);
        machine.setItem(CoffeeMachineBlockEntity.GROUNDS, grounds);
        BlockPos at = helper.absolutePos(MACHINE);
        ServerLevel level = helper.getLevel();
        for (int tick = 0; tick < CoffeeMachineBlockEntity.brewingTime(); tick++) {
            CoffeeMachineBlockEntity.serverTick(level, at, level.getBlockState(at), machine);
        }
        helper.assertTrue(machine.getItem(CoffeeMachineBlockEntity.OUTPUT).is(ModItems.COFFEE.get()), "The machine should have brewed a cup");
        for (int slot : new int[]{CoffeeMachineBlockEntity.WATER, CoffeeMachineBlockEntity.MILK}) {
            ItemStack left = machine.getItem(slot);
            helper.assertTrue(left.is(Items.BUCKET), "Slot " + slot + " should hold an empty bucket, not " + left);
            helper.assertTrue(machine.canTakeItemThroughFace(slot, left, Direction.DOWN), "A hopper should be able to take the empty bucket out");
        }
        helper.succeed();
    }

    /**
     * What's in a tag; never empty, or the checks on it would prove nothing.
     */
    private static List<Item> items(GameTestHelper helper, TagKey<Item> tag) {
        List<Item> items = BuiltInRegistries.ITEM.getTag(tag).map(set -> set.stream().map(holder -> holder.value()).toList()).orElse(List.of());
        helper.assertFalse(items.isEmpty(), tag.location() + " shouldn't be empty");
        return items;
    }
}
