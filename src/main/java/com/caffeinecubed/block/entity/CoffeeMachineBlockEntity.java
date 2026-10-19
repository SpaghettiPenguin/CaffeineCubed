package com.caffeinecubed.block.entity;

import com.caffeinecubed.CaffeineCubedConfig;
import com.caffeinecubed.block.CoffeeMachineBlock;
import com.caffeinecubed.block.CoffeeMachineSideAttachmentBlock;
import com.caffeinecubed.client.CoffeeMachineBrewingSound;
import com.caffeinecubed.coffee.Syrup;
import com.caffeinecubed.item.CoffeeGrinderItem;
import com.caffeinecubed.menu.CoffeeMachineMenu;
import com.caffeinecubed.recipe.CoffeeBrewingRecipe;
import com.caffeinecubed.recipe.CoffeeRecipes;
import com.caffeinecubed.registry.ModBlockEntities;
import com.caffeinecubed.registry.ModBlocks;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

/**
 * Brews a cup of Coffee from a bucket of water and three coffee grounds of the same roast and quality, burning furnace
 * fuel while it brews (its {@code caffeinecubed:brewing} recipes say which grounds, how many and what roast, and data
 * packs can change them). The empty bucket is left behind in the water slot. The coffee keeps the grounds' roast and
 * quality, which decide what drinking it does.
 * <p>
 * A block of ice in the water slot instead brews Ice Coffee, the same in every other way. Each cup uses up one block.
 * <p>
 * With a Grinder Attachment on top it also has a bean slot: roasted beans put there are ground into the grounds slot,
 * one every 100 ticks, without fuel or anything else.
 * <p>
 * With a Milk Frother on its right it also has a milk slot. Milk is optional: if there is a bucket of milk in the slot
 * when a cup is done, the cup is brewed with milk and the empty bucket is left behind.
 * <p>
 * With a Syrup Compactor on its left it also has a syrup slot, for pufferfish, fermented spider eyes, golden carrots or
 * rabbit's feet. Also optional: if one is there when a cup is done, it is used up and the cup gets that syrup.
 */
public class CoffeeMachineBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int WATER = 0;
    public static final int FUEL = 1;
    public static final int GROUNDS = 2;
    public static final int OUTPUT = 3;
    /**
     * Beans for the Grinder Attachment. Last, so machines saved before it existed keep their other slots.
     */
    public static final int BEANS = 4;
    /**
     * Milk for the Milk Frother, after the beans for the same reason.
     */
    public static final int MILK = 5;
    /**
     * Syrup ingredients for the Syrup Compactor, last of all.
     */
    public static final int SYRUP = 6;
    /**
     * Ticks to brew one cup: twice as long as a furnace takes to smelt an item. With furnace fuel values,
     * a coal (1,600) brews four cups.
     */
    public static final int BREWING_TIME = 400;
    /**
     * Ticks for the Grinder Attachment to grind one bean: half as long as a furnace takes to smelt an item.
     */
    public static final int GRINDING_TIME = 100;

    /**
     * Ticks to brew one cup, as set in the config (BREWING_TIME unless changed).
     */
    public static int brewingTime() {
        return CaffeineCubedConfig.get(CaffeineCubedConfig.BREWING_TIME);
    }

    /**
     * Ticks for the Grinder Attachment to grind one bean, as set in the config (GRINDING_TIME unless changed).
     */
    public static int grindingTime() {
        return CaffeineCubedConfig.get(CaffeineCubedConfig.ATTACHMENT_GRINDING_TIME);
    }
    // Grounds go in the top, water or ice, fuel, beans, milk and syrup ingredients in the sides, and coffee and empty
    // buckets come out of the bottom. A hopper above a Grinder Attachment or Milk Frother feeds it too (see ModCapabilities).
    private static final int[] SLOTS_FOR_UP = {GROUNDS};
    private static final int[] SLOTS_FOR_SIDES = {WATER, FUEL, BEANS, MILK, SYRUP};
    private static final int[] SLOTS_FOR_DOWN = {OUTPUT, WATER, FUEL, MILK};

    private NonNullList<ItemStack> items = NonNullList.withSize(7, ItemStack.EMPTY);
    /**
     * Fuel left from the fuel items burnt so far, in furnace burn ticks. One is used for each tick of brewing.
     */
    private int fuel;
    /**
     * How much fuel the last fuel item gave, so the menu can show how much of it is left.
     */
    private int fuelDuration;
    private int progress;
    private int grindProgress;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> fuel;
                case 1 -> fuelDuration;
                case 2 -> progress;
                case 3 -> grindProgress;
                case 4 -> hasGrinderAttachment() ? 1 : 0;
                case 5 -> hasMilkFrother() ? 1 : 0;
                case 6 -> hasSyrupCompactor() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> fuel = value;
                case 1 -> fuelDuration = value;
                case 2 -> progress = value;
                case 3 -> grindProgress = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public CoffeeMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COFFEE_MACHINE.get(), pos, state);
    }

    /**
     * Grounds the machine can brew: any that a brewing recipe uses.
     */
    public static boolean isGrounds(Level level, ItemStack stack) {
        return CoffeeRecipes.brewing(level, stack).isPresent();
    }

    /**
     * A water bucket: anything in {@code c:buckets/water}, so other mods' water buckets work too.
     */
    public static boolean isWater(ItemStack stack) {
        return stack.is(Tags.Items.BUCKETS_WATER);
    }

    public static boolean isIce(ItemStack stack) {
        return stack.is(Items.ICE);
    }

    /**
     * What goes in the water slot: a water bucket for Coffee, or ice for Ice Coffee.
     */
    public static boolean isWaterOrIce(ItemStack stack) {
        return isWater(stack) || isIce(stack);
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }

    /**
     * A milk bucket: anything in {@code c:buckets/milk}.
     */
    public static boolean isMilk(ItemStack stack) {
        return stack.is(Tags.Items.BUCKETS_MILK);
    }

    /**
     * What a used water or milk bucket leaves behind: its empty bucket.
     */
    private static ItemStack emptied(ItemStack bucket) {
        return bucket.hasCraftingRemainingItem() ? bucket.getCraftingRemainingItem() : new ItemStack(Items.BUCKET);
    }

    /**
     * The cup these grounds brew, Coffee or (with ice) Ice Coffee, with or without milk and a syrup: it keeps their roast
     * and quality. Nothing if they aren't coffee grounds.
     */
    public static ItemStack brew(Level level, ItemStack grounds, boolean iced, boolean milk, @Nullable Syrup syrup) {
        return CoffeeRecipes.brewing(level, grounds).map(recipe -> recipe.brew(grounds, iced, milk, syrup)).orElse(ItemStack.EMPTY);
    }

    /**
     * Beans the Grinder Attachment can grind: any roasted beans.
     */
    public static boolean isRoastedBeans(Level level, ItemStack stack) {
        return !CoffeeGrinderItem.grind(level, stack).isEmpty();
    }

    public boolean hasGrinderAttachment() {
        return level != null && level.getBlockState(worldPosition.above()).is(ModBlocks.GRINDER_ATTACHMENT.get());
    }

    /**
     * Whether a Milk Frother sits on the machine's right, as seen from the front, facing the same way.
     */
    public boolean hasMilkFrother() {
        return hasSideAttachment(ModBlocks.MILK_FROTHER.get());
    }

    /**
     * Whether a Syrup Compactor sits on the machine's left, as seen from the front, facing the same way.
     */
    public boolean hasSyrupCompactor() {
        return hasSideAttachment(ModBlocks.SYRUP_COMPACTOR.get());
    }

    private boolean hasSideAttachment(CoffeeMachineSideAttachmentBlock attachment) {
        if (level == null) {
            return false;
        }
        Direction facing = getBlockState().getValue(CoffeeMachineBlock.FACING);
        BlockState there = level.getBlockState(worldPosition.relative(attachment.towardsMachine(facing).getOpposite()));
        return there.is(attachment) && there.getValue(CoffeeMachineSideAttachmentBlock.FACING) == facing;
    }

    /**
     * Whether the cup being brewed will have milk: there has to be a Milk Frother, and milk in its slot.
     */
    private boolean brewsWithMilk() {
        return isMilk(items.get(MILK)) && hasMilkFrother();
    }

    /**
     * The syrup the cup being brewed will get: there has to be a Syrup Compactor, and an ingredient in its slot.
     */
    @Nullable
    private Syrup brewsWithSyrup() {
        return hasSyrupCompactor() ? Syrup.of(items.get(SYRUP)) : null;
    }

    /**
     * On the client: plays the brewing sound while the machine brews (the client never runs this on a server).
     */
    public static void clientTick(Level level, BlockPos pos, BlockState state, CoffeeMachineBlockEntity machine) {
        CoffeeMachineBrewingSound.update(machine);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoffeeMachineBlockEntity machine) {
        machine.grind();
        boolean brewing = machine.work();
        if (state.getValue(CoffeeMachineBlock.LIT) != brewing) {
            level.setBlock(pos, state.setValue(CoffeeMachineBlock.LIT, brewing), Block.UPDATE_ALL);
        }
    }

    /**
     * Brews for one tick, if there is water or ice, enough grounds, room for the coffee and fuel to burn. Whether the cup
     * is iced, or gets milk or a syrup, is settled when it is done, so they can be put in any time before then.
     *
     * @return whether it did
     */
    private boolean work() {
        ItemStack grounds = items.get(GROUNDS);
        ItemStack water = items.get(WATER);
        CoffeeBrewingRecipe recipe = CoffeeRecipes.brewing(level, grounds).orElse(null);
        if (recipe == null || grounds.getCount() < recipe.count() || !isWaterOrIce(water)) {
            return false;
        }
        boolean iced = isIce(water);
        boolean milk = brewsWithMilk();
        Syrup syrup = brewsWithSyrup();
        ItemStack coffee = recipe.brew(grounds, iced, milk, syrup);
        if (!hasRoomFor(coffee)) {
            return false;
        }
        if (fuel <= 0 && !burnFuelItem()) {
            return false;
        }
        fuel--;
        if (++progress >= brewingTime()) {
            progress = 0;
            grounds.shrink(recipe.count());
            if (iced) {
                water.shrink(1);
            } else {
                items.set(WATER, emptied(water));
            }
            if (milk) {
                items.set(MILK, emptied(items.get(MILK)));
            }
            if (syrup != null) {
                items.get(SYRUP).shrink(1);
            }
            ItemStack output = items.get(OUTPUT);
            if (output.isEmpty()) {
                items.set(OUTPUT, coffee);
            } else {
                output.grow(1);
            }
        }
        setChanged();
        return true;
    }

    /**
     * Grinds for one tick, if there is a Grinder Attachment on top, roasted beans in the bean slot and room in the
     * grounds slot. Grinding needs no fuel. A bean taken out, or the attachment removed, starts the bean over.
     */
    private void grind() {
        ItemStack beans = items.get(BEANS);
        ItemStack grounds = CoffeeGrinderItem.grind(level, beans);
        if (grounds.isEmpty() || !hasGrinderAttachment()) {
            grindProgress = 0;
            return;
        }
        ItemStack slot = items.get(GROUNDS);
        boolean room = slot.isEmpty() || (ItemStack.isSameItemSameComponents(slot, grounds) && slot.getCount() < slot.getMaxStackSize());
        if (!room) {
            return;
        }
        if (++grindProgress >= grindingTime()) {
            grindProgress = 0;
            beans.shrink(1);
            if (slot.isEmpty()) {
                items.set(GROUNDS, grounds);
            } else {
                slot.grow(1);
            }
        } else if (grindProgress % 20 == 10) {
            level.playSound(null, worldPosition, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.25F, 1.5F);
        }
        setChanged();
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
     * Cups only stack with the same kind of coffee of the same quality, so the output has to be empty or hold those.
     */
    private boolean hasRoomFor(ItemStack coffee) {
        ItemStack output = items.get(OUTPUT);
        return output.isEmpty() || (ItemStack.isSameItemSameComponents(output, coffee) && output.getCount() < output.getMaxStackSize());
    }

    public int getFuel() {
        return fuel;
    }

    public int getProgress() {
        return progress;
    }

    public int getGrindProgress() {
        return grindProgress;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        // Different grounds start over
        if (slot == GROUNDS && !ItemStack.isSameItemSameComponents(items.get(GROUNDS), stack)) {
            progress = 0;
        }
        if (slot == BEANS && !ItemStack.isSameItemSameComponents(items.get(BEANS), stack)) {
            grindProgress = 0;
        }
        super.setItem(slot, stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case WATER -> isWaterOrIce(stack);
            case FUEL -> isFuel(stack);
            case GROUNDS -> level != null && isGrounds(level, stack);
            case BEANS -> level != null && isRoastedBeans(level, stack) && hasGrinderAttachment();
            case MILK -> isMilk(stack) && hasMilkFrother();
            case SYRUP -> Syrup.of(stack) != null && hasSyrupCompactor();
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
        // Coffee comes out, and so do the empty buckets left by the water and by lava used as fuel
        return slot == OUTPUT || stack.is(Tags.Items.BUCKETS_EMPTY);
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
        return Component.translatable("container.caffeinecubed.coffee_machine");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new CoffeeMachineMenu(containerId, inventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Fuel", fuel);
        tag.putInt("FuelDuration", fuelDuration);
        tag.putInt("Progress", progress);
        tag.putInt("GrindProgress", grindProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        fuel = tag.getInt("Fuel");
        fuelDuration = tag.getInt("FuelDuration");
        progress = tag.getInt("Progress");
        grindProgress = tag.getInt("GrindProgress");
    }
}
