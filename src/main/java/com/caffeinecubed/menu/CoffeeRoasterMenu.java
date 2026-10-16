package com.caffeinecubed.menu;

import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
import com.caffeinecubed.registry.ModMenus;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The Coffee Roaster's interface, laid out like a furnace's: beans at the top left, fuel below them and the roasted
 * beans on the right, above the player's inventory.
 */
public class CoffeeRoasterMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 3;
    private static final int INVENTORY_START = MACHINE_SLOTS;
    private static final int HOTBAR_START = INVENTORY_START + 27;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;

    /**
     * The client's copy, which the server fills in through the slots and data.
     */
    public CoffeeRoasterMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(3));
    }

    public CoffeeRoasterMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.COFFEE_ROASTER.get(), containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;
        addSlot(new Slot(container, CoffeeRoasterBlockEntity.INPUT, 56, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CoffeeRoasterBlockEntity.isRoastable(stack);
            }
        });
        addSlot(new Slot(container, CoffeeRoasterBlockEntity.FUEL, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CoffeeRoasterBlockEntity.isFuel(stack);
            }
        });
        addSlot(new Slot(container, CoffeeRoasterBlockEntity.OUTPUT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    /**
     * Shift-clicking: roasted beans and machine slots go to the inventory; from the inventory, beans that can be
     * roasted go into the bean slot and fuel into the fuel slot, and anything else swaps between the inventory and
     * the hotbar.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (index < MACHINE_SLOTS) {
            moved = moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, index == CoffeeRoasterBlockEntity.OUTPUT);
        } else if (CoffeeRoasterBlockEntity.isRoastable(stack)) {
            moved = moveItemStackTo(stack, CoffeeRoasterBlockEntity.INPUT, CoffeeRoasterBlockEntity.INPUT + 1, false);
        } else if (CoffeeRoasterBlockEntity.isFuel(stack)) {
            moved = moveItemStackTo(stack, CoffeeRoasterBlockEntity.FUEL, CoffeeRoasterBlockEntity.FUEL + 1, false);
        } else if (index < HOTBAR_START) {
            moved = moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false);
        } else {
            moved = moveItemStackTo(stack, INVENTORY_START, HOTBAR_START, false);
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return original;
    }

    public boolean isBurning() {
        return data.get(0) > 0;
    }

    /**
     * How much of the current fuel item is left, from 0 to 1.
     */
    public float getFuelLeft() {
        int duration = data.get(1);
        return duration <= 0 ? 0 : Mth.clamp((float) data.get(0) / duration, 0, 1);
    }

    /**
     * How far along the current bean is, from 0 to 1.
     */
    public float getProgress() {
        return Mth.clamp((float) data.get(2) / CoffeeRoasterBlockEntity.ROASTING_TIME, 0, 1);
    }
}
