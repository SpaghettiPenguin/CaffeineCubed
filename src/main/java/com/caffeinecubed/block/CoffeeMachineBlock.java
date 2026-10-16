package com.caffeinecubed.block;

import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Brews coffee from water, coffee grounds and furnace fuel. Right-click to open it. It has two models: idle, and on
 * while it brews, with a cup filling under the spout. While it brews it glows like a lit furnace, steams and plays its
 * brewing sound. Both are drawn with the front to the north, which the blockstate
 * turns to face the player who placed it.
 */
public class CoffeeMachineBlock extends BaseEntityBlock {
    public static final MapCodec<CoffeeMachineBlock> CODEC = simpleCodec(CoffeeMachineBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /**
     * True while it is brewing. Switches to the "on" model, and steam rises from the cup.
     */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    /**
     * The outline of the model with its front to the north: the base, the tall back, the head over the drip tray and the
     * lid on top.
     */
    private static final Map<Direction, VoxelShape> SHAPES = HorizontalShapes.turned(Shapes.or(
            Block.box(1, 0, 1, 15, 2, 15),
            Block.box(1, 2, 9, 15, 14, 15),
            Block.box(1, 10, 2, 15, 14, 9),
            Block.box(4, 14, 5, 12, 16, 13)), Direction.NORTH);

    public CoffeeMachineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // BaseEntityBlock defaults to INVISIBLE, which is for blocks drawn entirely by a block entity renderer
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The front faces the player who placed it
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoffeeMachineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            // The client only ticks it for its brewing sound
            return createTickerHelper(type, ModBlockEntities.COFFEE_MACHINE.get(), CoffeeMachineBlockEntity::clientTick);
        }
        return createTickerHelper(type, ModBlockEntities.COFFEE_MACHINE.get(), CoffeeMachineBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CoffeeMachineBlockEntity machine) {
            player.openMenu(machine);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // Spills its items when broken, like a furnace
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        // Steam curls up from the cup under the spout, which sits towards the front
        Vec3 cup = Vec3.atBottomCenterOf(pos).add(0, 6 / 16.0, 0).relative(state.getValue(FACING), 3 / 16.0);
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.WHITE_SMOKE, cup.x + (random.nextDouble() - 0.5) * 0.1, cup.y,
                    cup.z + (random.nextDouble() - 0.5) * 0.1, 0, 0.02, 0);
        }
    }
}
