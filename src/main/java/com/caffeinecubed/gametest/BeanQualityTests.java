package com.caffeinecubed.gametest;

import static com.caffeinecubed.coffee.BeanQuality.DECENT;
import static com.caffeinecubed.coffee.BeanQuality.GOOD;
import static com.caffeinecubed.coffee.BeanQuality.GREAT;
import static com.caffeinecubed.coffee.BeanQuality.POOR;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.CoffeeBushBlock;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
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
public final class BeanQualityTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos SOIL = new BlockPos(1, 1, 1);
    private static final BlockPos BUSH = SOIL.above();

    /**
     * Every vanilla Overworld biome and the coffee it grows.
     */
    private static final Map<ResourceKey<Biome>, BeanQuality> OVERWORLD = Map.ofEntries(
            // Jungles
            Map.entry(Biomes.JUNGLE, GREAT), Map.entry(Biomes.SPARSE_JUNGLE, GREAT), Map.entry(Biomes.BAMBOO_JUNGLE, GREAT),
            // Savannas
            Map.entry(Biomes.SAVANNA, GOOD), Map.entry(Biomes.SAVANNA_PLATEAU, GOOD), Map.entry(Biomes.WINDSWEPT_SAVANNA, GOOD),
            // Cold: taigas, snowy and icy biomes, cold mountains and seas
            Map.entry(Biomes.TAIGA, POOR), Map.entry(Biomes.SNOWY_TAIGA, POOR), Map.entry(Biomes.OLD_GROWTH_PINE_TAIGA, POOR),
            Map.entry(Biomes.OLD_GROWTH_SPRUCE_TAIGA, POOR), Map.entry(Biomes.SNOWY_PLAINS, POOR), Map.entry(Biomes.ICE_SPIKES, POOR),
            Map.entry(Biomes.SNOWY_BEACH, POOR), Map.entry(Biomes.GROVE, POOR), Map.entry(Biomes.SNOWY_SLOPES, POOR),
            Map.entry(Biomes.JAGGED_PEAKS, POOR), Map.entry(Biomes.FROZEN_PEAKS, POOR), Map.entry(Biomes.WINDSWEPT_HILLS, POOR),
            Map.entry(Biomes.WINDSWEPT_GRAVELLY_HILLS, POOR), Map.entry(Biomes.WINDSWEPT_FOREST, POOR), Map.entry(Biomes.STONY_SHORE, POOR),
            Map.entry(Biomes.FROZEN_RIVER, POOR), Map.entry(Biomes.FROZEN_OCEAN, POOR), Map.entry(Biomes.DEEP_FROZEN_OCEAN, POOR),
            Map.entry(Biomes.COLD_OCEAN, POOR), Map.entry(Biomes.DEEP_COLD_OCEAN, POOR),
            // Everything else is decent
            Map.entry(Biomes.PLAINS, DECENT), Map.entry(Biomes.SUNFLOWER_PLAINS, DECENT), Map.entry(Biomes.FOREST, DECENT),
            Map.entry(Biomes.FLOWER_FOREST, DECENT), Map.entry(Biomes.BIRCH_FOREST, DECENT), Map.entry(Biomes.OLD_GROWTH_BIRCH_FOREST, DECENT),
            Map.entry(Biomes.DARK_FOREST, DECENT), Map.entry(Biomes.MEADOW, DECENT), Map.entry(Biomes.CHERRY_GROVE, DECENT),
            Map.entry(Biomes.STONY_PEAKS, DECENT), Map.entry(Biomes.SWAMP, DECENT), Map.entry(Biomes.MANGROVE_SWAMP, DECENT),
            Map.entry(Biomes.DESERT, DECENT), Map.entry(Biomes.BADLANDS, DECENT), Map.entry(Biomes.WOODED_BADLANDS, DECENT),
            Map.entry(Biomes.ERODED_BADLANDS, DECENT), Map.entry(Biomes.MUSHROOM_FIELDS, DECENT), Map.entry(Biomes.BEACH, DECENT),
            Map.entry(Biomes.RIVER, DECENT), Map.entry(Biomes.OCEAN, DECENT), Map.entry(Biomes.DEEP_OCEAN, DECENT),
            Map.entry(Biomes.LUKEWARM_OCEAN, DECENT), Map.entry(Biomes.DEEP_LUKEWARM_OCEAN, DECENT), Map.entry(Biomes.WARM_OCEAN, DECENT),
            Map.entry(Biomes.DRIPSTONE_CAVES, DECENT), Map.entry(Biomes.LUSH_CAVES, DECENT), Map.entry(Biomes.DEEP_DARK, DECENT));

    @GameTest(template = EMPTY)
    public static void everyOverworldBiomeGrowsTheRightQuality(GameTestHelper helper) {
        Registry<Biome> biomes = biomes(helper);
        for (Map.Entry<ResourceKey<Biome>, BeanQuality> entry : OVERWORLD.entrySet()) {
            BeanQuality grown = BeanQuality.of(biomes.getHolderOrThrow(entry.getKey()));
            helper.assertTrue(grown == entry.getValue(),
                    entry.getKey().location() + " should grow " + entry.getValue() + " coffee, but grows " + grown);
        }
        // Make sure the list above doesn't miss a vanilla Overworld biome
        for (Holder<Biome> biome : biomes.getTagOrEmpty(BiomeTags.IS_OVERWORLD)) {
            ResourceKey<Biome> key = biome.unwrapKey().orElseThrow();
            helper.assertTrue(!key.location().getNamespace().equals("minecraft") || OVERWORLD.containsKey(key),
                    "The test doesn't say what " + key.location() + " should grow");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void pickedBeansGetTheBiomesQuality(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Map<ResourceKey<Biome>, BeanQuality> spots = Map.of(Biomes.JUNGLE, GREAT, Biomes.SAVANNA, GOOD,
                Biomes.FOREST, DECENT, Biomes.SNOWY_TAIGA, POOR);
        for (Map.Entry<ResourceKey<Biome>, BeanQuality> spot : spots.entrySet()) {
            setBiome(helper, spot.getKey());
            BlockState bush = placeRipeBush(helper);
            BlockPos pos = helper.absolutePos(BUSH);
            bush.useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            List<ItemEntity> dropped = helper.getEntities(EntityType.ITEM);
            helper.assertTrue(!dropped.isEmpty(), "Picking the bush in " + spot.getKey().location() + " gave no beans");
            for (ItemEntity item : dropped) {
                assertQuality(helper, item.getItem(), spot.getValue(), "Beans picked in " + spot.getKey().location());
                item.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void brokenBushDropsBeansOfTheBiomesQuality(GameTestHelper helper) {
        setBiome(helper, Biomes.SAVANNA);
        List<ItemStack> drops = Block.getDrops(placeRipeBush(helper), helper.getLevel(), helper.absolutePos(BUSH), null);
        helper.assertTrue(drops.size() == 1, "Expected one stack of beans but got " + drops);
        assertQuality(helper, drops.getFirst(), GOOD, "Beans from a bush broken in a savanna");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void pickBlockShowsTheQualityToExpect(GameTestHelper helper) {
        setBiome(helper, Biomes.ICE_SPIKES);
        BlockState bush = placeRipeBush(helper);
        ItemStack picked = bush.getBlock().getCloneItemStack(helper.getLevel(), helper.absolutePos(BUSH), bush);
        assertQuality(helper, picked, POOR, "Pick block on a bush in ice spikes");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void beansAreDecentUnlessToldOtherwise(GameTestHelper helper) {
        ItemStack plain = new ItemStack(ModItems.RAW_COFFEE_BEANS.get());
        assertQuality(helper, plain, DECENT, "New beans");
        ItemStack decent = plain.copy();
        decent.set(ModDataComponents.BEAN_QUALITY.get(), DECENT);
        helper.assertTrue(ItemStack.isSameItemSameComponents(plain, decent), "Decent beans from a bush should stack with plain beans");
        ItemStack great = plain.copy();
        great.set(ModDataComponents.BEAN_QUALITY.get(), GREAT);
        helper.assertFalse(ItemStack.isSameItemSameComponents(plain, great), "Great beans should not stack with decent ones");
        helper.succeed();
    }

    /**
     * Turns the area around the bush into this biome. Biomes are stored in 4x4x4 cells and a block can take its biome
     * from a neighbouring cell, so this covers 2 blocks either side, which stays clear of the tests next door.
     */
    static void setBiome(GameTestHelper helper, ResourceKey<Biome> biome) {
        BlockPos bush = helper.absolutePos(BUSH);
        FillBiomeCommand.fill(helper.getLevel(), bush.offset(-2, -2, -2), bush.offset(2, 2, 2), biomes(helper).getHolderOrThrow(biome));
        helper.assertTrue(helper.getLevel().getBiome(bush).is(biome), "Could not turn the test area into " + biome.location());
    }

    private static Registry<Biome> biomes(GameTestHelper helper) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
    }

    private static BlockState placeRipeBush(GameTestHelper helper) {
        helper.setBlock(SOIL, Blocks.GRASS_BLOCK);
        BlockState bush = ModBlocks.COFFEE_BUSH.get().defaultBlockState().setValue(CoffeeBushBlock.AGE, CoffeeBushBlock.MAX_AGE);
        helper.setBlock(BUSH, bush);
        return bush;
    }

    private static void assertQuality(GameTestHelper helper, ItemStack beans, BeanQuality expected, String what) {
        helper.assertTrue(beans.is(ModItems.RAW_COFFEE_BEANS.get()), what + " should be Raw Coffee Beans, but are " + beans);
        BeanQuality quality = BeanQuality.of(beans);
        helper.assertTrue(quality == expected, what + " should be " + expected + " but are " + quality);
    }
}
