package com.caffeinecubed.gametest;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.compat.jade.MachineProvider;
import com.caffeinecubed.compat.jade.MachineSteps;
import com.caffeinecubed.compat.jade.MachineSteps.Step;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import snownee.jade.impl.WailaCommonRegistration;

/**
 * What Jade shows for the machines, their attachments and the bush (see {@link MachineSteps}): each row holds what's in
 * the machine, in order, with its arrows as far along as the machine is, and it reaches the player intact. The
 * machines are ticked by hand, within one tick, so the arrows are at a known point.
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class JadeTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(1, 1, 1);
    // A machine faces north unless placed otherwise, so its right is west and its left east
    private static final BlockPos GRINDER = MACHINE.above();
    private static final BlockPos FROTHER = MACHINE.west();
    private static final BlockPos COMPACTOR = MACHINE.east();

    private JadeTests() {
    }

    @GameTest(template = EMPTY)
    public static void emptyMachinesShowNothing(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.COFFEE_ROASTER.get());
        helper.assertTrue(MachineSteps.of((CoffeeRoasterBlockEntity) helper.getBlockEntity(MACHINE)).isEmpty(), "An empty roaster should show nothing");
        helper.setBlock(MACHINE, ModBlocks.TRIPLE_AXIS_SIPHON.get());
        helper.assertTrue(MachineSteps.of((TripleAxisSiphonBlockEntity) helper.getBlockEntity(MACHINE)).isEmpty(), "An empty siphon should show nothing");
        CoffeeMachineBlockEntity machine = machineWithAttachments(helper);
        helper.assertTrue(MachineSteps.of(machine).isEmpty(), "An empty machine should show nothing");
        for (BlockPos attachment : List.of(GRINDER, FROTHER, COMPACTOR)) {
            helper.assertTrue(attachment(helper, attachment).isEmpty(), "An empty attachment should show nothing");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theRoasterShowsItsBeansAndFuelThenTheArrowThenTheResult(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.COFFEE_ROASTER.get());
        CoffeeRoasterBlockEntity roaster = (CoffeeRoasterBlockEntity) helper.getBlockEntity(MACHINE);
        roaster.setItem(CoffeeRoasterBlockEntity.INPUT, stack(ModItems.RAW_COFFEE_BEANS.get(), 4));
        roaster.setItem(CoffeeRoasterBlockEntity.FUEL, new ItemStack(Items.COAL, 2));
        BlockPos at = helper.absolutePos(MACHINE);
        ServerLevel level = helper.getLevel();
        for (int tick = 0; tick < CoffeeRoasterBlockEntity.roastingTime() / 2; tick++) {
            CoffeeRoasterBlockEntity.serverTick(level, at, level.getBlockState(at), roaster);
        }
        List<Step> row = MachineSteps.of(roaster);
        expect(helper, row, 0, List.of(ModItems.RAW_COFFEE_BEANS.get(), Items.COAL), 0.5F);
        expect(helper, row, 1, List.of(Items.AIR), MachineSteps.NO_ARROW);
        helper.assertTrue(row.size() == 2, "The roaster's row should be its beans and fuel, then the result, not " + row);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theCoffeeMachineShowsItsGrinderFirst(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = machineWithAttachments(helper);
        machine.setItem(CoffeeMachineBlockEntity.BEANS, stack(ModItems.DARK_ROAST_COFFEE_BEANS.get(), 2));
        machine.setItem(CoffeeMachineBlockEntity.GROUNDS, stack(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), 3));
        machine.setItem(CoffeeMachineBlockEntity.WATER, new ItemStack(Items.WATER_BUCKET));
        machine.setItem(CoffeeMachineBlockEntity.FUEL, new ItemStack(Items.COAL, 2));
        machine.setItem(CoffeeMachineBlockEntity.MILK, new ItemStack(Items.MILK_BUCKET));
        machine.setItem(CoffeeMachineBlockEntity.SYRUP, new ItemStack(Items.PUFFERFISH));
        BlockPos at = helper.absolutePos(MACHINE);
        ServerLevel level = helper.getLevel();
        for (int tick = 0; tick < CoffeeMachineBlockEntity.grindingTime() / 2; tick++) {
            CoffeeMachineBlockEntity.serverTick(level, at, level.getBlockState(at), machine);
        }
        List<Step> row = MachineSteps.of(machine);
        helper.assertTrue(row.size() == 3, "The machine's row should be the grinder's beans, what it brews with, then the cup, not " + row);
        expect(helper, row, 0, List.of(ModItems.DARK_ROAST_COFFEE_BEANS.get()), 0.5F);
        expect(helper, row, 1, List.of(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), Items.WATER_BUCKET, Items.COAL, Items.MILK_BUCKET, Items.PUFFERFISH),
                (float) (CoffeeMachineBlockEntity.grindingTime() / 2) / CoffeeMachineBlockEntity.brewingTime());
        expect(helper, row, 2, List.of(Items.AIR), MachineSteps.NO_ARROW);

        // Without beans there's no grinder arrow
        machine.setItem(CoffeeMachineBlockEntity.BEANS, ItemStack.EMPTY);
        helper.assertTrue(MachineSteps.of(machine).size() == 2, "Without beans the machine's row shouldn't start with the grinder");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void attachmentsShowTheirPartOfTheMachine(GameTestHelper helper) {
        CoffeeMachineBlockEntity machine = machineWithAttachments(helper);
        machine.setItem(CoffeeMachineBlockEntity.BEANS, stack(ModItems.LIGHT_ROAST_COFFEE_BEANS.get(), 5));
        machine.setItem(CoffeeMachineBlockEntity.MILK, new ItemStack(Items.MILK_BUCKET));
        machine.setItem(CoffeeMachineBlockEntity.SYRUP, new ItemStack(Items.RABBIT_FOOT, 3));
        List<Step> grinder = attachment(helper, GRINDER);
        expect(helper, grinder, 0, List.of(ModItems.LIGHT_ROAST_COFFEE_BEANS.get()), 0);
        expect(helper, grinder, 1, List.of(Items.AIR), MachineSteps.NO_ARROW);
        List<Step> frother = attachment(helper, FROTHER);
        helper.assertTrue(frother.size() == 1, "The Milk Frother should show just its milk, not " + frother);
        expect(helper, frother, 0, List.of(Items.MILK_BUCKET), MachineSteps.NO_ARROW);
        List<Step> compactor = attachment(helper, COMPACTOR);
        helper.assertTrue(compactor.size() == 1, "The Syrup Compactor should show just its ingredient, not " + compactor);
        expect(helper, compactor, 0, List.of(Items.RABBIT_FOOT), MachineSteps.NO_ARROW);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theSiphonShowsItsInputsThenTheArrowThenTheResult(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.TRIPLE_AXIS_SIPHON.get());
        TripleAxisSiphonBlockEntity siphon = (TripleAxisSiphonBlockEntity) helper.getBlockEntity(MACHINE);
        siphon.setItem(TripleAxisSiphonBlockEntity.LEFT, stack(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), 1));
        siphon.setItem(TripleAxisSiphonBlockEntity.TOP, stack(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), 1));
        siphon.setItem(TripleAxisSiphonBlockEntity.RIGHT, stack(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), 1));
        BlockPos at = helper.absolutePos(MACHINE);
        ServerLevel level = helper.getLevel();
        for (int tick = 0; tick < TripleAxisSiphonBlockEntity.siphoningTime() / 4; tick++) {
            TripleAxisSiphonBlockEntity.serverTick(level, at, level.getBlockState(at), siphon);
        }
        List<Step> row = MachineSteps.of(siphon);
        expect(helper, row, 0, List.of(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(),
                ModItems.DARK_ROAST_COFFEE_GROUNDS.get()), 0.25F);
        expect(helper, row, 1, List.of(Items.AIR), MachineSteps.NO_ARROW);
        helper.succeed();
    }

    /**
     * The server sends each row to the player looking at the machine.
     */
    @GameTest(template = EMPTY)
    public static void rowsReachPlayersUnchanged(GameTestHelper helper) {
        List<Step> row = List.of(new Step(List.of(stack(ModItems.DARK_ROAST_COFFEE_BEANS.get(), 7), new ItemStack(Items.COAL, 2)), 0.375F),
                new Step(List.of(ItemStack.EMPTY), MachineSteps.NO_ARROW));
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            Step.LIST_STREAM_CODEC.encode(buffer, row);
            List<Step> received = Step.LIST_STREAM_CODEC.decode(buffer);
            helper.assertTrue(received.size() == row.size(), "The row lost steps on the way");
            for (int i = 0; i < row.size(); i++) {
                helper.assertTrue(received.get(i).progress() == row.get(i).progress()
                        && ItemStack.listMatches(received.get(i).items(), row.get(i).items()), "Step " + i + " changed on the way: " + received.get(i));
            }
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    /**
     * Jade found the plugin and knows which providers go with which blocks (the dev runs always have Jade).
     */
    @GameTest(template = EMPTY)
    public static void jadeKnowsWhatToAskTheMachinesFor(GameTestHelper helper) {
        if (!ModList.get().isLoaded("jade")) {
            helper.succeed();
            return;
        }
        CoffeeMachineBlockEntity machine = machineWithAttachments(helper);
        WailaCommonRegistration jade = WailaCommonRegistration.instance();
        helper.assertTrue(jade.getBlockNBTProviders(ModBlocks.COFFEE_MACHINE.get(), machine).contains(MachineProvider.COFFEE_MACHINE),
                "Jade should ask the Coffee Machine for its row");
        for (BlockPos attachment : List.of(GRINDER, FROTHER, COMPACTOR)) {
            helper.assertTrue(jade.getBlockNBTProviders(helper.getBlockState(attachment).getBlock(), null).contains(MachineProvider.COFFEE_MACHINE_ATTACHMENTS),
                    "Jade should ask " + helper.getBlockState(attachment).getBlock() + " for its part of the machine");
        }
        helper.setBlock(MACHINE.north(), ModBlocks.COFFEE_ROASTER.get());
        helper.assertTrue(jade.getBlockNBTProviders(ModBlocks.COFFEE_ROASTER.get(), helper.getBlockEntity(MACHINE.north()))
                .contains(MachineProvider.COFFEE_ROASTER), "Jade should ask the Coffee Roaster for its row");
        helper.succeed();
    }

    private static CoffeeMachineBlockEntity machineWithAttachments(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.COFFEE_MACHINE.get());
        helper.setBlock(GRINDER, ModBlocks.GRINDER_ATTACHMENT.get());
        helper.setBlock(FROTHER, ModBlocks.MILK_FROTHER.get());
        helper.setBlock(COMPACTOR, ModBlocks.SYRUP_COMPACTOR.get());
        CoffeeMachineBlockEntity machine = (CoffeeMachineBlockEntity) helper.getBlockEntity(MACHINE);
        helper.assertTrue(machine.hasGrinderAttachment() && machine.hasMilkFrother() && machine.hasSyrupCompactor(),
                "The test machine should have all three attachments");
        return machine;
    }

    private static List<Step> attachment(GameTestHelper helper, BlockPos pos) {
        return MachineSteps.ofAttachment(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos));
    }

    private static void expect(GameTestHelper helper, List<Step> row, int index, List<Item> items, float progress) {
        helper.assertTrue(row.size() > index, "The row should have a step " + index + ": " + row);
        Step step = row.get(index);
        List<Item> found = step.items().stream().map(ItemStack::getItem).toList();
        helper.assertTrue(found.equals(items), "Step " + index + " should hold " + items + " but holds " + found);
        helper.assertTrue(Math.abs(step.progress() - progress) < 0.001F, "Step " + index + "'s arrow should be at " + progress + ", not " + step.progress());
    }

    private static ItemStack stack(Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.GOOD);
        return stack;
    }
}
