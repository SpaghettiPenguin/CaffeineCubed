package com.caffeinecubed.gametest;

import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.BEANS;
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
import com.caffeinecubed.menu.CoffeeMachineMenu;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
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
public final class SyrupCompactorTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(1, 1, 1);
    private static final int BREW = CoffeeMachineBlockEntity.BREWING_TIME;
    private static final int MINUTE = 60 * 20;

    @GameTest(template = EMPTY)
    public static void theCompactorOnlyGoesOnTheMachinesLeftSide(GameTestHelper helper) {
        for (Direction facing : List.of(Direction.NORTH, Direction.SOUTH)) {
            helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get().defaultBlockState().setValue(CoffeeMachineBlock.FACING, facing));
            // From the front, the machine's left is its own right
            BlockPos left = MACHINE.relative(facing.getClockWise());
            BlockPos right = MACHINE.relative(facing.getCounterClockWise());
            helper.setBlock(left, Blocks.AIR);
            helper.setBlock(right, Blocks.AIR);

            placeCompactor(helper, right);
            helper.assertBlockNotPresent(ModBlocks.SYRUP_COMPACTOR.get(), right);
            placeCompactor(helper, left);
            helper.assertBlockState(left, state -> state.is(ModBlocks.SYRUP_COMPACTOR.get())
                            && state.getValue(CoffeeMachineSideAttachmentBlock.FACING) == facing,
                    () -> "Facing " + facing + ", a compactor should go on the machine's left, facing the same way");
            helper.setBlock(left, Blocks.AIR);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theCompactorDropsOffWhenTheMachineGoes(GameTestHelper helper) {
        place(helper, false, true, false);
        helper.setBlock(MACHINE, Blocks.AIR);
        helper.runAfterDelay(1, () -> {
            helper.assertBlockNotPresent(ModBlocks.SYRUP_COMPACTOR.get(), compactorPos());
            helper.assertItemEntityPresent(ModItems.SYRUP_COMPACTOR.get());
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void eachIngredientMakesItsSyrupAndAddsAMinuteOfItsEffect(GameTestHelper helper) {
        Map<Item, Holder<MobEffect>> effects = Map.of(Items.PUFFERFISH, MobEffects.WATER_BREATHING,
                Items.FERMENTED_SPIDER_EYE, MobEffects.INVISIBILITY, Items.GOLDEN_CARROT, MobEffects.NIGHT_VISION,
                Items.RABBIT_FOOT, MobEffects.JUMP);
        CoffeeMachineBlockEntity machine = place(helper, false, true, false);
        for (Map.Entry<Item, Holder<MobEffect>> entry : effects.entrySet()) {
            load(machine);
            machine.setItem(OUTPUT, ItemStack.EMPTY);
            machine.setItem(SYRUP, new ItemStack(entry.getKey(), 2));
            tick(helper, machine, BREW);
            ItemStack coffee = machine.getItem(OUTPUT);
            Syrup syrup = CupOfCoffeeItem.getSyrup(coffee);
            helper.assertTrue(syrup != null && syrup.ingredient() == entry.getKey(), entry.getKey() + " should make its syrup, but the cup has " + syrup);
            helper.assertTrue(machine.getItem(SYRUP).getCount() == 1, "One ingredient should be used per cup");

            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            coffee.copy().finishUsingItem(helper.getLevel(), player);
            MobEffectInstance effect = player.getEffect(entry.getValue());
            helper.assertTrue(effect != null && effect.getAmplifier() == 0 && effect.getDuration() == MINUTE,
                    entry.getKey() + " syrup should give a minute of " + entry.getValue().getRegisteredName() + " I, but gave " + effect);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void syrupIsOptional(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false, true, false);
        load(machine);
        tick(helper, machine, BREW);
        ItemStack coffee = machine.getItem(OUTPUT);
        helper.assertTrue(coffee.is(ModItems.COFFEE.get()) && CupOfCoffeeItem.getSyrup(coffee) == null, "Without an ingredient the coffee should have no syrup");
        helper.assertFalse(machine.canPlaceItem(SYRUP, new ItemStack(Items.SUGAR)), "Only the four ingredients go in the syrup slot");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void withoutTheCompactorIngredientsAreLeftAlone(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false, false, false);
        helper.assertFalse(machine.canPlaceItem(SYRUP, new ItemStack(Items.GOLDEN_CARROT)), "Without a compactor the syrup slot takes nothing");
        load(machine);
        machine.setItem(SYRUP, new ItemStack(Items.GOLDEN_CARROT));
        tick(helper, machine, BREW);
        helper.assertTrue(CupOfCoffeeItem.getSyrup(machine.getItem(OUTPUT)) == null, "Without a compactor the coffee shouldn't get syrup");
        helper.assertTrue(machine.getItem(SYRUP).getCount() == 1, "The golden carrot should be left alone");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void allThreeAttachmentsTogether(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, true, true, true);
        machine.setItem(BEANS, beans(3));
        machine.setItem(WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(FUEL, new ItemStack(Items.COAL));
        machine.setItem(MILK, new ItemStack(Items.MILK_BUCKET));
        machine.setItem(SYRUP, new ItemStack(Items.RABBIT_FOOT));
        tick(helper, machine, 3 * CoffeeMachineBlockEntity.GRINDING_TIME + BREW);
        ItemStack coffee = machine.getItem(OUTPUT);
        helper.assertTrue(CupOfCoffeeItem.getRoast(coffee) == Roast.LIGHT && BeanQuality.of(coffee) == BeanQuality.GOOD
                        && CupOfCoffeeItem.hasMilk(coffee) && CupOfCoffeeItem.getSyrup(coffee) == Syrup.RABBIT_FOOT,
                "Beans, milk and a rabbit's foot should make a good light roast coffee with milk and rabbit's foot syrup, but made " + coffee);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        coffee.finishUsingItem(helper.getLevel(), player);
        for (Holder<MobEffect> effect : List.of(MobEffects.MOVEMENT_SPEED, MobEffects.SLOW_FALLING, MobEffects.REGENERATION, MobEffects.JUMP)) {
            helper.assertTrue(player.hasEffect(effect), "The cup should give " + effect.getRegisteredName());
        }
        helper.assertTrue(player.getActiveEffects().size() == 4, "Expected four effects, but got " + player.getActiveEffects());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aHopperOnTheCompactorFeedsItIngredients(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false, true, false);
        helper.setBlock(compactorPos().above(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        HopperBlockEntity hopper = helper.getBlockEntity(compactorPos().above());
        hopper.setItem(0, new ItemStack(Items.PUFFERFISH, 2));
        helper.runAtTickTime(30, () -> {
            helper.assertTrue(machine.getItem(SYRUP).is(Items.PUFFERFISH) && machine.getItem(SYRUP).getCount() == 2,
                    "The pufferfish should have gone through the compactor into the machine");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void theSyrupSlotOnlyShowsWithTheCompactor(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false, false, false);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        CoffeeMachineMenu menu = (CoffeeMachineMenu) machine.createMenu(0, player.getInventory(), player);
        helper.assertFalse(menu.showsCompactor(), "Without the compactor there should be no syrup slot");
        place(helper, true, true, true);
        helper.assertTrue(menu.showsCompactor() && menu.showsFrother() && menu.showsGrinder(), "With all three attachments all three slots should show");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void compactorRecipe(GameTestHelper helper) {
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        CraftingInput grid = CraftingInput.of(3, 3, List.of(ItemStack.EMPTY, new ItemStack(Items.PISTON), ItemStack.EMPTY,
                bottle, iron, bottle, iron, iron, iron));
        ItemStack made = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel())
                .map(recipe -> recipe.value().assemble(grid, helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(made.is(ModItems.SYRUP_COMPACTOR.get()), "Expected a Syrup Compactor but crafted " + made);
        helper.succeed();
    }

    /**
     * The spot on the left of a north-facing machine, as seen from the front: its east.
     */
    private static BlockPos compactorPos() {
        return MACHINE.east();
    }

    private static CoffeeMachineBlockEntity place(GameTestHelper helper, boolean grinder, boolean compactor, boolean frother) {
        helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get().defaultBlockState().setValue(CoffeeMachineBlock.FACING, Direction.NORTH));
        if (grinder) {
            helper.setBlock(MACHINE.above(), ModBlocks.GRINDER_ATTACHMENT.get());
        }
        if (compactor) {
            helper.setBlock(compactorPos(), ModBlocks.SYRUP_COMPACTOR.get().defaultBlockState()
                    .setValue(CoffeeMachineSideAttachmentBlock.FACING, Direction.NORTH));
        }
        if (frother) {
            helper.setBlock(MACHINE.west(), ModBlocks.MILK_FROTHER.get().defaultBlockState()
                    .setValue(CoffeeMachineSideAttachmentBlock.FACING, Direction.NORTH));
        }
        return helper.getBlockEntity(MACHINE);
    }

    /**
     * Places a compactor the way a player would, standing on the ground and clicking the top of the block below the spot.
     */
    private static void placeCompactor(GameTestHelper helper, BlockPos spot) {
        helper.setBlock(spot.below(), Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack compactor = new ItemStack(ModItems.SYRUP_COMPACTOR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, compactor);
        BlockPos ground = helper.absolutePos(spot.below());
        compactor.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(ground).add(0, 0.5, 0), Direction.UP, ground, false)));
    }

    private static void load(CoffeeMachineBlockEntity machine) {
        machine.setItem(WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(FUEL, new ItemStack(Items.COAL));
        ItemStack grounds = new ItemStack(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), 3);
        grounds.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.DECENT);
        machine.setItem(GROUNDS, grounds);
    }

    private static ItemStack beans(int count) {
        ItemStack beans = new ItemStack(ModItems.LIGHT_ROAST_COFFEE_BEANS.get(), count);
        beans.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.GOOD);
        return beans;
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
