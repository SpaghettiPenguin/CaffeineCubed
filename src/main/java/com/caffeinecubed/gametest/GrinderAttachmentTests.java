package com.caffeinecubed.gametest;

import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.BEANS;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.FUEL;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.GROUNDS;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.OUTPUT;
import static com.caffeinecubed.block.entity.CoffeeMachineBlockEntity.WATER;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.CoffeeMachineBlock;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.Roast;
import com.caffeinecubed.item.CupOfCoffeeItem;
import com.caffeinecubed.menu.CoffeeMachineMenu;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class GrinderAttachmentTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(1, 0, 1);
    private static final BlockPos ATTACHMENT = MACHINE.above();
    private static final int GRIND = CoffeeMachineBlockEntity.GRINDING_TIME;

    @GameTest(template = EMPTY)
    public static void theAttachmentOnlyGoesOnTopOfACoffeeMachine(GameTestHelper helper) {
        helper.setBlock(MACHINE, Blocks.STONE);
        helper.assertFalse(ModBlocks.GRINDER_ATTACHMENT.get().defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(ATTACHMENT)),
                "The attachment shouldn't go on stone");
        place(helper, true);
        helper.assertBlockPresent(ModBlocks.GRINDER_ATTACHMENT.get(), ATTACHMENT);
        // Taking the machine away pops the attachment off
        helper.setBlock(MACHINE, Blocks.AIR);
        helper.runAfterDelay(1, () -> {
            helper.assertBlockNotPresent(ModBlocks.GRINDER_ATTACHMENT.get(), ATTACHMENT);
            helper.assertItemEntityPresent(ModItems.GRINDER_ATTACHMENT.get());
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void grindsABeanInHalfAFurnacesTimeWithoutFuel(GameTestHelper helper) {
        helper.assertTrue(GRIND == 200 / 2, "Grinding should take half a furnace's 200 ticks");
        CoffeeMachineBlockEntity machine = place(helper, true);
        machine.setItem(BEANS, coffee(ModItems.MEDIUM_ROAST_COFFEE_BEANS.get(), BeanQuality.GOOD, 3));
        tick(helper, machine, GRIND - 1);
        helper.assertTrue(machine.getItem(GROUNDS).isEmpty(), "The first bean shouldn't be ground yet");
        tick(helper, machine, 1);
        ItemStack grounds = machine.getItem(GROUNDS);
        helper.assertTrue(grounds.is(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get()) && grounds.getCount() == 1 && BeanQuality.of(grounds) == BeanQuality.GOOD,
                "Good medium roast beans should grind into good medium roast grounds, but made " + grounds);
        tick(helper, machine, 2 * GRIND);
        helper.assertTrue(machine.getItem(GROUNDS).getCount() == 3 && machine.getItem(BEANS).isEmpty(), "All three beans should be ground");
        helper.assertTrue(machine.getFuel() == 0 && machine.getItem(FUEL).isEmpty(), "Grinding should need no fuel");
        helper.assertBlockProperty(MACHINE, CoffeeMachineBlock.LIT, false);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void withoutTheAttachmentNothingIsGround(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false);
        ItemStack beans = coffee(ModItems.DARK_ROAST_COFFEE_BEANS.get(), BeanQuality.DECENT, 2);
        helper.assertFalse(machine.canPlaceItem(BEANS, beans), "Without the attachment the bean slot takes nothing");
        machine.setItem(BEANS, beans);
        tick(helper, machine, 2 * GRIND);
        helper.assertTrue(machine.getItem(GROUNDS).isEmpty() && machine.getItem(BEANS).getCount() == 2, "Without the attachment nothing should be ground");

        helper.setBlock(ATTACHMENT, ModBlocks.GRINDER_ATTACHMENT.get());
        helper.assertTrue(machine.canPlaceItem(BEANS, beans), "With the attachment roasted beans go in");
        helper.assertTrue(machine.canPlaceItemThroughFace(BEANS, beans, Direction.NORTH), "Hoppers at the sides can feed it beans");
        helper.assertFalse(machine.canPlaceItem(BEANS, coffee(ModItems.RAW_COFFEE_BEANS.get(), BeanQuality.DECENT, 1)), "Raw beans have to be roasted first");
        helper.assertFalse(machine.canPlaceItem(BEANS, coffee(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.DECENT, 1)), "Grounds aren't beans");
        tick(helper, machine, GRIND);
        helper.assertTrue(machine.getItem(GROUNDS).getCount() == 1, "With the attachment the beans should be ground");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void waitsForRoomInTheGroundsSlot(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, true);
        machine.setItem(GROUNDS, coffee(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 1));
        machine.setItem(BEANS, coffee(ModItems.MEDIUM_ROAST_COFFEE_BEANS.get(), BeanQuality.DECENT, 1));
        tick(helper, machine, 2 * GRIND);
        helper.assertTrue(machine.getItem(BEANS).getCount() == 1, "Medium roast grounds can't go onto light roast ones");

        machine.setItem(GROUNDS, coffee(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), BeanQuality.DECENT, 64));
        tick(helper, machine, 2 * GRIND);
        helper.assertTrue(machine.getItem(BEANS).getCount() == 1, "A full grounds slot leaves no room");

        machine.setItem(GROUNDS, ItemStack.EMPTY);
        tick(helper, machine, GRIND);
        helper.assertTrue(machine.getItem(BEANS).isEmpty() && machine.getItem(GROUNDS).getCount() == 1, "Once there is room the bean should be ground");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void grindsBeansThenBrewsThem(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, true);
        machine.setItem(BEANS, coffee(ModItems.DARK_ROAST_COFFEE_BEANS.get(), BeanQuality.GREAT, 3));
        machine.setItem(WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(FUEL, new ItemStack(Items.COAL));
        tick(helper, machine, 3 * GRIND + CoffeeMachineBlockEntity.BREWING_TIME);
        ItemStack coffee = machine.getItem(OUTPUT);
        helper.assertTrue(coffee.is(ModItems.COFFEE.get()) && CupOfCoffeeItem.getRoast(coffee) == Roast.DARK && BeanQuality.of(coffee) == BeanQuality.GREAT,
                "Three great dark roast beans should end up as a cup of great dark roast coffee, but made " + coffee);
        helper.assertTrue(machine.getFuel() == 1600 - CoffeeMachineBlockEntity.BREWING_TIME,
                "Only brewing should burn fuel, but " + machine.getFuel() + " of the coal's 1,600 is left");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aHopperOnTheAttachmentFeedsItBeans(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, true);
        helper.setBlock(ATTACHMENT.above(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        HopperBlockEntity hopper = helper.getBlockEntity(ATTACHMENT.above());
        hopper.setItem(0, coffee(ModItems.LIGHT_ROAST_COFFEE_BEANS.get(), BeanQuality.POOR, 2));
        helper.runAtTickTime(40, () -> {
            helper.assertTrue(hopper.isEmpty(), "The hopper should have emptied into the attachment");
            helper.assertTrue(machine.getItem(BEANS).getCount() == 2, "The beans should be in the machine's bean slot");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void theBeanSlotOnlyShowsWithTheAttachment(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = place(helper, false);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        CoffeeMachineMenu menu = (CoffeeMachineMenu) machine.createMenu(0, player.getInventory(), player);
        helper.assertFalse(menu.showsGrinder(), "Without the attachment there should be no bean slot");

        helper.setBlock(ATTACHMENT, ModBlocks.GRINDER_ATTACHMENT.get());
        helper.assertTrue(menu.hasGrinder() && menu.showsGrinder(), "With the attachment the bean slot should appear");
        machine.setItem(BEANS, coffee(ModItems.LIGHT_ROAST_COFFEE_BEANS.get(), BeanQuality.DECENT, 4));
        helper.setBlock(ATTACHMENT, Blocks.AIR);
        helper.assertTrue(!menu.hasGrinder() && menu.showsGrinder(), "Beans left behind should stay in sight, so they can be taken out");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void attachmentRecipe(GameTestHelper helper) {
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        CraftingInput grid = CraftingInput.of(3, 2, List.of(ItemStack.EMPTY, new ItemStack(Items.HOPPER), ItemStack.EMPTY,
                iron, new ItemStack(ModItems.COFFEE_GRINDER.get()), iron));
        ItemStack made = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel())
                .map(recipe -> recipe.value().assemble(grid, helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(made.is(ModItems.GRINDER_ATTACHMENT.get()), "Expected a Grinder Attachment but crafted " + made);
        helper.succeed();
    }

    private static CoffeeMachineBlockEntity place(GameTestHelper helper, boolean withAttachment) {
        helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get());
        if (withAttachment) {
            helper.setBlock(ATTACHMENT, ModBlocks.GRINDER_ATTACHMENT.get());
        }
        return helper.getBlockEntity(MACHINE);
    }

    private static ItemStack coffee(Item item, BeanQuality quality, int count) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        return stack;
    }

    /**
     * Runs the machine for this many ticks straight away, instead of waiting for the game to.
     */
    private static void tick(GameTestHelper helper, CoffeeMachineBlockEntity machine, int ticks) {
        BlockPos pos = helper.absolutePos(MACHINE);
        for (int i = 0; i < ticks; i++) {
            CoffeeMachineBlockEntity.serverTick(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), machine);
        }
    }
}
