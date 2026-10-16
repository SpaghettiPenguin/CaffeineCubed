package com.caffeinecubed.gametest;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.CaffeineCubedConfig;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.CoffeeEffects;
import com.caffeinecubed.item.CoffeeGrinderItem;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The config's defaults are the mod's own numbers, and changing a setting changes the game. Each test puts back what it
 * changed before it ends; it all happens within one tick, so no other test sees it.
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class ConfigTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(1, 1, 1);

    @GameTest(template = EMPTY)
    public static void theDefaultsAreTheModsOwnNumbers(GameTestHelper helper) {
        helper.assertTrue(CaffeineCubedConfig.SPEC.isLoaded(), "The config should be loaded with the world");
        helper.assertTrue(CoffeeRoasterBlockEntity.roastingTime() == CoffeeRoasterBlockEntity.ROASTING_TIME
                && CoffeeGrinderItem.grindTime() == CoffeeGrinderItem.GRIND_TIME
                && CoffeeMachineBlockEntity.brewingTime() == CoffeeMachineBlockEntity.BREWING_TIME
                && CoffeeMachineBlockEntity.grindingTime() == CoffeeMachineBlockEntity.GRINDING_TIME
                && TripleAxisSiphonBlockEntity.siphoningTime() == TripleAxisSiphonBlockEntity.SIPHONING_TIME,
                "The machines' default times should be 200, 20, 400, 100 and 800 ticks");
        int[] coffee = {60, 120, 180, 240};
        int[] gods = {30, 60, 90, 120};
        for (BeanQuality quality : BeanQuality.values()) {
            helper.assertTrue(CoffeeEffects.baseEffectSeconds(quality) == coffee[quality.ordinal()]
                    && CoffeeEffects.godsCoffeeSeconds(quality) == gods[quality.ordinal()], "Wrong default durations for " + quality);
        }
        helper.assertTrue(CaffeineCubedConfig.get(CaffeineCubedConfig.JUNGLE_GRASS_BEAN_CHANCE) == 1 / 32.0, "Beans should drop 1 time in 32");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aShorterBrewingTimeBrewsSooner(GameTestHelper helper) {
        changed(CaffeineCubedConfig.BREWING_TIME, 100, () -> {
            helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get());
            CoffeeMachineBlockEntity machine = helper.getBlockEntity(MACHINE);
            machine.setItem(CoffeeMachineBlockEntity.WATER, new ItemStack(Items.WATER_BUCKET));
            machine.setItem(CoffeeMachineBlockEntity.FUEL, new ItemStack(Items.COAL));
            machine.setItem(CoffeeMachineBlockEntity.GROUNDS, new ItemStack(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), 3));
            BlockPos pos = helper.absolutePos(MACHINE);
            for (int i = 0; i < 100; i++) {
                CoffeeMachineBlockEntity.serverTick(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), machine);
            }
            helper.assertTrue(machine.getItem(CoffeeMachineBlockEntity.OUTPUT).is(ModItems.COFFEE.get()),
                    "With a brewing time of 100 ticks, a cup should be done after 100 ticks");
        });
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void longerEffectsLastLonger(GameTestHelper helper) {
        changed(CaffeineCubedConfig.BASE_EFFECT_SECONDS_GREAT, 600, () -> {
            ItemStack coffee = new ItemStack(ModItems.COFFEE.get());
            coffee.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.GREAT);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            coffee.finishUsingItem(helper.getLevel(), player);
            MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
            helper.assertTrue(speed != null && speed.getDuration() == 600 * 20, "Great coffee should now give 10 minutes of Speed, but gave " + speed);
        });
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theBeanChanceDecidesJungleGrassDrops(GameTestHelper helper) {
        helper.setBlock(MACHINE.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(MACHINE, Blocks.SHORT_GRASS);
        BlockPos pos = helper.absolutePos(MACHINE);
        FillBiomeCommand.fill(helper.getLevel(), pos.offset(-2, -2, -2), pos.offset(2, 2, 2),
                helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.JUNGLE));
        changed(CaffeineCubedConfig.JUNGLE_GRASS_BEAN_CHANCE, 1.0, () -> helper.assertTrue(beansFromGrass(helper) == 50,
                "With a chance of 1, all 50 grass breaks should drop a bean"));
        changed(CaffeineCubedConfig.JUNGLE_GRASS_BEAN_CHANCE, 0.0, () -> helper.assertTrue(beansFromGrass(helper) == 0,
                "With a chance of 0, no grass should drop a bean"));
        helper.succeed();
    }

    private static int beansFromGrass(GameTestHelper helper) {
        BlockState grass = helper.getBlockState(MACHINE);
        int beans = 0;
        for (int i = 0; i < 50; i++) {
            for (ItemStack drop : Block.getDrops(grass, helper.getLevel(), helper.absolutePos(MACHINE), null)) {
                if (drop.is(ModItems.RAW_COFFEE_BEANS.get())) {
                    beans++;
                }
            }
        }
        return beans;
    }

    /**
     * Runs the check with a setting changed, then puts the setting back.
     */
    private static <T> void changed(ModConfigSpec.ConfigValue<T> setting, T value, Runnable check) {
        T before = setting.get();
        setting.set(value);
        try {
            check.run();
        } finally {
            setting.set(before);
        }
    }
}
