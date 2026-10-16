package com.caffeinecubed.gametest;

import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.FUEL;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.GROUNDS;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.OUTPUT;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.WATER;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.CoffeeMachineBlock;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.Roast;
import com.caffeinecubed.item.CupOfCoffeeItem;
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
public final class CoffeeMachineTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(1, 1, 1);
    private static final int BREW = CoffeeMachineBlockEntity.BREWING_TIME;

    @GameTest(template = EMPTY, timeoutTicks = BREW + 100)
    public static void brewsACupInTwiceAFurnacesTime(GameTestHelper helper) {
        helper.assertTrue(BREW == 2 * 200, "Brewing should take twice as long as a furnace's 200 ticks");
        CoffeeMachineBlockEntity machine = place(helper);
        machine.setItem(WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(FUEL, new ItemStack(Items.COAL));
        machine.setItem(GROUNDS, grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GOOD, 4));
        helper.runAtTickTime(5, () -> helper.assertBlockProperty(MACHINE, CoffeeMachineBlock.LIT, true));
        helper.runAtTickTime(BREW - 10, () -> helper.assertTrue(machine.getItem(OUTPUT).isEmpty(), "The cup shouldn't be ready yet"));
        helper.runAtTickTime(BREW + 10, () -> {
            ItemStack coffee = machine.getItem(OUTPUT);
            helper.assertTrue(coffee.is(ModItems.COFFEE.get()) && coffee.getCount() == 1, "Expected a cup of coffee but got " + coffee);
            helper.assertTrue(BeanQuality.of(coffee) == BeanQuality.GOOD, "Good grounds should brew good coffee, not " + BeanQuality.of(coffee));
            helper.assertTrue(CupOfCoffeeItem.getRoast(coffee) == Roast.DARK, "Dark roast grounds should brew dark roast coffee");
            helper.assertTrue(machine.getItem(GROUNDS).getCount() == 1, "Three of the four grounds should be used");
            helper.assertTrue(machine.getItem(WATER).is(Items.BUCKET), "The water should be used up, leaving the empty bucket");
            // With the water gone it stops, and goes back to the idle model
            helper.assertBlockProperty(MACHINE, CoffeeMachineBlock.LIT, false);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void needsWaterFuelAndThreeGrounds(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper);
        machine.setItem(WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(FUEL, new ItemStack(Items.COAL));
        machine.setItem(GROUNDS, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.DECENT, 2));
        tick(helper, machine, 50);
        helper.assertTrue(machine.getProgress() == 0, "Two grounds aren't enough for a cup");

        machine.setItem(GROUNDS, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.DECENT, 3));
        machine.setItem(WATER, ItemStack.EMPTY);
        tick(helper, machine, 50);
        helper.assertTrue(machine.getProgress() == 0, "Without water nothing should brew");

        machine.setItem(WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(FUEL, ItemStack.EMPTY);
        tick(helper, machine, 50);
        helper.assertTrue(machine.getProgress() == 0, "Without fuel nothing should brew");

        machine.setItem(FUEL, new ItemStack(Items.OAK_PLANKS));
        tick(helper, machine, 50);
        helper.assertTrue(machine.getProgress() == 50, "With water, fuel and three grounds it should brew, but progress is " + machine.getProgress());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void oneCoalBrewsFourCups(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper);
        machine.setItem(FUEL, new ItemStack(Items.COAL));
        machine.setItem(GROUNDS, grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 15));
        for (int cup = 1; cup <= 4; cup++) {
            machine.setItem(WATER, new ItemStack(Items.WATER_BUCKET));
            tick(helper, machine, BREW);
            int made = machine.getItem(OUTPUT).getCount();
            helper.assertTrue(made == cup, "Expected " + cup + " cups but have " + made);
        }
        helper.assertTrue(machine.getItem(FUEL).isEmpty() && machine.getFuel() == 0, "The coal should be used up exactly by four cups");
        machine.setItem(WATER, new ItemStack(Items.WATER_BUCKET));
        tick(helper, machine, 50);
        helper.assertTrue(machine.getProgress() == 0, "A fifth cup needs more fuel");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void waitsWhileTheOutputHoldsDifferentCoffee(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper);
        ItemStack greatCoffee = new ItemStack(ModItems.COFFEE.get());
        greatCoffee.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.GREAT);
        machine.setItem(OUTPUT, greatCoffee);
        machine.setItem(WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(FUEL, new ItemStack(Items.COAL));
        machine.setItem(GROUNDS, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.POOR, 3));
        tick(helper, machine, BREW + 50);
        helper.assertTrue(machine.getProgress() == 0 && machine.getItem(GROUNDS).getCount() == 3, "Poor coffee shouldn't be poured onto great coffee");

        machine.setItem(OUTPUT, ItemStack.EMPTY);
        tick(helper, machine, BREW);
        ItemStack coffee = machine.getItem(OUTPUT);
        helper.assertTrue(coffee.is(ModItems.COFFEE.get()) && BeanQuality.of(coffee) == BeanQuality.POOR,
                "Once the cup is taken, the poor grounds should brew, but got " + coffee);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void eachSlotOnlyTakesWhatBelongsThere(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper);
        ItemStack water = new ItemStack(Items.WATER_BUCKET);
        ItemStack coal = new ItemStack(Items.COAL);
        ItemStack grounds = grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.DECENT, 1);
        helper.assertTrue(machine.canPlaceItem(WATER, water) && !machine.canPlaceItem(WATER, new ItemStack(Items.BUCKET)), "Only water buckets go in the water slot");
        helper.assertTrue(machine.canPlaceItem(FUEL, coal) && !machine.canPlaceItem(FUEL, water), "Only fuel goes in the fuel slot");
        helper.assertTrue(machine.canPlaceItem(GROUNDS, grounds), "Grounds go in the grounds slot");
        helper.assertFalse(machine.canPlaceItem(GROUNDS, new ItemStack(ModItems.DARK_ROAST_COFFEE_BEANS.get())), "Beans have to be ground first");
        helper.assertFalse(machine.canPlaceItem(OUTPUT, new ItemStack(ModItems.COFFEE.get())), "Nothing goes into the cup slot");
        helper.assertTrue(machine.canTakeItemThroughFace(WATER, new ItemStack(Items.BUCKET), Direction.DOWN), "Empty buckets come out of the bottom");
        helper.assertFalse(machine.canTakeItemThroughFace(WATER, water, Direction.DOWN), "Water buckets stay in until they're used");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = BREW + 200)
    public static void hoppersFeedAndEmptyIt(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper);
        // Grounds from above, water and coal from the side, coffee and the empty bucket out of the bottom
        helper.setBlock(MACHINE.above(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        helper.setBlock(MACHINE.west(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST));
        helper.setBlock(MACHINE.below(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST));
        hopper(helper, MACHINE.above()).setItem(0, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 3));
        hopper(helper, MACHINE.west()).setItem(0, new ItemStack(Items.WATER_BUCKET));
        hopper(helper, MACHINE.west()).setItem(1, new ItemStack(Items.COAL));
        helper.runAtTickTime(BREW + 150, () -> {
            HopperBlockEntity below = hopper(helper, MACHINE.below());
            boolean coffee = false;
            boolean bucket = false;
            for (int slot = 0; slot < below.getContainerSize(); slot++) {
                coffee |= below.getItem(slot).is(ModItems.COFFEE.get()) && BeanQuality.of(below.getItem(slot)) == BeanQuality.GREAT;
                bucket |= below.getItem(slot).is(Items.BUCKET);
            }
            helper.assertTrue(coffee, "The hopper below should have collected a cup of great coffee");
            helper.assertTrue(bucket, "The hopper below should have collected the empty bucket");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void shiftClickingSortsWaterGroundsAndFuel(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        AbstractContainerMenu menu = machine.createMenu(0, player.getInventory(), player);
        // The machine's slots come first, then 27 inventory slots and the 9 of the hotbar
        int firstInventorySlot = menu.slots.size() - 36;
        menu.getSlot(firstInventorySlot).set(new ItemStack(Items.WATER_BUCKET));
        menu.getSlot(firstInventorySlot + 1).set(grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), BeanQuality.DECENT, 6));
        menu.getSlot(firstInventorySlot + 2).set(new ItemStack(Items.CHARCOAL, 2));
        for (int i = 0; i < 3; i++) {
            menu.quickMoveStack(player, firstInventorySlot + i);
        }
        helper.assertTrue(machine.getItem(WATER).is(Items.WATER_BUCKET), "The water bucket should go in the water slot");
        helper.assertTrue(machine.getItem(GROUNDS).getCount() == 6, "The grounds should go in the grounds slot");
        helper.assertTrue(machine.getItem(FUEL).is(Items.CHARCOAL), "The charcoal should go in the fuel slot");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void hitboxTurnsWithTheMachine(GameTestHelper helper) {
        // Under the head at the front there is only the drip tray, while the back is solid all the way up
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            BlockState state = ModBlocks.COFFEE_MACHINE.get().defaultBlockState().setValue(CoffeeMachineBlock.FACING, facing);
            VoxelShape shape = state.getShape(helper.getLevel(), BlockPos.ZERO, CollisionContext.empty());
            double height = 6 / 16.0;
            double frontX = 0.5 + facing.getStepX() * 3 / 16.0;
            double frontZ = 0.5 + facing.getStepZ() * 3 / 16.0;
            double backX = 0.5 - facing.getStepX() * 4 / 16.0;
            double backZ = 0.5 - facing.getStepZ() * 4 / 16.0;
            helper.assertTrue(shape.toAabbs().stream().noneMatch(box -> box.contains(frontX, height, frontZ)),
                    "Facing " + facing + ", there should be room for a cup at the front");
            helper.assertTrue(shape.toAabbs().stream().anyMatch(box -> box.contains(backX, height, backZ)),
                    "Facing " + facing + ", the back should be solid");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void machineRecipe(GameTestHelper helper) {
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        ItemStack glass = new ItemStack(Items.GLASS_PANE);
        CraftingInput grid = CraftingInput.of(3, 3, List.of(iron, iron, iron, glass, new ItemStack(Items.PAPER), glass,
                iron, new ItemStack(Items.FURNACE), iron));
        ItemStack made = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel())
                .map(recipe -> recipe.value().assemble(grid, helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(made.is(ModItems.COFFEE_MACHINE.get()), "Expected a Coffee Machine but crafted " + made);
        helper.succeed();
    }

    private static CoffeeMachineBlockEntity place(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get());
        return helper.getBlockEntity(MACHINE);
    }

    private static ItemStack grounds(Item roast, BeanQuality quality, int count) {
        ItemStack grounds = new ItemStack(roast, count);
        grounds.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        return grounds;
    }

    private static HopperBlockEntity hopper(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockEntity(pos);
    }

    /**
     * Runs the machine for this many ticks straight away, instead of waiting for the game to.
     */
    private static void tick(GameTestHelper helper, CoffeeMachineBlockEntity machine, int ticks) {
        BlockPos pos = helper.absolutePos(MACHINE);
        for (int i = 0; i < ticks; i++) {
            CoffeeMachineBlockEntity.serverTick(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), machine);
        }
    }
}
