package com.caffeinecubed.block.entity;

import com.caffeinecubed.block.CoffeeRoasterBlock;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.menu.CoffeeRoasterMenu;
import com.caffeinecubed.registry.ModBlockEntities;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Works like a furnace for coffee beans: each bean comes out one roast darker, Raw to Light to Medium to Dark,
 * keeping its quality. It burns furnace fuel, but only while roasting, so one coal always roasts eight beans.
 */
public class CoffeeRoasterBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int INPUT = 0;
    public static final int FUEL = 1;
    public static final int OUTPUT = 2;
    /**
     * Ticks to roast one bean, the time a furnace takes to smelt one item. With furnace fuel values,
     * a coal (1,600) roasts eight beans.
     */
    public static final int ROASTING_TIME = 200;
    /**
     * Every roast, lightest first. Roasting a bean turns it into the next one; Dark Roast is as far as it goes.
     */
    public static final List<DeferredItem<? extends Item>> ROASTS = List.of(ModItems.RAW_COFFEE_BEANS,
            ModItems.LIGHT_ROAST_COFFEE_BEANS, ModItems.MEDIUM_ROAST_COFFEE_BEANS, ModItems.DARK_ROAST_COFFEE_BEANS);
    // Like a furnace: beans go in the top, fuel in the sides, and roasted beans come out of the bottom
    private static final int[] SLOTS_FOR_UP = {INPUT};
    private static final int[] SLOTS_FOR_SIDES = {FUEL};
    private static final int[] SLOTS_FOR_DOWN = {OUTPUT, FUEL};

    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    /**
     * Fuel left from the fuel items burnt so far, in furnace burn ticks. One is used for each tick of roasting.
     */
    private int fuel;
    /**
     * How much fuel the last fuel item gave, so the menu can show how much of it is left.
     */
    private int fuelDuration;
    private int progress;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> fuel;
                case 1 -> fuelDuration;
                case 2 -> progress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> fuel = value;
                case 1 -> fuelDuration = value;
                case 2 -> progress = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public CoffeeRoasterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COFFEE_ROASTER.get(), pos, state);
    }

    public static boolean isRoastable(ItemStack stack) {
        return !roast(stack).isEmpty();
    }

    /**
     * What roasting one of these beans makes: the next roast, of the same quality. Nothing if they can't be roasted
     * any darker, or aren't beans at all.
     */
    public static ItemStack roast(ItemStack beans) {
        for (int i = 0; i < ROASTS.size() - 1; i++) {
            if (beans.is(ROASTS.get(i))) {
                ItemStack roasted = new ItemStack(ROASTS.get(i + 1).get());
                roasted.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.of(beans));
                return roasted;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoffeeRoasterBlockEntity roaster) {
        boolean roasting = roaster.work();
        if (state.getValue(CoffeeRoasterBlock.LIT) != roasting) {
            level.setBlock(pos, state.setValue(CoffeeRoasterBlock.LIT, roasting), Block.UPDATE_ALL);
        }
    }

    /**
     * Roasts for one tick, if there are beans to roast, room for the result and fuel to burn.
     *
     * @return whether it did
     */
    private boolean work() {
        ItemStack result = roast(items.get(INPUT));
        if (result.isEmpty() || !hasRoomFor(result)) {
            return false;
        }
        if (fuel <= 0 && !burnFuelItem()) {
            return false;
        }
        fuel--;
        if (++progress >= ROASTING_TIME) {
            progress = 0;
            items.get(INPUT).shrink(1);
            ItemStack output = items.get(OUTPUT);
            if (output.isEmpty()) {
                items.set(OUTPUT, result);
            } else {
                output.grow(1);
            }
        }
        setChanged();
        return true;
    }

    /**
     * Burns the next fuel item.
     */
    private boolean burnFuelItem() {
        ItemStack fuelItem = items.get(FUEL);
        int value = fuelItem.getBurnTime(RecipeType.SMELTING);
        if (value <= 0) {
            return false;
        }
        fuel += value;
        fuelDuration = value;
        // Lava buckets leave their bucket behind, as in a furnace
        if (fuelItem.hasCraftingRemainingItem()) {
            items.set(FUEL, fuelItem.getCraftingRemainingItem());
        } else {
            fuelItem.shrink(1);
        }
        return true;
    }

    /**
     * Roasted beans only stack with the same roast of the same quality, so the output has to be empty or hold those.
     */
    private boolean hasRoomFor(ItemStack result) {
        ItemStack output = items.get(OUTPUT);
        return output.isEmpty() || (ItemStack.isSameItemSameComponents(output, result) && output.getCount() < output.getMaxStackSize());
    }

    public int getFuel() {
        return fuel;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        // Different beans start over
        if (slot == INPUT && !ItemStack.isSameItemSameComponents(items.get(INPUT), stack)) {
            progress = 0;
        }
        super.setItem(slot, stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case INPUT -> isRoastable(stack);
            case FUEL -> isFuel(stack);
            default -> false;
        };
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case UP -> SLOTS_FOR_UP;
            case DOWN -> SLOTS_FOR_DOWN;
            default -> SLOTS_FOR_SIDES;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        // Only roasted beans come out, plus the empty buckets lava leaves in the fuel slot
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
        return Component.translatable("container.caffeinecubed.coffee_roaster");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new CoffeeRoasterMenu(containerId, inventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Fuel", fuel);
        tag.putInt("FuelDuration", fuelDuration);
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        fuel = tag.getInt("Fuel");
        fuelDuration = tag.getInt("FuelDuration");
        progress = tag.getInt("Progress");
    }
}
