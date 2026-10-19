package com.caffeinecubed.compat.jade;

import com.caffeinecubed.block.CoffeeMachineSideAttachmentBlock;
import com.caffeinecubed.block.GrinderAttachmentBlock;
import com.caffeinecubed.block.MilkFrotherBlock;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What Jade shows for a machine, read out of it on the server: a row of what's in it, with an arrow for each thing it
 * is working on, filling up as it goes, like Jade shows a furnace. For example the Coffee Roaster's is its beans and
 * fuel, the roasting arrow, then the roasted beans. Nothing in a machine shows nothing. Kept apart from Jade's own
 * classes, so it works (and can be tested) without Jade.
 */
public final class MachineSteps {
    /**
     * Some items, then an arrow showing how far along the work on them is; no arrow when {@code progress} is
     * {@link #NO_ARROW}.
     */
    public record Step(List<ItemStack> items, float progress) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Step> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_LIST_STREAM_CODEC, Step::items,
                ByteBufCodecs.FLOAT, Step::progress,
                Step::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, List<Step>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

        public boolean hasArrow() {
            return progress != NO_ARROW;
        }
    }

    public static final float NO_ARROW = -1;

    private MachineSteps() {
    }

    /**
     * Beans and fuel, the roasting arrow, then the roasted beans.
     */
    public static List<Step> of(CoffeeRoasterBlockEntity roaster) {
        return withResult(present(roaster.getItem(CoffeeRoasterBlockEntity.INPUT), roaster.getItem(CoffeeRoasterBlockEntity.FUEL)),
                fraction(roaster.getProgress(), CoffeeRoasterBlockEntity.roastingTime()), roaster.getItem(CoffeeRoasterBlockEntity.OUTPUT));
    }

    /**
     * Beans being ground by a Grinder Attachment and that arrow first, if there are any; then the grounds, water or ice,
     * fuel, milk and syrup ingredient, the brewing arrow, and the coffee.
     */
    public static List<Step> of(CoffeeMachineBlockEntity machine) {
        List<ItemStack> ingredients = present(machine.getItem(CoffeeMachineBlockEntity.GROUNDS), machine.getItem(CoffeeMachineBlockEntity.WATER),
                machine.getItem(CoffeeMachineBlockEntity.FUEL), machine.getItem(CoffeeMachineBlockEntity.MILK), machine.getItem(CoffeeMachineBlockEntity.SYRUP));
        List<Step> steps = withResult(ingredients, fraction(machine.getProgress(), CoffeeMachineBlockEntity.brewingTime()),
                machine.getItem(CoffeeMachineBlockEntity.OUTPUT));
        ItemStack beans = machine.getItem(CoffeeMachineBlockEntity.BEANS);
        if (beans.isEmpty()) {
            return steps;
        }
        List<Step> withBeans = new ArrayList<>();
        withBeans.add(new Step(List.of(beans), fraction(machine.getGrindProgress(), CoffeeMachineBlockEntity.grindingTime())));
        if (steps.isEmpty()) {
            // Nothing else in the machine yet: the grinder's arrow points at the empty grounds slot
            withBeans.add(new Step(List.of(ItemStack.EMPTY), NO_ARROW));
        } else {
            withBeans.addAll(steps);
        }
        return withBeans;
    }

    /**
     * The three inputs, the arrow, then what they make.
     */
    public static List<Step> of(TripleAxisSiphonBlockEntity siphon) {
        return withResult(present(siphon.getItem(TripleAxisSiphonBlockEntity.LEFT), siphon.getItem(TripleAxisSiphonBlockEntity.TOP),
                siphon.getItem(TripleAxisSiphonBlockEntity.RIGHT)), fraction(siphon.getProgress(), TripleAxisSiphonBlockEntity.siphoningTime()),
                siphon.getItem(TripleAxisSiphonBlockEntity.OUTPUT));
    }

    /**
     * For an attachment, just its part of the Coffee Machine it's on: a Grinder Attachment's beans, its arrow and the
     * grounds; a Milk Frother's milk; a Syrup Compactor's syrup ingredient.
     */
    public static List<Step> ofAttachment(Level level, BlockPos pos, BlockState state) {
        CoffeeMachineBlockEntity machine = machineOf(level, pos, state);
        if (machine == null) {
            return List.of();
        }
        if (state.getBlock() instanceof GrinderAttachmentBlock) {
            ItemStack beans = machine.getItem(CoffeeMachineBlockEntity.BEANS);
            return beans.isEmpty() ? List.of() : List.of(new Step(List.of(beans), fraction(machine.getGrindProgress(), CoffeeMachineBlockEntity.grindingTime())),
                    new Step(List.of(machine.getItem(CoffeeMachineBlockEntity.GROUNDS)), NO_ARROW));
        }
        ItemStack stack = machine.getItem(state.getBlock() instanceof MilkFrotherBlock ? CoffeeMachineBlockEntity.MILK : CoffeeMachineBlockEntity.SYRUP);
        return stack.isEmpty() ? List.of() : List.of(new Step(List.of(stack), NO_ARROW));
    }

    /**
     * The Coffee Machine an attachment is on: below a Grinder Attachment, beside a Milk Frother or Syrup Compactor.
     */
    @Nullable
    public static CoffeeMachineBlockEntity machineOf(Level level, BlockPos pos, BlockState state) {
        BlockPos machinePos;
        if (state.getBlock() instanceof GrinderAttachmentBlock) {
            machinePos = pos.below();
        } else if (state.getBlock() instanceof CoffeeMachineSideAttachmentBlock attachment) {
            machinePos = pos.relative(attachment.towardsMachine(state.getValue(CoffeeMachineSideAttachmentBlock.FACING)));
        } else {
            return null;
        }
        return level.getBlockEntity(machinePos) instanceof CoffeeMachineBlockEntity machine ? machine : null;
    }

    /**
     * The ingredients and their arrow, then the result (even if there isn't one yet, for the arrow to point at); nothing
     * at all if the machine is empty.
     */
    private static List<Step> withResult(List<ItemStack> ingredients, float progress, ItemStack result) {
        if (ingredients.isEmpty() && result.isEmpty()) {
            return List.of();
        }
        return List.of(new Step(ingredients, progress), new Step(List.of(result), NO_ARROW));
    }

    private static List<ItemStack> present(ItemStack... stacks) {
        return Stream.of(stacks).filter(stack -> !stack.isEmpty()).toList();
    }

    private static float fraction(int progress, int total) {
        return total <= 0 ? 0 : Mth.clamp((float) progress / total, 0, 1);
    }
}
