package com.caffeinecubed.block.entity;

import com.caffeinecubed.CaffeineCubedConfig;
import com.caffeinecubed.block.TripleAxisSiphonBlock;
import com.caffeinecubed.menu.TripleAxisSiphonMenu;
import com.caffeinecubed.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Slowly draws three ingredients together into one (see {@link SiphonRecipe}), with no fuel: light, medium and dark
 * roast grounds of the same quality into a Rainbow Blend of that quality, and three Rainbow Blends, a bucket of water
 * (or ice) and an enchanted golden apple into God's Coffee (or God's Ice Coffee). Each input slot takes a different
 * ingredient, in any order. Taking one out starts the blend over. A water bucket leaves its empty bucket behind in its
 * slot, as in the Coffee Machine. While it works it hums like a beacon, as often as a beacon does, and glows.
 */
public class TripleAxisSiphonBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int OUTPUT = 3;
    /**
     * Ticks to make one blend: four times as long as a furnace takes to smelt an item.
     */
    public static final int SIPHONING_TIME = 800;

    /**
     * Ticks to make one thing, as set in the config (SIPHONING_TIME unless changed).
     */
    public static int siphoningTime() {
        return CaffeineCubedConfig.get(CaffeineCubedConfig.SIPHONING_TIME);
    }
    /**
     * Ticks between hums, the same as a beacon's.
     */
    public static final int HUM_INTERVAL = 80;
    private static final int[] INPUTS = {LEFT, TOP, RIGHT};
    // Each arm takes in from its own side, the top from above, and what it makes and empty buckets come out of the
    // bottom. The front and back can fill any arm. (A hopper's side is the face it touches, so the siphon's left is its
    // own clockwise turn.)
    private static final int[] SLOTS_FOR_UP = {TOP};
    private static final int[] SLOTS_FOR_LEFT = {LEFT};
    private static final int[] SLOTS_FOR_RIGHT = {RIGHT};
    private static final int[] SLOTS_FOR_DOWN = {OUTPUT, LEFT, TOP, RIGHT};

    private NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private int progress;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? progress : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public TripleAxisSiphonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRIPLE_AXIS_SIPHON.get(), pos, state);
    }

    /**
     * Whether this stack can go in this input slot, alongside what is in the other two.
     */
    public static boolean fits(Container container, int slot, ItemStack stack) {
        List<ItemStack> others = new ArrayList<>();
        for (int input : INPUTS) {
            if (input != slot) {
                others.add(container.getItem(input));
            }
        }
        return SiphonRecipe.fits(stack, others);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TripleAxisSiphonBlockEntity siphon) {
        boolean working = siphon.work();
        if (state.getValue(TripleAxisSiphonBlock.LIT) != working) {
            level.setBlock(pos, state.setValue(TripleAxisSiphonBlock.LIT, working), Block.UPDATE_ALL);
        }
    }

    /**
     * Works for one tick, if the three inputs make something and there is room for it. Without a full set of matching
     * inputs the blend starts over; with the output full it just waits.
     *
     * @return whether it did
     */
    private boolean work() {
        List<ItemStack> inputs = List.of(items.get(LEFT), items.get(TOP), items.get(RIGHT));
        SiphonRecipe recipe = SiphonRecipe.find(inputs);
        ItemStack result = SiphonRecipe.blend(inputs);
        if (recipe == null || result.isEmpty()) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return false;
        }
        ItemStack output = items.get(OUTPUT);
        boolean room = output.isEmpty() || (ItemStack.isSameItemSameComponents(output, result) && output.getCount() < output.getMaxStackSize());
        if (!room) {
            return false;
        }
        if (++progress >= siphoningTime()) {
            progress = 0;
            for (int input : INPUTS) {
                use(input, recipe);
            }
            if (output.isEmpty()) {
                items.set(OUTPUT, result);
            } else {
                output.grow(1);
            }
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (progress % HUM_INTERVAL == 1) {
            level.playSound(null, worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        setChanged();
        return true;
    }

    /**
     * Uses up what this recipe takes from this input slot. A water bucket leaves its empty bucket behind.
     */
    private void use(int slot, SiphonRecipe recipe) {
        ItemStack stack = items.get(slot);
        ItemStack remainder = stack.getCraftingRemainingItem();
        recipe.part(stack).ifPresent(part -> stack.shrink(part.count()));
        if (stack.isEmpty() && !remainder.isEmpty()) {
            items.set(slot, remainder);
        }
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != OUTPUT && fits(this, slot, stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        Direction facing = getBlockState().getValue(TripleAxisSiphonBlock.FACING);
        if (side == Direction.UP) {
            return SLOTS_FOR_UP;
        } else if (side == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        } else if (side == facing.getClockWise()) {
            return SLOTS_FOR_LEFT;
        } else if (side == facing.getCounterClockWise()) {
            return SLOTS_FOR_RIGHT;
        }
        return INPUTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        // What it made comes out, and so do empty buckets left by the water
        return slot == OUTPUT || stack.is(Items.BUCKET);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.caffeinecubed.triple_axis_siphon");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new TripleAxisSiphonMenu(containerId, inventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        progress = tag.getInt("Progress");
    }
}
