package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.CoffeeBushBlock;
import com.caffeinecubed.block.CoffeeMachineBlock;
import com.caffeinecubed.block.CoffeeRoasterBlock;
import com.caffeinecubed.block.GrinderAttachmentBlock;
import com.caffeinecubed.block.MilkFrotherBlock;
import com.caffeinecubed.block.SyrupCompactorBlock;
import com.caffeinecubed.block.TripleAxisSiphonBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CaffeineCubed.MODID);

    // Behaves like a sweet berry bush: no collision, breaks instantly, the same rustling sounds
    public static final DeferredBlock<CoffeeBushBlock> COFFEE_BUSH = BLOCKS.register("coffee_bush",
            () -> new CoffeeBushBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH)));

    // As tough as a furnace and mined with a pickaxe, but made of metal and not a full block
    public static final DeferredBlock<CoffeeRoasterBlock> COFFEE_ROASTER = BLOCKS.register("coffee_roaster",
            () -> new CoffeeRoasterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .lightLevel(ModBlocks::glowWhileWorking)
                    .noOcclusion()));

    // Built like the roaster: as tough as a furnace, mined with a pickaxe, metal and not a full block
    public static final DeferredBlock<CoffeeMachineBlock> COFFEE_MACHINE = BLOCKS.register("coffee_machine",
            () -> new CoffeeMachineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .lightLevel(ModBlocks::glowWhileWorking)
                    .noOcclusion()));

    // Sits on top of a Coffee Machine: metal, mined with a pickaxe, lighter work to break than the machine
    public static final DeferredBlock<GrinderAttachmentBlock> GRINDER_ATTACHMENT = BLOCKS.register("grinder_attachment",
            () -> new GrinderAttachmentBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    // Sits on the right of a Coffee Machine: built like the machine
    public static final DeferredBlock<MilkFrotherBlock> MILK_FROTHER = BLOCKS.register("milk_frother",
            () -> new MilkFrotherBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    // Sits on the left of a Coffee Machine: built like the machine
    public static final DeferredBlock<SyrupCompactorBlock> SYRUP_COMPACTOR = BLOCKS.register("syrup_compactor",
            () -> new SyrupCompactorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    // Marble and gold: as tough as the machines, mined with a pickaxe, not a full block
    public static final DeferredBlock<TripleAxisSiphonBlock> TRIPLE_AXIS_SIPHON = BLOCKS.register("triple_axis_siphon",
            () -> new TripleAxisSiphonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .lightLevel(ModBlocks::glowWhileWorking)
                    .noOcclusion()));

    private ModBlocks() {
    }

    /**
     * The machines glow while they work, as brightly as a lit furnace.
     */
    private static int glowWhileWorking(BlockState state) {
        return state.getValue(BlockStateProperties.LIT) ? 13 : 0;
    }
}
