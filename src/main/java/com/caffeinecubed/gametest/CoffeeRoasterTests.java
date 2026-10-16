package com.caffeinecubed.gametest;

import static com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity.FUEL;
import static com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity.INPUT;
import static com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity.OUTPUT;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.CoffeeRoasterBlock;
import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class CoffeeRoasterTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos ROASTER = new BlockPos(1, 1, 1);
    // One bean takes 200 ticks, past the default limit of 100
    private static final int TIMEOUT = 300;

    @GameTest(template = EMPTY)
    public static void eachRoastComesOutOneDarkerAndKeepsItsQuality(GameTestHelper helper) {
        List<Item> roasts = List.of(ModItems.RAW_COFFEE_BEANS.get(), ModItems.LIGHT_ROAST_COFFEE_BEANS.get(),
                ModItems.MEDIUM_ROAST_COFFEE_BEANS.get(), ModItems.DARK_ROAST_COFFEE_BEANS.get());
        for (int i = 0; i < roasts.size() - 1; i++) {
            for (BeanQuality quality : BeanQuality.values()) {
                ItemStack roasted = CoffeeRoasterBlockEntity.roast(beans(roasts.get(i), quality, 1));
                helper.assertTrue(roasted.is(roasts.get(i + 1)) && roasted.getCount() == 1,
                        "Roasting " + roasts.get(i) + " should make " + roasts.get(i + 1) + " but made " + roasted);
                helper.assertTrue(BeanQuality.of(roasted) == quality, quality + " beans came out " + BeanQuality.of(roasted));
            }
        }
        helper.assertTrue(CoffeeRoasterBlockEntity.roast(beans(ModItems.DARK_ROAST_COFFEE_BEANS.get(), BeanQuality.GREAT, 1)).isEmpty(),
                "Dark roast is as dark as beans go");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void onlyBeansGoInAndOnlyFuelBurns(GameTestHelper helper) {
        CoffeeRoasterBlockEntity roaster = place(helper);
        for (Item roastable : List.of(ModItems.RAW_COFFEE_BEANS.get(), ModItems.LIGHT_ROAST_COFFEE_BEANS.get(), ModItems.MEDIUM_ROAST_COFFEE_BEANS.get())) {
            helper.assertTrue(roaster.canPlaceItem(INPUT, new ItemStack(roastable)), roastable + " should go in to be roasted");
        }
        helper.assertFalse(roaster.canPlaceItem(INPUT, new ItemStack(ModItems.DARK_ROAST_COFFEE_BEANS.get())), "Dark roast can't be roasted any more");
        helper.assertFalse(roaster.canPlaceItem(INPUT, new ItemStack(Items.WHEAT_SEEDS)), "Only coffee beans can be roasted");
        helper.assertTrue(roaster.canPlaceItem(FUEL, new ItemStack(Items.COAL)), "Coal is fuel");
        helper.assertFalse(roaster.canPlaceItem(FUEL, new ItemStack(ModItems.RAW_COFFEE_BEANS.get())), "Beans aren't fuel");
        helper.assertFalse(roaster.canPlaceItem(OUTPUT, new ItemStack(ModItems.LIGHT_ROAST_COFFEE_BEANS.get())), "Nothing goes into the output");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = TIMEOUT)
    public static void roastingABeanTakesAsLongAsSmelting(GameTestHelper helper) {
        CoffeeRoasterBlockEntity roaster = place(helper);
        roaster.setItem(INPUT, beans(ModItems.RAW_COFFEE_BEANS.get(), BeanQuality.GREAT, 2));
        roaster.setItem(FUEL, new ItemStack(Items.COAL));
        helper.runAtTickTime(5, () -> helper.assertBlockProperty(ROASTER, CoffeeRoasterBlock.LIT, true));
        helper.runAtTickTime(190, () -> helper.assertTrue(roaster.getItem(OUTPUT).isEmpty(), "The first bean shouldn't be done yet"));
        helper.runAtTickTime(210, () -> {
            ItemStack output = roaster.getItem(OUTPUT);
            helper.assertTrue(output.is(ModItems.LIGHT_ROAST_COFFEE_BEANS.get()) && output.getCount() == 1, "Expected 1 light roast but got " + output);
            helper.assertTrue(BeanQuality.of(output) == BeanQuality.GREAT, "Great beans should roast into great beans, not " + BeanQuality.of(output));
            helper.assertTrue(roaster.getItem(INPUT).getCount() == 1, "One raw bean should be left");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void oneCoalRoastsEightBeans(GameTestHelper helper) {
        CoffeeRoasterBlockEntity roaster = place(helper);
        roaster.setItem(INPUT, beans(ModItems.RAW_COFFEE_BEANS.get(), BeanQuality.DECENT, 9));
        roaster.setItem(FUEL, new ItemStack(Items.COAL));
        tick(helper, roaster, 8 * CoffeeRoasterBlockEntity.ROASTING_TIME + 100);
        ItemStack output = roaster.getItem(OUTPUT);
        helper.assertTrue(output.is(ModItems.LIGHT_ROAST_COFFEE_BEANS.get()) && output.getCount() == 8, "One coal should roast 8 beans, but made " + output);
        helper.assertTrue(roaster.getItem(INPUT).getCount() == 1 && roaster.getProgress() == 0, "The ninth bean should wait for more fuel");
        helper.assertTrue(roaster.getItem(FUEL).isEmpty() && roaster.getFuel() == 0, "The coal should be used up exactly");
        helper.assertBlockProperty(ROASTER, CoffeeRoasterBlock.LIT, false);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void fuelOnlyBurnsWhileRoasting(GameTestHelper helper) {
        CoffeeRoasterBlockEntity roaster = place(helper);
        roaster.setItem(FUEL, new ItemStack(Items.COAL));
        tick(helper, roaster, 300);
        helper.assertTrue(roaster.getItem(FUEL).getCount() == 1, "With no beans, the coal shouldn't burn");

        roaster.setItem(INPUT, beans(ModItems.RAW_COFFEE_BEANS.get(), BeanQuality.DECENT, 1));
        tick(helper, roaster, 500);
        helper.assertTrue(roaster.getItem(FUEL).isEmpty() && roaster.getFuel() == 1600 - CoffeeRoasterBlockEntity.ROASTING_TIME,
                "Roasting one bean should use 200 of the coal's 1,600, but " + roaster.getFuel() + " is left");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void waitsWhileTheOutputHoldsDifferentBeans(GameTestHelper helper) {
        CoffeeRoasterBlockEntity roaster = place(helper);
        roaster.setItem(OUTPUT, beans(ModItems.LIGHT_ROAST_COFFEE_BEANS.get(), BeanQuality.GREAT, 1));
        roaster.setItem(INPUT, beans(ModItems.RAW_COFFEE_BEANS.get(), BeanQuality.POOR, 1));
        roaster.setItem(FUEL, new ItemStack(Items.COAL));
        tick(helper, roaster, 300);
        helper.assertTrue(roaster.getItem(INPUT).getCount() == 1 && roaster.getProgress() == 0 && roaster.getItem(FUEL).getCount() == 1,
                "Poor beans shouldn't roast onto great ones");

        roaster.setItem(OUTPUT, ItemStack.EMPTY);
        tick(helper, roaster, 300);
        ItemStack output = roaster.getItem(OUTPUT);
        helper.assertTrue(output.is(ModItems.LIGHT_ROAST_COFFEE_BEANS.get()) && BeanQuality.of(output) == BeanQuality.POOR,
                "Once the output is cleared the poor beans should roast, but got " + output);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = TIMEOUT)
    public static void hoppersFeedAndEmptyIt(GameTestHelper helper) {
        CoffeeRoasterBlockEntity roaster = place(helper);
        // Beans from above, coal from the side, roasted beans out of the bottom
        helper.setBlock(ROASTER.above(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        helper.setBlock(ROASTER.west(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST));
        helper.setBlock(ROASTER.below(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST));
        hopper(helper, ROASTER.above()).setItem(0, beans(ModItems.MEDIUM_ROAST_COFFEE_BEANS.get(), BeanQuality.GOOD, 1));
        hopper(helper, ROASTER.west()).setItem(0, new ItemStack(Items.COAL));
        helper.runAtTickTime(250, () -> {
            ItemStack collected = hopper(helper, ROASTER.below()).getItem(0);
            helper.assertTrue(collected.is(ModItems.DARK_ROAST_COFFEE_BEANS.get()) && BeanQuality.of(collected) == BeanQuality.GOOD,
                    "The hopper below should have collected good dark roast, but has " + collected);
            helper.assertTrue(roaster.getItem(FUEL).isEmpty() && roaster.getFuel() > 0, "The coal should have gone in and be burning");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void shiftClickingSortsBeansAndFuel(GameTestHelper helper) {
        CoffeeRoasterBlockEntity roaster = place(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        AbstractContainerMenu menu = roaster.createMenu(0, player.getInventory(), player);
        // Slots 0-2 are the roaster's, then 27 inventory slots and the hotbar
        int firstInventorySlot = 3;
        menu.getSlot(firstInventorySlot).set(beans(ModItems.LIGHT_ROAST_COFFEE_BEANS.get(), BeanQuality.GOOD, 5));
        menu.getSlot(firstInventorySlot + 1).set(new ItemStack(Items.CHARCOAL, 3));
        menu.quickMoveStack(player, firstInventorySlot);
        menu.quickMoveStack(player, firstInventorySlot + 1);
        helper.assertTrue(roaster.getItem(INPUT).is(ModItems.LIGHT_ROAST_COFFEE_BEANS.get()) && roaster.getItem(INPUT).getCount() == 5,
                "Shift-clicking beans should put them in the bean slot");
        helper.assertTrue(roaster.getItem(FUEL).is(Items.CHARCOAL) && roaster.getItem(FUEL).getCount() == 3,
                "Shift-clicking charcoal should put it in the fuel slot");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void hitboxTurnsWithTheRoaster(GameTestHelper helper) {
        // The hopper sits on the front half of the roaster, so at its height the front is solid and the back is not
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            BlockState state = ModBlocks.COFFEE_ROASTER.get().defaultBlockState().setValue(CoffeeRoasterBlock.FACING, facing);
            VoxelShape shape = state.getShape(helper.getLevel(), BlockPos.ZERO, CollisionContext.empty());
            double hopperHeight = 13 / 16.0;
            double frontX = 0.5 + facing.getStepX() * 4 / 16.0;
            double frontZ = 0.5 + facing.getStepZ() * 4 / 16.0;
            double backX = 0.5 - facing.getStepX() * 4 / 16.0;
            double backZ = 0.5 - facing.getStepZ() * 4 / 16.0;
            helper.assertTrue(shape.toAabbs().stream().anyMatch(box -> box.contains(frontX, hopperHeight, frontZ)),
                    "Facing " + facing + ", the hopper should be at the front");
            helper.assertTrue(shape.toAabbs().stream().noneMatch(box -> box.contains(backX, hopperHeight, backZ)),
                    "Facing " + facing + ", there should be nothing above the back of the drum");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void roasterRecipe(GameTestHelper helper) {
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        CraftingInput grid = CraftingInput.of(3, 3, List.of(ItemStack.EMPTY, new ItemStack(Items.HOPPER), ItemStack.EMPTY,
                iron, new ItemStack(Items.FURNACE), iron, iron, ItemStack.EMPTY, iron));
        ItemStack made = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel())
                .map(recipe -> recipe.value().assemble(grid, helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(made.is(ModItems.COFFEE_ROASTER.get()), "Expected a Coffee Roaster but crafted " + made);
        helper.succeed();
    }

    private static CoffeeRoasterBlockEntity place(GameTestHelper helper) {
        helper.setBlock(ROASTER, ModBlocks.COFFEE_ROASTER.get());
        return helper.getBlockEntity(ROASTER);
    }

    private static ItemStack beans(Item roast, BeanQuality quality, int count) {
        ItemStack beans = new ItemStack(roast, count);
        beans.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        return beans;
    }

    private static HopperBlockEntity hopper(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockEntity(pos);
    }

    /**
     * Runs the roaster for this many ticks straight away, instead of waiting for the game to.
     */
    private static void tick(GameTestHelper helper, CoffeeRoasterBlockEntity roaster, int ticks) {
        BlockPos pos = helper.absolutePos(ROASTER);
        for (int i = 0; i < ticks; i++) {
            CoffeeRoasterBlockEntity.serverTick(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), roaster);
        }
    }
}
