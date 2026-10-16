package com.caffeinecubed.menu;

import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.coffee.Syrup;
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
 * The Coffee Machine's interface: the water bucket above the fuel on the left, with the flame between them, and the
 * grounds above the cup on the right, with an arrow pointing down between them. With a Grinder Attachment on the
 * machine, a bean slot appears left of the grounds, with an arrow pointing into them; with a Milk Frother, a milk slot
 * appears in the top right corner; with a Syrup Compactor, a syrup slot appears in the bottom left corner. It is
 * 10 pixels taller than a furnace's, so the player's inventory sits a little lower.
 */
public class CoffeeMachineMenu extends AbstractContainerMenu {
    public static final int HEIGHT = 176;
    private static final int MACHINE_SLOTS = 7;
    private static final int INVENTORY_START = MACHINE_SLOTS;
    private static final int HOTBAR_START = INVENTORY_START + 27;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;

    /**
     * The client's copy, which the server fills in through the slots and data.
     */
    public CoffeeMachineMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(7));
    }

    public CoffeeMachineMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.COFFEE_MACHINE.get(), containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, 7);
        this.container = container;
        this.data = data;
        addSlot(new Slot(container, CoffeeMachineBlockEntity.WATER, 42, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CoffeeMachineBlockEntity.isWaterOrIce(stack);
            }
        });
        addSlot(new Slot(container, CoffeeMachineBlockEntity.FUEL, 42, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CoffeeMachineBlockEntity.isFuel(stack);
            }
        });
        addSlot(new Slot(container, CoffeeMachineBlockEntity.GROUNDS, 110, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CoffeeMachineBlockEntity.isGrounds(stack);
            }
        });
        addSlot(new Slot(container, CoffeeMachineBlockEntity.OUTPUT, 110, 57) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(container, CoffeeMachineBlockEntity.BEANS, 65, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return hasGrinder() && CoffeeMachineBlockEntity.isRoastedBeans(stack);
            }

            @Override
            public boolean isActive() {
                // Shown while the attachment is there, and also while beans are left in it, so they can be taken out
                return hasGrinder() || hasItem();
            }
        });
        addSlot(new Slot(container, CoffeeMachineBlockEntity.MILK, 146, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return hasFrother() && CoffeeMachineBlockEntity.isMilk(stack);
            }

            @Override
            public boolean isActive() {
                // Shown while the frother is there, and also while something is left in it, so it can be taken out
                return hasFrother() || hasItem();
            }
        });
        addSlot(new Slot(container, CoffeeMachineBlockEntity.SYRUP, 14, 57) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return hasCompactor() && Syrup.of(stack) != null;
            }

            @Override
            public boolean isActive() {
                // Shown while the compactor is there, and also while ingredients are left in it, so they can be taken out
                return hasCompactor() || hasItem();
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

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    /**
     * Shift-clicking: coffee and machine slots go to the inventory; from the inventory, water buckets, ice, grounds, fuel,
     * (with a Grinder Attachment) roasted beans, (with a Milk Frother) milk and (with a Syrup Compactor) syrup
     * ingredients go into their slots, and anything else swaps between the inventory and the hotbar.
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
            moved = moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, index == CoffeeMachineBlockEntity.OUTPUT);
        } else if (Syrup.of(stack) != null && hasCompactor()) {
            moved = moveItemStackTo(stack, CoffeeMachineBlockEntity.SYRUP, CoffeeMachineBlockEntity.SYRUP + 1, false);
        } else if (CoffeeMachineBlockEntity.isMilk(stack) && hasFrother()) {
            moved = moveItemStackTo(stack, CoffeeMachineBlockEntity.MILK, CoffeeMachineBlockEntity.MILK + 1, false);
        } else if (CoffeeMachineBlockEntity.isWaterOrIce(stack)) {
            moved = moveItemStackTo(stack, CoffeeMachineBlockEntity.WATER, CoffeeMachineBlockEntity.WATER + 1, false);
        } else if (CoffeeMachineBlockEntity.isGrounds(stack)) {
            moved = moveItemStackTo(stack, CoffeeMachineBlockEntity.GROUNDS, CoffeeMachineBlockEntity.GROUNDS + 1, false);
        } else if (CoffeeMachineBlockEntity.isRoastedBeans(stack) && hasGrinder()) {
            moved = moveItemStackTo(stack, CoffeeMachineBlockEntity.BEANS, CoffeeMachineBlockEntity.BEANS + 1, false);
        } else if (CoffeeMachineBlockEntity.isFuel(stack)) {
            moved = moveItemStackTo(stack, CoffeeMachineBlockEntity.FUEL, CoffeeMachineBlockEntity.FUEL + 1, false);
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
     * How far along the current cup is, from 0 to 1.
     */
    public float getProgress() {
        return Mth.clamp((float) data.get(2) / CoffeeMachineBlockEntity.BREWING_TIME, 0, 1);
    }

    /**
     * How far along the bean being ground is, from 0 to 1.
     */
    public float getGrindProgress() {
        return Mth.clamp((float) data.get(3) / CoffeeMachineBlockEntity.GRINDING_TIME, 0, 1);
    }

    /**
     * Whether a Grinder Attachment sits on the machine.
     */
    public boolean hasGrinder() {
        return data.get(4) != 0;
    }

    /**
     * Whether to show the bean slot: while the attachment is there, and while beans are left in it.
     */
    public boolean showsGrinder() {
        return slots.get(CoffeeMachineBlockEntity.BEANS).isActive();
    }

    /**
     * Whether a Milk Frother sits on the machine's right.
     */
    public boolean hasFrother() {
        return data.get(5) != 0;
    }

    /**
     * Whether to show the milk slot: while the frother is there, and while something is left in it.
     */
    public boolean showsFrother() {
        return slots.get(CoffeeMachineBlockEntity.MILK).isActive();
    }

    /**
     * Whether a Syrup Compactor sits on the machine's left.
     */
    public boolean hasCompactor() {
        return data.get(6) != 0;
    }

    /**
     * Whether to show the syrup slot: while the compactor is there, and while ingredients are left in it.
     */
    public boolean showsCompactor() {
        return slots.get(CoffeeMachineBlockEntity.SYRUP).isActive();
    }
}
