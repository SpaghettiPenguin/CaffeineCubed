package com.caffeinecubed.block;

import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A coffee bush, grown from Raw Coffee Beans planted on grass or dirt. It works like a sweet berry bush:
 * a starter bush (age 0) grows into a bush (1), starts to fruit with green cherries (2) and ends up covered in
 * ripe red cherries (3). Right-clicking a ripe bush picks 1-3 beans and leaves the bush standing, back at age 1.
 * The beans' {@link BeanQuality} comes from the biome the bush grows in.
 * Unlike a sweet berry bush it has no thorns, so walking through it slows you down but doesn't hurt.
 */
public class CoffeeBushBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<CoffeeBushBlock> CODEC = simpleCodec(CoffeeBushBlock::new);
    public static final int MAX_AGE = 3;
    /**
     * The bush goes back to this age once its beans are picked.
     */
    public static final int PICKED_AGE = 1;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape STARTER_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
    private static final VoxelShape BUSH_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public CoffeeBushBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public MapCodec<CoffeeBushBlock> codec() {
        return CODEC;
    }

    public static boolean isRipe(BlockState state) {
        return state.getValue(AGE) == MAX_AGE;
    }

    /**
     * Beans of the quality this bush grows where it stands, which depends on the biome.
     */
    public static ItemStack beans(LevelReader level, BlockPos pos, int count) {
        ItemStack beans = new ItemStack(ModItems.RAW_COFFEE_BEANS.get(), count);
        beans.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.at(level, pos));
        return beans;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        // Pick block gives the beans this bush grows, so it shows the quality to expect
        return beans(level, pos, 1);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AGE) == 0 ? STARTER_SHAPE : BUSH_SHAPE;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !isRipe(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // The same pace as a sweet berry bush: a 1 in 5 chance each random tick, given enough light
        int age = state.getValue(AGE);
        if (age < MAX_AGE && level.getRawBrightness(pos.above(), 0) >= 9
                && net.neoforged.neoforge.common.CommonHooks.canCropGrow(level, pos, state, random.nextInt(5) == 0)) {
            BlockState grown = state.setValue(AGE, age + 1);
            level.setBlock(pos, grown, Block.UPDATE_CLIENTS);
            net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(grown));
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE) {
            entity.makeStuckInBlock(state, new Vec3(0.8F, 0.75, 0.8F));
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hitResult) {
        // Bone meal on a bush that isn't ripe yet grows it instead of trying to pick it
        return !isRipe(state) && stack.is(Items.BONE_MEAL)
                ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
                : super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!isRipe(state)) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
        popResource(level, pos, beans(level, pos, 1 + level.random.nextInt(3)));
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        BlockState picked = state.setValue(AGE, PICKED_AGE);
        level.setBlock(pos, picked, Block.UPDATE_CLIENTS);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, picked));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !isRipe(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, Math.min(MAX_AGE, state.getValue(AGE) + 1)), Block.UPDATE_CLIENTS);
    }
}
