package com.caffeinecubed.block;

import com.caffeinecubed.registry.ModBlocks;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An attachment that goes on one particular side of a Coffee Machine, like the Milk Frother on its right and the Syrup
 * Compactor on its left. It can only be placed there, faces the same way as the machine, and drops off if the machine
 * is taken away. The machine does the work; the attachment just has to be there.
 */
public abstract class CoffeeMachineSideAttachmentBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    protected CoffeeMachineSideAttachmentBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /**
     * Which way the machine is from the attachment, when both face this way.
     */
    public abstract Direction towardsMachine(Direction facing);

    /**
     * The hitbox facing each way.
     */
    protected abstract Map<Direction, VoxelShape> shapes();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes().get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Look for a machine this spot is the right side of, and face the same way as it
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState neighbour = context.getLevel().getBlockState(context.getClickedPos().relative(side));
            if (neighbour.is(ModBlocks.COFFEE_MACHINE.get())) {
                Direction machineFacing = neighbour.getValue(CoffeeMachineBlock.FACING);
                if (towardsMachine(machineFacing) == side) {
                    return defaultBlockState().setValue(FACING, machineFacing);
                }
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockState machine = level.getBlockState(pos.relative(towardsMachine(facing)));
        return machine.is(ModBlocks.COFFEE_MACHINE.get()) && machine.getValue(CoffeeMachineBlock.FACING) == facing;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        // Pops off, dropping itself, when the machine beside it is gone
        return direction == towardsMachine(state.getValue(FACING)) && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
