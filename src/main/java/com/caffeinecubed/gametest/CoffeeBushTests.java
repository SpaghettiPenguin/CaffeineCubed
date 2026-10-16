package com.caffeinecubed.gametest;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.CoffeeBushBlock;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModItems;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class CoffeeBushTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos SOIL = new BlockPos(1, 1, 1);
    private static final BlockPos BUSH = SOIL.above();
    private static final int ROLLS = 400;
    // Beans from grass are rarer, so it takes more breaking to tell their odds apart
    private static final int GRASS_ROLLS = 3200;

    @GameTest(template = EMPTY)
    public static void beansPlantAStarterBushOnGrass(GameTestHelper helper) {
        helper.setBlock(SOIL, Blocks.GRASS_BLOCK);
        plantBean(helper);
        helper.assertBlockState(BUSH, state -> state.is(ModBlocks.COFFEE_BUSH.get()) && state.getValue(CoffeeBushBlock.AGE) == 0,
                () -> "Planting a bean on grass should give a starter bush");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void beansDontPlantOnStone(GameTestHelper helper) {
        helper.setBlock(SOIL, Blocks.STONE);
        plantBean(helper);
        helper.assertBlockNotPresent(ModBlocks.COFFEE_BUSH.get(), BUSH);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bushGrowsUntilRipe(GameTestHelper helper) {
        BlockState bush = placeBush(helper, 0);
        for (int i = 0; i < 400 && !CoffeeBushBlock.isRipe(bush); i++) {
            bush.randomTick(helper.getLevel(), helper.absolutePos(BUSH), helper.getLevel().random);
            bush = helper.getBlockState(BUSH);
        }
        helper.assertTrue(CoffeeBushBlock.isRipe(bush), "The bush should have grown ripe, but is at age " + bush.getValue(CoffeeBushBlock.AGE));
        helper.assertFalse(bush.isRandomlyTicking(), "A ripe bush has nothing left to grow");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void boneMealGrowsOneStageAtATime(GameTestHelper helper) {
        placeBush(helper, 0);
        for (int age = 1; age <= CoffeeBushBlock.MAX_AGE; age++) {
            helper.assertTrue(BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), helper.getLevel(), helper.absolutePos(BUSH)),
                    "Bone meal should grow the bush");
            int expected = age;
            helper.assertBlockState(BUSH, state -> state.getValue(CoffeeBushBlock.AGE) == expected, () -> "Expected age " + expected);
        }
        helper.assertFalse(BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), helper.getLevel(), helper.absolutePos(BUSH)),
                "Bone meal should do nothing to a ripe bush");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void unripeBushCannotBePicked(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int age = 0; age < CoffeeBushBlock.MAX_AGE; age++) {
            BlockState bush = placeBush(helper, age);
            InteractionResult result = rightClick(helper, bush, player);
            helper.assertTrue(result == InteractionResult.PASS, "Right-clicking an age " + age + " bush should do nothing, but gave " + result);
            helper.assertTrue(helper.getBlockState(BUSH) == bush, "Right-clicking an age " + age + " bush changed it");
            helper.assertTrue(pickUpBeans(helper) == 0, "An age " + age + " bush should give no beans");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void ripeBushGivesOneToThreeBeansAndStays(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Set<Integer> amounts = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            BlockState bush = placeBush(helper, CoffeeBushBlock.MAX_AGE);
            rightClick(helper, bush, player);
            int beans = pickUpBeans(helper);
            helper.assertTrue(beans >= 1 && beans <= 3, "A ripe bush should give 1-3 beans, but gave " + beans);
            amounts.add(beans);
            helper.assertBlockState(BUSH, state -> state.is(ModBlocks.COFFEE_BUSH.get()) && state.getValue(CoffeeBushBlock.AGE) == CoffeeBushBlock.PICKED_AGE,
                    () -> "A picked bush should stay, back at age " + CoffeeBushBlock.PICKED_AGE);
        }
        helper.assertTrue(amounts.size() == 3, "Picking should give 1, 2 and 3 beans at different times, but only gave " + amounts);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void breakingDropsBeansOnlyWhenRipe(GameTestHelper helper) {
        for (int age = 0; age < CoffeeBushBlock.MAX_AGE; age++) {
            List<ItemStack> drops = Block.getDrops(placeBush(helper, age), helper.getLevel(), helper.absolutePos(BUSH), null);
            helper.assertTrue(drops.isEmpty(), "Breaking an age " + age + " bush should drop nothing, but dropped " + drops);
        }
        BlockState ripe = placeBush(helper, CoffeeBushBlock.MAX_AGE);
        for (int i = 0; i < ROLLS; i++) {
            List<ItemStack> drops = Block.getDrops(ripe, helper.getLevel(), helper.absolutePos(BUSH), null);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(ModItems.RAW_COFFEE_BEANS.get())
                    && drops.getFirst().getCount() >= 1 && drops.getFirst().getCount() <= 3, "Breaking a ripe bush should drop 1-3 beans, but dropped " + drops);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void jungleGrassSometimesDropsGreatBeans(GameTestHelper helper) {
        helper.setBlock(SOIL, Blocks.GRASS_BLOCK);
        helper.setBlock(BUSH, Blocks.SHORT_GRASS);

        BeanQualityTests.setBiome(helper, Biomes.PLAINS);
        helper.assertTrue(beansFromGrass(helper, ItemStack.EMPTY) == 0, "Grass outside a jungle should not drop coffee beans");

        BeanQualityTests.setBiome(helper, Biomes.JUNGLE);
        int byHand = beansFromGrass(helper, ItemStack.EMPTY);
        // One in thirty-two
        helper.assertTrue(byHand > GRASS_ROLLS / 64 && byHand < GRASS_ROLLS / 16,
                "Jungle grass should drop beans about 1 time in 32, but did " + byHand + " times in " + GRASS_ROLLS);
        helper.assertTrue(beansFromGrass(helper, new ItemStack(Items.SHEARS)) == 0, "Shearing jungle grass should not drop coffee beans");
        helper.succeed();
    }

    private static void plantBean(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack beans = new ItemStack(ModItems.RAW_COFFEE_BEANS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, beans);
        BlockPos soil = helper.absolutePos(SOIL);
        beans.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(soil).add(0, 0.5, 0), Direction.UP, soil, false)));
    }

    private static BlockState placeBush(GameTestHelper helper, int age) {
        helper.setBlock(SOIL, Blocks.GRASS_BLOCK);
        BlockState bush = ModBlocks.COFFEE_BUSH.get().defaultBlockState().setValue(CoffeeBushBlock.AGE, age);
        helper.setBlock(BUSH, bush);
        return bush;
    }

    private static InteractionResult rightClick(GameTestHelper helper, BlockState bush, Player player) {
        BlockPos pos = helper.absolutePos(BUSH);
        return bush.useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    /**
     * Counts the beans lying around the test area and clears them away.
     */
    private static int pickUpBeans(GameTestHelper helper) {
        int beans = 0;
        for (ItemEntity item : helper.getEntities(EntityType.ITEM)) {
            if (item.getItem().is(ModItems.RAW_COFFEE_BEANS.get())) {
                beans += item.getItem().getCount();
            }
            item.discard();
        }
        return beans;
    }

    private static int beansFromGrass(GameTestHelper helper, ItemStack tool) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockState grass = helper.getBlockState(BUSH);
        int beans = 0;
        for (int i = 0; i < GRASS_ROLLS; i++) {
            for (ItemStack drop : Block.getDrops(grass, helper.getLevel(), helper.absolutePos(BUSH), null, player, tool)) {
                if (drop.is(ModItems.RAW_COFFEE_BEANS.get())) {
                    // Only jungles drop beans, so they are always great
                    helper.assertTrue(BeanQuality.of(drop) == BeanQuality.GREAT, "Beans from jungle grass should be great, but are "
                            + BeanQuality.of(drop));
                    beans += drop.getCount();
                }
            }
        }
        return beans;
    }
}
