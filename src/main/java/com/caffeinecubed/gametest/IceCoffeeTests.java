package com.caffeinecubed.gametest;

import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.FUEL;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.GROUNDS;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.MILK;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.OUTPUT;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.SYRUP;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.WATER;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.CoffeeMachineBlock;
import com.caffeinecubed.block.CoffeeMachineSideAttachmentBlock;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.Roast;
import com.caffeinecubed.coffee.Syrup;
import com.caffeinecubed.item.CupOfCoffeeItem;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class IceCoffeeTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(1, 1, 1);
    private static final int BREW = CoffeeMachineBlockEntity.BREWING_TIME;
    private static final int MINUTE = 60 * 20;

    @GameTest(template = EMPTY)
    public static void iceInTheWaterSlotBrewsIceCoffee(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false, false);
        machine.setItem(WATER, new ItemStack(Items.ICE, 2));
        machine.setItem(FUEL, new ItemStack(Items.COAL));
        machine.setItem(GROUNDS, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 6));
        tick(helper, machine, BREW);
        ItemStack coffee = machine.getItem(OUTPUT);
        helper.assertTrue(coffee.is(ModItems.ICE_COFFEE.get()) && coffee.getCount() == 1, "Expected a cup of Ice Coffee but got " + coffee);
        helper.assertTrue(CupOfCoffeeItem.getRoast(coffee) == Roast.LIGHT && BeanQuality.of(coffee) == BeanQuality.GREAT,
                "The Ice Coffee should keep the grounds' roast and quality, but is " + coffee);
        helper.assertTrue(machine.getItem(WATER).is(Items.ICE) && machine.getItem(WATER).getCount() == 1,
                "One block of ice should be used per cup, leaving one, but left " + machine.getItem(WATER));

        tick(helper, machine, BREW);
        helper.assertTrue(machine.getItem(OUTPUT).getCount() == 2, "The second block of ice should brew a second cup");
        helper.assertTrue(machine.getItem(WATER).isEmpty(), "Both blocks of ice should be used up");
        tick(helper, machine, BREW);
        helper.assertTrue(machine.getItem(OUTPUT).getCount() == 2, "Without ice or water nothing more should brew");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void iceCoffeeGivesFireResistanceInsteadOfSpeed(GameTestHelper helper) {
        int[] minutes = {1, 2, 3, 4};
        for (BeanQuality quality : BeanQuality.values()) {
            for (Roast roast : Roast.values()) {
                ItemStack coffee = brew(ModItems.ICE_COFFEE.get(), quality, roast);
                Player player = helper.makeMockPlayer(GameType.SURVIVAL);
                coffee.finishUsingItem(helper.getLevel(), player);
                MobEffectInstance fireResistance = player.getEffect(MobEffects.FIRE_RESISTANCE);
                helper.assertTrue(fireResistance != null && fireResistance.getAmplifier() == 0
                                && fireResistance.getDuration() == minutes[quality.ordinal()] * MINUTE,
                        quality + " Ice Coffee should give " + minutes[quality.ordinal()] + " minutes of Fire Resistance I, but gave " + fireResistance);
                helper.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Ice Coffee shouldn't give Speed");
                MobEffectInstance roastEffect = player.getEffect(roastEffect(roast));
                helper.assertTrue(roastEffect != null && roastEffect.getAmplifier() == 0 && roastEffect.getDuration() == MINUTE,
                        roast + " Ice Coffee should give a minute of " + roastEffect(roast).getRegisteredName() + " I, but gave " + roastEffect);
                helper.assertTrue(player.getActiveEffects().size() == 2, "Expected two effects, but got " + player.getActiveEffects());
                helper.assertTrue(coffee.isEmpty(), "The cup should be used up");
            }
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void iceCoffeeTakesMilkAndSyrupToo(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, true, true);
        machine.setItem(WATER, new ItemStack(Items.ICE));
        machine.setItem(FUEL, new ItemStack(Items.COAL));
        machine.setItem(GROUNDS, grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GOOD, 3));
        machine.setItem(MILK, new ItemStack(Items.MILK_BUCKET));
        machine.setItem(SYRUP, new ItemStack(Items.PUFFERFISH));
        tick(helper, machine, BREW);
        ItemStack coffee = machine.getItem(OUTPUT);
        helper.assertTrue(coffee.is(ModItems.ICE_COFFEE.get()) && CupOfCoffeeItem.hasMilk(coffee) && CupOfCoffeeItem.getSyrup(coffee) == Syrup.PUFFERFISH,
                "Ice, milk and a pufferfish should make Ice Coffee with milk and pufferfish syrup, but made " + coffee);
        helper.assertTrue(machine.getItem(MILK).is(Items.BUCKET), "The milk's empty bucket should stay in its slot");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        coffee.finishUsingItem(helper.getLevel(), player);
        for (Holder<MobEffect> effect : List.of(MobEffects.FIRE_RESISTANCE, MobEffects.DAMAGE_BOOST, MobEffects.REGENERATION, MobEffects.WATER_BREATHING)) {
            helper.assertTrue(player.hasEffect(effect), "The cup should give " + effect.getRegisteredName());
        }
        helper.assertTrue(player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration() == 3 * MINUTE, "Good Ice Coffee should give 3 minutes of Fire Resistance");
        helper.assertTrue(player.getActiveEffects().size() == 4, "Expected four effects, but got " + player.getActiveEffects());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void iceCoffeeAndCoffeeAreKeptApart(GameTestHelper helper) {
        ItemStack ice = brew(ModItems.ICE_COFFEE.get(), BeanQuality.DECENT, Roast.MEDIUM);
        ItemStack hot = brew(ModItems.COFFEE.get(), BeanQuality.DECENT, Roast.MEDIUM);
        helper.assertFalse(ItemStack.isSameItemSameComponents(ice, hot), "Ice Coffee shouldn't stack with Coffee");
        helper.assertTrue(ice.getMaxStackSize() == hot.getMaxStackSize(), "Ice Coffee should stack as far as Coffee");

        // A cup of hot coffee waiting in the output stops ice coffee from brewing, until it is taken out
        CoffeeMachineBlockEntity machine = place(helper, false, false);
        machine.setItem(WATER, new ItemStack(Items.ICE));
        machine.setItem(FUEL, new ItemStack(Items.COAL));
        machine.setItem(GROUNDS, grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), BeanQuality.DECENT, 3));
        machine.setItem(OUTPUT, hot.copy());
        tick(helper, machine, BREW);
        helper.assertTrue(machine.getItem(OUTPUT).is(ModItems.COFFEE.get()) && machine.getItem(OUTPUT).getCount() == 1,
                "Ice Coffee shouldn't be brewed onto a cup of Coffee");
        helper.assertTrue(machine.getItem(WATER).is(Items.ICE), "The ice should wait");
        machine.setItem(OUTPUT, ItemStack.EMPTY);
        tick(helper, machine, BREW);
        helper.assertTrue(machine.getItem(OUTPUT).is(ModItems.ICE_COFFEE.get()), "With the output empty the Ice Coffee should brew");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void onlyIceGoesInTheWaterSlot(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false, false);
        helper.assertTrue(machine.canPlaceItem(WATER, new ItemStack(Items.ICE)), "Ice should go in the water slot");
        helper.assertTrue(machine.canPlaceItem(WATER, new ItemStack(Items.WATER_BUCKET)), "Water buckets should still go in the water slot");
        for (Item other : List.of(Items.PACKED_ICE, Items.BLUE_ICE, Items.SNOW_BLOCK, Items.SNOWBALL)) {
            helper.assertFalse(machine.canPlaceItem(WATER, new ItemStack(other)), other + " shouldn't go in the water slot");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void shiftClickingPutsIceInTheWaterSlot(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false, false);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        AbstractContainerMenu menu = machine.createMenu(0, player.getInventory(), player);
        int firstInventorySlot = menu.slots.size() - 36;
        menu.getSlot(firstInventorySlot).set(new ItemStack(Items.ICE, 10));
        menu.quickMoveStack(player, firstInventorySlot);
        helper.assertTrue(machine.getItem(WATER).is(Items.ICE) && machine.getItem(WATER).getCount() == 10, "The ice should go in the water slot");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 2 * BREW + 200)
    public static void hoppersFeedIceAndTakeOutOnlyTheCoffee(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false, false);
        helper.setBlock(MACHINE.west(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST));
        helper.setBlock(MACHINE.below(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST));
        HopperBlockEntity side = helper.getBlockEntity(MACHINE.west());
        side.setItem(0, new ItemStack(Items.ICE, 3));
        side.setItem(1, new ItemStack(Items.COAL));
        machine.setItem(GROUNDS, grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.POOR, 3));
        helper.runAtTickTime(BREW + 150, () -> {
            HopperBlockEntity below = helper.getBlockEntity(MACHINE.below());
            boolean coffee = false;
            boolean ice = false;
            for (int slot = 0; slot < below.getContainerSize(); slot++) {
                coffee |= below.getItem(slot).is(ModItems.ICE_COFFEE.get());
                ice |= below.getItem(slot).is(Items.ICE);
            }
            helper.assertTrue(coffee, "The hopper below should have collected the Ice Coffee");
            helper.assertFalse(ice, "The ice should stay in the machine");
            helper.assertTrue(machine.getItem(WATER).is(Items.ICE) && machine.getItem(WATER).getCount() == 2,
                    "The ice from the side hopper should be in the water slot, one block used, but it has " + machine.getItem(WATER));
            helper.succeed();
        });
    }

    private static CoffeeMachineBlockEntity place(GameTestHelper helper, boolean frother, boolean compactor) {
        helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get().defaultBlockState().setValue(CoffeeMachineBlock.FACING, Direction.NORTH));
        // From the front of a north-facing machine, its right is west and its left east
        if (frother) {
            helper.setBlock(MACHINE.west(), ModBlocks.MILK_FROTHER.get().defaultBlockState()
                    .setValue(CoffeeMachineSideAttachmentBlock.FACING, Direction.NORTH));
        }
        if (compactor) {
            helper.setBlock(MACHINE.east(), ModBlocks.SYRUP_COMPACTOR.get().defaultBlockState()
                    .setValue(CoffeeMachineSideAttachmentBlock.FACING, Direction.NORTH));
        }
        return helper.getBlockEntity(MACHINE);
    }

    private static ItemStack grounds(Item roast, BeanQuality quality, int count) {
        ItemStack grounds = new ItemStack(roast, count);
        grounds.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        return grounds;
    }

    private static ItemStack brew(Item cup, BeanQuality quality, Roast roast) {
        ItemStack coffee = new ItemStack(cup);
        coffee.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        coffee.set(ModDataComponents.ROAST.get(), roast);
        return coffee;
    }

    private static Holder<MobEffect> roastEffect(Roast roast) {
        return switch (roast) {
            case LIGHT -> MobEffects.SLOW_FALLING;
            case MEDIUM -> MobEffects.DIG_SPEED;
            case DARK -> MobEffects.DAMAGE_BOOST;
        };
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
