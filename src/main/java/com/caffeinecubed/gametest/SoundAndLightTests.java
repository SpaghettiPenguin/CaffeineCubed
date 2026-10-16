package com.caffeinecubed.gametest;

import static com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity.LEFT;
import static com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity.RIGHT;
import static com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity.TOP;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.TripleAxisSiphonBlock;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import com.caffeinecubed.registry.ModSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class SoundAndLightTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(1, 1, 1);
    /**
     * As bright as a lit furnace.
     */
    private static final int FURNACE_LIGHT = 13;

    @GameTest(template = EMPTY)
    public static void machinesGlowLikeAFurnaceOnlyWhileWorking(GameTestHelper helper) {
        for (Block machine : List.<Block>of(ModBlocks.COFFEE_ROASTER.get(), ModBlocks.COFFEE_MACHINE.get(), ModBlocks.TRIPLE_AXIS_SIPHON.get())) {
            BlockState idle = machine.defaultBlockState();
            helper.assertTrue(idle.getLightEmission() == 0, machine + " shouldn't glow while idle");
            helper.assertTrue(idle.setValue(BlockStateProperties.LIT, true).getLightEmission() == FURNACE_LIGHT,
                    machine + " should glow like a lit furnace while working");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theSiphonLightsUpWhileItWorks(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.TRIPLE_AXIS_SIPHON.get());
        TripleAxisSiphonBlockEntity siphon = helper.getBlockEntity(MACHINE);
        tick(helper, siphon, 5);
        helper.assertBlockProperty(MACHINE, TripleAxisSiphonBlock.LIT, false);
        siphon.setItem(LEFT, grounds(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get()));
        siphon.setItem(TOP, grounds(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get()));
        siphon.setItem(RIGHT, grounds(ModItems.DARK_ROAST_COFFEE_GROUNDS.get()));
        tick(helper, siphon, 5);
        helper.assertBlockProperty(MACHINE, TripleAxisSiphonBlock.LIT, true);
        helper.assertTrue(helper.getBlockState(MACHINE).getLightEmission() == FURNACE_LIGHT, "A working siphon should glow");
        tick(helper, siphon, TripleAxisSiphonBlockEntity.SIPHONING_TIME);
        helper.assertBlockProperty(MACHINE, TripleAxisSiphonBlock.LIT, false);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theCoffeeMachineHasItsOwnBrewingSound(GameTestHelper helper) {
        ResourceLocation id = BuiltInRegistries.SOUND_EVENT.getKey(ModSounds.COFFEE_MACHINE_BREWING.get());
        helper.assertTrue(ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "block.coffee_machine.brewing").equals(id),
                "The brewing sound should be registered, but is " + id);
        // The sound loops while the machine is lit, which it is exactly while brewing
        helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get());
        CoffeeMachineBlockEntity machine = helper.getBlockEntity(MACHINE);
        machine.setItem(CoffeeMachineBlockEntity.WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(CoffeeMachineBlockEntity.FUEL, new ItemStack(Items.COAL));
        machine.setItem(CoffeeMachineBlockEntity.GROUNDS, new ItemStack(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), 3));
        BlockPos pos = helper.absolutePos(MACHINE);
        CoffeeMachineBlockEntity.serverTick(helper.getLevel(), pos, helper.getBlockState(MACHINE), machine);
        helper.assertBlockProperty(MACHINE, BlockStateProperties.LIT, true);
        helper.succeed();
    }

    private static ItemStack grounds(Item item) {
        ItemStack grounds = new ItemStack(item);
        grounds.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.GOOD);
        return grounds;
    }

    private static void tick(GameTestHelper helper, TripleAxisSiphonBlockEntity siphon, int ticks) {
        BlockPos pos = helper.absolutePos(MACHINE);
        for (int i = 0; i < ticks; i++) {
            TripleAxisSiphonBlockEntity.serverTick(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), siphon);
        }
    }
}
