package com.caffeinecubed.gametest;

import static com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity.LEFT;
import static com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity.OUTPUT;
import static com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity.RIGHT;
import static com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity.TOP;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.TripleAxisSiphonBlock;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class TripleAxisSiphonTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos SIPHON = new BlockPos(1, 1, 1);
    private static final int TIME = TripleAxisSiphonBlockEntity.SIPHONING_TIME;

    @GameTest(template = EMPTY)
    public static void threeRoastsMakeARainbowBlendOfTheirQuality(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        for (BeanQuality quality : BeanQuality.values()) {
            siphon.setItem(OUTPUT, ItemStack.EMPTY);
            siphon.setItem(LEFT, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), quality, 2));
            siphon.setItem(TOP, grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), quality, 2));
            siphon.setItem(RIGHT, grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), quality, 2));
            tick(helper, siphon, TIME - 1);
            helper.assertTrue(siphon.getItem(OUTPUT).isEmpty(), "A blend should take four times a furnace's time, not less");
            tick(helper, siphon, 1);
            ItemStack blend = siphon.getItem(OUTPUT);
            helper.assertTrue(blend.is(ModItems.RAINBOW_BLEND.get()) && blend.getCount() == 1 && BeanQuality.of(blend) == quality,
                    "Three " + quality + " roasts should make one " + quality + " Rainbow Blend, but made " + blend);
            for (int slot : new int[]{LEFT, TOP, RIGHT}) {
                helper.assertTrue(siphon.getItem(slot).getCount() == 1, "One of each roast should be used up");
            }
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theRoastsGoInAnyOrder(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        siphon.setItem(LEFT, grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GOOD, 1));
        siphon.setItem(TOP, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GOOD, 1));
        siphon.setItem(RIGHT, grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GOOD, 1));
        tick(helper, siphon, TIME);
        helper.assertTrue(siphon.getItem(OUTPUT).is(ModItems.RAINBOW_BLEND.get()), "The roasts should blend in any order");
        helper.assertTrue(siphon.getItem(LEFT).isEmpty() && siphon.getItem(TOP).isEmpty() && siphon.getItem(RIGHT).isEmpty(),
                "All three should be used up");
        tick(helper, siphon, TIME);
        helper.assertTrue(siphon.getItem(OUTPUT).getCount() == 1, "With the inputs used up nothing more should be made");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void eachSlotTakesADifferentRoastOfTheSameQuality(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        siphon.setItem(LEFT, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 1));
        helper.assertFalse(siphon.canPlaceItem(TOP, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 1)),
                "A second slot shouldn't take the same roast");
        helper.assertFalse(siphon.canPlaceItem(TOP, grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), BeanQuality.DECENT, 1)),
                "A slot shouldn't take grounds of a different quality");
        helper.assertTrue(siphon.canPlaceItem(TOP, grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 1)),
                "A slot should take another roast of the same quality");
        helper.assertTrue(siphon.canPlaceItem(LEFT, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 1)),
                "A slot should take more of what it already has");
        for (ItemStack other : List.of(new ItemStack(ModItems.MEDIUM_ROAST_COFFEE_BEANS.get()), new ItemStack(ModItems.RAINBOW_BLEND.get()),
                new ItemStack(Items.DIRT))) {
            helper.assertFalse(siphon.canPlaceItem(RIGHT, other), other + " shouldn't go in the siphon");
        }
        helper.assertFalse(siphon.canPlaceItem(OUTPUT, grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 1)),
                "Nothing goes into the output");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void mixedQualitiesDontBlend(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        // Set straight into the slots, past the checks, as if they got there some other way
        siphon.setItem(LEFT, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 1));
        siphon.setItem(TOP, grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), BeanQuality.POOR, 1));
        siphon.setItem(RIGHT, grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 1));
        tick(helper, siphon, TIME);
        helper.assertTrue(siphon.getItem(OUTPUT).isEmpty() && siphon.getProgress() == 0, "Mixed qualities shouldn't blend");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void takingAnInputOutStartsOver(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        fill(siphon, BeanQuality.DECENT, 1);
        tick(helper, siphon, TIME / 2);
        helper.assertTrue(siphon.getProgress() == TIME / 2, "The blend should be halfway");
        ItemStack top = siphon.removeItemNoUpdate(TOP);
        tick(helper, siphon, 1);
        helper.assertTrue(siphon.getProgress() == 0, "Taking an input out should start the blend over");
        siphon.setItem(TOP, top);
        tick(helper, siphon, TIME - 1);
        helper.assertTrue(siphon.getItem(OUTPUT).isEmpty(), "The blend should have started from the beginning");
        tick(helper, siphon, 1);
        helper.assertTrue(siphon.getItem(OUTPUT).is(ModItems.RAINBOW_BLEND.get()), "The blend should finish a full time after it started over");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void itWaitsForRoomInTheOutput(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        fill(siphon, BeanQuality.GREAT, 2);
        // A blend of another quality is in the way
        siphon.setItem(OUTPUT, grounds(ModItems.RAINBOW_BLEND.get(), BeanQuality.GOOD, 1));
        tick(helper, siphon, TIME);
        helper.assertTrue(siphon.getItem(OUTPUT).getCount() == 1 && siphon.getItem(LEFT).getCount() == 2,
                "Great grounds shouldn't blend onto a Good blend");
        siphon.setItem(OUTPUT, grounds(ModItems.RAINBOW_BLEND.get(), BeanQuality.GREAT, 1));
        tick(helper, siphon, TIME);
        helper.assertTrue(siphon.getItem(OUTPUT).getCount() == 2, "Great grounds should blend onto a Great blend");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = TIME + 300)
    public static void hoppersFeedEachArmAndTheTop(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        // Facing north, the siphon's left arm reaches east and its right arm west
        helper.setBlock(SIPHON.east(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.WEST));
        helper.setBlock(SIPHON.above(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        helper.setBlock(SIPHON.west(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST));
        helper.setBlock(SIPHON.below(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST));
        hopper(helper, SIPHON.east()).setItem(0, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GOOD, 1));
        hopper(helper, SIPHON.above()).setItem(0, grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GOOD, 1));
        hopper(helper, SIPHON.west()).setItem(0, grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GOOD, 1));
        helper.runAtTickTime(20, () -> {
            helper.assertTrue(siphon.getItem(LEFT).is(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get()), "The hopper on the left should fill the left slot");
            helper.assertTrue(siphon.getItem(TOP).is(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get()), "The hopper on top should fill the top slot");
            helper.assertTrue(siphon.getItem(RIGHT).is(ModItems.DARK_ROAST_COFFEE_GROUNDS.get()), "The hopper on the right should fill the right slot");
        });
        helper.runAtTickTime(TIME + 200, () -> {
            HopperBlockEntity below = hopper(helper, SIPHON.below());
            boolean blend = false;
            for (int slot = 0; slot < below.getContainerSize(); slot++) {
                blend |= below.getItem(slot).is(ModItems.RAINBOW_BLEND.get()) && BeanQuality.of(below.getItem(slot)) == BeanQuality.GOOD;
            }
            helper.assertTrue(blend, "The hopper below should have collected a Good Rainbow Blend");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void shiftClickingGivesEachRoastItsOwnSlot(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        AbstractContainerMenu menu = siphon.createMenu(0, player.getInventory(), player);
        // The siphon's four slots come first, then 27 inventory slots and the 9 of the hotbar
        int firstInventorySlot = menu.slots.size() - 36;
        menu.getSlot(firstInventorySlot).set(grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.POOR, 5));
        menu.getSlot(firstInventorySlot + 1).set(grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.POOR, 5));
        menu.getSlot(firstInventorySlot + 2).set(grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), BeanQuality.POOR, 5));
        menu.getSlot(firstInventorySlot + 3).set(grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.POOR, 4));
        menu.getSlot(firstInventorySlot + 4).set(grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 4));
        for (int i = 0; i < 5; i++) {
            menu.quickMoveStack(player, firstInventorySlot + i);
        }
        helper.assertTrue(siphon.getItem(LEFT).is(ModItems.DARK_ROAST_COFFEE_GROUNDS.get()) && siphon.getItem(LEFT).getCount() == 9,
                "Both stacks of dark roast should go in the first slot, but it has " + siphon.getItem(LEFT));
        helper.assertTrue(siphon.getItem(TOP).is(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get()) && siphon.getItem(TOP).getCount() == 5,
                "The light roast should go in the next slot, but it has " + siphon.getItem(TOP));
        helper.assertTrue(siphon.getItem(RIGHT).is(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get()), "The medium roast should go in the last slot");
        helper.assertTrue(menu.getSlot(firstInventorySlot + 4).getItem().getCount() == 4, "Great grounds shouldn't join Poor ones");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void itFacesWhoeverPlacesIt(GameTestHelper helper) {
        helper.setBlock(SIPHON.below(), Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        // Looking east, at the block's west side
        player.setYRot(-90);
        ItemStack siphon = new ItemStack(ModItems.TRIPLE_AXIS_SIPHON.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, siphon);
        BlockPos ground = helper.absolutePos(SIPHON.below());
        siphon.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(ground).add(0, 0.5, 0), Direction.UP, ground, false)));
        helper.assertBlockState(SIPHON, state -> state.is(ModBlocks.TRIPLE_AXIS_SIPHON.get())
                && state.getValue(TripleAxisSiphonBlock.FACING) == Direction.WEST, () -> "The siphon should face the player, west");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void siphonRecipe(GameTestHelper helper) {
        ItemStack gold = new ItemStack(Items.GOLD_INGOT);
        ItemStack quartz = new ItemStack(Items.QUARTZ_BLOCK);
        CraftingInput grid = CraftingInput.of(3, 3, List.of(ItemStack.EMPTY, gold, ItemStack.EMPTY,
                gold, new ItemStack(Items.NETHER_STAR), gold, quartz, quartz, quartz));
        ItemStack made = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel())
                .map(recipe -> recipe.value().assemble(grid, helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(made.is(ModItems.TRIPLE_AXIS_SIPHON.get()), "Expected a Triple Axis Siphon but crafted " + made);
        helper.succeed();
    }

    private static TripleAxisSiphonBlockEntity place(GameTestHelper helper) {
        helper.setBlock(SIPHON, ModBlocks.TRIPLE_AXIS_SIPHON.get().defaultBlockState().setValue(TripleAxisSiphonBlock.FACING, Direction.NORTH));
        return helper.getBlockEntity(SIPHON);
    }

    private static void fill(TripleAxisSiphonBlockEntity siphon, BeanQuality quality, int count) {
        siphon.setItem(LEFT, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), quality, count));
        siphon.setItem(TOP, grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), quality, count));
        siphon.setItem(RIGHT, grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), quality, count));
    }

    private static ItemStack grounds(Item item, BeanQuality quality, int count) {
        ItemStack grounds = new ItemStack(item, count);
        grounds.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        return grounds;
    }

    private static HopperBlockEntity hopper(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockEntity(pos);
    }

    /**
     * Runs the siphon for this many ticks straight away, instead of waiting for the game to.
     */
    private static void tick(GameTestHelper helper, TripleAxisSiphonBlockEntity siphon, int ticks) {
        BlockPos pos = helper.absolutePos(SIPHON);
        for (int i = 0; i < ticks; i++) {
            TripleAxisSiphonBlockEntity.serverTick(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), siphon);
        }
    }
}
