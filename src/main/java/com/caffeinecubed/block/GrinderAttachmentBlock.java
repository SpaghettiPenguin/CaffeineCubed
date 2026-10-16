package com.caffeinecubed.block;

import com.caffeinecubed.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An attachment for the Coffee Machine: a bean hopper with burrs that grinds roasted beans straight into the machine's
 * grounds slot, without fuel. It only goes on top of a Coffee Machine, and drops off if the machine is taken away.
 * The machine does the grinding (see CoffeeMachineBlockEntity); this block just has to be there.
 */
public class GrinderAttachmentBlock extends Block {
    public static final MapCodec<GrinderAttachmentBlock> CODEC = simpleCodec(GrinderAttachmentBlock::new);
    /**
     * The funnel, narrowing down to where it sits in the machine's lid.
     */
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(6, 0, 6, 10, 2, 10),
            Block.box(5, 2, 5, 11, 4, 11),
            Block.box(3, 4, 3, 13, 6, 13),
            Block.box(4, 4, 1, 12, 6, 3),
            Block.box(4, 4, 13, 12, 6, 15),
            Block.box(13, 4, 4, 15, 6, 12),
            Block.box(1, 4, 4, 3, 6, 12));

    public GrinderAttachmentBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ModBlocks.COFFEE_MACHINE.get());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        // Pops off, dropping itself, when the machine underneath is gone
        return direction == Direction.DOWN && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.caffeinecubed.grinder_attachment").withStyle(ChatFormatting.GRAY));
    }
}
