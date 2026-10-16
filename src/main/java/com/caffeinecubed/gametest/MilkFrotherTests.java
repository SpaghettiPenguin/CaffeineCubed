package com.caffeinecubed.gametest;

import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.FUEL;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.GROUNDS;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.MILK;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.OUTPUT;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.WATER;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.CoffeeMachineBlock;
import com.caffeinecubed.block.MilkFrotherBlock;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.item.CupOfCoffeeItem;
import com.caffeinecubed.menu.CoffeeMachineMenu;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
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
public final class MilkFrotherTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(1, 1, 1);
    private static final int BREW = CoffeeMachineBlockEntity.BREWING_TIME;

    @GameTest(template = EMPTY)
    public static void theFrotherOnlyGoesOnTheMachinesRightSide(GameTestHelper helper) {
        for (Direction facing : List.of(Direction.NORTH, Direction.SOUTH)) {
            helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get().defaultBlockState().setValue(CoffeeMachineBlock.FACING, facing));
            // From the front, the machine's right is its own left
            BlockPos right = MACHINE.relative(facing.getCounterClockWise());
            BlockPos left = MACHINE.relative(facing.getClockWise());
            helper.setBlock(right, Blocks.AIR);
            helper.setBlock(left, Blocks.AIR);

            placeFrother(helper, left);
            helper.assertBlockNotPresent(ModBlocks.MILK_FROTHER.get(), left);
            placeFrother(helper, right);
            helper.assertBlockState(right, state -> state.is(ModBlocks.MILK_FROTHER.get()) && state.getValue(MilkFrotherBlock.FACING) == facing,
                    () -> "Facing " + facing + ", a frother should go on the machine's right, facing the same way");
            helper.setBlock(right, Blocks.AIR);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theFrotherDropsOffWhenTheMachineGoes(GameTestHelper helper) {
        place(helper, true);
        helper.setBlock(MACHINE, Blocks.AIR);
        helper.runAfterDelay(1, () -> {
            helper.assertBlockNotPresent(ModBlocks.MILK_FROTHER.get(), frotherPos());
            helper.assertItemEntityPresent(ModItems.MILK_FROTHER.get());
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void milkIsOptional(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, true);
        load(machine, 6);
        tick(helper, machine, BREW);
        ItemStack plain = machine.getItem(OUTPUT);
        helper.assertTrue(plain.is(ModItems.COFFEE.get()) && !CupOfCoffeeItem.hasMilk(plain), "Without milk the cup should be plain coffee, but is " + plain);

        machine.setItem(OUTPUT, ItemStack.EMPTY);
        machine.setItem(WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(MILK, new ItemStack(Items.MILK_BUCKET));
        tick(helper, machine, BREW);
        ItemStack milky = machine.getItem(OUTPUT);
        helper.assertTrue(milky.is(ModItems.COFFEE.get()) && CupOfCoffeeItem.hasMilk(milky), "With milk the cup should have milk, but is " + milky);
        helper.assertTrue(BeanQuality.of(milky) == BeanQuality.GOOD, "Milk shouldn't change the quality");
        helper.assertTrue(machine.getItem(MILK).is(Items.BUCKET) && machine.getItem(WATER).is(Items.BUCKET),
                "Both the milk and the water should be used up, leaving their buckets");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void milkCanBeAddedWhileItBrews(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, true);
        load(machine, 3);
        tick(helper, machine, BREW / 2);
        machine.setItem(MILK, new ItemStack(Items.MILK_BUCKET));
        tick(helper, machine, BREW / 2);
        helper.assertTrue(CupOfCoffeeItem.hasMilk(machine.getItem(OUTPUT)), "Milk put in during brewing should go into that cup");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void withoutTheFrotherMilkIsLeftAlone(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false);
        helper.assertFalse(machine.canPlaceItem(MILK, new ItemStack(Items.MILK_BUCKET)), "Without a frother the milk slot takes nothing");
        load(machine, 3);
        machine.setItem(MILK, new ItemStack(Items.MILK_BUCKET));
        tick(helper, machine, BREW);
        helper.assertFalse(CupOfCoffeeItem.hasMilk(machine.getItem(OUTPUT)), "Without a frother the coffee shouldn't get milk");
        helper.assertTrue(machine.getItem(MILK).is(Items.MILK_BUCKET), "The milk should be left alone");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void coffeeWithMilkAddsThirtySecondsOfRegeneration(GameTestHelper helper) {
        ItemStack milky = CoffeeMachineBlockEntity.brew(helper.getLevel(), grounds(BeanQuality.DECENT, 3), false, true, null);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        milky.finishUsingItem(helper.getLevel(), player);
        MobEffectInstance regeneration = player.getEffect(MobEffects.REGENERATION);
        helper.assertTrue(regeneration != null && regeneration.getAmplifier() == 0 && regeneration.getDuration() == 30 * 20,
                "Coffee with milk should give 30 seconds of Regeneration I, but gave " + regeneration);
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED) && player.hasEffect(MobEffects.DIG_SPEED),
                "Milk should come on top of the usual Speed and roast effect");

        Player other = helper.makeMockPlayer(GameType.SURVIVAL);
        CoffeeMachineBlockEntity.brew(helper.getLevel(), grounds(BeanQuality.DECENT, 3), false, false, null).finishUsingItem(helper.getLevel(), other);
        helper.assertFalse(other.hasEffect(MobEffects.REGENERATION), "Coffee without milk shouldn't give Regeneration");
        helper.assertFalse(ItemStack.isSameItemSameComponents(milky, CoffeeMachineBlockEntity.brew(helper.getLevel(), grounds(BeanQuality.DECENT, 3), false, false, null)),
                "Coffee with and without milk shouldn't stack");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aHopperOnTheFrotherFeedsItMilk(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, true);
        helper.setBlock(frotherPos().above(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        HopperBlockEntity hopper = helper.getBlockEntity(frotherPos().above());
        hopper.setItem(0, new ItemStack(Items.MILK_BUCKET));
        helper.runAtTickTime(20, () -> {
            helper.assertTrue(machine.getItem(MILK).is(Items.MILK_BUCKET), "The milk should have gone through the frother into the machine");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void theMilkSlotOnlyShowsWithTheFrother(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        CoffeeMachineMenu menu = (CoffeeMachineMenu) machine.createMenu(0, player.getInventory(), player);
        helper.assertFalse(menu.showsFrother(), "Without the frother there should be no milk slot");
        helper.setBlock(frotherPos(), ModBlocks.MILK_FROTHER.get().defaultBlockState().setValue(MilkFrotherBlock.FACING, Direction.NORTH));
        helper.assertTrue(menu.hasFrother() && menu.showsFrother(), "With the frother the milk slot should appear");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void frotherRecipe(GameTestHelper helper) {
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        ItemStack glass = new ItemStack(Items.GLASS_PANE);
        CraftingInput grid = CraftingInput.of(3, 3, List.of(ItemStack.EMPTY, iron, ItemStack.EMPTY,
                glass, new ItemStack(Items.MILK_BUCKET), glass, iron, iron, iron));
        ItemStack made = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel())
                .map(recipe -> recipe.value().assemble(grid, helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(made.is(ModItems.MILK_FROTHER.get()), "Expected a Milk Frother but crafted " + made);
        helper.succeed();
    }

    /**
     * The spot on the right of a north-facing machine, as seen from the front: its west.
     */
    private static BlockPos frotherPos() {
        return MACHINE.west();
    }

    private static CoffeeMachineBlockEntity place(GameTestHelper helper, boolean withFrother) {
        helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get().defaultBlockState().setValue(CoffeeMachineBlock.FACING, Direction.NORTH));
        if (withFrother) {
            helper.setBlock(frotherPos(), ModBlocks.MILK_FROTHER.get().defaultBlockState().setValue(MilkFrotherBlock.FACING, Direction.NORTH));
        }
        return helper.getBlockEntity(MACHINE);
    }

    /**
     * Places a frother the way a player would, standing on the ground and clicking the top of the block below the spot.
     */
    private static void placeFrother(GameTestHelper helper, BlockPos spot) {
        helper.setBlock(spot.below(), Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack frother = new ItemStack(ModItems.MILK_FROTHER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, frother);
        BlockPos ground = helper.absolutePos(spot.below());
        frother.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(ground).add(0, 0.5, 0), Direction.UP, ground, false)));
    }

    private static void load(CoffeeMachineBlockEntity machine, int grounds) {
        machine.setItem(WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(FUEL, new ItemStack(Items.COAL));
        machine.setItem(GROUNDS, grounds(BeanQuality.GOOD, grounds));
    }

    private static ItemStack grounds(BeanQuality quality, int count) {
        ItemStack grounds = new ItemStack(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), count);
        grounds.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        return grounds;
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
