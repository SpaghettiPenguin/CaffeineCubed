package com.caffeinecubed.menu;

import com.caffeinecubed.block.entity.SiphonRecipe;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
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
 * The Triple Axis Siphon's interface: an input slot on the left, one on top and one on the right, each with an arrow
 * pointing into the blend in the middle. As tall as the Coffee Machine's, so the player's inventory sits in the same
 * place.
 */
public class TripleAxisSiphonMenu extends AbstractContainerMenu {
    public static final int HEIGHT = 176;
    private static final int MACHINE_SLOTS = 4;
    private static final int INVENTORY_START = MACHINE_SLOTS;
    private static final int HOTBAR_START = INVENTORY_START + 27;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;

    /**
     * The client's copy, which the server fills in through the slots and data.
     */
    public TripleAxisSiphonMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(1));
    }

    public TripleAxisSiphonMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.TRIPLE_AXIS_SIPHON.get(), containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, 1);
        this.container = container;
        this.data = data;
        addSlot(new InputSlot(container, TripleAxisSiphonBlockEntity.LEFT, 26, 57));
        addSlot(new InputSlot(container, TripleAxisSiphonBlockEntity.TOP, 80, 19));
        addSlot(new InputSlot(container, TripleAxisSiphonBlockEntity.RIGHT, 134, 57));
        addSlot(new Slot(container, TripleAxisSiphonBlockEntity.OUTPUT, 80, 57) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        int inventoryTop = HEIGHT - 82;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, inventoryTop + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, inventoryTop + 58));
        }
        addDataSlots(data);
    }

    /**
     * An input slot: it only takes an ingredient that can go with what is in the other two.
     */
    private static class InputSlot extends Slot {
        InputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return TripleAxisSiphonBlockEntity.fits(container, getContainerSlot(), stack);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    /**
     * Shift-clicking: the siphon's slots go to the inventory; from the inventory, ingredients go into the first input
     * slot they fit, so each roast fills a slot of its own, and anything else swaps between the inventory and the hotbar.
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
            moved = moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, index == TripleAxisSiphonBlockEntity.OUTPUT);
        } else if (SiphonRecipe.isIngredient(stack)) {
            moved = moveItemStackTo(stack, TripleAxisSiphonBlockEntity.LEFT, TripleAxisSiphonBlockEntity.RIGHT + 1, false);
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

    /**
     * How far along the blend is, from 0 to 1.
     */
    public float getProgress() {
        return Mth.clamp((float) data.get(0) / TripleAxisSiphonBlockEntity.siphoningTime(), 0, 1);
    }
}
